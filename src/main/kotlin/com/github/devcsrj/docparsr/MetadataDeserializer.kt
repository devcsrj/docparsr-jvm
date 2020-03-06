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

internal class MetadataDeserializer : StdDeserializer<Metadata>(Metadata::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Metadata {
        val root = p.codec.readTree<JsonNode>(p)

        val id = MetadataId.valueOf(root["id"].asInt())
        val elementIds = root["elements"].map { ElementId.valueOf(it.asLong()) }.toList()
        val type = Metadata.Type.fromString(root["type"].asText())
        val content = when (type) {
            Metadata.Type.REGEX -> p.codec.treeToValue(root["data"], Metadata.RegexContent::class.java)
            Metadata.Type.UNKNOWN -> p.codec.treeToValue(root["data"], Metadata.RawContent::class.java)
        }

        return Metadata(
            id = id,
            elementIds = elementIds,
            type = type,
            content = content
        )
    }
}
