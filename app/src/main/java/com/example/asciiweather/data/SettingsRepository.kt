package com.example.asciiweather.data
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.asciiweather.ui.BorderStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "ascii_settings")

data class UserSettings(
    val bgHex: String = "#0D1117",
    val textHex: String = "#00FF41",
    val borderStyle: BorderStyle = BorderStyle.CLASSIC,
    val morningHour: Int = 7,
    val eveningHour: Int = 19,
    val locationName: String = "BURRADOO, NSW",
    val latitude: Double = -34.5014,
    val longitude: Double = 150.4042,
    val cachedTemp: Double? = null,
    val cachedWind: Double? = null,
    val cachedWmo: Int = 0
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val BG = stringPreferencesKey("bg")
        val FG = stringPreferencesKey("fg")
        val BORDER = stringPreferencesKey("border")
        val M_HR = intPreferencesKey("m_hr")
        val E_HR = intPreferencesKey("e_hr")
        val LOC = stringPreferencesKey("loc")
        val LAT = doublePreferencesKey("lat")
        val LON = doublePreferencesKey("lon")
        val C_TEMP = doublePreferencesKey("c_temp")
        val C_WIND = doublePreferencesKey("c_wind")
        val C_WMO = intPreferencesKey("c_wmo")
    }
    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            bgHex = p[Keys.BG] ?: "#0D1117",
            textHex = p[Keys.FG] ?: "#00FF41",
            borderStyle = BorderStyle.valueOf(p[Keys.BORDER] ?: BorderStyle.CLASSIC.name),
            morningHour = p[Keys.M_HR] ?: 7,
            eveningHour = p[Keys.E_HR] ?: 19,
            locationName = p[Keys.LOC] ?: "BURRADOO, NSW",
            latitude = p[Keys.LAT] ?: -34.5014,
            longitude = p[Keys.LON] ?: 150.4042,
            cachedTemp = p[Keys.C_TEMP],
            cachedWind = p[Keys.C_WIND],
            cachedWmo = p[Keys.C_WMO] ?: 0
        )
    }
    suspend fun updateSettings(bg: String, fg: String, style: BorderStyle, mHr: Int, eHr: Int) {
        context.dataStore.edit { p ->
            p[Keys.BG] = bg; p[Keys.FG] = fg; p[Keys.BORDER] = style.name
            p[Keys.M_HR] = mHr.coerceIn(0, 23); p[Keys.E_HR] = eHr.coerceIn(0, 23)
        }
    }
    suspend fun updateLocation(name: String, lat: Double, lon: Double) {
        context.dataStore.edit { p -> p[Keys.LOC] = name; p[Keys.LAT] = lat; p[Keys.LON] = lon }
    }
    suspend fun cacheWeather(temp: Double, wind: Double, wmo: Int) {
        context.dataStore.edit { p -> p[Keys.C_TEMP] = temp; p[Keys.C_WIND] = wind; p[Keys.C_WMO] = wmo }
    }
}
