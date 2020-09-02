package com.gofishing.com.database.bean

import org.jetbrains.exposed.sql.Table

data class User(val name: String, val phoneNum: Long, val password: String, val registerDate: Long?)

object Users : Table() {
    val name = varchar("name", 255)
    val password = varchar("password", 255)
    val phoneNum = long("phoneNum")
    var registerDate = long("registerDate")
    override val primaryKey = PrimaryKey(phoneNum)
}