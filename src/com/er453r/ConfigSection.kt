package com.er453r

data class ConfigSection(
    val name: String,
    val rules: List<Regex>,
    val replace: List<Pair<String,String>>,
) {
    companion object {
        const val ALL = "all"
    }
}
