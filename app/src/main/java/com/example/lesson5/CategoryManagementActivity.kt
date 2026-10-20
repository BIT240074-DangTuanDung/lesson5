package com.example.lesson5

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lesson5.adapter.CategoryAdapter
import com.example.lesson5.api.RetrofitClient
import com.example.lesson5.model.Category
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class CategoryManagementActivity : AppCompatActivity() {

    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var btnAddCategory: Button
    private lateinit var categoryAdapter: CategoryAdapter

    private var categoryList: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_management)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Quản lý danh mục"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        setupRecyclerView()
        fetchCategories()
    }

    private fun initViews() {
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        btnAddCategory.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun setupRecyclerView() {
        recyclerViewCategories.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(
            emptyList(),
            onEditClick = { category -> showAddEditDialog(category) },
            onDeleteClick = { category -> checkAndDeleteCategory(category) }
        )
        recyclerViewCategories.adapter = categoryAdapter
    }

    private fun fetchCategories() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategories()
                if (response.isSuccessful) {
                    categoryList = response.body() ?: emptyList()
                    categoryAdapter.updateData(categoryList)
                } else {
                    Toast.makeText(this@CategoryManagementActivity, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoryManagementActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddEditDialog(categoryToEdit: Category?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_category, null)
        val edtName = dialogView.findViewById<EditText>(R.id.edtCategoryName)
        val edtDescription = dialogView.findViewById<EditText>(R.id.edtCategoryDescription)

        if (categoryToEdit != null) {
            edtName.setText(categoryToEdit.name)
            edtDescription.setText(categoryToEdit.description)
        }

        val title = if (categoryToEdit == null) "Thêm danh mục mới" else "Sửa danh mục"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = edtName.text.toString().trim()
                val description = edtDescription.text.toString().trim()

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (categoryToEdit == null) {
                    createCategory(name, description)
                } else {
                    updateCategory(categoryToEdit.id, name, description)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun createCategory(name: String, description: String) {
        lifecycleScope.launch {
            try {
                val newCategory = Category(id = "", name = name, description = description)
                val response = RetrofitClient.instance.createCategory(newCategory)
                if (response.isSuccessful) {
                    Toast.makeText(this@CategoryManagementActivity, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show()
                    fetchCategories()
                } else {
                    Toast.makeText(this@CategoryManagementActivity, "Thêm danh mục thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoryManagementActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategory(id: String, name: String, description: String) {
        lifecycleScope.launch {
            try {
                val updatedCategory = Category(id = id, name = name, description = description)
                val response = RetrofitClient.instance.updateCategory(id, updatedCategory)
                if (response.isSuccessful) {
                    Toast.makeText(this@CategoryManagementActivity, "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show()
                    fetchCategories()
                } else {
                    Toast.makeText(this@CategoryManagementActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoryManagementActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndDeleteCategory(category: Category) {
        lifecycleScope.launch {
            try {
                val courseResponse = RetrofitClient.instance.getCourses()
                if (courseResponse.isSuccessful) {
                    val courses = courseResponse.body() ?: emptyList()
                    val hasCourses = courses.any { item -> item.categoryId == category.id }

                    if (hasCourses) {
                        AlertDialog.Builder(this@CategoryManagementActivity)
                            .setTitle("Không thể xóa")
                            .setMessage("Danh mục \"${category.name}\" đang chứa khóa học, bạn không thể xóa danh mục này!")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        AlertDialog.Builder(this@CategoryManagementActivity)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa danh mục \"${category.name}\" không?")
                            .setPositiveButton("Xóa") { _, _ ->
                                deleteCategory(category.id)
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                    }
                } else {
                    Toast.makeText(this@CategoryManagementActivity, "Không thể kiểm tra danh sách khóa học", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoryManagementActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCategory(categoryId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteCategory(categoryId)
                if (response.isSuccessful) {
                    Toast.makeText(this@CategoryManagementActivity, "Xóa danh mục thành công", Toast.LENGTH_SHORT).show()
                    fetchCategories()
                } else {
                    Toast.makeText(this@CategoryManagementActivity, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoryManagementActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
