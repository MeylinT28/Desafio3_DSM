
package com.aprendeapp.storage

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "aprende_session",
        Context.MODE_PRIVATE
    )

    private val favoritePrefs = context.getSharedPreferences(
        "aprende_favorites",
        Context.MODE_PRIVATE
    )

    private fun normalizedEmail(): String =
        email().trim().lowercase()

    fun saveUser(email: String, role: String, name: String? = null) {
        val normalized = email.trim().lowercase()

        prefs.edit()
            .putString("email", normalized)
            .putString("role", role)
            .putString(
                "display_name",
                name?.takeIf { it.isNotBlank() }
                    ?: nameFor(normalized)
            )
            .apply()
    }

    fun displayName(): String =
        prefs.getString("display_name", "") ?: ""

    fun nameFor(email: String): String =
        prefs.getString(
            "name_${email.trim().lowercase()}",
            ""
        ) ?: ""

    fun email(): String =
        prefs.getString("email", "") ?: ""

    fun role(): String =
        prefs.getString("role", "") ?: ""

    fun isLoggedIn(): Boolean = email().isNotBlank()

    fun logout() {
        prefs.edit()
            .remove("email")
            .remove("role")
            .remove("display_name")
            .apply()
    }

    fun saveAccount(
        email: String,
        password: String,
        role: String,
        name: String
    ) {
        val normalized = email.trim().lowercase()

        prefs.edit()
            .putString("account_$normalized", password)
            .putString("role_$normalized", role)
            .putString("name_$normalized", name)
            .apply()
    }

    fun passwordFor(email: String): String? =
        prefs.getString(
            "account_${email.trim().lowercase()}",
            null
        )

    fun roleFor(email: String): String =
        prefs.getString(
            "role_${email.trim().lowercase()}",
            "Estudiante"
        ) ?: "Estudiante"

    fun favorites(): MutableSet<String> {
        val key = "favorites_${normalizedEmail()}"

        return favoritePrefs
            .getStringSet(key, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
    }

    fun toggleFavorite(id: String): Boolean {
        if (id.isBlank() || normalizedEmail().isBlank()) {
            return false
        }

        val key = "favorites_${normalizedEmail()}"
        val current = favorites()

        val added = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }

        // Guardar una copia independiente del conjunto.
        val saved = favoritePrefs.edit()
            .putStringSet(key, HashSet(current))
            .commit()

        if (!saved) {
            return !added
        }

        return added
    }
}
