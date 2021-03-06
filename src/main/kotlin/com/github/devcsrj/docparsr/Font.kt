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


data class Font(

    val id: FontId,
    val name: String,
    val size: Double,
    val weight: FontWeight,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val color: FontColor,
    val sizeUnit: String
)

data class FontId(val value: Int) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun valueOf(value: Int) = FontId(value)
    }
}

data class FontWeight(val value: String) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun valueOf(value: String) = FontWeight(value)
    }
}

data class FontColor(val value: String) {
    companion object {

        private const val HEX_LENGTH = 6

        @JvmStatic
        @JsonCreator
        fun fromHex(hex: String): FontColor {
            var str = hex
            while (str.startsWith("#") && str.isNotBlank()) {
                str = str.substringAfter("#")
            }
            require(str.length == HEX_LENGTH) {
                "expecting a hex code of length $HEX_LENGTH, but got '$str'"
            }
            return FontColor(hex)
        }
    }
}
