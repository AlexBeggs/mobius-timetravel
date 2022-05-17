package dev.alexbeggs.mobius.timetravel

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.MobiusLoop.Controller
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
import java.util.Collections
import java.util.LinkedList

class TimeTravelController<M, E, F>(
    private val controller: Controller<M, E>,
    private val update: Update<M, E, F>,
    private val timeTravelServer: TimeTravelServer = TimeTravelServer.timeTravelServer,
) : Controller<M, E> {

    private val listeners = mutableListOf<TimeTravelControllerListener>()
    private val records =
        Collections.synchronizedList(LinkedList<TimeTravelUpdate.Record<M, E, F>>())

    private var timeline: MutableList<Pair<RecordType, Any>> = mutableListOf()
    val consumer: Consumer<TimeTravelUpdate.Record<M, E, F>> =
        Consumer { value: TimeTravelUpdate.Record<M, E, F> ->
            value.event?.let {
                val event = Pair(RecordType.EVENT, it)
                timeline.add(event)
            }
            value.after?.let {
                val model = Pair(RecordType.MODEL, it)
                timeline.add(model)
            }
                ?: timeline.add(Pair(RecordType.MODEL_NO_UPDATE, value.before as Any))
            value.effects?.let {
                it.forEach { f ->
                    f?.let {
                        val effect = Pair(RecordType.EFFECT, f)
                        timeline.add(effect)
                    }
                }
            }
            records.add(value)
            listeners.forEach { l -> l.changed(this.timeline) }
        }

    var timelineIndex: Int = 0
        private set

    fun addListener(listener: TimeTravelControllerListener) = listeners.add(listener)

    fun removeListener(listener: TimeTravelControllerListener) = listeners.remove(listener)

    fun select() {
        this.controller.stop()
        val prevModelPair = prevModel(timelineIndex, timeline)
        if (prevModelPair != null) {
            val prevEventPair = prevEvent(timelineIndex, timeline)
            if (prevEventPair != null) {
                try {
                    this.timeline = timeline.subList(0, prevEventPair.first)
                    this.update.update(prevModelPair.second, prevEventPair.second)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        } else {
            // update the timelineIndex
            this.timeline = timeline.subList(0, timelineIndex + 1)
        }
        timelineIndex = prevModel(timeline.size, timeline)?.first ?: 0
        prevModel(timeline.size, timeline)?.let { this.controller.replaceModel(it.second) }
        this.controller.start()
        listeners.forEach { it.changed(this.timeline) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun prevModel(startIndex: Int, timeline: List<Pair<RecordType, Any>>): Pair<Int, M>? {
        for (i in startIndex - 1 downTo 0) {
            if (timeline[i].first == RecordType.MODEL) {
                return Pair(i, timeline[i].second as M)
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun prevEvent(startIndex: Int, timeline: List<Pair<RecordType, Any>>): Pair<Int, E>? {
        for (i in startIndex - 1 downTo 0) {
            if (timeline[i].first == RecordType.EVENT) {
                return Pair(i, timeline[i].second as E)
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun nextEvent(startIndex: Int, timeline: List<Pair<RecordType, Any>>): Pair<Int, E>? {
        for (i in startIndex until timeline.size) {
            if (timeline[i].first == RecordType.EVENT) {
                return Pair(i, timeline[i].second as E)
            }
        }
        return null
    }

    fun forward(): Int? {
        if (timelineIndex >= 0) {
            for (i in timelineIndex + 1 until timeline.size) {
                if (timeline[i].first == RecordType.MODEL) {
                    timelineIndex = i
                    return i
                }
            }
        }
        return null
    }

    fun rewind(): Int? {
        if (timelineIndex < timeline.size) {
            for (i in timelineIndex - 1 downTo 0) {
                if (timeline[i].first == RecordType.MODEL) {
                    timelineIndex = i
                    return i
                }
            }
        }
        return null
    }

    override fun isRunning(): Boolean {
        return controller.isRunning
    }

    override fun connect(view: Connectable<M, E>) {
        model?.let { timeline.add(Pair(RecordType.MODEL, controller.model as Any)) }
        controller.connect(view)
        timeTravelServer.registerController(this)
    }

    override fun disconnect() {
        timeTravelServer.deregisterController(this)
        records.clear()
        controller.disconnect()
    }

    override fun start() {
        controller.start()
    }

    override fun stop() {
        controller.stop()
    }

    override fun replaceModel(model: M) {
        controller.replaceModel(model)
        // clear all the records, as this doesn't make any sense anymore in the timeline...?
    }

    override fun getModel(): M {
        return controller.model
    }

    fun getTimeline(): List<*> {
        return Collections.unmodifiableList(timeline)
    }

    enum class RecordType {
        MODEL,
        MODEL_NO_UPDATE,
        EVENT,
        EFFECT,
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <M, E, F> connect(
            timeTravelController: TimeTravelController<M, E, F>,
            timeTravelUpdate: TimeTravelUpdate<M, E, F>,
        ) {
            val consumer: Consumer<TimeTravelUpdate.Record<M, E, F>> = timeTravelController.consumer
            val connectable = Connectable { output: Consumer<TimeTravelUpdate.Record<M, E, F>> ->
                val connection: Connection<*> =
                    object : Connection<TimeTravelUpdate.Record<M, E, F>> {
                        override fun accept(value: TimeTravelUpdate.Record<M, E, F>) {
                            output.accept(value)
                        }

                        override fun dispose() {
                            // might need to remove the output connection
                        }
                    }
                connection as Connection<TimeTravelUpdate.Record<M, E, F>>
            }
            timeTravelUpdate.connect(connectable.connect(consumer))
        }
    }

    interface TimeTravelControllerListener {

        fun changed(timeline: List<Pair<RecordType, Any>>)
    }
}
