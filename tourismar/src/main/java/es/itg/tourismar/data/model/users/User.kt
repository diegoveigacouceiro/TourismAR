package es.itg.tourismar.data.model.users

data class User(
    val userName: String,
    val userAge: String,
    val userOccupation: String
) {
    constructor(): this("", "", "")
}