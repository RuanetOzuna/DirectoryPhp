package com.ruanetrevan.agendaws

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ruanetrevan.agendaws.Objetos.Contactos
import com.ruanetrevan.agendaws.Objetos.Device
import com.ruanetrevan.agendaws.Objetos.ProcesosPHP
import org.json.JSONObject

class ListaActivity : Activity() {
    private lateinit var btnAgregar: Button
    private lateinit var colaPeticiones: RequestQueue
    private val listaContactos = mutableListOf<Contactos>()
    private val urlBase = "http://3.149.232.108/WebService/"
    private val contexto: Context = this
    private val php = ProcesosPHP()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        colaPeticiones = Volley.newRequestQueue(this)
        php.setContext(this)
        btnAgregar = findViewById(R.id.btnNuevo)
        btnAgregar.setOnClickListener { cerrarActividad() }
        cargarContactos()
    }

    private fun cargarContactos() {
        val url = "$urlBase/ruanetConsultarTodos.php?idMovil=${Device.getSecureId(this)}"
        val peticion = JsonObjectRequest(Request.Method.GET, url, null, { respuesta ->
            procesarRespuesta(respuesta)
        }, { error ->
            mostrarError("No se pudo obtener la información")
        })

        colaPeticiones.add(peticion)
    }

    private fun procesarRespuesta(respuesta: JSONObject) {
        val contactosArray = respuesta.optJSONArray("contactos") ?: return

        for (i in 0 until contactosArray.length()) {
            val jsonContacto = contactosArray.getJSONObject(i)
            val contacto = Contactos().apply {
                _ID = jsonContacto.optInt("_ID")
                nombre = jsonContacto.optString("nombre")
                telefono1 = jsonContacto.optString("telefono1")
                telefono2 = jsonContacto.optString("telefono2")
                direccion = jsonContacto.optString("direccion")
                notas = jsonContacto.optString("notas")
                favorite = jsonContacto.optInt("favorite")
                idMovil = jsonContacto.optString("idMovil")
            }
            listaContactos.add(contacto)
        }

        actualizarLista()
    }

    private fun actualizarLista() {
        val adaptador = ContactosAdaptador(this, R.layout.layout_contacto, listaContactos)
        findViewById<android.widget.ListView>(android.R.id.list).adapter = adaptador
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun cerrarActividad() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    inner class ContactosAdaptador(
        private val contexto: Context,
        private val recursoLayoutId: Int,
        private val contactos: List<Contactos>
    ) : ArrayAdapter<Contactos>(contexto, recursoLayoutId, contactos) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = LayoutInflater.from(contexto)
            val view = convertView ?: inflater.inflate(recursoLayoutId, parent, false)

            val txtNombre: TextView = view.findViewById(R.id.lblNombreContacto)
            val txtTelefono: TextView = view.findViewById(R.id.lblTelefonoContacto)
            val btnEditar: Button = view.findViewById(R.id.btnModificar)
            val btnEliminar: Button = view.findViewById(R.id.btnBorrar)

            val contacto = contactos[position]

            txtNombre.text = contacto.nombre
            txtTelefono.text = contacto.telefono1

            val colorTexto = if (contacto.favorite > 0)
                ContextCompat.getColor(contexto, R.color.barra)
            else
                ContextCompat.getColor(contexto, R.color.black)

            txtNombre.setTextColor(colorTexto)
            txtTelefono.setTextColor(colorTexto)

            btnEliminar.setOnClickListener { confirmarEliminarContacto(contacto, position) }
            btnEditar.setOnClickListener { editarContacto(contacto) }

            return view
        }

        private fun confirmarEliminarContacto(contacto: Contactos, posicion: Int) {
            AlertDialog.Builder(contexto).apply {
                setTitle("Eliminar contacto")
                setMessage("¿Deseas eliminar este contacto?")
                setPositiveButton("Sí") { dialog, _ ->
                    php.eliminarContacto(contacto._ID)
                    listaContactos.removeAt(posicion)
                    notifyDataSetChanged()
                    Toast.makeText(contexto, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        }

        private fun editarContacto(contacto: Contactos) {
            val intent = Intent().apply {
                putExtra("contacto", contacto)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
