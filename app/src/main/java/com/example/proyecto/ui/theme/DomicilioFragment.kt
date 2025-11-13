package com.example.proyecto.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class DomicilioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_domicilio, container, false)

            // ✅ Configurar toolbar con botón volver
            val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener {
                // Volver a la pantalla anterior (HomeScreen)
                parentFragmentManager.popBackStack()
            }

            // Inicializar vistas
            val etDireccion = view.findViewById<TextInputEditText>(R.id.etDireccion)
            val etTelefono = view.findViewById<TextInputEditText>(R.id.etTelefono)
            val etNotas = view.findViewById<TextInputEditText>(R.id.etNotas)
            val btnSolicitar = view.findViewById<Button>(R.id.btnSolicitar)
            val tvUsuario = view.findViewById<TextView>(R.id.tvUsuario)

            // Obtener datos del usuario actual
            val currentUser = FirebaseAuth.getInstance().currentUser
            tvUsuario.text = "¡Hola, ${currentUser?.displayName ?: "Usuario"}!"

            // Pre-llenar teléfono si está disponible
            currentUser?.phoneNumber?.let {
                etTelefono.setText(it)
            }

            btnSolicitar.setOnClickListener {
                val direccion = etDireccion.text.toString().trim()
                val telefono = etTelefono.text.toString().trim()
                val notas = etNotas.text.toString().trim()

                // Validaciones
                if (direccion.isEmpty()) {
                    etDireccion.error = "Ingresa tu dirección"
                    return@setOnClickListener
                }

                if (telefono.isEmpty()) {
                    etTelefono.error = "Ingresa tu teléfono"
                    return@setOnClickListener
                }

                if (telefono.length < 10) {
                    etTelefono.error = "Teléfono inválido (mínimo 10 dígitos)"
                    return@setOnClickListener
                }

                // ✅ Navegar al mapa
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, MapaFragment())
                    .addToBackStack(null)
                    .commit()

                Toast.makeText(
                    requireContext(),
                    "Solicitud registrada. Selecciona la ubicación en el mapa.",
                    Toast.LENGTH_LONG
                ).show()
            }

            view

        } catch (e: Exception) {
            android.util.Log.e("DOMICILIO_ERROR", "Error al inflar layout: ${e.message}")
            Toast.makeText(requireContext(), "Error al cargar el formulario: ${e.message}", Toast.LENGTH_SHORT).show()

            val errorView = TextView(requireContext()).apply {
                text = "Error: ${e.message}\n\nVerifica que fragment_domicilio.xml existe en res/layout/"
                setPadding(32, 32, 32, 32)
                textSize = 16f
            }
            errorView
        }
    }
}