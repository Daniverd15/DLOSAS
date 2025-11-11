package com.example.proyecto.ui.theme

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var markerInicio: Marker? = null
    private var markerDestino: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Toast.makeText(requireContext(), "Toque el mapa para marcar origen y destino", Toast.LENGTH_LONG).show()

        // Centra la cámara (por ejemplo, Bucaramanga)
        val inicio = LatLng(7.1193, -73.1227)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inicio, 13f))

        // Escuchar los toques del usuario
        mMap.setOnMapClickListener { latLng ->
            if (markerInicio == null) {
                // Primer toque: punto de inicio
                markerInicio = mMap.addMarker(
                    MarkerOptions().position(latLng).title("Inicio")
                )
            } else if (markerDestino == null) {
                // Segundo toque: punto de destino
                markerDestino = mMap.addMarker(
                    MarkerOptions().position(latLng).title("Destino")
                )
                // Dibujar la ruta entre los dos puntos
                trazarRuta(markerInicio!!.position, markerDestino!!.position)
            } else {
                // Reiniciar selección
                mMap.clear()
                markerInicio = mMap.addMarker(MarkerOptions().position(latLng).title("Inicio"))
                markerDestino = null
            }
        }
    }

    private fun trazarRuta(origen: LatLng, destino: LatLng) {
        val apiKey = getString(R.string.google_maps_key)
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&key=$apiKey"

        thread {
            try {
                val connection = URL(url).openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val data = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val json = JSONObject(data)
                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "No se encontró ruta", Toast.LENGTH_SHORT).show()
                    }
                    return@thread
                }

                val puntos = decodificarRuta(
                    routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                )

                requireActivity().runOnUiThread {
                    val polylineOptions = PolylineOptions()
                        .addAll(puntos)
                        .width(10f)
                        .color(Color.BLUE)
                    mMap.addPolyline(polylineOptions)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun decodificarRuta(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat / 1E5, lng / 1E5)
            poly.add(latLng)
        }

        return poly
    }
}
