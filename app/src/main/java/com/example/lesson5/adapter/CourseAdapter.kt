package com.example.lesson5.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lesson5.R
import com.example.lesson5.model.Course

class CourseAdapter(
    private var courseList: List<Course>,
    private var categoryMap: Map<String, String>,
    private val onItemClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvInstructor: TextView = itemView.findViewById(R.id.tvInstructor)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.tvTitle.text = course.title
        holder.tvInstructor.text = "GV: ${course.instructor}"
        holder.tvPrice.text = "${course.price} đ"

        val categoryName = categoryMap[course.categoryId] ?: "Khác"
        holder.tvCategory.text = categoryName

        Glide.with(holder.itemView.context)
            .load(course.thumbnail)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .centerCrop()
            .into(holder.imgThumbnail)

        holder.itemView.setOnClickListener {
            onItemClick(course)
        }
    }

    override fun getItemCount(): Int = courseList.size

    fun updateData(newCourses: List<Course>, newCategoryMap: Map<String, String>) {
        courseList = newCourses
        categoryMap = newCategoryMap
        notifyDataSetChanged()
    }
}
