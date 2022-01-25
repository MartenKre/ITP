package com.example.app_prototype

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class Dialog_Window_2(val mainActivity3: MainActivity3): DialogFragment() {

    lateinit var ok_btn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.dialog_window_2, container, false)
        ok_btn = rootView.findViewById<Button>(R.id.button_ok)

        ok_btn.setOnClickListener{
            dismiss()
        }


        return rootView
    }
}