package com.example.productlist.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val rating: String,
    val imageUrl: String,
    val description: String,
) : Parcelable
