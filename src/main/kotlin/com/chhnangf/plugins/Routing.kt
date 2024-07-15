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
                        val directoryPath = "./uploads" // 指定目录路径
                        val filename = "${UUID.randomUUID()}.jpg" // 生成唯一的文件名
                        saveByteArrayAsFile(directoryPath, filename, byteArray)

                        // 将文件名添加到列表中
                        filenamesList.add(filename)
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
                println("filenamesList -> index: $index, string: $string")
            }
            call.respond(HttpStatusCode.OK, listOf(post))
        }
        get("/file") {
            val file = File("93b9ac2e-8e29-4353-b521-f91ea5abe7c2.jpg")
            call.respondFile(file)
        }

        get("/uploads/{filename}") {
            // 从路径参数中获取文件名
            val filename = call.parameters["filename"]!!

            // 指定读取文件的存储目录
            val uploadDirection = File("./uploads")

            // 构造文件的完整路径
            val file = File(uploadDirection,filename)

            // 检查文件是否存在
            if (file.exists() && file.isFile) {
                // 如果文件存在，读取并返回文件内容
                call.respondFile(file)
            } else {
                // 如果文件不存在，返回404状态码
                call.respond(HttpStatusCode.NotFound)
            }

        }

        route("serialTasks") {
        }
    }


}
