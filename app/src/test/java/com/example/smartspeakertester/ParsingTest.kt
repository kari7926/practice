package com.example.smartspeakertester

import com.example.smartspeakertester.parsing.CsvCommandParser
import com.example.smartspeakertester.parsing.XlsxCommandParser
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class ParsingTest {
    @Test
    fun csvParsesCommandsSkippingHeader() = runBlocking {
        val csv = "command\nTurn on lights\nPlay music\n"
        val parser = CsvCommandParser()
        val commands = parser.parse(ByteArrayInputStream(csv.toByteArray()))
        assertEquals(listOf("Turn on lights", "Play music"), commands)
    }

    @Test
    fun xlsxParsesCommandsSkippingHeader() = runBlocking {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Commands")
        sheet.createRow(1).createCell(0).setCellValue("Turn off TV")
        sheet.createRow(2).createCell(0).setCellValue("Volume up")
        val output = java.io.ByteArrayOutputStream()
        workbook.write(output)
        workbook.close()
        val parser = XlsxCommandParser()
        val commands = parser.parse(ByteArrayInputStream(output.toByteArray()))
        assertEquals(listOf("Turn off TV", "Volume up"), commands)
    }
}
