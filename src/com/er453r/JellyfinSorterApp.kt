package com.er453r

import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    if (args.size != 1)
        die("Provide at least 1 argument")

    val directory = File(args.first()).also {
        if (!it.isDirectory)
            die("Not a directory!")
    }

    val configFile = File(directory.absolutePath + File.separator + ".jellyfin-sorter").also {
        if (!it.exists())
            die("Config file $it does not exists!")
    }

    val configLines = configFile.readLines()

     logger.info { "Config file: \n${configFile.readText()}" }

    val config = configLines
        .filter { it.isNotBlank() }
        .chunkedBy { it.startsWith("[") }
        .map { chunk ->
            ConfigSection(
                name = chunk.first().substring(1, chunk.first().length - 1),
                rules = chunk.filterIndexed { n, _ -> n > 0 }.map { Regex(it) },
            )
        }

    logger.info { "Config loaded" }
    config.forEach { logger.info { "\t$it" } }

    Sorter().sort(directory, config)

    logger.info { "ALL DONE!" }
}
