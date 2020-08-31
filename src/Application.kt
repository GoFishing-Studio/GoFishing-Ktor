package com.gofishing.com

import com.gofishing.com.auth.SimpleJWT
import com.gofishing.com.auth.User
import com.gofishing.com.bean.PostSnippet
import com.gofishing.com.bean.Snippet
import com.gofishing.com.exception.AuthenticateErrorException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.http.*
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val simpleJwt = SimpleJWT("my-super-secret-for-jwt")
    val snippets by lazy {
        mutableListOf<Snippet>()
    }
    val users by lazy {
        Collections.synchronizedMap(
            listOf(User("test", "test"))
                .associateBy { it.name }
                .toMutableMap()
        )
    }

    install(StatusPages) {
        exception<AuthenticateErrorException> { exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
    }
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost()
    }

    install(ContentNegotiation) {
        gson {
            this.setPrettyPrinting()
        }
    }
    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    routing {
        route("/snippets") {
            authenticate {
                get {
                    call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
                }
            }
            authenticate {
                post {
                    val post = call.receive<PostSnippet>()
                    val principal =
                        call.principal<UserIdPrincipal>() ?: throw AuthenticateErrorException("No principal")
                    snippets += Snippet(principal.name, post.snippet.text)
                    call.respond(mapOf("OK" to true))
                }
            }

        }
        post("/login") {
            val post = call.receive<User>()
            val user = users[post.name] ?: throw AuthenticateErrorException("unknown User")
            if (user.password != post.password) throw AuthenticateErrorException("Invalid credentials")
            call.respond(mapOf("token" to simpleJwt.sign(user.name)))
        }

        post("/register") {
            val post = call.receive<User>()
            if (users.containsKey(post.name)) throw AuthenticateErrorException("User is exist")
            users[post.name] = post
            call.respond(mapOf("token" to simpleJwt.sign(post.name)))
        }

    }
}


