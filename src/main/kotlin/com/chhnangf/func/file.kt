package com.chhnangf.func

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

fun saveByteArrayAsFile(directoryPath: String, filename: String, byteArray: ByteArray) {
    // 构建完整的文件路径
    val filePath = "$directoryPath/$filename"

    // 使用Files.write保存字节数组到文件
    try {
        Files.write(Paths.get(filePath), byteArray)
        println("File saved to: $filePath")
    } catch (e: IOException) {
        e.printStackTrace() // 处理可能的IOException
    }
}