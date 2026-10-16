package com.example.asciiweather
import android.Manifest
import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import com.example.asciiweather.data.*
import com.example.asciiweather.ui.*
import com.example.asciiweather.widget.AsciiWeatherWidget
import com.example.asciiweather.worker.BomWeatherWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = SettingsRepository(this)
        val api = OpenMeteoBomApi.create()
        setContent { AsciiWeatherApp(repo, api) }
    }
}
@Composable
fun AsciiWeatherApp(repo: SettingsRepository, api: OpenMeteoBomApi) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val s by repo.settingsFlow.collectAsState(initial = UserSettings())
    val locHelper = remember { LocationHelper(ctx) }
    var res by remember { mutableStateOf<BomWeatherResponse?>(null) }
    var status by remember { mutableStateOf("SYNCING BOM ACCESS-G...") }
    var showCfg by remember { mutableStateOf(false) }
    var frame by remember { mutableIntStateOf(0) }

    val bg = remember(s.bgHex) { try { Color(AColor.parseColor(s.bgHex.trim())) } catch (e: Exception) { Color(0xFF0D1117) } }
    val fg = remember(s.textHex) { try { Color(AColor.parseColor(s.textHex.trim())) } catch (e: Exception) { Color(0xFF00FF41) } }
    val mono = TextStyle(fontFamily = FontFamily.Monospace, color = fg, fontSize = 14.sp, lineHeight = 18.sp)

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        if (p[Manifest.permission.ACCESS_FINE_LOCATION] == true || p[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            status = "LOCKING GPS..."
            scope.launch {
                locHelper.detectCurrentLocation()?.let {
                    repo.updateLocation(it.name, it.latitude, it.longitude)
                    status = "GPS: ${it.name}"
                } ?: run { status = "ERR: GPS TIMEOUT" }
            }
        }
    }

    LaunchedEffect(Unit) { while (true) { delay(800L); frame = (frame + 1) % 2 } }
    LaunchedEffect(s.latitude, s.longitude) {
        try {
            val r = api.getBomWeather(s.latitude, s.longitude)
            res = r
            repo.cacheWeather(r.current.temperature, r.current.windSpeed, r.current.weatherCode)
            AsciiWeatherWidget().updateAll(ctx)
            status = "BOM_ACCESS_GLOBAL [OK]"
        } catch (e: Exception) { status = "ERR: ${e.localizedMessage?.take(20)}" }
    }
    CompositionLocalProvider(LocalTextStyle provides mono) {
        Column(Modifier.fillMaxSize().background(bg).padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), Arrangement.SpaceBetween) {
                Text(if (showCfg) "[ < TERMINAL ]" else "[ * CONFIG ]", Modifier.clickable { showCfg = !showCfg })
                if (!showCfg) Text("[ @ GPS AUTO ]", Modifier.clickable {
                    permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                })
            }
            if (!showCfg) {
                val cur = res?.current
                val cat = cur?.let { AsciiArtRepository.getCategoryFromWmo(it.weatherCode) } ?: "OVERCAST"
                val art = (AsciiArtRepository.weatherArtMap[cat] ?: listOf(""))[frame].lines()
                val lines = mutableListOf("LOC  : ${s.locationName}", "COOR : ${s.latitude}, ${s.longitude}", "------------------------------").apply {
                    addAll(art)
                    add("------------------------------")
                    add("COND : $cat")
                    add("TEMP : ${cur?.temperature ?: "--"} C")
                    add("WIND : ${cur?.windSpeed ?: "--"} km/h")
                    add("THERM: ${cur?.let { AsciiArtRepository.buildMiniThermometer(it.temperature) } ?: "--"}")
                    add("------------------------------")
                    add("7-DAY MIN/MAX BAR CHART (C)")
                    res?.daily?.let { addAll(AsciiArtRepository.buildSevenDayChart(it)) }
                }
                Text(AsciiArtRepository.renderAsciiBox("BOM ACCESS-G TELEMETRY", lines, s.borderStyle))
                Spacer(Modifier.height(8.dp))
                Text("> $status")
            } else {
                var bHex by remember { mutableStateOf(s.bgHex) }
                var fHex by remember { mutableStateOf(s.textHex) }
                var st by remember { mutableStateOf(s.borderStyle) }
                var mH by remember { mutableStateOf(s.morningHour.toString()) }
                var eH by remember { mutableStateOf(s.eveningHour.toString()) }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BG HEX:"); BasicTextField(bHex, { bHex = it }, Modifier.fillMaxWidth().border(1.dp, fg).padding(6.dp), textStyle = mono, cursorBrush = SolidColor(fg))
                    Text("FG HEX:"); BasicTextField(fHex, { fHex = it }, Modifier.fillMaxWidth().border(1.dp, fg).padding(6.dp), textStyle = mono, cursorBrush = SolidColor(fg))
                    BorderStyle.entries.forEach { style -> Text("${if (style == st) "[X]" else "[ ]"} ${style.label}", Modifier.clickable { st = style }) }
                    Text("MORNING HR:"); BasicTextField(mH, { mH = it }, Modifier.fillMaxWidth().border(1.dp, fg).padding(6.dp), textStyle = mono, cursorBrush = SolidColor(fg))
                    Text("EVENING HR:"); BasicTextField(eH, { eH = it }, Modifier.fillMaxWidth().border(1.dp, fg).padding(6.dp), textStyle = mono, cursorBrush = SolidColor(fg))
                    Text("[ SAVE & SCHEDULE ]", Modifier.border(1.dp, fg).padding(8.dp).clickable {
                        scope.launch {
                            repo.updateSettings(bHex, fHex, st, mH.toIntOrNull() ?: 7, eH.toIntOrNull() ?: 19)
                            BomWeatherWorker.scheduleTwiceDaily(ctx, mH.toIntOrNull() ?: 7, eH.toIntOrNull() ?: 19)
                            AsciiWeatherWidget().updateAll(ctx)
                            showCfg = false
                        }
                    })
                }
            }
        }
    }
}
