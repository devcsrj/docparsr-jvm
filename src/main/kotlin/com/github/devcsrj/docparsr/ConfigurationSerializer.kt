package com.github.devcsrj.docparsr

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

internal class ConfigurationSerializer : StdSerializer<Configuration>(Configuration::class.java) {

    override fun serialize(value: Configuration, gen: JsonGenerator, provider: SerializerProvider) {
    }
}
