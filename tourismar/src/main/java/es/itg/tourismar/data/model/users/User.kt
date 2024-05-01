package es.itg.tourismar.data.model.users

data class User(
    val userName: String,
    val userId: String,
    val email: String,
    val userLevel: UserLevel
) {

    fun toMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "userName" to this.userName,
            "userId" to this.userId,
            "email" to this.email,
            "userLevel" to this.userLevel
        )
    }
}
