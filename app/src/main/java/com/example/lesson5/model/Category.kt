package com.example.lesson5.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Category(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
) : Serializable
