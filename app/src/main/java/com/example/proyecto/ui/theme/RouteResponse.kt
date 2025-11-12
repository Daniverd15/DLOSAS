package com.example.proyecto.ui.theme

data class RouteResponse(
    val routes: List<Route>?,
    val status: String?
)

data class Route(
    val overview_polyline: OverviewPolyline?,
    val legs: List<Leg>?
)

data class OverviewPolyline(
    val points: String?
)

data class Leg(
    val distance: Distance?,
    val duration: Duration?
)

data class Distance(val text: String?)
data class Duration(val text: String?)
