package com.example.lesson5

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lesson5.api.RetrofitClient
import com.example.lesson5.model.Category
import com.example.lesson5.model.Course
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class AddEditCourseActivity : AppCompatActivity() {

    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtInstructor: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtDuration: EditText
    private lateinit var edtThumbnail: EditText
    private lateinit var edtTrailerUrl: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSave: Button

    private var categoriesList: List<Category> = emptyList()
    private var courseToEdit: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_course)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        courseToEdit = intent.getSerializableExtra("COURSE_DATA") as? Course
        supportActionBar?.title = if (courseToEdit != null) "Sửa khóa học" else "Thêm khóa học mới"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        loadCategoriesAndInit()
    }

    private fun initViews() {
        edtTitle = findViewById(R.id.edtTitle)
        edtDescription = findViewById(R.id.edtDescription)
        edtInstructor = findViewById(R.id.edtInstructor)
        edtPrice = findViewById(R.id.edtPrice)
        edtDuration = findViewById(R.id.edtDuration)
        edtThumbnail = findViewById(R.id.edtThumbnail)
        edtTrailerUrl = findViewById(R.id.edtTrailerUrl)
        spinnerCategory = findViewById(R.id.spinnerEditCategory)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun loadCategoriesAndInit() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategories()
                if (response.isSuccessful) {
                    categoriesList = response.body() ?: emptyList()
                    val spinnerAdapter = ArrayAdapter(
                        this@AddEditCourseActivity,
                        android.R.layout.simple_spinner_item,
                        categoriesList.map { it.name }
                    )
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = spinnerAdapter

                    // Nếu là chế độ Sửa, điền dữ liệu cũ vào form
                    courseToEdit?.let { course ->
                        edtTitle.setText(course.title)
                        edtDescription.setText(course.description)
                        edtInstructor.setText(course.instructor)
                        edtPrice.setText(course.price.toString())
                        edtDuration.setText(course.duration.toString())
                        edtThumbnail.setText(course.thumbnail)
                        edtTrailerUrl.setText(course.trailerUrl)

                        val index = categoriesList.indexOfFirst { it.id == course.categoryId }
                        if (index != -1) {
                            spinnerCategory.setSelection(index)
                        }
                    }
                } else {
                    Toast.makeText(this@AddEditCourseActivity, "Không thể tải danh mục", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditCourseActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener {
            validateAndSave()
        }
    }

    private fun validateAndSave() {
        val title = edtTitle.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val instructor = edtInstructor.text.toString().trim()
        val priceStr = edtPrice.text.toString().trim()
        val durationStr = edtDuration.text.toString().trim()
        val thumbnail = edtThumbnail.text.toString().trim()
        val trailerUrl = edtTrailerUrl.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || instructor.isEmpty() ||
            priceStr.isEmpty() || durationStr.isEmpty() || thumbnail.isEmpty() || trailerUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ tất cả các trường", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price < 0.0) {
            Toast.makeText(this, "Học phí phải là số hợp lệ và không được âm", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = durationStr.toIntOrNull()
        if (duration == null || duration < 0) {
            Toast.makeText(this, "Thời lượng phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categoriesList.getOrNull(spinnerCategory.selectedItemPosition)
        if (selectedCategory == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
            return
        }
        val categoryId = selectedCategory.id

        lifecycleScope.launch {
            try {
                if (courseToEdit == null) {
                    val newCourse = Course(
                        id = "",
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        instructor = instructor,
                        price = price,
                        duration = duration,
                        thumbnail = thumbnail,
                        trailerUrl = trailerUrl,
                        createdAt = System.currentTimeMillis().toString()
                    )
                    val response = RetrofitClient.instance.createCourse(newCourse)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddEditCourseActivity, "Thêm khóa học thành công", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddEditCourseActivity, "Thêm thất bại", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val updatedCourse = Course(
                        id = courseToEdit!!.id,
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        instructor = instructor,
                        price = price,
                        duration = duration,
                        thumbnail = thumbnail,
                        trailerUrl = trailerUrl,
                        createdAt = courseToEdit!!.createdAt
                    )
                    val response = RetrofitClient.instance.updateCourse(courseToEdit!!.id, updatedCourse)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddEditCourseActivity, "Cập nhật khóa học thành công", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddEditCourseActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditCourseActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
