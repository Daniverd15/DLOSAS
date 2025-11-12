package com.example.proyecto.ui.theme

data class RouteResponseORS(
    val features: List<Feature>?
)

data class Feature(
    val geometry: Geometry?,
    val properties: Properties?
)

data class Geometry(
    val coordinates: List<List<Double>>?
)

data class Properties(
    val summary: Summary?
)

data class Summary(
    val distance: Double?,
    val duration: Double?
)
