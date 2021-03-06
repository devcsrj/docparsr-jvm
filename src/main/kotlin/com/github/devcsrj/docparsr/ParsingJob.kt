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
import java.time.ZonedDateTime

interface ParsingJob {

    /**
     * The configuration used for this job
     */
    fun configuration(): Configuration

    /**
     * Starts the job, and notifies the [callback] accordingly
     */
    fun enqueue(callback: Callback)

    /**
     * Blocks this call until the [ParsingResult] is received, or
     * if an exception is thrown
     */
    fun execute(): ParsingResult

    interface Callback {
        fun onFailure(jobId: String?, e: Exception)
        fun onProgress(jobId: String, progress: Progress)
        fun onSuccess(jobId: String, result: ParsingResult)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Progress(
        @JsonProperty("start-date")
        val timestamp: ZonedDateTime,
        @JsonProperty("status")
        val message: String?
    )

}
