package com.aprendeapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aprendeapp.R
import com.aprendeapp.controller.ResourceController
import com.aprendeapp.model.Resource
import com.aprendeapp.network.ApiClient
import com.aprendeapp.storage.SessionManager
import kotlinx.coroutines.*

class ResourcesActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var controller: ResourceController
    private lateinit var adapter: ResourceAdapter
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var allResources: List<Resource> = emptyList()
    private var showingFavorites = false
    private var filterType = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)
        val root = findViewById<View>(R.id.rootResources)
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
        session = SessionManager(this); controller = ResourceController(ApiClient.service)
        val teacher = session.role() == "Docente"
        findViewById<TextView>(R.id.txtWelcome).text = "¡Hola, ${session.displayName().ifBlank { "estudiante" }}!"
        findViewById<TextView>(R.id.txtRole).text = session.role()
        val progress = findViewById<ProgressBar>(R.id.progress)
        val empty = findViewById<TextView>(R.id.txtEmpty)
        val recycler = findViewById<RecyclerView>(R.id.recyclerResources)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ResourceAdapter(emptyList(), teacher, { session.favorites().contains(it) },
            { resource -> resource.id?.let { val added = session.toggleFavorite(it); Toast.makeText(this, if (added) "Agregado a favoritos" else "Eliminado de favoritos", Toast.LENGTH_SHORT).show(); render() } },
            { resource -> try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resource.enlace))) } catch (_: Exception) { Toast.makeText(this, "Enlace no válido", Toast.LENGTH_SHORT).show() } },
            { resource -> showResourceDialog(resource) },
            { resource -> confirmDelete(resource) },
            { resource -> showRatingDialog(resource) })
        recycler.adapter = adapter
        val add = findViewById<Button>(R.id.btnAdd)
        add.visibility = if (teacher) View.VISIBLE else View.GONE
        add.setOnClickListener { showResourceDialog(null) }
        findViewById<Button>(R.id.btnLogout).setOnClickListener { session.logout(); startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<Button>(R.id.btnAll).setOnClickListener { showingFavorites = false; render() }
        findViewById<Button>(R.id.btnFavorites).setOnClickListener { showingFavorites = true; render() }
        val spinner = findViewById<Spinner>(R.id.spnFilter)

        val opciones = listOf("Todos", "Libro", "Video", "Artículo", "Tutorial")

        val filtroAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return (super.getView(position, convertView, parent) as TextView).apply {
                    setTextColor(android.graphics.Color.parseColor("#743344"))
                    textSize = 15f
                    setPadding(18, 8, 18, 8)
                }
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                    setTextColor(android.graphics.Color.parseColor("#743344"))
                    textSize = 15f
                    setPadding(24, 18, 24, 18)
                    setBackgroundColor(android.graphics.Color.WHITE)
                }
            }
        }

        filtroAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = filtroAdapter
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { filterType = spinner.selectedItem.toString(); render() }
        }
        findViewById<EditText>(R.id.edtSearch).addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        loadResources()
    }

    private fun loadResources() {
        findViewById<ProgressBar>(R.id.progress).visibility = View.VISIBLE
        scope.launch {
            try { allResources = withContext(Dispatchers.IO) { controller.list() }; render() }
            catch (e: Exception) { Toast.makeText(this@ResourcesActivity, "No se pudo conectar con la API. Revisa la URL y tu internet.", Toast.LENGTH_LONG).show() }
            finally { findViewById<ProgressBar>(R.id.progress).visibility = View.GONE }
        }
    }

    private fun render() {
        if (!::adapter.isInitialized) return
        val query = findViewById<EditText>(R.id.edtSearch).text.toString().trim().lowercase()
        var filtered = allResources
        if (showingFavorites) filtered = filtered.filter { session.favorites().contains(it.id ?: "") }
        if (filterType != "Todos") filtered = filtered.filter { it.tipo.equals(filterType, true) }
        if (query.isNotEmpty()) filtered = filtered.filter { (it.id ?: "").contains(query) || it.titulo.lowercase().contains(query) || it.tipo.lowercase().contains(query) }
        adapter.update(filtered)
        findViewById<TextView>(R.id.txtEmpty).visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showResourceDialog(resource: Resource?) {
        if (session.role() != "Docente") return
        val fields = listOf("Título", "Descripción", "Tipo (Libro/Video/Artículo/Tutorial)", "Enlace https://", "URL de imagen (opcional)")
        val inputs = fields.map { EditText(this).apply { hint = it } }
        inputs[0].setText(resource?.titulo ?: ""); inputs[1].setText(resource?.descripcion ?: "")
        inputs[2].setText(resource?.tipo ?: "Libro"); inputs[3].setText(resource?.enlace ?: ""); inputs[4].setText(resource?.imagen ?: "")
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(45, 10, 45, 0); inputs.forEach { addView(it) } }
        AlertDialog.Builder(this).setTitle(if (resource == null) "Nuevo recurso" else "Editar recurso").setView(ScrollView(this).apply { addView(layout) })
            .setPositiveButton("Guardar", null).setNegativeButton("Cancelar", null).create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val title = inputs[0].text.toString().trim(); val description = inputs[1].text.toString().trim()
                        val type = inputs[2].text.toString().trim().replaceFirstChar { it.uppercase() }; val link = inputs[3].text.toString().trim(); val image = inputs[4].text.toString().trim()
                        if (title.isBlank() || description.isBlank() || link.isBlank()) { Toast.makeText(this, "Completa título, descripción y enlace", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                        if (!link.startsWith("https://") && !link.startsWith("http://")) { Toast.makeText(this, "El enlace debe iniciar con http:// o https://", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                        if (type !in listOf("Libro", "Video", "Artículo", "Tutorial")) { Toast.makeText(this, "Tipo permitido: Libro, Video, Artículo o Tutorial", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                        val data = Resource(id = resource?.id, titulo = title, descripcion = description, tipo = type, enlace = link, imagen = image, promedio = resource?.promedio ?: 0.0, cantidadCalificaciones = resource?.cantidadCalificaciones ?: 0)
                        dialog.dismiss(); saveResource(data)
                    }
                }
                dialog.show()
            }
    }

    private fun saveResource(resource: Resource) {
        findViewById<ProgressBar>(R.id.progress).visibility = View.VISIBLE
        scope.launch {
            try { withContext(Dispatchers.IO) { if (resource.id == null) controller.create(resource) else controller.update(resource.id, resource) }; Toast.makeText(this@ResourcesActivity, "Recurso guardado", Toast.LENGTH_SHORT).show(); loadResources() }
            catch (e: Exception) { Toast.makeText(this@ResourcesActivity, "Error al guardar. Verifica la API.", Toast.LENGTH_LONG).show() }
            finally { findViewById<ProgressBar>(R.id.progress).visibility = View.GONE }
        }
    }

    private fun confirmDelete(resource: Resource) {
        AlertDialog.Builder(this).setTitle("Eliminar recurso").setMessage("¿Seguro que deseas eliminar ${resource.titulo}?")
            .setNegativeButton("Cancelar", null).setPositiveButton("Eliminar") { _, _ ->
                scope.launch { try { withContext(Dispatchers.IO) { controller.delete(resource.id!!) }; Toast.makeText(this@ResourcesActivity, "Recurso eliminado", Toast.LENGTH_SHORT).show(); loadResources() }
                    catch (e: Exception) { Toast.makeText(this@ResourcesActivity, "No se pudo eliminar", Toast.LENGTH_SHORT).show() } }
            }.show()
    }

    private fun showRatingDialog(resource: Resource) {
        val choices = arrayOf("1 estrella", "2 estrellas", "3 estrellas", "4 estrellas", "5 estrellas")
        AlertDialog.Builder(this).setTitle("Califica ${resource.titulo}").setItems(choices) { _, which ->
            scope.launch { try { withContext(Dispatchers.IO) { controller.rate(resource, which + 1) }; Toast.makeText(this@ResourcesActivity, "¡Gracias por calificar!", Toast.LENGTH_SHORT).show(); loadResources() }
                catch (e: Exception) { Toast.makeText(this@ResourcesActivity, "No se pudo guardar la calificación en la API", Toast.LENGTH_SHORT).show() } }
        }.setNegativeButton("Cancelar", null).show()
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
