package tachiyomi.source

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import tachiyomi.core.http.GET
import tachiyomi.source.model.ImageUrl
import tachiyomi.source.model.Listing
import tachiyomi.source.model.PageComplete
import tachiyomi.source.model.PageUrl
import java.security.MessageDigest

/**
 * A simple implementation for sources from a website.
 */
@Suppress("unused", "unused_parameter")
abstract class HttpSource(private val dependencies: Dependencies) : CatalogSource {

  /**
   * Base url of the website without the trailing slash, like: http://mysite.com
   */
  abstract val baseUrl: String

  /**
   * Version id used to generate the source id. If the site completely changes and urls are
   * incompatible, you may increase this value and it'll be considered as a new source.
   */
  open val versionId = 1

  /**
   * Id of the source. By default it uses a generated id using the first 16 characters (64 bits)
   * of the MD5 of the string: sourcename/language/versionId
   * Note the generated id sets the sign bit to 0.
   */
  override val id by lazy {
    val key = "${name.toLowerCase()}/$lang/$versionId"
    val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
    (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }.reduce(Long::or) and Long.MAX_VALUE
  }

  /**
   * Headers used for requests.
   */
  val headers: Headers by lazy { headersBuilder().build() }

  /**
   * Default network client for doing requests.
   */
  open val client: OkHttpClient
    get() = dependencies.http.defaultClient

  /**
   * Headers builder for requests. Implementations can override this method for custom headers.
   */
  protected open fun headersBuilder() = Headers.Builder().apply {
    add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)")
  }

  /**
   * Visible name of the source.
   */
  override fun toString() = "$name (${lang.toUpperCase()})"

  open suspend fun getPage(page: PageUrl): PageComplete {
    throw Exception("Incomplete source implementation. Please override getPage when using PageUrl")
  }

  open suspend fun getImageRequest(page: ImageUrl): Request {
    return GET(page.url, headers)
  }

  override fun getListings(): List<Listing> {
    return emptyList()
  }

}
