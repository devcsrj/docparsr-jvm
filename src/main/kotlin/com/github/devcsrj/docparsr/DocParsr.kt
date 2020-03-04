/**
 * Copyright (c) 2020, Reijhanniel Jearl Campos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devcsrj.docparsr

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
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