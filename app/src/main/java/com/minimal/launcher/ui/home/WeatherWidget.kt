package com.minimal.launcher.ui.home

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

private fun fetchJson(url: String): JSONObject {
    val connection = URL(url).openConnection() as HttpURLConnection
    return try {
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
    } finally {
        connection.disconnect()
    }
}

private fun condition(code: Int) = when (code) {
    0 -> "Clear"
    1 -> "Mostly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    in 51..57 -> "Drizzle"
    in 61..67 -> "Rain"
    in 71..77, 85, 86 -> "Snow"
    in 80..82 -> "Rain showers"
    in 95..99 -> "Thunderstorms"
    else -> "Current weather"
}

@Composable
fun WeatherWidget() {
    val prefs = LocalContext.current.getSharedPreferences("weather", Context.MODE_PRIVATE)
    var city by remember { mutableStateOf(prefs.getString("city", "").orEmpty()) }
    var editing by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    var forecast by remember(city) { mutableStateOf(prefs.getString("forecast", "").orEmpty()) }
    var sky by remember(city) { mutableStateOf(prefs.getString("sky", null) ?: forecast.substringBefore("\n").substringAfterLast(" · ", "")) }
    var message by remember(city) { mutableStateOf("") }
    LaunchedEffect(city, attempt) {
        if (city.isBlank()) return@LaunchedEffect
        while (true) {
            val age = System.currentTimeMillis() - prefs.getLong("updated", 0)
            if (forecast.isNotEmpty() && age in 0 until 900000L) delay(900000L - age)
            try {
                message = if (forecast.isEmpty()) "Loading weather…" else ""
                val reading = withContext(Dispatchers.IO) {
                    val places = fetchJson("https://geocoding-api.open-meteo.com/v1/search?count=1&name=" + Uri.encode(city))
                    val place = places.optJSONArray("results")?.optJSONObject(0)
                        ?: throw NoSuchElementException()
                    val current = fetchJson("https://api.open-meteo.com/v1/forecast?latitude=" +
                        place.getDouble("latitude") + "&longitude=" + place.getDouble("longitude") +
                        "&current=temperature_2m,weather_code").getJSONObject("current")
                    val temp = current.getDouble("temperature_2m").roundToInt()
                    val sky = condition(current.getInt("weather_code"))
                    val stamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                    sky to "${place.getString("name")} · $temp°C · $sky\nOpen-Meteo · $stamp"
                }
                sky = reading.first
                forecast = reading.second
                prefs.edit().putString("sky", sky).putString("forecast", forecast).putLong("updated", System.currentTimeMillis()).apply()
                message = ""
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: NoSuchElementException) {
                message = "City not found · tap to change"
            } catch (_: Exception) {
                message = "Weather unavailable · tap to retry"
            }
            delay(if (message.isEmpty()) 900000L else 300000L)
        }
    }
    OutlinedButton(onClick = { editing = true }) {
        WeatherMotion(sky)
        Spacer(Modifier.width(8.dp))
        Text(listOf(forecast, message).filter { it.isNotEmpty() }.joinToString("\n")
            .ifEmpty { if (city.isBlank()) "Weather · Choose city" else city })
    }
    if (editing) {
        var input by remember { mutableStateOf(city) }
        AlertDialog(onDismissRequest = { editing = false }, title = { Text("Weather location") },
            text = { OutlinedTextField(input, { input = it }, singleLine = true, label = { Text("City") }) },
            confirmButton = { TextButton(enabled = input.isNotBlank(), onClick = {
                val query = input.trim()
                if (query != city) {
                    prefs.edit().putString("city", query).remove("sky").remove("forecast").remove("updated").apply()
                    city = query
                } else prefs.edit().remove("updated").apply()
                attempt++
                editing = false
            }) { Text("Show weather") } }, dismissButton = {
                TextButton(onClick = { editing = false }) { Text("Cancel") }
            })
    }
}
