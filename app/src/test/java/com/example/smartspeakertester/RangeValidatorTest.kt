package com.example.smartspeakertester

import com.example.smartspeakertester.domain.RangeValidator
import com.example.smartspeakertester.domain.TestRunOptions
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeValidatorTest {
    @Test
    fun validAllRange() {
        val result = RangeValidator.validate(5, TestRunOptions(useAll = true))
        assertTrue(result.isValid)
    }

    @Test
    fun invalidWhenStartGreaterThanEnd() {
        val result = RangeValidator.validate(5, TestRunOptions(useAll = false, startIndex = 4, endIndex = 2))
        assertFalse(result.isValid)
    }

    @Test
    fun invalidWhenOutOfBounds() {
        val result = RangeValidator.validate(3, TestRunOptions(useAll = false, startIndex = 1, endIndex = 5))
        assertFalse(result.isValid)
    }
}
