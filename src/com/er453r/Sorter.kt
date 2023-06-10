package com.er453r

import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

private val logger = KotlinLogging.logger {}

class Sorter {
    fun sort(directory: File, config: List<ConfigSection>, dryRun: Boolean = true) {
        val sort = mutableMapOf<String, MutableList<File>>()

        walk(directory, filter = { file ->
            config.first { it.name == ConfigSection.ALL }.rules.firstOrNull { it.containsMatchIn(file.path) } != null
        }) { file ->
            run {
                config.filter { it.name != ConfigSection.ALL }.forEach { section ->
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

        // link creation
        sort.forEach { (section, list) ->
            val dir = File("${directory.path}/.jellyfin/$section")

            list.forEach file@{
                val link = File("$dir/${it.relativeTo(directory)}")
                val relative = File("${it.relativeTo(link.parentFile)}")

                if (link.exists()) {
                    val currentRelative = Files.readSymbolicLink(link.toPath()).relativeTo(directory.toPath())

                    if(currentRelative != relative){
                        logger.info { "Removing invalid link $currentRelative != $relative" }

                        if(!dryRun)
                            link.delete()
                    }
                    else
                        return@file
                }

                logger.info { "Creating link: $link -> $relative" }

                if (!dryRun) {
                    link.parentFile.mkdirs()
                    Files.createSymbolicLink(link.toPath(), relative.toPath())
                }
            }
        }

        logger.info { "Sorting $directory done!" }
    }
}
