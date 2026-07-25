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

        // Enviar evento a la pantalla principal
        val intent = Intent("ACCION_NUEVA_LLAMADA")
        intent.putExtra("NUMERO_LLAMADA", numero)
        sendBroadcast(intent)

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
