package com.example.lesson5

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lesson5.api.RetrofitClient
import com.example.lesson5.model.Course
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var imgThumbnail: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvCategoryName: TextView
    private lateinit var tvInstructor: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnWatchTrailer: Button
    private lateinit var btnEditCourse: Button
    private lateinit var btnDeleteCourse: Button

    private var course: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chi tiết khóa học"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        course = intent.getSerializableExtra("COURSE_DATA") as? Course

        course?.let {
            displayCourseDetails(it)
            fetchCategoryName(it.categoryId)
            setupActions(it)
        }
    }

    private fun initViews() {
        imgThumbnail = findViewById(R.id.imgDetailThumbnail)
        tvTitle = findViewById(R.id.tvDetailTitle)
        tvCategoryName = findViewById(R.id.tvDetailCategoryName)
        tvInstructor = findViewById(R.id.tvDetailInstructor)
        tvPrice = findViewById(R.id.tvDetailPrice)
        tvDuration = findViewById(R.id.tvDetailDuration)
        tvDescription = findViewById(R.id.tvDetailDescription)
        btnWatchTrailer = findViewById(R.id.btnWatchTrailer)
        btnEditCourse = findViewById(R.id.btnEditCourse)
        btnDeleteCourse = findViewById(R.id.btnDeleteCourse)
    }

    private fun displayCourseDetails(course: Course) {
        tvTitle.text = course.title
        tvInstructor.text = "Giảng viên: ${course.instructor}"
        tvPrice.text = "Giá: ${course.price} đ"
        tvDuration.text = "Thời lượng: ${course.duration} giờ"
        tvDescription.text = course.description

        Glide.with(this)
            .load(course.thumbnail)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .centerCrop()
            .into(imgThumbnail)
    }

    private fun fetchCategoryName(categoryId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategories()
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    val category = categories.find { it.id == categoryId }
                    tvCategoryName.text = "Danh mục: ${category?.name ?: "Không rõ"}"
                }
            } catch (_: Exception) {
                tvCategoryName.text = "Danh mục: Không rõ"
            }
        }
    }

    private fun setupActions(course: Course) {
        btnWatchTrailer.setOnClickListener {
            val url = course.trailerUrl
            if (!url.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Không có URL trailer", Toast.LENGTH_SHORT).show()
            }
        }

        btnEditCourse.setOnClickListener {
            val intent = Intent(this, AddEditCourseActivity::class.java).apply {
                putExtra("COURSE_DATA", course)
            }
            startActivity(intent)
        }

        btnDeleteCourse.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa khóa học \"${course.title}\" này không?")
                .setPositiveButton("Xóa") { _, _ ->
                    deleteCourse(course.id)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun deleteCourse(courseId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteCourse(courseId)
                if (response.isSuccessful) {
                    Toast.makeText(this@CourseDetailActivity, "Xóa khóa học thành công", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CourseDetailActivity, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CourseDetailActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
