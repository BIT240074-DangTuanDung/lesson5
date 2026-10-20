package com.example.lesson5

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lesson5.adapter.CourseAdapter
import com.example.lesson5.api.RetrofitClient
import com.example.lesson5.model.Category
import com.example.lesson5.model.Course
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var spinnerCategory: Spinner
    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter

    private var allCourses: List<Course> = emptyList()
    private var categoriesList: List<Category> = emptyList()
    private var categoryMap: Map<String, String> = emptyMap()

    private var currentSearchQuery = ""
    private var selectedCategoryId = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Quản lý khóa học"

        initViews()
        setupRecyclerView()
        setupSearchAndSpinner()
        fetchData()
    }

    private fun initViews() {
        searchView = findViewById(R.id.searchView)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses)
    }

    private fun setupRecyclerView() {
        recyclerViewCourses.layoutManager = LinearLayoutManager(this)
        courseAdapter = CourseAdapter(emptyList(), emptyMap()) { course ->
            val intent = Intent(this, CourseDetailActivity::class.java).apply {
                putExtra("COURSE_DATA", course)
            }
            startActivity(intent)
        }
        recyclerViewCourses.adapter = courseAdapter
    }

    private fun setupSearchAndSpinner() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                filterCourses()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                filterCourses()
                return true
            }
        })

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryId = if (position == 0) {
                    "All"
                } else {
                    categoriesList[position - 1].id
                }
                filterCourses()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = "All"
                filterCourses()
            }
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            try {
                val catResponse = RetrofitClient.instance.getCategories()
                val courseResponse = RetrofitClient.instance.getCourses()

                if (catResponse.isSuccessful && courseResponse.isSuccessful) {
                    categoriesList = catResponse.body() ?: emptyList()
                    allCourses = courseResponse.body() ?: emptyList()

                    categoryMap = categoriesList.associate { it.id to it.name }

                    val spinnerItems = mutableListOf("Tất cả danh mục")
                    spinnerItems.addAll(categoriesList.map { it.name })
                    val spinnerAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        spinnerItems
                    )
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = spinnerAdapter

                    filterCourses()
                } else {
                    Toast.makeText(this@MainActivity, "Lỗi tải dữ liệu từ API", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterCourses() {
        val filtered = allCourses.filter { course ->
            val matchesSearch = course.title.contains(currentSearchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryId == "All" || course.categoryId == selectedCategoryId
            matchesSearch && matchesCategory
        }
        courseAdapter.updateData(filtered, categoryMap)
    }
}
