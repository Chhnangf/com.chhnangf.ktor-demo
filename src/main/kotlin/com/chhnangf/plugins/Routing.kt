package com.chhnangf.plugins

import com.chhnangf.func.saveByteArrayAsFile
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
import java.util.UUID
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
                contentType = ContentType.parse("text/html"), text = tasks.tasksAsTable()
            )

        }
        staticResources("static", "static")
        get("/serialTasks") {
            val tasks = SerialTaskRepository.allTasks()
            call.respond(HttpStatusCode.OK, tasks)
        }
        post("/serialTasks") {
            // 从请求中获取 body，反序列化为 PhotoObject 或 List<PhotoObject>
            val tasks = call.receive<List<PhotoObject>>()
            println("call.receive<List<PhotoObject>>() -> $tasks")
            // 调用存储逻辑，保存PhotoObject
            SerialTaskRepository.addListTask(tasks)
            // 响应成功状态
            call.respond(HttpStatusCode.Created, SerialTaskRepository.allTasks())

        }

        post("/upload") {
            val multipartData = call.receive<MultiPartData>()
            println("server -> /upload $multipartData")

            // 在处理 multipart 请求的地方
            var postTitle = ""
            var postDescription  = ""
            val filenamesList = mutableListOf<String>() // 初始化一个列表来存储文件名

            // 遍历多部分数据中的每个部分
            multipartData.forEachPart { part ->
                println("multipartData.forEachPart $part")
                when (part) {
                    is PartData.FormItem -> {
                        // 处理文本表单字段
                        println("Received form field: ${part.name} with value: ${part.value}")
                        if (part.name == "title") { postTitle = part.value }
                        if (part.name == "description") { postDescription = part.value }
                    }

                    is PartData.FileItem -> {
                        println("Received file part: name: ${part.name}, originalFileName: ${part.originalFileName}")
                        println("Data type: FileItem")
                        val byteReadChannel = part.provider()
                        val bytePacket = byteReadChannel.readRemaining()
                        val byteArray = bytePacket.readBytes()
                        println("byte array: $byteArray")

                        // save File
                        val filename = "${UUID.randomUUID()}.jpg"
                        println(filename)
                        saveByteArrayAsFile(filename, byteArray)

                        // 将文件名添加到列表中
                        filenamesList.add(filename)
//                        call.respondBytes(
//                            bytes = byteArray,
//                            contentType = ContentType.Image.JPEG, // 设置内容类型为 JPEG 图像
//                            status = HttpStatusCode.OK // 设置状态码为 OK
//                        ) {
//                            println("call.respondBytes -> ${byteArray.size}")
//                        }
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
                            bytes = input.readBytes(), contentType = ContentType.Image.JPEG, // 设置内容类型为 JPEG 图像
                            status = HttpStatusCode.OK // 设置状态码为 OK
                        ) {
                            println("call.respondBytes -> ${input.readBytes().size}")
                        }

                    }
                }
            }
            // 使用收集到的文件名列表创建 Post 对象
            val post = PostPostObject(postTitle, postDescription, filenamesList)
            filenamesList.forEachIndexed { index, string ->
                println("index: $index, string: $string")
            }
            call.respond(HttpStatusCode.OK, listOf(post))
            // 现在 post 对象包含了所有上传的图片文件名
            // 构造响应并发送回客户端
            //call.respondText("Data received successfully!", status = HttpStatusCode.OK)
        }
        get("/file") {
            val file = File("93b9ac2e-8e29-4353-b521-f91ea5abe7c2.jpg")
            call.respondFile(file)
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
