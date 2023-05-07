package com.er453r

import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun die(message: String) {
    System.err.println(message)
    exitProcess(-1)
}

fun walk(directory: File, filter: (File) -> Boolean, logic: (File) -> Unit) {
    directory.listFiles()!!
        .filter { !it.relativeTo(directory).path.startsWith(".") }
        .forEach {
            if (it.isDirectory)
                walk(it, filter, logic)
            else if (filter(it))
                logic(it)
        }
}

fun <T> Iterable<T>.chunkedBy(logic: (T) -> Boolean): Iterable<Iterable<T>> {
    val chunks = mutableListOf<MutableList<T>>()

    this.forEach {
        if (logic(it))
            chunks.add(mutableListOf())

        chunks.last().add(it)
    }

    return chunks
}

private const val ALL = "all"

data class ConfigSection(
    val name: String,
    val rules: List<Regex>,
)

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

    val sort = mutableMapOf<String, MutableList<File>>()

    walk(directory, filter = { file ->
        config.first { it.name == ALL }.rules.firstOrNull { it.containsMatchIn(file.path) } != null
    }) { file ->
        run {
            config.filter { it.name != ALL }.forEach { section ->
                if (section.rules.firstOrNull { it.containsMatchIn(file.path) } != null) {
                    sort.getOrPut(section.name) {
                        mutableListOf()
                    }.add(file)

                    return@run
                }
            }

            logger.warn { "Do not know what to do with $file" } // should probably never happen
        }
    }

    logger.info { "Finished sorting" }

    sort.forEach { (section, list) ->
        logger.info { "$section - ${list.size}" }

//        list.forEach {
//            logger.info { "\t${it.relativeTo(directory)}" }
//        }
    }

    val links = mutableSetOf<String>()

    // link creation
    sort.forEach { (section, list) ->
        val dir = File("${directory.path}/.jellyfin/$section")

        list.forEach file@{
            val link = File("$dir/${it.relativeTo(directory)}")

            links += "$section/${it.relativeTo(directory)}"

            if (link.exists())
                return@file

            val relative = it.relativeTo(link)
            logger.info { "Creating link: $link -> $relative" }
            link.mkdirs()
            Files.createSymbolicLink(link.toPath(), relative.toPath())
        }
    }

    logger.info { "Done!" }
}
