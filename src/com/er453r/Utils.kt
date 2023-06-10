package com.er453r

import java.io.File
import kotlin.system.exitProcess

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
