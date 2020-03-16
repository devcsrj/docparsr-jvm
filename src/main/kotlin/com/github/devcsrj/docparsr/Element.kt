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

import com.fasterxml.jackson.annotation.JsonCreator
import java.lang.IllegalArgumentException

interface Element<T> {

    fun type(): Type
    fun id(): ElementId
    fun box(): Box
    fun properties(): Properties
    fun metadata(): List<MetadataId>
    fun content(): T?

    enum class Type {
        CHARACTER,
        DRAWING,
        HEADING,
        IMAGE,
        LIST,
        PARAGRAPH,
        TABLE,
        TABLE_CELL,
        TABLE_ROW,
        WORD,
        LINE,
        BARCODE;

        companion object {

            @JsonCreator
            @JvmStatic
            fun fromValue(str: String): Type {
                return Type.values().find { it.name.toLowerCase().replace("_", "-") == str }
                    ?: throw IllegalArgumentException("Unknown type: $str")
            }
        }
    }
}

data class ElementId(val value: Long) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun valueOf(value: Long) = ElementId(value)
    }
}
