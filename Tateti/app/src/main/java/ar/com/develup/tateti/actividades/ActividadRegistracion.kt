package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.actividad_registracion.*
import java.util.*

class ActividadRegistracion : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_registracion)
        firebaseAnalytics = Firebase.analytics
        registrar.setOnClickListener { registrarse() }
    }

    fun registrarse() {
        val passwordIngresada = password.text.toString()
        val confirmarPasswordIngresada = confirmarPassword.text.toString()
        val email = email.text.toString()

        if (email.isEmpty()) {
            showAlert("El email es requerido")
        } else if (passwordIngresada.isEmpty() || confirmarPasswordIngresada.isEmpty()) {
            showAlert("La contraseña es requerida")
        } else if (passwordIngresada == confirmarPasswordIngresada) {

            registrarUsuarioEnFirebase(email, passwordIngresada)
        } else {
            showAlert("Las contraseñas no coinciden")
            Snackbar.make(rootView, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun registrarUsuarioEnFirebase(email: String, passwordIngresada: String) {
        // TODO-05-AUTHENTICATION
        // Crear el usuario con el email y passwordIngresada
        // Ademas, registrar en CompleteListener el listener registracionCompletaListener definido mas abajo
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passwordIngresada)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showHome()
                } else {
                    showAlert("Ocurrio un error registrando al usuario")
                }
            }
        firebaseAnalytics.logEvent("new_user") {
            param("Email", email)
        }
    }

    private fun showAlert(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showHome() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

//    private val registracionCompletaListener: OnCompleteListener<AuthResult?> = OnCompleteListener { task ->
//        if (task.isSuccessful) {
//            // Si se registro OK, muestro mensaje y envio mail de verificacion
//            Snackbar.make(rootView, "Registro exitoso", Snackbar.LENGTH_SHORT).show()
//            enviarEmailDeVerificacion()
//        } else if (task.exception is FirebaseAuthUserCollisionException) {
//            // Si el usuario ya existe, mostramos error
//            Snackbar.make(rootView, "El usuario ya existe", Snackbar.LENGTH_SHORT).show()
//        } else {
//            // Por cualquier otro error, mostramos un mensaje de error
//            Snackbar.make(rootView, "El registro fallo: " + task.exception, Snackbar.LENGTH_LONG).show()
//        }
//    }

    private fun enviarEmailDeVerificacion() {
        // TODO-05-AUTHENTICATION
        // Enviar mail de verificacion al usuario currentUser
    }
}