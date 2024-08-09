package com.ruanetrevan.agendaws.Objetos

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.nio.charset.Charset

class ProcesosPHP : Response.Listener<JSONObject>, Response.ErrorListener {

    private var request: RequestQueue? = null
    private val direccionServidor = "http://3.149.232.108/WebService/"

    fun setContext(contexto: Context) {
        request = Volley.newRequestQueue(contexto)
    }

    fun agregarContacto(contacto: Contactos) {
        val url = construirUrl(
            ruta = "ruanetRegistro.php",
            parametros = mapOf(
                "nombre" to contacto.nombre,
                "telefono1" to contacto.telefono1,
                "telefono2" to contacto.telefono2,
                "direccion" to contacto.direccion,
                "notas" to contacto.notas,
                "favorite" to contacto.favorite.toString(),
                "idMovil" to contacto.idMovil
            )
        )

        if (request == null) {
            println("Error: la cola de solicitudes no está inicializada.")
            return
        }

        val solicitudJson = SolicitudPersonalizadaJsonObject(Request.Method.GET, url, null, this, this)
        request?.add(solicitudJson)
    }

    fun modificarContacto(contacto: Contactos, id: Int) {
        val url = construirUrl(
            ruta = "ruanetActualizar.php",
            parametros = mapOf(
                "_ID" to id.toString(),
                "nombre" to contacto.nombre,
                "direccion" to contacto.direccion,
                "telefono1" to contacto.telefono1,
                "telefono2" to contacto.telefono2,
                "notas" to contacto.notas,
                "favorite" to contacto.favorite.toString()
            )
        )

        if (request == null) {
            println("Error: la cola de solicitudes no está inicializada.")
            return
        }

        val solicitudJson = SolicitudPersonalizadaJsonObject(Request.Method.GET, url, null, this, this)
        request?.add(solicitudJson)
    }

    fun eliminarContacto(id: Int) {
        val url = "${direccionServidor}ruanetEliminar.php?_ID=$id"

        if (request == null) {
            println("Error: la cola de solicitudes no está inicializada.")
            return
        }

        val solicitudJson = SolicitudPersonalizadaJsonObject(Request.Method.GET, url, null, this, this)
        request?.add(solicitudJson)
    }

    private fun construirUrl(ruta: String, parametros: Map<String, String>): String {
        return parametros.entries.joinToString("&", prefix = "$direccionServidor$ruta?") { (clave, valor) ->
            "${clave}=${valor.replace(" ", "%20")}"
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        val mensajeError = when (error.networkResponse) {
            null -> "Error de conexión: ${error.message}"
            else -> {
                val codigoEstado = error.networkResponse.statusCode
                val datosRespuesta = String(error.networkResponse.data)
                "Código de estado HTTP: $codigoEstado\nDatos de respuesta: $datosRespuesta"
            }
        }
        println("Se produjo un error durante la operación: $mensajeError")
    }

    override fun onResponse(respuesta: JSONObject) {
        val exito = respuesta.optBoolean("success", false)
        val mensaje = respuesta.optString("message", "Mensaje no proporcionado")
        val mensajeResultado = if (exito) {
            "La operación fue exitosa: $mensaje"
        } else {
            "La operación falló: $mensaje"
        }
        println(mensajeResultado)
    }

    private class SolicitudPersonalizadaJsonObject(
        metodo: Int,
        url: String,
        solicitudJson: JSONObject?,
        listener: Response.Listener<JSONObject>,
        listenerError: Response.ErrorListener
    ) : JsonObjectRequest(metodo, url, solicitudJson, listener, listenerError) {
        override fun parseNetworkResponse(respuesta: NetworkResponse): Response<JSONObject> {
            return try {
                val jsonString = String(
                    respuesta.data,
                    Charset.forName(HttpHeaderParser.parseCharset(respuesta.headers, "utf-8"))
                )
                Response.success(JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(respuesta))
            } catch (e: Exception) {
                Response.error(ParseError(e))
            }
        }
    }
}
