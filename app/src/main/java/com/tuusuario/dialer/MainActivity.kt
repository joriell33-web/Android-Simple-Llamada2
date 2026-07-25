package com.tuusuario.dialer

import android.app.role.RoleManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnContestar: Button
    private lateinit var btnAltavoz: Button
    private lateinit var btnColgar: Button
    private lateinit var audioManager: AudioManager
    private lateinit var powerManager: PowerManager
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var esAltavozActivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnContestar = findViewById(R.id.btnContestar)
        btnAltavoz = findViewById(R.id.btnAltavoz)
        btnColgar = findViewById(R.id.btnColgar)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        proximityWakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "MiTeléfono:SensorProximidad"
        )

        solicitarSerAppPredeterminada()

        btnContestar.setOnClickListener {
            val call = CallService.currentCall
            if (call != null) {
                call.answer(0)
                activarSensorProximidad()
                Toast.makeText(this, "Llamada contestada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No hay llamada activa", Toast.LENGTH_SHORT).show()
            }
        }

        btnAltavoz.setOnClickListener {
            esAltavozActivo = !esAltavozActivo
            audioManager.isSpeakerphoneOn = esAltavozActivo

            if (esAltavozActivo) {
                btnAltavoz.text = "ALTAVOZ (ON)"
                desactivarSensorProximidad()
            } else {
                btnAltavoz.text = "ALTAVOZ (OFF)"
                activarSensorProximidad()
            }
        }

        btnColgar.setOnClickListener {
            val call = CallService.currentCall
            if (call != null) {
                call.disconnect()
            }
            desactivarSensorProximidad()
            esAltavozActivo = false
            audioManager.isSpeakerphoneOn = false
            btnAltavoz.text = "ALTAVOZ (OFF)"
            Toast.makeText(this, "Llamada finalizada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun activarSensorProximidad() {
        if (proximityWakeLock?.isHeld == false) {
            proximityWakeLock?.acquire(10 * 60 * 1000L)
        }
    }

    private fun desactivarSensorProximidad() {
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock?.release()
        }
    }

    private fun solicitarSerAppPredeterminada() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, 101)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        desactivarSensorProximidad()
    }
}
