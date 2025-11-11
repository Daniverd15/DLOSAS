package com.example.proyecto
import android.R.attr.end
import android.R.attr.start
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.auth.AuthEvent
import com.example.proyecto.auth.AuthViewModel
import com.example.proyecto.ui.theme.ApiService
import com.example.proyecto.ui.theme.AppTheme
import com.example.proyecto.ui.theme.RouteResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private lateinit var btnCalculate: Button

    private var start: String = ""
    private var end: String = ""

    var poly: Polyline? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                btnCalculate = findViewById(R.id.btnCalculateRouter)
                btnCalculate.setOnClickListener {
                    start = ""
                    end = ""
                    poly?.remove()
                    poly = null
                    Toast.makeText(this, "Seleccione punto de origen y final", Toast.LENGTH_SHORT).show()
                    if (::map.isInitialized) {
                        map.setOnMapClickListener {
                            if (start.isEmpty()) {
                                start = "${it.longitude},${it.latitude}"
                            } else if (end.isEmpty()) {
                                end = "${it.longitude},${it.latitude}"
                                createRoute()
                            }
                        }
                    }
                }

                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
                val vm: AuthViewModel = viewModel()
                val state by vm.state.collectAsState()
                val repo = remember { UserRepository() }
                val snackBar = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    vm.events.collect { e ->
                        when (e) {
                            is AuthEvent.Success -> {
                                // TODO: navegar a Admin o Home usuario
                                scope.launch {
                                    snackBar.showSnackbar(
                                        if (e.isAdmin) "Admin OK" else "Login OK"
                                    )
                                }
                            }
                            is AuthEvent.Error -> scope.launch {
                                snackBar.showSnackbar(e.message)
                            }
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackBar) }
                ) { padding ->   // <-- usa el padding que entrega Scaffold
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .padding(padding) // <-- aplicado aquí
                    ) {
                        LoginScreen(
                            email = state.email,
                            password = state.password,
                            onEmailChange = vm::updateEmail,
                            onPasswordChange = vm::updatePassword,
                            onLoginClick = { vm.signIn { uid -> repo.isAdmin(uid) } },
                            onForgotClick = {
                                vm.sendReset {
                                    scope.launch { snackBar.showSnackbar("Correo enviado") }
                                }
                            },
                            onRegisterClick = { /* TODO: ir a registro */ },
                            loading = state.loading
                        )
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }
    private fun createRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java)
                .getRoute("google_api_key", start, end)
            if (call.isSuccessful) {
                drawRoute(call.body())
            } else {
                Log.i("aris", "KO")
            }
        }
    }

    private fun drawRoute(routeResponse: RouteResponse?) {
        val polyLineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            poly = map.addPolyline(polyLineOptions)
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
