package com.github.devcsrj.docparsr

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.AssertionError
import kotlin.reflect.full.declaredMemberProperties

internal class ConfigurationSerializer : StdSerializer<Configuration>(Configuration::class.java) {

    override fun serialize(value: Configuration, gen: JsonGenerator, provider: SerializerProvider) {
        gen.apply {
            writeStartObject()
            writeVersion(value.version, gen)
            writeExtractorField(value.extractor, gen)
            writeCleanersField(value.cleaners, gen)
            writeOutputField(value.output, gen)
            writeEndObject()
        }
    }

    private fun writeVersion(value: String, gen: JsonGenerator) {
        gen.apply {
            writeNumberField("version", value.toDouble())
        }
    }

    private fun writeExtractorField(value: Extractor, gen: JsonGenerator) {
        gen.apply {
            writeObjectFieldStart("extractor")
            writeStringField("pdf", value.pdfExtractor.name)
            writeStringField("ocr", value.ocrExtractor.name)
            writeArrayFieldStart("language")
            value.languages.forEach { writeRawValue(it) }
            writeEndArray()
            writeEndObject()
        }
    }

    private fun writeCleanersField(cleaners: Set<Cleaner>, gen: JsonGenerator) {
        gen.apply {
            writeArrayFieldStart("cleaner")
            for (cleaner in cleaners) {
                if (cleaner::class.objectInstance != null) {
                    writeRawValue(cleaner.name)
                    continue
                } else {
                    writeStartArray()
                    writeRawValue(cleaner.name)
                    writeStartObject()

                    cleaner.javaClass.kotlin.declaredMemberProperties.forEach {
                        val prop = it.get(cleaner)
                        writeFieldName(it.name)
                        writeObject(prop)
                    }

                    writeEndObject()
                    writeEndArray()
                }
            }
            writeEndArray()
        }
    }

    private fun writeOutputField(output: Output, gen: JsonGenerator) {
        gen.apply {
            writeObjectFieldStart("output")
            writeStringField("granularity", output.granularity.name.toLowerCase())
            writeBooleanField("includeMarginals", output.includeMarginals)
            writeObjectFieldStart("formats")
            for (sealedSubclass in Format::class.sealedSubclasses) {
                val obj = sealedSubclass.objectInstance ?: throw AssertionError()
                writeBooleanField(obj.name, output.formats.contains(obj))
            }
            writeEndObject()
            writeEndObject()
        }
    }
}
