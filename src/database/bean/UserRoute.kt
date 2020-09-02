package com.gofishing.com.database.bean

import com.gofishing.com.auth.SimpleJWT
import com.gofishing.com.business.user.UserService
import com.gofishing.com.exception.AuthenticateErrorException
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.userRout(userService: UserService, simpleJwt: SimpleJWT) {
    post("/login") {

        val post = call.receive<User>()
        val user = userService.getUserByPhone(post.phoneNum) ?: throw AuthenticateErrorException("unknown User")
        if (user.password != post.password) throw AuthenticateErrorException("Invalid credentials")
        call.respond(mapOf("token" to simpleJwt.sign(user.phoneNum)))
    }

    post("/register") {
        val post = call.receive<User>()
        val user = userService.getUserByPhone(post.phoneNum)
        if (user != null) throw AuthenticateErrorException("User is exist")
        userService.insertUser(post)
        call.respond(mapOf("token" to simpleJwt.sign(post.phoneNum)))
    }
}