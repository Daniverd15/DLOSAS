package com.example.proyecto.ui.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var start = ""
    private var end = ""
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private lateinit var infoLayout: LinearLayout
    private lateinit var distanceText: TextView
    private lateinit var durationText: TextView
    private var btnConfirmar: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)

        // Inicializar fragmento del mapa
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment

        if (mapFragment == null) {
            Toast.makeText(requireContext(), "Error: mapa no encontrado", Toast.LENGTH_SHORT).show()
            Log.e("MAPA_ERROR", "SupportMapFragment not found in fragment_mapa.xml")
        } else {
            com.google.android.gms.maps.MapsInitializer.initialize(requireContext())
            mapFragment.getMapAsync(this)
        }

        // Referencias UI
        val btnCalculate = view.findViewById<Button>(R.id.btnCalculateRouter)
        infoLayout = view.findViewById(R.id.infoLayout)
        distanceText = view.findViewById(R.id.textDistance)
        durationText = view.findViewById(R.id.textDuration)

        // ✅ Botón confirmar (si existe en tu layout)
        btnConfirmar = view.findViewById(R.id.btnConfirmarRuta)

        // Acción principal - Calcular ruta
        btnCalculate.setOnClickListener {
            if (::map.isInitialized) {
                map.clear()
                start = ""
                end = ""
                startMarker = null
                endMarker = null
                infoLayout.visibility = View.GONE

                Toast.makeText(requireContext(), "Toque el mapa para elegir origen y destino", Toast.LENGTH_SHORT).show()

                map.setOnMapClickListener { point ->
                    if (start.isEmpty()) {
                        start = "${point.longitude},${point.latitude}" // ORS usa long,lat
                        startMarker = map.addMarker(
                            MarkerOptions()
                                .position(point)
                                .title("Origen")
                                .icon(bitmapDescriptorFromVector(R.drawable.ic_start))
                        )
                        Toast.makeText(requireContext(), "Origen seleccionado ✅", Toast.LENGTH_SHORT).show()
                    } else if (end.isEmpty()) {
                        end = "${point.longitude},${point.latitude}" // ORS usa long,lat
                        endMarker = map.addMarker(
                            MarkerOptions()
                                .position(point)
                                .title("Destino")
                                .icon(bitmapDescriptorFromVector(R.drawable.ic_end))
                        )
                        Toast.makeText(requireContext(), "Destino seleccionado ✅", Toast.LENGTH_SHORT).show()
                        createRoute()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "El mapa no está listo", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Acción del botón confirmar
        btnConfirmar?.setOnClickListener {
            if (start.isNotEmpty() && end.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "¡Ubicación confirmada! El servicio fue solicitado.",
                    Toast.LENGTH_LONG
                ).show()

                // Volver a la pantalla anterior (HomeScreen)
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Primero calcula una ruta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        val initialLocation = LatLng(7.119349, -73.122741) // Bucaramanga
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 13f))

        Log.i("MAPA", "Google Maps ready ✅")
    }

    // Crear la ruta con OpenRouteService
    private fun createRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImJlYjdhNDg4OWViOTRhMWZiOTczZTlkM2NmODFkYmEzIiwiaCI6Im11cm11cjY0In0="
                val call = getRetrofit().create(ApiService::class.java)
                    .getRoute(apiKey, start, end)

                if (call.isSuccessful) {
                    val routeResponse = call.body()
                    if (!routeResponse?.features.isNullOrEmpty()) {
                        Log.i("RUTA", "Ruta ORS recibida correctamente ✅")
                        drawRouteORS(routeResponse)
                    } else {
                        showToast("No se encontró una ruta entre los puntos")
                        Log.e("RUTA", "Respuesta vacía o sin rutas")
                    }
                } else {
                    Log.e("RUTA", "Error HTTP ${call.code()} - ${call.message()}")
                    showToast("Error de conexión con OpenRouteService (${call.code()})")
                }
            } catch (e: Exception) {
                Log.e("RUTA", "Error: ${e.message}", e)
                showToast("Error: ${e.localizedMessage}")
            }
        }
    }

    // Dibujar la ruta y mostrar la información
    private fun drawRouteORS(routeResponse: RouteResponseORS?) {
        val coordinates = routeResponse?.features?.firstOrNull()?.geometry?.coordinates ?: return
        val polylineOptions = PolylineOptions()
            .width(10f)
            .color(Color.BLUE)

        for (coord in coordinates) {
            val lon = coord[0]
            val lat = coord[1]
            polylineOptions.add(LatLng(lat, lon))
        }

        val summary = routeResponse.features?.firstOrNull()?.properties?.summary
        val distancia = summary?.distance?.div(1000)?.let { String.format("%.2f km", it) } ?: "Desconocida"
        val duracion = summary?.duration?.div(60)?.let { String.format("%.1f min", it) } ?: "Desconocido"

        requireActivity().runOnUiThread {
            map.addPolyline(polylineOptions)

            // Centrar cámara
            val boundsBuilder = LatLngBounds.Builder()
            for (point in polylineOptions.points) boundsBuilder.include(point)
            val bounds = boundsBuilder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

            // Mostrar la info de distancia y tiempo
            distanceText.text = "Distancia: $distancia"
            durationText.text = "Duración: $duracion"
            infoLayout.visibility = View.VISIBLE

            Log.i("RUTA", "Ruta dibujada: $distancia, $duracion ✅")
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun showToast(msg: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor {
        return try {
            val vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId)
            if (vectorDrawable == null) {
                Log.e("MAPA", "Vector drawable not found: $vectorResId")
                return BitmapDescriptorFactory.defaultMarker()
            }

            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("MAPA", "Error creating bitmap from vector: ${e.message}", e)
            BitmapDescriptorFactory.defaultMarker()
        }
    }
}