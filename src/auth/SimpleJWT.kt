package com.gofishing.com.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(phoneNumber: Long): String = JWT.create().withClaim("phoneNum", phoneNumber).sign(algorithm)
}