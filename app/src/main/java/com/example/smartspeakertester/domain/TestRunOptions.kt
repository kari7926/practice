package com.example.smartspeakertester.domain

data class TestRunOptions(
    val useAll: Boolean = true,
    val startIndex: Int? = null,
    val endIndex: Int? = null
)
