package com.example.lightmonitor

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private lateinit var txtLux: TextView
    private lateinit var txtKategori: TextView
    private lateinit var txtThreshold: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtNamaSensor: TextView
    private lateinit var txtTipeSensor: TextView

    private data class Kategori(val nama: String, val rentang: String, val warna: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtLux = findViewById(R.id.txtLux)
        txtKategori = findViewById(R.id.txtKategori)
        txtThreshold = findViewById(R.id.txtThreshold)
        txtStatus = findViewById(R.id.txtStatus)
        txtNamaSensor = findViewById(R.id.txtNamaSensor)
        txtTipeSensor = findViewById(R.id.txtTipeSensor)

        sensorManager = getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val sensor = lightSensor
        if (sensor == null) {
            txtNamaSensor.text = "Nama sensor: -"
            txtTipeSensor.text = "Tipe sensor: TYPE_LIGHT (tidak tersedia)"
            txtStatus.text = "Status deteksi: Sensor tidak tersedia"
            txtKategori.text = "-"
            Toast.makeText(this,
                "Light sensor tidak tersedia",
                Toast.LENGTH_LONG).show()
        } else {
            txtNamaSensor.text = "Nama sensor: ${sensor.name}"
            txtTipeSensor.text = "Tipe sensor: TYPE_LIGHT (${sensor.type})"
            txtStatus.text = "Status deteksi: Menunggu data sensor..."
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensor ->
            sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            val kategori = kategoriCahaya(lux)

            txtLux.text = "%.1f lux".format(lux)
            txtKategori.text = kategori.nama
            (txtKategori.background.mutate() as GradientDrawable).setColor(kategori.warna)
            txtThreshold.text = "Threshold: ${kategori.rentang}"
            txtStatus.text = "Status deteksi: Aktif, data diterima"
        }
    }

    // Ambang batas ini contoh awal, sesuaikan setelah pengujian di perangkatmu
    private fun kategoriCahaya(lux: Float): Kategori {
        return when {
            lux < 10f   -> Kategori("Gelap", "< 10 lux", Color.parseColor("#455A64"))
            lux < 200f  -> Kategori("Redup", "10 – 199 lux", Color.parseColor("#1E88E5"))
            lux < 1000f -> Kategori("Terang", "200 – 999 lux", Color.parseColor("#43A047"))
            else        -> Kategori("Sangat terang", "≥ 1000 lux", Color.parseColor("#F57C00"))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak ada tindakan khusus.
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        if (lightSensor != null) {
            txtStatus.text = "Status deteksi: Dijeda"
        }
    }
}