
package com.aprendeapp.controller

import android.util.Patterns
import com.aprendeapp.storage.SessionManager
import java.security.MessageDigest

class AuthController(private val session: SessionManager) {

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun validatePassword(password: String): String? {
        if (password.length < 12)
            return "La contraseña debe tener mínimo 12 caracteres."

        if (!password.any { it.isUpperCase() })
            return "Agrega al menos una mayúscula."

        if (!password.any { it.isLowerCase() })
            return "Agrega al menos una minúscula."

        if (!password.any { it.isDigit() })
            return "Agrega al menos un número."

        if (!password.any { "!@#$%^&*".contains(it) })
            return "Agrega un carácter especial (!@#$%^&*)."

        return null
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): String? {

        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.isBlank())
            return "Escribe tu nombre para crear la cuenta."

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches())
            return "Escribe un correo válido."

        if (role !in listOf("Estudiante", "Docente"))
            return "Selecciona un rol válido."

        validatePassword(password)?.let { return it }

        if (session.passwordFor(cleanEmail) != null)
            return "Ese correo ya está registrado en este dispositivo."

        session.saveAccount(cleanEmail, hash(password), role, cleanName)
        session.saveUser(cleanEmail, role, cleanName)

        return null
    }

    fun login(
        email: String,
        password: String,
        selectedRole: String
    ): String? {

        val cleanEmail = email.trim().lowercase()

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches())
            return "Escribe un correo válido."

        if (selectedRole !in listOf("Estudiante", "Docente"))
            return "Selecciona un rol válido."

        if (session.passwordFor(cleanEmail) != hash(password))
            return "Correo o contraseña incorrectos. Regístrate primero."

        val savedRole = session.roleFor(cleanEmail)

        if (savedRole != selectedRole)
            return "Selecciona el rol asociado a tu cuenta: $savedRole."

        session.saveUser(cleanEmail, savedRole, session.nameFor(cleanEmail))

        return null
    }
}
