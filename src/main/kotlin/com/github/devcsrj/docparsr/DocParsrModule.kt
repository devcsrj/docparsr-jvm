package com.github.devcsrj.docparsr

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleSerializers

object DocParsrModule : Module() {

    override fun getModuleName() = "com.github.devcsrj.docparsr"

    override fun version() = Version(1, 0, 0, "", "com.github.devcsrj", "docparsr")

    override fun setupModule(context: SetupContext) {
        val deserializers = SimpleDeserializers()
        deserializers.addDeserializer(Configuration::class.java, ConfigurationDeserializer())
        context.addDeserializers(deserializers)

        val serializers = SimpleSerializers()
        serializers.addSerializer(Configuration::class.java, ConfigurationSerializer())
        context.addSerializers(serializers)
    }
}
