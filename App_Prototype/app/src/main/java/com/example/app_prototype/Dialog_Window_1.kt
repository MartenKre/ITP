package com.example.app_prototype

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Dialog_Window_1(val mainActivity: MainActivity): DialogFragment() {

    lateinit var yes_btn: Button
    lateinit var no_btn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.dialog_window_1, container, false)
        yes_btn = rootView.findViewById<Button>(R.id.yes_button)
        no_btn = rootView.findViewById<Button>(R.id.no_button)

        yes_btn.setOnClickListener{
            Log.d("AL", "test2")
            mainActivity.open_Activity2()
            dismiss()
        }

        no_btn.setOnClickListener{
            dismiss()
        }


        return rootView
    }
}