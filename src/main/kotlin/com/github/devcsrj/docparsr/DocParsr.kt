package com.github.devcsrj.docparsr

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import java.lang.IllegalArgumentException
import java.net.URI
import java.time.Duration

/**
 * The central object for accessing [https://github.com/axa-group/Parsr]
 */
interface DocParsr {

    fun getDefaultConfig(): Configuration

    fun newParsingJob(file: File, config: Configuration): ParsingJob

    companion object {

        /**
         * Creates an HTTP-backed [DocParsr]
         *
         * @param baseUri the base URI
         */
        fun create(baseUri: URI): DocParsr {
            val uri = baseUri.toHttpUrlOrNull() ?: throw IllegalArgumentException("Not a valid uri: $baseUri")
            return DefaultDocParsr(uri, Duration.ofSeconds(1L))
        }
    }
}