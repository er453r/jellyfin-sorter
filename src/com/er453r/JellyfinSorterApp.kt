package com.er453r

import mu.KotlinLogging
import java.io.File
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

fun main() {
    val config = Config(
        sourceDirectory = System.getenv("SOURCE_DIRECTORY") ?: "",
        targetDirectory = System.getenv("TARGET_DIRECTORY") ?: "",
        configFile = System.getenv("CONFIG_FILE") ?: "",
        dryRun = System.getenv("DRY_RUN") != null,
        intervalSeconds = System.getenv("INTERVAL_S") ?: "30",
    )

    logger.info { "Config: $config" }

    if(config.dryRun)
        logger.info { "Starting a dry-run only!" }

    logger.info { "Starting..." }

    while(true) {
        try {
            sleep(1000 * config.intervalSeconds.toLong())

            val sourceDirectory = File(config.sourceDirectory).also {
                if (!it.isDirectory)
                    throw Exception("Source is not a directory")
            }

            val targetDirectory = File(config.targetDirectory).also {
                if (!it.isDirectory)
                    throw Exception("Target is not a directory")
            }

            val configFile = File(config.configFile).also {
                if (!it.exists()) {
                    throw Exception("Config file does not exist!")
                }
            }

            val configLines = configFile.readLines()

            logger.info { "Config file: \n${configFile.readText()}" }

            val sortConfig = configLines
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
            sortConfig.forEach { logger.info { "\t$it" } }

            Sorter().sort(
                sourceDirectory = sourceDirectory,
                targetDirectory = targetDirectory,
                config = sortConfig,
                dryRun = config.dryRun,
            )

            logger.info { "ALL DONE!" }
        }catch (e:Exception){
            logger.error { "Error: $e" }
        }
    }
}
