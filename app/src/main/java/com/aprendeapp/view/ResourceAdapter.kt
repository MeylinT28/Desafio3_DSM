package com.aprendeapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aprendeapp.R
import com.aprendeapp.model.Resource
import com.bumptech.glide.Glide
import android.widget.ImageView

class ResourceAdapter(
    private var items: List<Resource>, private val isTeacher: Boolean,
    private val isFavorite: (String) -> Boolean,
    private val favorite: (Resource) -> Unit, private val open: (Resource) -> Unit,
    private val edit: (Resource) -> Unit, private val delete: (Resource) -> Unit,
    private val rate: (Resource) -> Unit
) : RecyclerView.Adapter<ResourceAdapter.ResourceHolder>() {
    class ResourceHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.imgResource); val title: TextView = v.findViewById(R.id.txtTitle); val type: TextView = v.findViewById(R.id.txtType)
        val description: TextView = v.findViewById(R.id.txtDescription); val rating: TextView = v.findViewById(R.id.txtRating)
        val open: Button = v.findViewById(R.id.btnOpen); val favorite: Button = v.findViewById(R.id.btnFavorite)
        val actions: View = v.findViewById(R.id.teacherActions); val edit: Button = v.findViewById(R.id.btnEdit)
        val delete: Button = v.findViewById(R.id.btnDelete); val rate: Button = v.findViewById(R.id.btnRate)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ResourceHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_resource, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: ResourceHolder, position: Int) {
        val item = items[position]; val id = item.id ?: ""
        if (item.imagen.isNotBlank()) { h.image.visibility = View.VISIBLE; Glide.with(h.image).load(item.imagen).centerCrop().into(h.image) } else { h.image.visibility = View.GONE };
        h.title.text = item.titulo; h.type.text = item.tipo; h.description.text = item.descripcion
        h.rating.text = if (item.cantidadCalificaciones > 0) "Promedio: %.1f/5 (%d votos)".format(item.promedio, item.cantidadCalificaciones) else "Promedio: sin calificaciones"
        h.favorite.text = if (isFavorite(id)) "★ Guardado" else "☆ Favorito"
        h.actions.visibility = if (isTeacher) View.VISIBLE else View.GONE
        h.open.setOnClickListener { open(item) }; h.favorite.setOnClickListener { favorite(item) }
        h.edit.setOnClickListener { edit(item) }; h.delete.setOnClickListener { delete(item) }; h.rate.setOnClickListener { rate(item) }
    }
    fun update(newItems: List<Resource>) { items = newItems; notifyDataSetChanged() }
}
