package com.example.smartspeakertester.domain

data class RangeValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val resolvedStart: Int = 1,
    val resolvedEnd: Int = 0
)

object RangeValidator {
    fun validate(totalCommands: Int, options: TestRunOptions): RangeValidationResult {
        if (totalCommands <= 0) {
            return RangeValidationResult(false, "No commands to run.")
        }
        if (options.useAll) {
            return RangeValidationResult(true, null, 1, totalCommands)
        }
        val start = options.startIndex ?: return RangeValidationResult(false, "Start index required.")
        val end = options.endIndex ?: return RangeValidationResult(false, "End index required.")
        if (start < 1 || end < 1) {
            return RangeValidationResult(false, "Indices must be 1 or greater.")
        }
        if (start > totalCommands || end > totalCommands) {
            return RangeValidationResult(false, "Indices must be within command count ($totalCommands).")
        }
        if (start > end) {
            return RangeValidationResult(false, "Start index must be before end index.")
        }
        return RangeValidationResult(true, null, start, end)
    }
}
