package com.chhnangf.func

// 服务器端的ID生成器
class IdGenerator {
    private var lastId = 0
    suspend fun getNextId(): Int {
        return synchronized(this) {
            lastId++
        }
    }
}

