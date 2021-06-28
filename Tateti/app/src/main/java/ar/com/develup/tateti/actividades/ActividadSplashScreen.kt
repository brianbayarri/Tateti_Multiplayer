package ar.com.develup.tateti.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import ar.com.develup.tateti.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlin.properties.Delegates

class ActividadSplashScreen : AppCompatActivity() {

    private lateinit var remoteConfig : FirebaseRemoteConfig
    private var botonOlvideHabilitado by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad_splash_screen)
        botonOlvideHabilitado = false

        inicializarRemoteConfig()
        actualizarRemoteConfig()
        startTimer()
    }

    fun startTimer() {
        object: CountDownTimer(5000, 1000) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                val intent = Intent(applicationContext, ActividadInicial::class.java)
                intent.putExtra("olvideHabilitado", botonOlvideHabilitado)
                startActivity(intent)
            }
        }.start()
    }

    private fun inicializarRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
            fetchTimeoutInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
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
                botonOlvideHabilitado = Firebase.remoteConfig.getBoolean("show_forget_password")
            }
        }
    }
}