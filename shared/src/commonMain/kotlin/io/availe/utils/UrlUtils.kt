package io.availe.utils

import io.availe.STANDARD_NLIP_ENDPOINTS
import io.ktor.http.*

fun normaliseUrl(input: String, ensureTrailingSlash: Boolean = false): Url {
    val builder = URLBuilder(input.trim())
    val path = builder.encodedPath
        .split("/")
        .filter { it.isNotEmpty() }
        .joinToString("/", prefix = if (builder.encodedPath.startsWith("/")) "/" else "")

    builder.encodedPath = if (ensureTrailingSlash && !path.endsWith("/")) {
        "$path/"
    } else {
        path
    }

    return builder.build()
}

fun samePathAs(a: Url, b: Url): Boolean =
    a.encodedPath.trimEnd('/') == b.encodedPath.trimEnd('/')

fun isNLIPEndpoint(url: Url): Boolean =
    url.encodedPath.trim('/').startsWith("nlip")

fun isStandardNLIPEndpoint(url: Url): Boolean =
    STANDARD_NLIP_ENDPOINTS.contains(url.encodedPath.trim('/'))
