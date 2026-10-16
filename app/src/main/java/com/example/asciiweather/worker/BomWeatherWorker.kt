package com.example.asciiweather.worker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.example.asciiweather.data.OpenMeteoBomApi
import com.example.asciiweather.data.SettingsRepository
import com.example.asciiweather.ui.AsciiArtRepository
import com.example.asciiweather.widget.AsciiWeatherWidget
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class BomWeatherWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = try {
        val repo = SettingsRepository(applicationContext)
        val s = repo.settingsFlow.first()
        val res = OpenMeteoBomApi.create().getBomWeather(s.latitude, s.longitude)
        repo.cacheWeather(res.current.temperature, res.current.windSpeed, res.current.weatherCode)
        AsciiWeatherWidget().updateAll(applicationContext)
        val cat = AsciiArtRepository.getCategoryFromWmo(res.current.weatherCode)
        val therm = AsciiArtRepository.buildMiniThermometer(res.current.temperature)
        val mgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.createNotificationChannel(NotificationChannel("bom_ch", "BOM Alerts", NotificationManager.IMPORTANCE_DEFAULT))
        val notif = NotificationCompat.Builder(applicationContext, "bom_ch")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("BOM ACCESS-G // ${s.locationName}")
            .setContentText("$therm [$cat]")
            .build()
        mgr.notify(1001, notif)
        Result.success()
    } catch (e: Exception) { Result.retry() }

    companion object {
        fun scheduleTwiceDaily(ctx: Context, mHr: Int, eHr: Int) {
            listOf(mHr to "bom_m", eHr to "bom_e").forEach { (hr, name) ->
                val now = LocalDateTime.now()
                var next = now.withHour(hr).withMinute(0).withSecond(0)
                if (now.isAfter(next)) next = next.plusDays(1)
                val req = PeriodicWorkRequestBuilder<BomWeatherWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
                    .build()
                WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.UPDATE, req)
            }
        }
    }
}
