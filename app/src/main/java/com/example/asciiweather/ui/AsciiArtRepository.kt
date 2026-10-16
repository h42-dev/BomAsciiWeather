package com.example.asciiweather.ui
import com.example.asciiweather.data.DailyWeather
import java.time.LocalDate
import java.time.format.TextStyle as JStyle
import java.util.Locale
import kotlin.math.roundToInt

enum class BorderStyle(val label: String, val h: Char, val v: Char, val corner: Char) {
    CLASSIC("Classic (+ - |)", '-', '|', '+'),
    DOUBLE("Double (= || #)", '=', '|', '#'),
    MINIMAL("Minimal (. : +)", '.', ':', '+')
}

object AsciiArtRepository {
    val weatherArtMap: Map<String, List<String>> = mapOf(
        "CLEAR" to listOf("   \\  |  /\n    .-.\n --(   )--\n    `-'\n   /  |  \\", "    \\ | /\n    .-.\n  -(   )-\n    `-'\n    / | \\"),
        "OVERCAST" to listOf("    .--.\n .-(    ).\n(___.__)__)", "     .--.\n  .-(    ).\n (___.__)__)"),
        "RAIN" to listOf("    .-.\n   (   ).\n  (___(__)\n  ' ' ' '\n ' ' ' '", "    .-.\n   (   ).\n  (___(__)\n , , , ,\n  , , , ,"),
        "STORM" to listOf("    .-.\n   (   ).\n  (___(__)\n    /_\n     /", "    .-.\n   (   ).\n  (___(__)\n  ' /_ '\n   ' / '")
    )
    fun getCategoryFromWmo(c: Int) = when (c) {
        0, 1 -> "CLEAR"; 2, 3, 45, 48 -> "OVERCAST"
        in 51..67, in 80..82 -> "RAIN"; in 95..99 -> "STORM"; else -> "OVERCAST"
    }
    fun buildMiniThermometer(t: Double): String {
        val f = ((t.coerceIn(0.0, 45.0) / 45.0) * 8).toInt()
        return "(o)[${"#".repeat(f)}${"-".repeat(8 - f)}] ${t}C"
    }
    fun buildSevenDayChart(d: DailyWeather, w: Int = 12): List<String> {
        val n = minOf(7, d.time.size, d.tempMax.size, d.tempMin.size)
        if (n == 0) return listOf("NO DATA")
        val minW = d.tempMin.take(n).minOrNull() ?: 0.0
        val span = ((d.tempMax.take(n).maxOrNull() ?: 40.0) - minW).coerceAtLeast(1.0)
        return (0 until n).map { i ->
            val day = try { LocalDate.parse(d.time[i]).dayOfWeek.getDisplayName(JStyle.SHORT, Locale.ENGLISH).uppercase().take(3) } catch (e: Exception) { "DAY" }
            val s = (((d.tempMin[i] - minW) / span) * (w - 1)).roundToInt().coerceIn(0, w - 1)
            val e = (((d.tempMax[i] - minW) / span) * (w - 1)).roundToInt().coerceIn(s, w - 1)
            val bar = (0 until w).joinToString("") { if (it in s..e) "#" else "." }
            "$day${d.tempMin[i].roundToInt().toString().padStart(2)}[$bar]${d.tempMax[i].roundToInt().toString().padStart(2)}C"
        }
    }
    fun renderAsciiBox(title: String, lines: List<String>, s: BorderStyle, w: Int = 34): String {
        val iw = w - 2
        val top = "${s.corner}${s.h.toString().repeat(iw)}${s.corner}"
        val div = "${s.corner}${"=".repeat(iw)}${s.corner}"
        val head = "${s.v} ${title.padEnd(iw - 2).take(iw - 2)}${s.v}"
        val body = lines.joinToString("\n") { "${s.v} ${it.take(iw - 2).padEnd(iw - 2)}${s.v}" }
        return listOf(top, head, div, body, top).joinToString("\n")
    }
}
