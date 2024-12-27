package com.er453r

import mu.KotlinLogging
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class Sorter {
    private fun mutatedPath(path:String, replacements:List<Pair<String,String>>):String{
        var result = path

        replacements.forEach {
            result = result.replace(Regex(it.first, RegexOption.IGNORE_CASE), it.second)
        }

        return result
    }

    fun sort(sourceDirectory: File, targetDirectory: File, config: List<ConfigSection>, dryRun: Boolean = true) {
        val sort = mutableMapOf<String, MutableList<File>>()

        val replacements = config.first { it.name == ConfigSection.ALL }.replace

        walk(sourceDirectory, filter = { file ->
            config.first { it.name == ConfigSection.ALL }.rules.firstOrNull { it.containsMatchIn(file.path) } != null
        }) { file ->
            run {
                config.filter { it.name != ConfigSection.ALL }.forEach { section ->
                    val sectionReplacements = replacements + section.replace

                    if (section.rules.firstOrNull { it.containsMatchIn(mutatedPath(file.path, sectionReplacements)) } != null) {
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

        val shouldExist = mutableSetOf<String>()

        // link creation
        sort.forEach { (section, list) ->
            val dir = File("${targetDirectory.path}/$section")
            val sectionReplacements = replacements + config.first { it.name == section }.replace

            list.forEach file@{
                val link = File("$dir/${mutatedPath("${it.relativeTo(sourceDirectory)}", sectionReplacements)}")
                val relative = File("${it.relativeTo(link.parentFile)}")

                shouldExist += link.absolutePath

                if (link.exists()) {
                    val currentRelative = Files.readSymbolicLink(link.toPath())

                    if (currentRelative.toString() != relative.toString()) {
                        logger.info { "Removing invalid link $currentRelative != $relative" }

                        if (!dryRun)
                            link.delete()
                    } else
                        return@file
                }

                logger.info { "Creating link: $link -> $relative" }

                if (!dryRun) {
                    link.parentFile.mkdirs()
                    Files.createSymbolicLink(link.toPath(), relative.toPath())
                }
            }
        }

        logger.info { "Sorting $sourceDirectory done! Clean up..." }

        logger.info { "Removing unneeded files..." }
        walk(File(targetDirectory.path), { !it.isDirectory }) {
            if (it.absolutePath !in shouldExist) {
                logger.info { "$it should not exist! Deleting..." }

                if (!dryRun)
                    it.delete()
            }
        }

        logger.info { "Removing unneeded directories..." }
        walk(File(targetDirectory.path), { it.isDirectory }) {
            if (it.listFiles()!!.isEmpty()) {
                logger.info { "$it is empty! Deleting..." }

                if (!dryRun)
                    it.delete()
            }
        }
    }
}
