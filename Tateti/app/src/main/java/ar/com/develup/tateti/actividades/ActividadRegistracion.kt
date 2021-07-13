package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passwordIngresada)
            .addOnCompleteListener(this, registracionCompletaListener)
    }

    private fun showAlert(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showHome() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private val registracionCompletaListener: OnCompleteListener<AuthResult?> = OnCompleteListener { task ->
        if (task.isSuccessful) {
            showAlert("Registro exitoso")
            enviarEmailDeVerificacion()
            showHome()
        } else if (task.exception is FirebaseAuthUserCollisionException) {
            showAlert("El usuario ya existe")
        } else {
            showAlert("El registro fallo: " + task.exception)
        }
    }

    private fun enviarEmailDeVerificacion() {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
    }
}