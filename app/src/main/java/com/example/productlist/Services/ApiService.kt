package com.example.productlist.Services
import com.example.productlist.models.Product
import retrofit2.http.GET

interface ApiService {
    @GET("866592d4df655060f42c")
    suspend fun getProducts(): List<Product>
}