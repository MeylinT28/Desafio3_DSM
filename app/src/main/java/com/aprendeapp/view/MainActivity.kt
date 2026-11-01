package com.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.aprendeapp.R
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aprendeapp.controller.AuthController
import com.aprendeapp.storage.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)
        if (session.isLoggedIn()) { openResources(); return }
        setContentView(R.layout.activity_main)
        val root = findViewById<android.view.View>(R.id.rootMain)
        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(initialLeft, initialTop + bars.top, initialRight, initialBottom + bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
        val name = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtName)
        val email = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtEmail)
        val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtPassword)
        val role = findViewById<Spinner>(R.id.spnRole)
        val roleOptions = listOf("Selecciona tu rol", "Estudiante", "Docente")
        val roleAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            roleOptions
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return (super.getView(position, convertView, parent) as TextView).apply {
                    setTextColor(android.graphics.Color.parseColor("#49353A"))
                    textSize = 15f
                }
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                    setTextColor(android.graphics.Color.parseColor("#49353A"))
                    textSize = 15f
                    setPadding(24, 18, 24, 18)
                }
            }
        }
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        role.adapter = roleAdapter
        role.setPopupBackgroundResource(android.R.color.white)
        val auth = AuthController(session)
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            if (role.selectedItemPosition == 0) {
                Toast.makeText(this, "Selecciona Estudiante o Docente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val error = auth.register(name.text.toString().trim(), email.text.toString().trim(), password.text.toString(), role.selectedItem.toString())
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show() else { Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show(); openResources() }
        }
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            if (role.selectedItemPosition == 0) {
                Toast.makeText(this, "Selecciona Estudiante o Docente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val error = auth.login(email.text.toString().trim(), password.text.toString(), role.selectedItem.toString())
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show() else openResources()
        }
    }
    private fun openResources() { startActivity(Intent(this, ResourcesActivity::class.java)); finish() }
}
