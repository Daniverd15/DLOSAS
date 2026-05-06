package com.example.proyecto.data.vehicle

import com.example.proyecto.Vehiculo

data class NewVehicleInput(
    val tipoVehiculo: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val placa: String,
    val kilometraje: Int,
    val color: String
)

interface VehicleRepository {
    suspend fun getCurrentUserVehicles(): List<Vehiculo>
    suspend fun saveVehicle(input: NewVehicleInput): String
}
