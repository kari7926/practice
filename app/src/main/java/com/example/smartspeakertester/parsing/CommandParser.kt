package com.example.smartspeakertester.parsing

import java.io.InputStream

interface CommandParser {
    suspend fun parse(inputStream: InputStream): List<String>
}
