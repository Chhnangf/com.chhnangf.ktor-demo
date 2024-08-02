package com.chhnangf.plugins

import com.chhnangf.func.IdGenerator
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
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.nio.charset.Charset.forName
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.io.use

fun Application.configureRouting() {
    routing {
        staticResources("/.well-known", "certs")

        get("/") {
            call.respondText("Hello World!")
        }
        get("/searchKeyword/") {
            val pathParam = call.parameters["path"] // 从URL参数中获取文件路径
            val keywordParam = call.parameters["keyword"] // 从URL参数中获取关键词
            if (pathParam == null || keywordParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing path or keyword parameter")
                return@get
            }

            val charset = forName("GBK") // 指定GBK编码
            val type = ContentType.Text.Html // 设置响应的Content-Type
            val sb = StringBuilder() // 使用StringBuilder来构建HTML内容
            sb.append("<html><body>") // 添加HTML头部

            val logPath = "h:\\trunk\\client\\logs\\JX3Client_2052-zhcn\\2024_07_31\\JX3Client_2052-zhcn_2024_07_31_14_21_17_12536.log"

            val path = Paths.get(pathParam)
            val reader = Files.newBufferedReader(path, charset)
            var found = false // 标记是否找到包含关键词的行


            for (line in reader.lines()) { // 使用for循环逐行读取
                if (line.contains(keywordParam)) {
                    found = true // 找到包含关键词的行
                    sb.append("<p>$line</p>") // 将匹配的行添加到StringBuilder中
                }
            }
            reader.close() // 关闭BufferedReader

            if (found) {
                sb.append("</body></html>") // 如果找到匹配的行，添加HTML尾部并响应
                call.respondText(sb.toString(), type)
            } else {
                call.respondText("""<html><body>No lines contain the keyword "$keywordParam".</body></html>""", type)
            }
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
            val idGenerator = IdGenerator().getNextId()
            val post = PostPostObject(idGenerator,postTitle, postDescription, filenamesList)
            filenamesList.forEachIndexed { index, string ->
                println("filenamesList -> index: $index, string: $string")
            }
            println("post onject -> $post")
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

// 确保这个函数在您的项目中的某个地方定义
fun readFileContent(filePath: String,charset: Charset): String {
    return try {
        Files.readAllLines(Paths.get(filePath), charset).joinToString("\n")
    } catch (e: FileNotFoundException) {
        // 处理文件未找到的异常，或者抛出异常让调用者处理
        throw FileNotFoundException("文件未找到: $filePath")
    }
}
