package com.chhnangf.func

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

fun saveByteArrayAsFile(filePath:String,byteArray: ByteArray ) {
    Files.write(Paths.get(filePath),byteArray)
}