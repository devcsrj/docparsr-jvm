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

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Metadata(
    val id: MetadataId,
    @JsonProperty("elements")
    val elementIds: List<ElementId>,
    val type: Type,
    @JsonProperty("data")
    val content: Content
) {

    enum class Type {
        REGEX,
        UNKNOWN;

        companion object {

            @JsonCreator
            @JvmStatic
            fun fromString(str: String): Type {
                return values().find { it.name.toLowerCase() == str } ?: UNKNOWN
            }
        }
    }

    interface Content

    data class RegexContent(
        val name: String,
        val regex: String,
        val fullMatch: String,
        val groups: List<String>
    ) : Content

    open class RawContent : Content {

        private val map = mutableMapOf<String, Any>()

        @JsonAnySetter
        internal fun put(key: String, value: Any) {
            map[key] = value
        }

        fun <T> get(key: String): T? {
            return map[key] as T?
        }
    }
}

data class MetadataId(val value: Int) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun valueOf(value: Int) = MetadataId(value)
    }
}
