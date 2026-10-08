package ar.edu.ifts29.clubdeportivo

import android.app.Application
import ar.edu.ifts29.clubdeportivo.vm.ClubRepositorio

/** Abre la base SQLite antes de cualquier Activity. */
class ClubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ClubRepositorio.iniciar(this)
    }
}
