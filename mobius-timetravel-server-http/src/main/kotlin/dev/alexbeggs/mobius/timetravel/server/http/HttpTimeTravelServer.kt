package dev.alexbeggs.mobius.timetravel.server.http

import dev.alexbeggs.mobius.timetravel.TimeTravelController
import dev.alexbeggs.mobius.timetravel.TimeTravelServer
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpTimeTravelServer(private val port: Int = 8080) : TimeTravelServer {
    private var server: Server = Server(port)

    init {
        server.stopAtShutdown = true
        server.handler =
            object : AbstractHandler() {
                @Throws(IOException::class, ServletException::class)
                override fun handle(
                    target: String,
                    baseRequest: Request,
                    request: HttpServletRequest,
                    response: HttpServletResponse,
                ) {
                    response.writer.append("<HTML><BODY><TABLE>")
                    if (baseRequest.method == "GET") {
                        if (target == "/") {

                            timeTravelControllerList
                                .filter { isActiveController(it) }
                                .forEach { controller ->
                                    writeTimeTravelController(controller, response)
                                }
                            // list the current models
                            baseRequest.isHandled = true
                        }
                        if (target == "/forward") {
                            timeTravelControllerList
                                .filter { isActiveController(it) }
                                .forEach { controller ->
                                    controller.forward()
                                    writeTimeTravelController(controller, response)
                                    // list the current models
                                    baseRequest.isHandled = true
                                }
                        }
                        if (target == "/rewind") {
                            timeTravelControllerList
                                .filter { isActiveController(it) }
                                .forEach { controller ->
                                    controller.rewind()
                                    writeTimeTravelController(controller, response)
                                }
                            baseRequest.isHandled = true
                        }
                        if (target == "/select") {
                            for (controller in timeTravelControllerList) {
                                controller.select()
                                writeTimeTravelController(controller, response)
                                // list the current models
                                baseRequest.isHandled = true
                            }
                        }
                    }
                    response.writer.append("</TABLE></BODY></HTML>")
                }
            }
        start()
    }

    private fun isActiveController(controller: TimeTravelController<*, *, *>): Boolean =
        controller.getTimeline().isNotEmpty()

    private fun writeTimeTravelController(
        controller: TimeTravelController<*, *, *>,
        response: HttpServletResponse,
    ) {
        val index = controller.timelineIndex
        val entries = controller.getTimeline()
        // add controls
        response.writer.append(
            """
            <table><tr>
            <td><input type="button" onclick="location.href='/rewind';" value="Rewind" ${if (index <= 0) "disabled" else ""}></td>  
            <td><input type="button" onclick="location.href='/select';" value="Select"></td>
            <td><input type="button" onclick="location.href='/forward';" value="Forward"></td>
            <td><input type="button" onclick="location.href='/';" value="Refresh"></td>
            </tr></table>
        """
        )

        for (i in entries.indices) {
            if (index == i) {
                response.writer.append("<B>")
            }

            response.writer.append("Records[$i] ${entries[i]}<br>")
            if (index == i) {
                response.writer.append("</B>")
            }
        }
    }

    private val timeTravelControllerList: MutableList<TimeTravelController<*, *, *>> = ArrayList()

    override fun registerController(timeTravelController: TimeTravelController<*, *, *>) {
        timeTravelControllerList.add(timeTravelController)
    }

    override fun deregisterController(timeTravelController: TimeTravelController<*, *, *>) {
        timeTravelControllerList.remove(timeTravelController)
    }

    override fun start(): TimeTravelServer {
        server.start()
        return this
    }

    override fun stop(): TimeTravelServer {
        server.stop()
        return this
    }
}
