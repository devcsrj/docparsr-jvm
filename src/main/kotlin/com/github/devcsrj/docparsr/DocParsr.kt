package com.github.devcsrj.docparsr

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.lang.IllegalArgumentException
import java.net.URI

/**
 * The central object for accessing [https://github.com/axa-group/Parsr]
 */
interface DocParsr {

    fun getDefaultConfig(): Configuration

    companion object {

        /**
         * Creates an HTTP-backed [DocParsr]
         *
         * @param baseUri the base URI
         */
        fun create(baseUri: URI): DocParsr {
            val uri = baseUri.toHttpUrlOrNull() ?: throw IllegalArgumentException("Not a valid uri: $baseUri")
            return DefaultDocParsr(uri)
        }
    }
}