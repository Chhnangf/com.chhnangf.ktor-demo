package com.chhnangf.plugins

import com.chhnangf.model.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/test1") {
            val text = "<h1> Hello From Ktor </h1>"
            val type = ContentType.parse("text/html")
            call.respondText(text, contentType = type)
        }
        get("/tasks") {
            call.respondText(
                contentType = ContentType.parse("text/html"),
                text = tasks.tasksAsTable()
            )

        }
        staticResources("static", "static")
        get("/serialTasks") {
            val tasks = SerialTaskRepository.allTasks()
            call.respond(tasks)
        }

        /**
         *  http://127.0.0.1:9292/byName/learn
         *  http://127.0.0.1:9292/byName/project
         *  http://127.0.0.1:9292/byName/data
         */
        get("/byName/{taskName}") {
            val name = call.parameters["taskName"]
            if (name == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val serialTask = SerialTaskRepository.taskByName(name)
            if (serialTask == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            call.respond(serialTask)
        }
        get("/byPriority/{taskPriority}") {
            val priorityAsText = call.parameters["taskPriority"]
            if (priorityAsText == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val priority = Priority.valueOf(priorityAsText)
                val serialTask = SerialTaskRepository.tasksByPriority(priority)
                if (serialTask.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                call.respond(serialTask)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        route("serialTasks") {
            // post add the SerialTask
            post {
                try {
                    val serialTask = call.receive<SerialTask>()
                    SerialTaskRepository.addTask(serialTask)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (e: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                if (SerialTaskRepository.removeTask(name)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }




}
