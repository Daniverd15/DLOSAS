package com.example.proyecto.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.proyecto.R

class DomicilioFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout asociado a este fragmento
        val view = inflater.inflate(R.layout.fragment_domicilio, container, false)

        // Buscamos el botón en el layout
        val solicitarBtn = view.findViewById<Button>(R.id.btnSolicitar)

        // Acción al presionar el botón
        solicitarBtn.setOnClickListener {
            // Reemplazamos el fragmento actual por el MapaFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapaFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}


