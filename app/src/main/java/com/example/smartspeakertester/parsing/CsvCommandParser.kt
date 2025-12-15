package com.example.smartspeakertester.parsing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale

class CsvCommandParser : CommandParser {
    override suspend fun parse(inputStream: InputStream): List<String> = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.forEachIndexed { index, rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty()) return@forEachIndexed
                val firstCell = line.split(',').firstOrNull()?.trim().orEmpty()
                if (index == 0 && isHeader(firstCell)) return@forEachIndexed
                if (firstCell.isNotEmpty()) {
                    commands.add(firstCell)
                }
            }
        }
        commands
    }

    private fun isHeader(cell: String): Boolean {
        val lower = cell.lowercase(Locale.ROOT)
        return lower.contains("command")
    }
}
