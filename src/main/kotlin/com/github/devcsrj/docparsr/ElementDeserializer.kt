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

internal class ElementDeserializer : StdDeserializer<Element<*>>(Element::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Element<*> {
        val root = p.codec.readTree<JsonNode>(p)
        return when (Element.Type.fromValue(root["type"].asText())) {
            Element.Type.WORD -> p.codec.treeToValue(root, Word::class.java)
            Element.Type.HEADING -> p.codec.treeToValue(root, Heading::class.java)
            Element.Type.PARAGRAPH -> p.codec.treeToValue(root, Paragraph::class.java)
            Element.Type.LINE -> p.codec.treeToValue(root, Line::class.java)
            Element.Type.LIST -> p.codec.treeToValue(root, Items::class.java)
            else -> p.codec.treeToValue(root, AnyElement::class.java)
        }
    }
}
