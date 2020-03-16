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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Word(
    @JsonProperty("id") private val id: ElementId,
    @JsonProperty("box") private val box: Box,
    @JsonProperty("properties") private val properties: Properties,
    @JsonProperty("metadata") private val metadata: List<MetadataId>,
    @JsonProperty("content") private val content: String,
    @JsonProperty("font") private val fontId: FontId
) : Element<String> {

    override fun type() = Element.Type.WORD
    override fun id() = id
    override fun box() = box
    override fun properties() = properties
    override fun metadata() = metadata
    override fun content() = content
    fun fontId() = fontId
}
