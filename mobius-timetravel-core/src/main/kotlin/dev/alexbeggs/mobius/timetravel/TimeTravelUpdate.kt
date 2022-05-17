package dev.alexbeggs.mobius.timetravel

import com.spotify.mobius.Connection
import com.spotify.mobius.Next
import com.spotify.mobius.Update

class TimeTravelUpdate<M, E, F>(
    private val update: Update<M, E, F>,
    private var connection: Connection<Record<M, E, F>>? = null,
) : Update<M, E, F> {

    fun connect(connection: Connection<Record<M, E, F>>) {
        this.connection = connection
    }

    override fun update(model: M, event: E): Next<M, F> {
        val next = update.update(model, event)
        connection?.let {
            it.accept(
                Record(
                    model,
                    event,
                    if (next.hasModel()) next.modelUnsafe() else null,
                    next.effects().toSet()
                )
            )
        }
        return next
    }

    companion object {
        @JvmStatic
        fun <M, E, F> from(update: Update<M, E, F>): TimeTravelUpdate<M, E, F> {
            return TimeTravelUpdate(update)
        }
    }

    data class Record<M, E, F>(val before: M, val event: E, val after: M?, val effects: Set<F>?)
}
