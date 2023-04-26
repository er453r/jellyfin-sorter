package com.er453r.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.FileReader

data class Configuration(
    val media: String,
) {
    companion object {
        fun readFromFile(fileName: String = "config.yml"): Configuration {
            val mapper =
                ObjectMapper(YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)) // Enable YAML parsing
            mapper.registerModule(KotlinModule.Builder().build()) // Enable Kotlin support
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

            return mapper.readValue(FileReader(fileName))
        }
    }
}
