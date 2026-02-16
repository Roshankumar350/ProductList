package com.example.productlist.Repository
import com.example.productlist.Services.ApiService
import com.example.productlist.Services.RetrofitClient
import com.example.productlist.models.Product

class ProductRepository(private val apiService: ApiService) {
    suspend fun getProducts(): List<Product> {
        return apiService.getProducts()
    }
}