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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Modelo de datos para la solicitud de domicilio
data class SolicitudDomicilio(
    val solicitudId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val direccion: String = "",
    val telefonoContacto: String = "",
    val notas: String = "",
    val tipoServicio: String = "Domicilio",
    val estado: String = "PENDIENTE",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaSolicitud: Any = com.google.firebase.firestore.FieldValue.serverTimestamp()
)

class DomicilioFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val fragmentScope = CoroutineScope(Dispatchers.Main)

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
            val currentUser = auth.currentUser
            tvUsuario.text = "¡Hola, ${currentUser?.displayName ?: "Usuario"}!"

            // Pre-llenar teléfono desde Firestore
            cargarDatosUsuario(etTelefono)

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

                // Deshabilitar botón mientras se guarda
                btnSolicitar.isEnabled = false
                btnSolicitar.text = "Guardando..."

                // Guardar solicitud en Firestore
                fragmentScope.launch {
                    val resultado = guardarSolicitud(direccion, telefono, notas)

                    withContext(Dispatchers.Main) {
                        btnSolicitar.isEnabled = true
                        btnSolicitar.text = "Solicitar Servicio"

                        if (resultado.isSuccess) {
                            Toast.makeText(
                                requireContext(),
                                "✅ Solicitud registrada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Navegar al mapa (opcional)
                            parentFragmentManager.beginTransaction()
                                .replace(android.R.id.content, MapaFragment())
                                .addToBackStack(null)
                                .commit()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "❌ Error: ${resultado.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
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

    /**
     * Carga los datos del usuario desde Firestore para pre-llenar el teléfono
     */
    private fun cargarDatosUsuario(etTelefono: TextInputEditText) {
        val currentUser = auth.currentUser ?: return

        fragmentScope.launch {
            try {
                val userDoc = db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val phone = userDoc.getString("phone")
                withContext(Dispatchers.Main) {
                    if (!phone.isNullOrEmpty()) {
                        etTelefono.setText(phone)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DOMICILIO_ERROR", "Error al cargar datos: ${e.message}")
            }
        }
    }

    /**
     * Guarda la solicitud de servicio a domicilio en Firestore
     */
    private suspend fun guardarSolicitud(
        direccion: String,
        telefono: String,
        notas: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                    ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

                val uid = currentUser.uid

                // Obtener datos del usuario desde Firestore
                val userDoc = db.collection("users").document(uid).get().await()
                val userName = userDoc.getString("username") ?: currentUser.displayName ?: "Usuario"
                val userEmail = currentUser.email ?: ""
                val userPhone = userDoc.getString("phone") ?: telefono

                // Generar ID único para la solicitud
                val solicitudId = db.collection("solicitudes_domicilio").document().id

                // Crear objeto de solicitud
                val solicitud = SolicitudDomicilio(
                    solicitudId = solicitudId,
                    userId = uid,
                    userEmail = userEmail,
                    userName = userName,
                    userPhone = userPhone,
                    direccion = direccion,
                    telefonoContacto = telefono,
                    notas = notas,
                    tipoServicio = "Domicilio",
                    estado = "PENDIENTE",
                    latitud = null,  // Se puede actualizar después con el mapa
                    longitud = null
                )

                // Guardar en Firestore
                db.collection("solicitudes_domicilio")
                    .document(solicitudId)
                    .set(solicitud)
                    .await()

                android.util.Log.d("DOMICILIO_SUCCESS", "Solicitud guardada: $solicitudId")
                Result.success(solicitudId)

            } catch (e: Exception) {
                android.util.Log.e("DOMICILIO_ERROR", "Error al guardar: ${e.message}")
                Result.failure(e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancelar todas las corrutinas cuando se destruya la vista
        // (En producción, usa viewModelScope o lifecycleScope)
    }
}