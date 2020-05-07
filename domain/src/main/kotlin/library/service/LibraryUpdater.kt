/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tachiyomi.core.log.Logger
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@ProvidesSingletonInScope
class LibraryUpdater @Inject constructor() {

  private val queueChannel = Channel<Operation>(Channel.UNLIMITED)
  private val queuedOperations = mutableSetOf<Operation>()
  private val mutex = Mutex()

  init {
    GlobalScope.launch(Dispatchers.Default) {
      for (operation in queueChannel) {
        Logger.warn("Received job ${operation.categoryId}")
        operation.start(this, Dispatchers.IO)
        queuedOperations.remove(operation)
        Logger.warn("End job")
      }
    }
  }

  suspend fun enqueue(categoryId: Long, target: Target, source: suspend (Job) -> Any): QueueResult {
    return mutex.withLock {
      val operation = Operation(categoryId, target, source)
      if (operation in queuedOperations) {
        QueueResult.AlreadyEnqueued
      } else if (queuedOperations.isEmpty()) {
        enqueue(operation)
        QueueResult.Executing { operation.await() }
      } else {
        enqueue(operation)
        QueueResult.Queued { operation.await() }
      }
    }
  }

  private suspend fun enqueue(operation: Operation) {
    if (queuedOperations.add(operation)) {
      queueChannel.send(operation)
    }
  }

  fun cancel(categoryId: Long, target: Target) {
    GlobalScope.launch(Dispatchers.Default) {
      mutex.withLock {
        val operation = queuedOperations.find { it.categoryId == categoryId && it.target == target }
        if (operation != null) {
          operation.cancel()
          queuedOperations.remove(operation)
        }
      }
    }
  }

  fun cancelFirst() {
    GlobalScope.launch(Dispatchers.Default) {
      mutex.withLock {
        val operation = queuedOperations.firstOrNull()
        if (operation != null) {
          operation.cancel()
          queuedOperations.remove(operation)
        }
      }
    }
  }

  enum class Target {
    Chapters, Metadata;
  }

  sealed class QueueResult {
    abstract val awaitWork: suspend () -> Unit

    data class Executing(override val awaitWork: suspend () -> Unit) : QueueResult()
    data class Queued(override val awaitWork: suspend () -> Unit) : QueueResult()
    object AlreadyEnqueued : QueueResult() {
      override val awaitWork: suspend () -> Unit
        get() = {}
    }
  }

  private data class Operation(
    val categoryId: Long,
    val target: Target,
    val source: suspend (Job) -> Any
  ) {

    private val workJob = Job() // TODO is this really needed? Maybe awaitJob is enough
    private val awaitJob = Job()

    fun cancel() {
      workJob.cancel()
      awaitJob.complete()
    }

    suspend fun start(scope: CoroutineScope, context: CoroutineContext) {
      if (workJob.isCompleted) return

      @Suppress("DeferredResultUnused")
      scope.async(context + workJob) {
        try {
          source(awaitJob)
        } finally {
          awaitJob.complete()
        }
        workJob.complete()
      }
      awaitJob.join()
    }

    suspend fun await() {
      try {
        awaitJob.join()
      } catch (e: CancellationException) {
      }
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Operation

      if (categoryId != other.categoryId) return false
      if (target != other.target) return false

      return true
    }

    override fun hashCode(): Int {
      var result = categoryId.hashCode()
      result = 31 * result + target.hashCode()
      return result
    }

  }

}

// TODO: Structured concurrency test. Consider using this approach instead of context switch or
//  mutex
@Singleton
class LibUpdater @Inject constructor() {

  private val queueChannel = Channel<Operation>()
  private val cancelChannel = Channel<Pair<Long, LibraryUpdater.Target>>()

  init {
    GlobalScope.launch {
      val workerChannel = Channel<Operation>()

      worker(workerChannel, cancelChannel)
      queueDispatcher(queueChannel, workerChannel, cancelChannel)
    }
  }

  private fun CoroutineScope.queueDispatcher(
    queueOperations: ReceiveChannel<Operation>,
    workerOperations: SendChannel<Operation>,
    cancelOperations: ReceiveChannel<Pair<Long, LibraryUpdater.Target>>
  ) = launch {
    val queue = mutableSetOf<Operation>()

    while (true) {
      select<Unit> {
        queueOperations.onReceive { operation ->
          if (operation in queue) {
            operation.queueResult.complete(LibraryUpdater.QueueResult.AlreadyEnqueued)
          } else if (queue.isEmpty()) {
            workerOperations.send(operation)
            operation.queueResult.complete(
              LibraryUpdater.QueueResult.Executing { operation.await() })
          } else {
            workerOperations.send(operation)
            operation.queueResult.complete(
              LibraryUpdater.QueueResult.Queued { operation.await() }
            )
          }
        }
        cancelOperations.onReceive { (categoryId, target) ->
          val operation =
            queue.find { it.categoryId == categoryId && it.target == target }
          if (operation != null) {
            if (!operation.job.isCompleted) {
              operation.cancel()
            }
            queue.remove(operation)
          }
        }
      }
    }
  }

  private fun CoroutineScope.worker(
    operations: ReceiveChannel<Operation>,
    completionChannel: SendChannel<Pair<Long, LibraryUpdater.Target>>
  ) = launch {
    for (operation in operations) {
      operation.start(this, CoroutineName("LibWorker"))
      completionChannel.send(operation.categoryId to operation.target)
    }
  }

  suspend fun enqueue(
    categoryId: Long,
    target: LibraryUpdater.Target,
    source: (Job) -> Any
  ): LibraryUpdater.QueueResult {
    val operation = Operation(categoryId, target, source)
    queueChannel.send(operation)
    return operation.queueResult.await()
  }

  private data class Operation(
    val categoryId: Long,
    val target: LibraryUpdater.Target,
    val source: (Job) -> Any
  ) {

    val queueResult = CompletableDeferred<LibraryUpdater.QueueResult>()

    val job = Job()

    fun cancel() {
      job.complete()
    }

    suspend fun start(scope: CoroutineScope, context: CoroutineContext) {
      if (job.isCompleted) return

      @Suppress("DeferredResultUnused")
      scope.async(context + job) {
        try {
          source(job)
        } finally {
          job.complete()
        }
      }
      job.join()
    }

    suspend fun await() {
      try {
        job.join()
      } catch (e: CancellationException) {
      }
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Operation

      if (categoryId != other.categoryId) return false
      if (target != other.target) return false

      return true
    }

    override fun hashCode(): Int {
      var result = categoryId.hashCode()
      result = 31 * result + target.hashCode()
      return result
    }

  }

}
