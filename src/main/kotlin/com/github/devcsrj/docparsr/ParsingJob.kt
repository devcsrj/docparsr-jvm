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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import okio.Source
import java.time.ZonedDateTime

interface ParsingJob {

    fun configuration(): Configuration

    fun enqueue(callback: Callback)

    interface Callback {
        fun onFailure(job: ParsingJob, e: Exception)
        fun onProgress(job: ParsingJob, progress: Progress)
        fun onSuccess(job: ParsingJob, result: Result)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Progress(
        @JsonProperty("start-date")
        val timestamp: ZonedDateTime,
        @JsonProperty("status")
        val message: String
    )

    interface Result {
        fun id(): String

        /**
         * The parsed result for the given [format]
         *
         * @throws IllegalArgumentException if the format wasn't provided in the [Configuration]
         **/
        fun source(format: Format): Source
    }
}
