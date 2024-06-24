package com.chhnangf

import com.chhnangf.plugins.*
import io.ktor.server.application.*
import java.util.logging.Logger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureDatabases()
    configureTemplating()
    configureHTTP()
    configureRouting()
}
