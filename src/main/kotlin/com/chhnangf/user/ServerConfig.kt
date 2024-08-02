package com.chhnangf.user

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

const val JDBC_MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver"

data class JDBCConfig(private val app: Application, private val root: String, val driver: String) {
    val host = app.environment.config.property("$root.url").getString()
    val username = app.environment.config.property("$root.user").getString()
    val password = app.environment.config.property("$root.password").getString()
    fun connect(): Database = Database.connect(host, driver, username, password)
}

data class JWTConfig(private val app: Application, private val root: String) {
    val privateKey = app.environment.config.property("$root.privateKey").getString()
    val issuer = app.environment.config.property("$root.issuer").getString()
    val audience = app.environment.config.property("$root.audience").getString()
    val realm = app.environment.config.property("$root.realm").getString()
}

data class AuthTokenResult(val token:String)

fun Application.configureUserWithJwt() {
    // database config
    val jdbcConfig = JDBCConfig(this, "database", JDBC_MYSQL_DRIVER)
    val database = jdbcConfig.connect()
    val serverDaoImpl = ServerDaoImpl(database)
    val accountManager = AccountInformationManager(serverDaoImpl)

    // jwt config
    val jwtConfig = JWTConfig(this, "jwt")
    val jwtProvider = JwkProviderBuilder(jwtConfig.issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    fun generateToken(value:String):String {
        val publicKey = jwtProvider.get("b51ade2f-eb0e-4644-94d0-7efde6afe0ce").publicKey
        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(jwtConfig.privateKey))
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
        return JWT
            .create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("id", value)
            .withExpiresAt(Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
            .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtProvider, jwtConfig.issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                credential.payload.getClaim("id").asString().let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ -> // EXPIRED: [defaultScheme, realm ->]
                call.respond(HttpStatusCode.Unauthorized, "HttpStatusCode.Unauthorized")
            }
        }
    }

    routing {
        post("/register") {
            val struct = call.receiveNullable<AccountRegisteredStruct>() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "The client required struct data was not found"
            )
            try {
                val id = accountManager.register(struct)
                call.respond(HttpStatusCode.Created)
            } catch (e: ContentTransformationException) {
                e.printStackTrace()
            }
        }
        post("login/email") {
            val struct = call.receive<AccountRegisteredStruct>()
            try {
                val id = accountManager.search(struct)
                val token = generateToken(id.toString())
                call.respond(HttpStatusCode.OK, AuthTokenResult(token))
            } catch (e: ContentTransformationException) {
                e.printStackTrace()
            }
        }
    }


}

