package com.er453r

data class Config(
    val sourceDirectory: String,
    val targetDirectory: String,
    val configFile: String,
    val dryRun: Boolean,
    val intervalSeconds: String,
)
