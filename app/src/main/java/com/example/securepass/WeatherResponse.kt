package com.example.securepass

data class WeatherResponse(
    val main: Main
)

data class Main(
    val temp: Double
)
