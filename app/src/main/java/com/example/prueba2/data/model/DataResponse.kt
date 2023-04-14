package com.example.prueba2.data.model

data class DataResponse (

    val usuario: String,
    val contraseña: String,
    val token: String,
    val urlBase: String,
    val empresa: Empresa,
    val idTerminal: Int = 0

)