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
        val elements = deserializeElements(root["elements"], p)

        return Page(
            box = box,
            rotation = rotation,
            number = number,
            elements = elements
        )
    }

    private fun deserializeElements(root: JsonNode, p: JsonParser): List<Element<*>> {
        val elements = mutableListOf<Element<*>>()
        for (item in root) {
            val element = when (Element.Type.fromValue(item["type"].asText())) {
                Element.Type.WORD -> p.codec.treeToValue(item, Word::class.java)
                Element.Type.HEADING -> p.codec.treeToValue(item, Heading::class.java)
                Element.Type.PARAGRAPH -> p.codec.treeToValue(item, Paragraph::class.java)
                Element.Type.LINE -> p.codec.treeToValue(item, Line::class.java)
                else -> p.codec.treeToValue(item, AnyElement::class.java)
            }

            elements.add(element)
        }
        return elements
    }
}
