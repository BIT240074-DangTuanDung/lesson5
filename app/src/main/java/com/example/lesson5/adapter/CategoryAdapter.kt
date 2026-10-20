package com.example.lesson5.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lesson5.R
import com.example.lesson5.model.Category

class CategoryAdapter(
    private var categoryList: List<Category>,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvCategoryDescription)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.tvName.text = category.name
        holder.tvDescription.text = category.description

        holder.btnEdit.setOnClickListener {
            onEditClick(category)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(category)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateData(newList: List<Category>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}
