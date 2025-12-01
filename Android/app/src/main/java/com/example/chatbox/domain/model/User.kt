package com.example.chatbox.domain.model

/**
 * 登录用户的领域模型。
 * 现在非常简单：只保留 id 和 name，
 * 后面你要加头像、邮箱等再扩展。
 */
data class User(
    val id: Long,
    val name: String
)
