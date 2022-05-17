package dev.alexbeggs.mobius.timetravel.server.noop

import dev.alexbeggs.mobius.timetravel.TimeTravelController
import dev.alexbeggs.mobius.timetravel.TimeTravelServer

class NoopTimeTravelServer : TimeTravelServer {
    override fun registerController(timeTravelController: TimeTravelController<*, *, *>) {
        // no-op
    }

    override fun deregisterController(timeTravelController: TimeTravelController<*, *, *>) {
        // no-op
    }

    override fun start(): TimeTravelServer = this

    override fun stop(): TimeTravelServer = this
}
