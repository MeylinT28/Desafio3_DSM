package com.aprendeapp.controller

import android.util.Patterns
import com.aprendeapp.storage.SessionManager
import java.security.MessageDigest

class AuthController(private val session: SessionManager) {
    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
    fun validatePassword(password: String): String? {
        if (password.length < 12) return "La contraseña debe tener mínimo 12 caracteres."
        if (!password.any { it.isUpperCase() }) return "Agrega al menos una mayúscula."
        if (!password.any { it.isLowerCase() }) return "Agrega al menos una minúscula."
        if (!password.any { it.isDigit() }) return "Agrega al menos un número."
        if (!password.any { "!@#$%^&*".contains(it) }) return "Agrega un carácter especial (!@#$%^&*)."
        return null
    }
    fun register(name: String, email: String, password: String, role: String): String? {
        if (name.isBlank()) return "Escribe tu nombre para crear la cuenta."
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Escribe un correo válido."
        validatePassword(password)?.let { return it }
        if (session.passwordFor(email) != null) return "Ese correo ya está registrado en este dispositivo."
        session.saveAccount(email, hash(password), role, name.trim()); session.saveUser(email, role, name.trim()); return null
    }
    fun login(email: String, password: String, selectedRole: String): String? {
        if (session.passwordFor(email) != hash(password)) return "Correo o contraseña incorrectos. Regístrate primero."
        val savedRole = session.roleFor(email)
        if (savedRole != selectedRole) return "Selecciona el rol asociado a tu cuenta: $savedRole."
        session.saveUser(email, savedRole, session.nameFor(email)); return null
    }
}
