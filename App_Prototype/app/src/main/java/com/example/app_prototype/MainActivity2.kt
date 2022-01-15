package com.example.app_prototype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView

class MainActivity2 : AppCompatActivity() {

    lateinit var start_textview: TextView
    lateinit var ziel_textview: TextView
    lateinit var preis_textview: TextView
    lateinit var fahrzeit_textview: TextView
    lateinit var preis: String
    lateinit var fahrzeit: String
    lateinit var start: String
    lateinit var ziel: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        start_textview = findViewById<TextView>(R.id.start_textview)
        ziel_textview = findViewById<TextView>(R.id.ziel_texttview)
        preis_textview = findViewById<TextView>(R.id.preis_textview2)
        fahrzeit_textview = findViewById<TextView>(R.id.fahrzeit_textview2)

        preis = MainActivity.preis
        start = MainActivity.start
        ziel = MainActivity.ziel
        fahrzeit = MainActivity.fahrzeit

        start_textview.text = "Start: $start"
        ziel_textview.text = "Ziel: $ziel"
        preis_textview.text = "Preis: $preis"
        fahrzeit_textview.text = "Fahrzeit: $fahrzeit"
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        return
    }

}