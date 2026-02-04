package com.example.weather

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.ui.theme.WeatherTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val today = LocalDate.now()
val formattedDate = today.format(DateTimeFormatter.ofPattern("M/d"))
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
data class DailyWeather(
    val date: String,        // "2/4"
    val dayOfWeek: String,   // "月"
    val icon: String,        // "☀️"
    val maxTemp: String,     // "22℃"
    val minTemp: String,     // "16℃"
    val rainChance: String   // "30%"
)
val weekWeather = listOf(
    DailyWeather("2/4", "月", "☀️", "22℃", "16℃", "30%"),
    DailyWeather("2/5", "火", "☁️", "20℃", "15℃", "50%"),
    DailyWeather("2/6", "水", "🌧", "18℃", "14℃", "80%"),
    DailyWeather("2/7", "木", "☀️", "21℃", "15℃", "20%"),
    DailyWeather("2/8", "金", "☁️", "19℃", "13℃", "40%"),
    DailyWeather("2/9", "土", "☀️", "23℃", "17℃", "10%"),
    DailyWeather("2/10", "日", "🌧", "17℃", "12℃", "70%")
)
val dailyWeather = listOf(
    DailyWeather("2/4", "月", "☀️", "22℃", "16℃", "30%"),
    DailyWeather("2/5", "火", "☁️", "20℃", "15℃", "50%"),
    DailyWeather("2/6", "水", "🌧", "18℃", "14℃", "80%"),
    DailyWeather("2/7", "木", "☀️", "21℃", "15℃", "20%"),
    DailyWeather("2/8", "金", "☁️", "19℃", "13℃", "40%"),
    DailyWeather("2/9", "土", "☀️", "23℃", "17℃", "10%"),
    DailyWeather("2/10", "日", "🌧", "17℃", "12℃", "70%")
)
val message = "今日も一日頑張ってね"

@Composable
fun WeatherScreen(modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        //背景レイヤー
        WeatherBackground()
        //情報レイヤー
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Header(formattedDate, testlocation)
            MainWeather()
            RainSection()
            messageSection(message)
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
                DailyForecast(dailyWeather)
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
                WeekForecast(weekWeather)
            }
        }

    }
}
@Composable
fun WeatherBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4FC3F7),
                        Color(0xFFB3E5FC)
                    )
                )
            )
    ) {
        Text("", modifier = Modifier.padding(16.dp))
    }
}
@Composable
fun Header(date: String, city: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$date",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "$city",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
@Composable
fun MainWeather() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("22℃", style = MaterialTheme.typography.displayLarge)
        Text("晴れ")
    }
}
@Composable
fun RainSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("降水確率")
        Text("🐸🐸🐸")
    }
}
@Composable
fun messageSection(message: String,modifier: Modifier = Modifier) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column (
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
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
                , fontSize = 20.sp
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
                Text(day.date, style = MaterialTheme.typography.labelSmall)
                Text(day.dayOfWeek, style = MaterialTheme.typography.labelSmall)

                // 天気アイコン
                Text(day.icon, style = MaterialTheme.typography.titleMedium)

                // 最高気温
                Text(day.maxTemp, style = MaterialTheme.typography.bodySmall)

                // 降水確率
                Text(day.rainChance, style = MaterialTheme.typography.bodySmall)

                // 最低気温
                Text(day.minTemp, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
@Composable
fun DailyForecast(weekWeather: List<DailyWeather>) {
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
                Text(day.date, style = MaterialTheme.typography.labelSmall)
                Text(day.dayOfWeek, style = MaterialTheme.typography.labelSmall)

                // 天気アイコン
                Text(day.icon, style = MaterialTheme.typography.titleMedium)

                // 最高気温
                Text(day.maxTemp, style = MaterialTheme.typography.bodySmall)

                // 降水確率
                Text(day.rainChance, style = MaterialTheme.typography.bodySmall)

                // 最低気温
                Text(day.minTemp, style = MaterialTheme.typography.bodySmall)
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