package com.example.asciiweather.data
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class BomWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("daily") val daily: DailyWeather? = null
)
data class CurrentWeather(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("weather_code") val weatherCode: Int
)
data class DailyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>
)
interface OpenMeteoBomApi {
    @GET("v1/forecast")
    suspend fun getBomWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,wind_speed_10m,weather_code",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("models") models: String = "bom_access_global",
        @Query("timezone") timezone: String = "auto"
    ): BomWeatherResponse
    companion object {
        fun create(): OpenMeteoBomApi = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(OpenMeteoBomApi::class.java)
    }
}
