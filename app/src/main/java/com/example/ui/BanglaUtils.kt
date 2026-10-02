package com.example.ui

object BanglaUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBanglaDigits(input: Any): String {
        val str = input.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatSecondsToTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        val minStr = if (mins < 10) "0$mins" else "$mins"
        val secStr = if (secs < 10) "0$secs" else "$secs"
        return "${toBanglaDigits(minStr)}:${toBanglaDigits(secStr)}"
    }
}
