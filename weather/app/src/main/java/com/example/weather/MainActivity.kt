package com.example.weather

import com.example.weather.BuildConfig
import android.os.Bundle
import android.preference.PreferenceActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.ui.theme.WeatherTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle


val testlocation = "宮若市"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

//天気ごとに変えるセクション
enum class WeatherType {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY
}

data class WeatherUi(
    val backgroundColor: List<Color>,
    val message: String,
    val textColor: Color
)

fun weatherUi(type: WeatherType): WeatherUi =
    when (type) {
        WeatherType.SUNNY -> WeatherUi(
            backgroundColor = listOf(
                Color(0xFF4FC3F7),
                Color(0xFFB3E5FC)
            ),
            "晴れ",
            textColor = Color(0xFF000000)
        )

        WeatherType.CLOUDY -> WeatherUi(
            backgroundColor = listOf(
                Color(0xFF4F5459),
                Color(0xFFECEFF1)
            ),
            "曇り",
            textColor = Color(0xFFFFFFFF)
        )

        WeatherType.RAINY -> WeatherUi(
            backgroundColor = listOf(
                Color(0xFF0A4973),
                Color(0xFF6F7980)
            ),
            "雨",
            textColor = Color(0xFFFFFFFF)
        )

        WeatherType.SNOWY -> WeatherUi(
            backgroundColor = listOf(
                Color(0xFFFFFFFF),
                Color(0xFF989898)
            ),
            "雪",
            textColor = Color(0xFF000000)
        )
    }

//API処理系
data class WeatherResponse(
    val weather: List<WeatherInfo>,
    val main: MainInfo
)

data class WeatherInfo(
    val main: String // "Clear", "Clouds", "Rain"
)

data class MainInfo(
    val temp: Double
)

interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ja"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ja"
    ): ForecastResponse

}

object WeatherApiClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

//日間預言者新聞API
data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt: Long,
    val main: ForecastMain,
    val weather: List<WeatherInfo>,
    val pop: Double, //降水確率
    val dt_txt: String
)

data class ForecastMain(
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)

fun toWeatherType(main: String): WeatherType =
    when (main) {
        "Clear" -> WeatherType.SUNNY
        "Clouds" -> WeatherType.CLOUDY
        "Rain", "Drizzle", "Thunderstorm" -> WeatherType.RAINY
        "Snow" -> WeatherType.SNOWY
        else -> WeatherType.CLOUDY
    }

fun ForecastResponse.toDailyWeather(): List<DailyWeather> {
    val zone = java.time.ZoneId.systemDefault()

    return list
        .groupBy {
            java.time.Instant.ofEpochSecond(it.dt)
                .atZone(zone)
                .toLocalDate()
        }
        .entries
        .take(7)
        .map { (date, items) ->

            val maxTemp = items.maxOf { it.main.temp_max }
            val minTemp = items.minOf { it.main.temp_min }

            val mainWeather = items
                .groupingBy { it.weather.first().main }
                .eachCount()
                .maxBy { it.value }
                .key

            val rainChance =
                (items.maxOf { it.pop } * 100).toInt()

            DailyWeather(
                date = "${date.monthValue}/${date.dayOfMonth}",
                dayOfWeek = date.dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    java.util.Locale.JAPAN
                ),
                icon = when (mainWeather) {
                    "Clear" -> "☀️"
                    "Clouds" -> "☁️"
                    "Rain", "Drizzle", "Thunderstorm" -> "🌧"
                    "Snow" -> "❄️"
                    else -> "☁️"
                },
                maxTemp = "${maxTemp.toInt()}℃",
                minTemp = "${minTemp.toInt()}℃",
                rainChance = "${rainChance}%",
                frog = rainFrog(rainChance)
            )
        }
}
fun ForecastResponse.toHourlyWeather(): List<HourlyWeather> {
    val zone = ZoneId.systemDefault()
    val base = list.first()   // 今〜3時間のデータ

    val startTime = Instant.ofEpochSecond(base.dt)
        .atZone(zone)
        .toLocalTime()

    return (0 until 5).map { i ->
        val time = startTime.plusMinutes((i * 30).toLong())
            .toString()
            .substring(0, 5)

        val rainChance = (base.pop * 100).toInt()

        HourlyWeather(
            time = time,
            icon = when (base.weather.first().main) {
                "Clear" -> "☀️"
                "Clouds" -> "☁️"
                "Rain", "Drizzle", "Thunderstorm" -> "🌧"
                "Snow" -> "❄️"
                else -> "☁️"
            },
            temp = "${base.main.temp.toInt()}℃",
            rainChance = "${rainChance}%"
        )
    }
}

//カエル関数
fun rainFrog(rainChance: Int): String {
    val percent = rainChance

    return when {
        percent >= 90 -> "🐸🐸🐸🐸"
        percent >= 80 -> "🐸🐸🐸"
        percent >= 50 -> "🐸🐸"
        percent >= 30 -> "🐸"
        else -> ""
    }
}


//ダミーデータ
data class DailyWeather(
    val date: String,        // "2/4"
    val dayOfWeek: String,   // "月"
    val icon: String,        // "☀️"
    val maxTemp: String,     // "22℃"
    val minTemp: String,     // "16℃"
    val rainChance: String,  // "30%"
    val frog: String         // "🐸"
)
data class HourlyWeather(
    val time: String,
    val icon: String,
    val temp: String,      // "18℃"
    val rainChance: String// "40%"
)

fun weatherMessage(type: WeatherType): String {
    val sunny = listOf(
        "晴れらしいよ。",
        "いい天気なんだってさ",
        "天気予報信用ならんからなぁ",
        "さぶれがはしゃぎそうな天気"
    )
    val messages = listOf(
        "今日も一日頑張ってね",
        "さぶれ～",
        "マロン～"
    )
    val rainy = listOf(
        "傘を持っていきましょう",
        "傘を",
        "傘をね。",
        "傘持ってますか？",
        "雨だそうです。"
    )
    val snowy = listOf(
        "雪だ！珍しい！！",
        "雪です",
        "マロンがはしゃぐぞぉ"
    )
    return when(type) {
        WeatherType.SUNNY -> sunny.random()
        WeatherType.RAINY -> rainy.random()
        WeatherType.SNOWY -> snowy.random()
        WeatherType.CLOUDY -> messages.random()
    }
}

//ここまでダミーデータ
@Composable
fun WeatherScreen(modifier: Modifier = Modifier) {
    val formattedDate = remember {
        SimpleDateFormat("M/d", Locale.JAPAN).format(Date())
    }

    var weatherType by remember { mutableStateOf(WeatherType.SUNNY) }
    var temp by remember { mutableStateOf("―") }
    var dailyList by remember { mutableStateOf<List<DailyWeather>>(emptyList()) }
    var hourlyList by remember { mutableStateOf<List<HourlyWeather>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val lat = 33.7081
            val lon = 130.6642
            val res = WeatherApiClient.api.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.WEATHER_API_KEY
            )

            weatherType = toWeatherType(res.weather.first().main)
            temp = "${res.main.temp.toInt()}℃"

            val forecast = WeatherApiClient.api.getForecast(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.WEATHER_API_KEY
            )

            dailyList = forecast.toDailyWeather()
            hourlyList = forecast.toHourlyWeather()
        } catch (e: Exception) {
            e.printStackTrace()
            temp = "取得失敗"
        }
    }
    val ui = weatherUi(weatherType)
    Box(modifier = Modifier.fillMaxSize()) {
        //背景レイヤー
        WeatherBackground(ui.backgroundColor)
        //情報レイヤー
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Header(formattedDate, testlocation, ui)
            MainWeather(temp = temp, ui = ui)
            val today = dailyList.firstOrNull()
            if (today != null) {
                RainSection(ui = ui, frogWeather = today)
            }
            val randomMessage = remember(weatherType) {
                weatherMessage(weatherType)
            }
            messageSection(randomMessage)
        }
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xf2EEEEEE).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)     //影の高さ
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (hourlyList.isNotEmpty()) {
                    HourlyForecast(hourlyList)
                }
            }
        }
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xf2EEEEEE).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)     //影の高さ
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (dailyList.isNotEmpty()) {
                    WeekForecast(dailyList)
                }
            }
        }

    }
}

@Composable
fun WeatherBackground(colors: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors)
            )
    )
}

@Composable
fun Header(date: String, city: String, ui: WeatherUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$date",
            style = MaterialTheme.typography.titleMedium,
            color = ui.textColor,
            fontSize = 25.sp
        )
        Text(
            text = "$city",
            style = MaterialTheme.typography.titleLarge,
            color = ui.textColor,
            fontSize = 25.sp
        )
    }
}

@Composable
fun MainWeather(temp: String, ui: WeatherUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(temp,
            style = MaterialTheme.typography.displayLarge,
            color = ui.textColor,
            fontSize = 100.sp
        )
        Text(ui.message, color = ui.textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RainSection(ui: WeatherUi, frogWeather: DailyWeather) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "降水確率",
            color = ui.textColor,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 25.sp
        )
        Text(
            text = frogWeather.frog,
            fontSize = 22.sp
        )             //TODO:カエルの数を降水確率で変えれるように
    }
}

@Composable
fun messageSection(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "今日のメッセージ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = message,
                modifier = Modifier.padding(15.dp),
                style = MaterialTheme.typography.bodyLarge, fontSize = 20.sp
            )
        }
    }
}

@Composable
fun WeekForecast(weekWeather: List<DailyWeather>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekWeather.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                // 日付と曜日
                Text(
                    text = day.date,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(day.dayOfWeek, fontSize = 15.sp, style = MaterialTheme.typography.labelSmall)

                // 天気アイコン
                Text(day.icon, fontSize = 15.sp, style = MaterialTheme.typography.titleMedium)

                // 最高気温
                Text(day.maxTemp, fontSize = 15.sp, style = MaterialTheme.typography.bodySmall)

                // 降水確率
                Text(day.rainChance, fontSize = 15.sp, style = MaterialTheme.typography.bodySmall)

                // 最低気温
                Text(day.minTemp, fontSize = 15.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
@Composable
fun HourlyForecast(hourlyWeather: List<HourlyWeather>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        hourlyWeather.forEach { hour ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(hour.time, fontSize = 12.sp, style = MaterialTheme.typography.labelSmall)
                Text(hour.icon, fontSize = 36.sp)
                Text(hour.temp, fontSize = 18.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherTheme {
        WeatherScreen()
    }
}