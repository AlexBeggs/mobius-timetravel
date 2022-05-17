package dev.alexbeggs.mobius.timetravel

import java.util.ServiceLoader

interface TimeTravelServer {
    fun registerController(timeTravelController: TimeTravelController<*, *, *>)
    fun deregisterController(timeTravelController: TimeTravelController<*, *, *>)
    fun start(): TimeTravelServer
    fun stop(): TimeTravelServer

    companion object {
        @JvmStatic
        val timeTravelServer: TimeTravelServer by lazy {
            ServiceLoader.load(TimeTravelServer::class.java).first()
        }
    }
}
