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

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleSerializers
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object DocParsrModule : Module() {

    override fun getModuleName() = "com.github.devcsrj.docparsr"

    override fun version() = Version(1, 0, 0, "", "com.github.devcsrj", "docparsr")

    override fun setupModule(context: SetupContext) {
        val deserializers = SimpleDeserializers()
        deserializers.addDeserializer(Configuration::class.java, ConfigurationDeserializer())
        deserializers.addDeserializer(Element::class.java, ElementDeserializer())
        deserializers.addDeserializer(Metadata::class.java, MetadataDeserializer())
        deserializers.addDeserializer(Page::class.java, PageDeserializer())
        context.addDeserializers(deserializers)

        val serializers = SimpleSerializers()
        serializers.addSerializer(Configuration::class.java, ConfigurationSerializer())
        context.addSerializers(serializers)
    }

    override fun getDependencies(): MutableIterable<Module> {
        return mutableListOf(KotlinModule(), JavaTimeModule())
    }
}
