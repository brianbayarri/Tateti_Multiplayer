package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.actividad_inicial.*
import kotlinx.android.synthetic.main.actividad_inicial.email
import kotlinx.android.synthetic.main.actividad_inicial.password
import kotlinx.android.synthetic.main.actividad_inicial.rootView
import java.lang.Exception
import java.util.*

class ActividadInicial : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var remoteConfig : FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)
        val bundle = intent.extras
        val botonOlvideHabilitado = bundle?.getBoolean("olvideHabilitado")
        firebaseAnalytics = Firebase.analytics

        inicializarListener()
        chekEstadoDeUsuario()
        configurarOlvideMiContrasena(botonOlvideHabilitado!!)
    }

    private fun inicializarListener() {
        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }
    }

    private fun chekEstadoDeUsuario() {
        if (usuarioEstaLogueado()) {
            verPartidas()
            finish()
        }
    }

    private fun iniciarSesion() {
        val email = email.text.toString()
        val password = password.text.toString()

        when {
            email.isEmpty() -> {
                FirebaseCrashlytics.getInstance().recordException(Exception("El usuario no ingreso su email"))
                showMessage("El email es requerido")
            }
            password.isEmpty() -> {
                FirebaseCrashlytics.getInstance().recordException(Exception("El usuario no ingreso su contraseña"))
                showMessage("La contraseña es requerida")
            }
            else -> {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authenticationListener)
            }
        }
    }

    private val authenticationListener: OnCompleteListener<AuthResult?> = OnCompleteListener<AuthResult?> { task ->
        if (task.isSuccessful) {
            if (usuarioVerificoEmail()) {
                Firebase.analytics.logEvent("iniciar_sesion") {
                    param("accion", "Usuario inicio sesion")
                }
                verPartidas()
            } else {
                desloguearse()
                showMessage("Verifica tu email para continuar")
            }
        } else {
            if (task.exception is FirebaseAuthInvalidUserException) {
                FirebaseCrashlytics.getInstance().recordException(Exception("Usuario inexistente"))
                showMessage("El usuario no existe")
            } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                FirebaseCrashlytics.getInstance().recordException(Exception("Usuario ingreso credenciales invalidas"))
                showMessage("Credenciales invalidas")
            }
        }
    }

    private fun usuarioVerificoEmail(): Boolean {
        return FirebaseAuth.getInstance().currentUser!!.isEmailVerified
    }

    private fun usuarioEstaLogueado(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun registrate() {
        Firebase.analytics.logEvent("click_registro") {
            param("accion", "Usuario hizo click para registrarse")
        }
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun olvideMiContrasena() {
        val email = email.text.toString()

        if (email.isEmpty()) {
            showMessage("Completa el email")
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        showMessage("Email enviado")
                    } else {
                        FirebaseCrashlytics.getInstance().recordException(Exception("Error enviando email para restaurar contraseña"))
                        showMessage("Error enviando email")
                    }
                }
        }

        Firebase.analytics.logEvent("olvide_contrasena") {
            param("accion", "Usuario hizo click en olvide mi contraseña")
        }
    }

    private fun configurarOlvideMiContrasena(botonOlvideHabilitado: Boolean) {
        if (botonOlvideHabilitado) {
            olvideMiContrasena.visibility = View.VISIBLE
        } else {
            olvideMiContrasena.visibility = View.GONE
        }
    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun desloguearse() {
        Firebase.analytics.logEvent("cerrar_sesion") {
            param("accion", "Usuario cerro sesion")
        }
        FirebaseAuth.getInstance().signOut()
    }

    private fun showMessage(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

}