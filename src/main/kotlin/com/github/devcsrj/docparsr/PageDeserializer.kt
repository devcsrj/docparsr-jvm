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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

internal class PageDeserializer : StdDeserializer<Page>(Page::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Page {
        val root = p.codec.readTree<JsonNode>(p)

        val box = p.codec.treeToValue(root["box"], Box::class.java)
        val rotation = p.codec.treeToValue(root["rotation"], Rotation::class.java)
        val number = root["pageNumber"].asInt()
        val elements = ArrayList<Element<*>>()
        for (element in root["elements"]) {
            elements.add(p.codec.treeToValue(element, Element::class.java))
        }

        return Page(
            box = box,
            rotation = rotation,
            number = number,
            elements = elements
        )
    }
}
