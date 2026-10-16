package com.example.asciiweather.widget
import android.content.Context
import android.graphics.*
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import com.example.asciiweather.MainActivity
import com.example.asciiweather.data.SettingsRepository
import com.example.asciiweather.ui.AsciiArtRepository
import kotlinx.coroutines.flow.first

class AsciiWeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val s = SettingsRepository(context).settingsFlow.first()
        provideContent {
            val bg = try { Color.parseColor(s.bgHex.trim()) } catch (e: Exception) { Color.BLACK }
            val fg = try { Color.parseColor(s.textHex.trim()) } catch (e: Exception) { Color.GREEN }
            val cat = AsciiArtRepository.getCategoryFromWmo(s.cachedWmo)
            val lines = listOf(
                "LOC  : ${s.locationName}",
                "COND : $cat",
                "TEMP : ${s.cachedTemp ?: "--"} C",
                "WIND : ${s.cachedWind ?: "--"} km/h",
                "THERM: ${s.cachedTemp?.let { AsciiArtRepository.buildMiniThermometer(it) } ?: "--"}"
            )
            val box = AsciiArtRepository.renderAsciiBox("BOM WIDGET", lines, s.borderStyle)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fg; textSize = 30f; typeface = Typeface.MONOSPACE }
            val bLines = box.lines()
            val bmp = Bitmap.createBitmap(620, bLines.size * 38 + 24, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp).apply { drawColor(bg) }
            bLines.forEachIndexed { i, l -> c.drawText(l, 12f, 36f + i * 36f, paint) }
            Box(modifier = GlanceModifier.fillMaxSize().padding(4.dp).clickable(actionStartActivity<MainActivity>())) {
                Image(provider = ImageProvider(bmp), contentDescription = "ASCII Weather", modifier = GlanceModifier.fillMaxSize())
            }
        }
    }
}
class AsciiWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AsciiWeatherWidget()
}
