package dev.alexbeggs.counter

import android.app.Application
import dev.alexbeggs.mobius.timetravel.TimeTravelServer

class MviCounterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimeTravelServer.timeTravelServer.start()
    }
}