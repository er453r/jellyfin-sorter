package com.er453r

data class ConfigSection(
    val name: String,
    val rules: List<Regex>,
) {
    companion object {
        const val ALL = "all"
    }
}
