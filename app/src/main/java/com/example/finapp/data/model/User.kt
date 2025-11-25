package com.example.finapp.data.model

data class User(
    val id: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val role: UserRole = UserRole.CLIENT,
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = ""
)

enum class UserRole {
    CLIENT, ADMIN
}

