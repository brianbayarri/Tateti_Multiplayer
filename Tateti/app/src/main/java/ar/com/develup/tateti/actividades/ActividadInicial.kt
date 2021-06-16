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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.actividad_inicial.*
import kotlinx.android.synthetic.main.actividad_inicial.email
import kotlinx.android.synthetic.main.actividad_inicial.password
import kotlinx.android.synthetic.main.actividad_inicial.rootView
import kotlinx.android.synthetic.main.actividad_registracion.*

class ActividadInicial : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var remoteConfig : FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)
        firebaseAnalytics = Firebase.analytics

        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }

        //RemoteConfig
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
            fetchTimeoutInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)


        if (usuarioEstaLogueado()) {
            firebaseAnalytics.logEvent("user_already_log_in") {
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            }
            // Si el usuario esta logueado, se redirige a la pantalla
            // de partidas
            verPartidas()
            finish()
        }
        actualizarRemoteConfig()
    }

    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        // Validar que currentUser sea != null
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun registrate() {
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun actualizarRemoteConfig() {
        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()
    }

    private fun configurarDefaultsRemoteConfig() {
        remoteConfig.setDefaultsAsync(
            mapOf("show_forget_password" to false))
    }

    private fun configurarOlvideMiContrasena() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener() { task ->
            if(task.isSuccessful) {
                val botonOlvideHabilitado = Firebase.remoteConfig.getBoolean("show_forget_password")

                if (botonOlvideHabilitado) {
                    olvideMiContrasena.visibility = View.VISIBLE
                } else {
                    olvideMiContrasena.visibility = View.GONE
                }
            }
        }


    }

    private fun olvideMiContrasena() {
        val email = email.text.toString()

        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        showAlert("Email enviado")
                    } else {
                        showAlert("Error enviando email")
                    }
                }
        }
    }

    private fun iniciarSesion() {
        val email = email.text.toString()
        val password = password.text.toString()

        when {
            email.isEmpty() -> showAlert("El email es requerido")
            password.isEmpty() -> showAlert("La contraseña es requerida")
            else -> {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authenticationListener)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            verPartidas()
                            firebaseAnalytics.logEvent("user_log_in") {
                                param("user", email)
                            }
                        } else {
                            showAlert("Ocurrio un error iniciando sesion")
                        }
                    }
                firebaseAnalytics.logEvent("new_user") {
                    param("Email", email)
                }
            }
        }
        // TODO-05-AUTHENTICATION
        // Agregar en addOnCompleteListener el campo authenticationListener definido mas abajo
    }

        private val authenticationListener: OnCompleteListener<AuthResult?> = OnCompleteListener<AuthResult?> { task ->
           if (task.isSuccessful) {
                if (usuarioVerificoEmail()) {
                    verPartidas()
                } else {
                    desloguearse()
                    Snackbar.make(rootView!!, "Verifica tu email para continuar", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                if (task.exception is FirebaseAuthInvalidUserException) {
                    Snackbar.make(rootView!!, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
                } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(rootView!!, "Credenciales inválidas", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    private fun usuarioVerificoEmail(): Boolean {
        // TODO-05-AUTHENTICATION
        // Preguntar al currentUser si verifico email
        return false
    }

    private fun desloguearse() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun showAlert(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

}