package com.er453r

import com.er453r.data.Configuration
import com.er453r.data.SortClass
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun main() {
    val configuration = try {
        Configuration.readFromFile()
    } catch (e: Exception) {
        logger.error(e) { "Error parsing configuration!" }

        exitProcess(1)
    }

    logger.info { "Configuration is: $configuration" }

    val ignore = mutableSetOf(".jellyfin")

    val sortClasses = arrayOf(
        SortClass(
            name = "shows",
            regex = Regex("S\\d+"), // Regex("\\.S\\d+E\\d+\\.")
        ),
        SortClass(
            name = "movies",
            regex = null,
        ),
    )

    val files = File(configuration.media).list()!!.toSet() - ignore

    val sortMap = mutableMapOf<String, MutableSet<String>>()

    for (name in files) {
        sortClasses.firstOrNull { it.regex != null && it.regex.containsMatchIn(name) }?.let {
            sortMap.getOrPut(it.name) { mutableSetOf() }.add(name)
        } ?: run {
            sortMap.getOrPut(sortClasses.first { it.regex == null }.name) { mutableSetOf() }.add(name)
        }
    }

    // create directory if not exists
    for (sortClass in sortClasses)
        File("${configuration.media}/.jellyfin/${sortClass.name}").mkdirs()

    sortMap.keys.forEach { key ->
        sortMap[key]!!.forEach {
            Files.createSymbolicLink(Path.of("${configuration.media}/.jellyfin/$key/$it"), Path.of("../../$it"))
        }
    }
}
