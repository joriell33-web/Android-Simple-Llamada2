package com.tuusuario.dialer

import android.content.Intent
import android.net.Uri
import android.telecom.Call
import android.telecom.InCallService

class CallService : InCallService() {

    companion object {
        var currentCall: Call? = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call

        // Extraer número o identificador del contacto
        val handle: Uri? = call.details?.handle
        val numero = handle?.schemeSpecificPart ?: "Número Oculto / Desconocido"

        // Si la llamada está SONANDO (Entrante), abrimos la pantalla gigante inmediatamente
        if (call.state == Call.STATE_RINGING) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("NUMERO_LLAMADA", numero)
            }
            startActivity(intent)
        }

        // Notificar a la actividad si ya está abierta
        val broadcastIntent = Intent("ACCION_NUEVA_LLAMADA").apply {
            putExtra("NUMERO_LLAMADA", numero)
        }
        sendBroadcast(broadcastIntent)

        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                if (state == Call.STATE_DISCONNECTED) {
                    currentCall = null
                    val disconnectIntent = Intent("ACCION_LLAMADA_FINALIZADA")
                    sendBroadcast(disconnectIntent)
                }
            }
        })
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        currentCall = null
        val disconnectIntent = Intent("ACCION_LLAMADA_FINALIZADA")
        sendBroadcast(disconnectIntent)
    }
}
