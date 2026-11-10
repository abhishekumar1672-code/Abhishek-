package com.procut.editor.util

import com.procut.editor.model.TimeUs
import java.util.Locale

object TimecodeFormatter {

    fun formatTimecode(timeUs: TimeUs, frameRate: Int = 30): String {
        val safeFps = frameRate.coerceAtLeast(1)
        val totalSeconds = timeUs / 1_000_000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        val subSecondUs = timeUs % 1_000_000L
        val frame = (subSecondUs * safeFps / 1_000_000L).toInt()

        return String.format(Locale.US, "%02d:%02d:%02d:%02d", hours, minutes, seconds, frame)
    }

    fun formatDuration(timeUs: TimeUs): String {
        val totalSeconds = timeUs / 1_000_000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        val hours = minutes / 60L

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes % 60L, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
