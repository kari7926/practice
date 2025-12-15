package com.example.smartspeakertester.parsing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.util.Locale

class XlsxCommandParser : CommandParser {
    override suspend fun parse(inputStream: InputStream): List<String> = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        XSSFWorkbook(inputStream).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            sheet.forEachIndexed { rowIndex, row ->
                val cell = row.getCell(0)
                val value = cell?.stringCellValue?.trim().orEmpty()
                if (value.isEmpty()) return@forEachIndexed
                if (rowIndex == 0 && isHeader(value)) return@forEachIndexed
                commands.add(value)
            }
        }
        commands
    }

    private fun isHeader(value: String): Boolean {
        val lower = value.lowercase(Locale.ROOT)
        return lower.contains("command")
    }
}
