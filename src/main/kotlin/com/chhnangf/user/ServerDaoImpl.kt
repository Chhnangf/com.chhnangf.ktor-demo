package com.chhnangf.user

import com.chhnangf.units.CodableException
import com.chhnangf.user.ServerDaoImpl.Query
import com.chhnangf.user.ServerDaoImpl.Query.createDate
import com.chhnangf.user.ServerDaoImpl.Query.email
import com.chhnangf.user.ServerDaoImpl.Query.password
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest

interface ServerDao {
    suspend fun creat(user: AccountRegisteredStruct)
    suspend fun search(user: AccountRegisteredStruct): UserAccount?
    // 可以在这里添加其他数据库操作方法
}

class ServerDaoImpl(private val database: Database):ServerDao {

    // ... 表定义 ... UserAccount
    object Query: IntIdTable("user") {
        val email = varchar("email", length = 255)
        val password = varchar("password", length = 255)
        val createDate = timestamp("created_at")
    }

    init {
        transaction(database) {
            SchemaUtils.create(Query)
            addLogger(StdOutSqlLogger)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }

    override suspend fun creat(user: AccountRegisteredStruct): Unit = dbQuery {
        Query.select(email eq user.email)
            .map { UserAccount.from(it) }.singleOrNull()?.also {
                throw CodableException(-30991, "This account already exists.")
            } ?: Query.insert { insertRegisteredAccountToQuery(it, user)}
    }

    override suspend fun search(user: AccountRegisteredStruct): UserAccount? = dbQuery {
        // 查询数据库，获取存储的哈希密码
        Query.select(email eq user.email and(password eq  hashPassword(user.password)))
            .map { UserAccount.from(it) }.singleOrNull()
    }



}

fun UserAccount.Companion.from(row: ResultRow): UserAccount {
    return UserAccount(
        row[Query.id].value,
        row[email],
        row[password],
        row[createDate],
    )
}
private fun insertRegisteredAccountToQuery(sour: InsertStatement<Number>, dest: AccountRegisteredStruct) {
    sour[email] = dest.email
    sour[password] = hashPassword(dest.password)
}

fun hashPassword(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(password.toByteArray()).fold("") { str, it -> str + "%02x".format(it) }
}