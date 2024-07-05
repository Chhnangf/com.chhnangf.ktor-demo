package com.chhnangf.plugins

import com.chhnangf.model.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import kotlinx.css.input
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.use

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
            call.respond(HttpStatusCode.OK,tasks)
        }
        post("/serialTasks"){
            // 从请求中获取 body，反序列化为 PhotoObject 或 List<PhotoObject>
            val tasks = call.receive<List<PhotoObject>>()
            println("call.receive<List<PhotoObject>>() -> $tasks")
            // 调用存储逻辑，保存PhotoObject
            SerialTaskRepository.addListTask(tasks)
            // 响应成功状态
            call.respond(HttpStatusCode.Created,SerialTaskRepository.allTasks())

        }

        post("/upload") {
            // 接收多部分表单数据
            val multipartData = call.receive<MultiPartData>()
            println("server -> /upload $multipartData")

            // 遍历多部分数据中的每个部分
            multipartData.forEachPart { part ->
                println("multipartData.forEachPart $part")
                when (part) {
                    is PartData.FormItem -> {
                        // 处理文本表单字段
                        println("Received form field: ${part.name} with value: ${part.value}")
                    }
                    is PartData.FileItem -> {
                        println("Received file part: ${part.name}")
                        println("Data type: FileItem")
                        println("Filename: ${part.name}")
                        // 获取原始的 ByteReadChannel 并读取数据
//                        val originalStream = part.provider()
//                        // 将上传的文件保存到服务器上的指定路径
//                        val fileToSave = File("/path/to/save/${part.name}")
//                        println("originalStream: $originalStream, fileToSave: $fileToSave")

                        val byteReadChannel = part.provider()
                        val bytePacket = byteReadChannel.readRemaining()
                        val byteArray = bytePacket.readBytes()
                        println("byte array: $byteArray")

                        // save File
                        part.originalFileName?.let { Paths.get(it) }?.let { Files.write(it, byteArray) }
                        call.respondBytes(
                            bytes = byteArray,
                            contentType = ContentType.Image.JPEG, // 设置内容类型为 JPEG 图像
                            status = HttpStatusCode.OK // 设置状态码为 OK
                        ) {
                            println("call.respondBytes -> ${byteArray.size}")
                        }
                    }
                    is PartData.BinaryChannelItem -> {
                        // 处理二进制通道数据
                        println("Received binary channel part")
                        println("Data type: BinaryChannelItem")
                        // TODO: 处理二进制通道数据

                    }
                    is PartData.BinaryItem -> {
                        // 处理二进制数据
                        println("Received binary item")
                        println("Data type: BinaryItem")
                        // TODO: 处理二进制数据
                        // 拿到 ByteReadChannel 对象
                        val input = part.provider()
                        val inputLength = part.provider().remaining

                        call.respondBytes(
                            bytes = input.readBytes(),
                            contentType = ContentType.Image.JPEG, // 设置内容类型为 JPEG 图像
                            status = HttpStatusCode.OK // 设置状态码为 OK
                        ) {
                            println("call.respondBytes -> ${input.readBytes().size}")
                        }

                    }
                }
            }

            // 构造响应并发送回客户端
            call.respondText("Data received successfully!", status = HttpStatusCode.OK)
        }
        /**
         *  http://127.0.0.1:9292/byName/learn
         *  http://127.0.0.1:9292/byName/project
         *  http://127.0.0.1:9292/byName/data
         */
//        get("/byName/{taskName}") {
//            val name = call.parameters["taskName"]
//            if (name == null) {
//                call.respond(HttpStatusCode.BadRequest)
//                return@get
//            }
//
//            val serialTask = SerialTaskRepository.taskByName(name)
//            if (serialTask == null) {
//                call.respond(HttpStatusCode.BadRequest)
//                return@get
//            }
//            call.respond(serialTask)
//        }
//        get("/byPriority/{taskPriority}") {
//            val priorityAsText = call.parameters["taskPriority"]
//            if (priorityAsText == null) {
//                call.respond(HttpStatusCode.BadRequest)
//                return@get
//            }
//            try {
//                val priority = Priority.valueOf(priorityAsText)
//                val serialTask = SerialTaskRepository.tasksByPriority(priority)
//                if (serialTask.isEmpty()) {
//                    call.respond(HttpStatusCode.BadRequest)
//                    return@get
//                }
//                call.respond(serialTask)
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.BadRequest)
//            }
//        }

        route("serialTasks") {
            // post add the SerialTask
//            post {
//                try {
//                    val serialTask = call.receive<SerialTask>()
//                    SerialTaskRepository.addTask(serialTask)
//                    call.respond(HttpStatusCode.NoContent)
//                } catch (e: IllegalArgumentException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                } catch (e: JsonConvertException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                }
//            }

//            delete("/{taskName}") {
//                val name = call.parameters["taskName"]
//                if (name == null) {
//                    call.respond(HttpStatusCode.BadRequest)
//                    return@delete
//                }
//                if (SerialTaskRepository.removeTask(name)) {
//                    call.respond(HttpStatusCode.NoContent)
//                } else {
//                    call.respond(HttpStatusCode.NoContent)
//                }
//            }
        }
    }




}
