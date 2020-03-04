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