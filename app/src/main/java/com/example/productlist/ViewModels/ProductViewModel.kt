package com.example.productlist.ViewModels
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productlist.Repository.ProductRepository
import com.example.productlist.models.Product
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _favorites = MutableLiveData<Set<Int>>(emptySet())
    val favorites: LiveData<Set<Int>> get() = _favorites

    fun fetchProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getProducts()
                _products.postValue(response)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error fetching products", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getProductById(productId: Int): Product? {
        return products.value?.find { it.id == productId }
    }

    fun isFavorite(productId: Int): Boolean {
        return _favorites.value?.contains(productId) ?: false
    }

    fun addToFavorites(productId: Int) {
        _favorites.value = _favorites.value?.plus(productId)
    }

    fun removeFromFavorites(productId: Int) {
        _favorites.value = _favorites.value?.minus(productId)
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
