package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ar.com.develup.tateti.R
import ar.com.develup.tateti.adaptadores.AdaptadorPartidas
import ar.com.develup.tateti.modelo.Constantes
import ar.com.develup.tateti.modelo.Partida
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.actividad_partidas.*
import java.util.*

class ActividadPartidas : AppCompatActivity() {

    companion object {
        private const val TAG = "ActividadPartidas"
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var adaptadorPartidas: AdaptadorPartidas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_partidas)
        firebaseAnalytics = Firebase.analytics
        adaptadorPartidas = AdaptadorPartidas(this)
        partidas.layoutManager = LinearLayoutManager(this)
        partidas.adapter = adaptadorPartidas
        nuevaPartida.setOnClickListener { nuevaPartida() }
        cerrarSesionButton.setOnClickListener { cerrarSesion() }
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        showLoginScreen()
    }

    private fun showLoginScreen() {
        val intent = Intent(applicationContext, ActividadInicial::class.java)
        intent.putExtra("olvideHabilitado", false)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        FirebaseDatabase.getInstance()
            .reference.child(Constantes.TABLA_PARTIDAS)
            .addChildEventListener(listenerTablaPartidas)
    }

    fun nuevaPartida() {
        firebaseAnalytics.logEvent("new_game_match") {
            param("Datime", Calendar.getInstance().time.toString())
        }
        val intent = Intent(this, ActividadPartida::class.java)
        startActivity(intent)
    }

    private val listenerTablaPartidas: ChildEventListener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            Log.i(TAG, "onChildAdded: $dataSnapshot")
            val partida = dataSnapshot.getValue(Partida::class.java)!!
            partida.id = dataSnapshot.key
            adaptadorPartidas.agregarPartida(partida)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            Log.i(TAG, "onChildChanged: $s")
            val partida = dataSnapshot.getValue(Partida::class.java)!!
            partida.id = dataSnapshot.key
            adaptadorPartidas.partidaCambio(partida)
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            Log.i(TAG, "onChildRemoved: ")
            val partida = dataSnapshot.getValue(Partida::class.java)!!
            partida.id = dataSnapshot.key
            adaptadorPartidas.remover(partida)
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
            Log.i(TAG, "onChildMoved: $s")
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.i(TAG, "onCancelled: ")
        }
    }

}