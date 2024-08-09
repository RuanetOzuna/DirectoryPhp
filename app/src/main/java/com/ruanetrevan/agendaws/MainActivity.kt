package com.ruanetrevan.agendaws

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ruanetrevan.agendaws.Objetos.Contactos
import com.ruanetrevan.agendaws.Objetos.Device
import com.ruanetrevan.agendaws.Objetos.ProcesosPHP

class MainActivity : AppCompatActivity() {

    private lateinit var txtNombre: EditText
    private lateinit var txtDireccion: EditText
    private lateinit var txtTelefono1: EditText
    private lateinit var txtTelefono2: EditText
    private lateinit var txtNotas: EditText
    private lateinit var cbkFavorite: CheckBox
    private lateinit var php: ProcesosPHP
    private var contactoGuardado: Contactos? = null
    private var idContacto: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        php = ProcesosPHP().apply { setContext(this@MainActivity) }
        inicializarComponentesUI()
        inicializarEventosDeBotones()
    }

    private fun inicializarComponentesUI() {
        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
    }

    private fun inicializarEventosDeBotones() {
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardarContacto() }
        findViewById<Button>(R.id.btnListar).setOnClickListener { listarContactos() }
        findViewById<Button>(R.id.btnLimpiar).setOnClickListener { limpiarFormulario() }
    }

    private fun guardarContacto() {
        if (!hayRedDisponible()) {
            mostrarMensaje("Por favor, conéctese a Internet para guardar el contacto")
            return
        }

        if (!formularioEstaCompleto()) {
            mostrarMensaje("Complete todos los campos obligatorios antes de continuar")
            return
        }

        val nuevoContacto = crearContactoDesdeFormulario() ?: return

        if (contactoGuardado == null) {
            php.agregarContacto(nuevoContacto)
            mostrarMensaje("Contacto guardado exitosamente")
        } else {
            php.modificarContacto(nuevoContacto, idContacto)
            mostrarMensaje("Contacto actualizado correctamente")
        }

        limpiarFormulario()
    }

    private fun listarContactos() {
        limpiarFormulario()
        val intent = Intent(this, ListaActivity::class.java)
        startActivityForResult(intent, 0)
    }

    private fun formularioEstaCompleto(): Boolean {
        var formularioValido = true

        if (txtNombre.text.isNullOrEmpty()) {
            txtNombre.error = "Introduce un nombre"
            formularioValido = false
        }
        if (txtTelefono1.text.isNullOrEmpty()) {
            txtTelefono1.error = "Introduce el teléfono principal"
            formularioValido = false
        }
        if (txtDireccion.text.isNullOrEmpty()) {
            txtDireccion.error = "Introduce la dirección"
            formularioValido = false
        }

        return formularioValido
    }

    private fun crearContactoDesdeFormulario(): Contactos? {
        val idMovil = Device.getSecureId(this) ?: return null

        return Contactos(
            nombre = txtNombre.text.toString(),
            telefono1 = txtTelefono1.text.toString(),
            telefono2 = txtTelefono2.text.toString(),
            direccion = txtDireccion.text.toString(),
            notas = txtNotas.text.toString(),
            favorite = if (cbkFavorite.isChecked) 1 else 0,
            idMovil = idMovil
        )
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun hayRedDisponible(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun limpiarFormulario() {
        contactoGuardado = null
        txtNombre.setText("")
        txtTelefono1.setText("")
        txtTelefono2.setText("")
        txtNotas.setText("")
        txtDireccion.setText("")
        cbkFavorite.isChecked = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val contacto = data.getSerializableExtra("contacto") as? Contactos
            contacto?.let {
                contactoGuardado = it
                idContacto = it._ID
                txtNombre.setText(it.nombre)
                txtTelefono1.setText(it.telefono1)
                txtTelefono2.setText(it.telefono2)
                txtDireccion.setText(it.direccion)
                txtNotas.setText(it.notas)
                cbkFavorite.isChecked = it.favorite == 1
            }
        } else {
            limpiarFormulario()
        }
    }
}
