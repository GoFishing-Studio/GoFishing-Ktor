package com.gofishing.com.business.user

import com.gofishing.com.database.bean.User
import com.gofishing.com.database.bean.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {

    suspend fun getAllUsers(): List<User> = newSuspendedTransaction {
        Users.selectAll().map { toUsers(it) }
    }

    suspend fun getUserByPhone(phoneNumber: Long): User? = newSuspendedTransaction {
        Users.select {
            Users.phoneNum.eq(phoneNumber)
        }.map { toUsers(it) }
            .firstOrNull()
    }

    fun insertUser(user: User) {
        transaction {
            Users.insert {
                it[name] = user.name
                it[phoneNum] = user.phoneNum
                it[password] = user.password
                it[registerDate] = System.currentTimeMillis()
            }
        }
    }

    private fun toUsers(row: ResultRow): User {
        return User(
            name = row[Users.name],
            phoneNum = row[Users.phoneNum],
            password = row[Users.password],
            registerDate = row[Users.registerDate]
        )
    }
}