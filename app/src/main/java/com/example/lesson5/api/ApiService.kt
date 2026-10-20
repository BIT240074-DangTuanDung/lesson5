package com.example.lesson5.api

import com.example.lesson5.model.Category
import com.example.lesson5.model.Course
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // --- Category Endpoints ---
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @POST("categories")
    suspend fun createCategory(@Body category: Category): Response<Category>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body category: Category
    ): Response<Category>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Category>


    // --- Course Endpoints ---
    @GET("courses")
    suspend fun getCourses(): Response<List<Course>>

    @GET("courses/{id}")
    suspend fun getCourseById(@Path("id") id: String): Response<Course>

    @POST("courses")
    suspend fun createCourse(@Body course: Course): Response<Course>

    @PUT("courses/{id}")
    suspend fun updateCourse(
        @Path("id") id: String,
        @Body course: Course
    ): Response<Course>

    @DELETE("courses/{id}")
    suspend fun deleteCourse(@Path("id") id: String): Response<Course>
}
