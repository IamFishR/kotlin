package com.win11launcher.data.models

data class FilterCriteria(
    val keywords: List<String> = emptyList(),
    val excludeKeywords: List<String> = emptyList(),
    val regexPattern: String = "",
    val caseSensitive: Boolean = false
)

enum class FilterType {
    ALL,
    KEYWORD_INCLUDE,
    KEYWORD_EXCLUDE,
    REGEX
}