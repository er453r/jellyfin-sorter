package com.er453r

import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    if (args.isEmpty())
        die("Provide at least 1 argument")

    val directory = File(args.first()).also {
        if (!it.isDirectory)
            die("Not a directory!")
    }

    val dryRun = args.size == 1

    if(dryRun)
        logger.info { "Starting a dry-run only!" }

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
                rules = chunk.filterIndexed { n, it -> n > 0 && !it.contains(" -> ") }.map { Regex(it) },
                replace = chunk.filterIndexed { n, it -> n > 0 && it.contains(" -> ") }
                    .map { it.split(" -> ").let { l -> l[0] to l[1] } },
            )
        }

    logger.info { "Config loaded" }
    config.forEach { logger.info { "\t$it" } }

    Sorter().sort(directory, config, dryRun)

    logger.info { "ALL DONE!" }
}
