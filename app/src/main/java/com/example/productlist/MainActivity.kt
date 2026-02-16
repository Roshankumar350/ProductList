package com.example.productlist

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.productlist.Repository.ProductRepository
import com.example.productlist.Services.RetrofitClient
import com.example.productlist.ViewModels.ProductViewModel
import com.example.productlist.ViewModels.ProductViewModelFactory
import com.example.productlist.models.Product
import com.example.productlist.ui.theme.ProductListTheme
import com.example.productlist.utils.ConnectionState
import com.example.productlist.utils.currentConnectionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = RetrofitClient.apiService
        val repository = ProductRepository(apiService)
        val viewModelFactory = ProductViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ProductViewModel::class.java]

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            ProductListTheme {
                val navController = rememberNavController()
                val favorites by viewModel.favorites.observeAsState(emptySet())
                val connectionState = currentConnectionState()

                NavHost(navController = navController, startDestination = "productList") {
                    composable("productList") {
                        when (connectionState) {
                            ConnectionState.Available -> {
                                val products by viewModel.products.observeAsState(initial = emptyList())
                                LaunchedEffect(Unit) {
                                    viewModel.fetchProducts()
                                }
                                ProductListScreen(
                                    products = products,
                                    cartCount = favorites.size,
                                    navController = navController
                                )
                            }
                            ConnectionState.Unavailable -> {
                                NoInternetScreen()
                            }
                        }
                    }
                    composable("productDetail/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
                        if (productId != null) {
                            val product = viewModel.getProductById(productId)
                            if (product != null) {
                                ProductDetailScreen(
                                    product = product,
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                    composable("profile") {
                        ProfileScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun NoInternetScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "No Internet Connection") },
            text = { Text(text = "Please check your internet connection and try again.") },
            confirmButton = {
                TextButton(onClick = { /* TODO: Add a retry mechanism */ }) {
                    Text("Retry")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(products: List<Product>, cartCount: Int, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = androidx.compose.ui.graphics.Color.White)
                    }
                    IconButton(onClick = { /* Navigate to Cart */ }) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(containerColor = androidx.compose.ui.graphics.Color.Red) {
                                        Text(cartCount.toString(), color = androidx.compose.ui.graphics.Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Black)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(products) { product ->
                ProductItem(product = product, navController = navController)
                HorizontalDivider(thickness = 0.5.dp, color = androidx.compose.ui.graphics.Color.LightGray)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("productDetail/${product.id}") }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .background(androidx.compose.ui.graphics.Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Yellow, modifier = Modifier.size(14.dp))
                Text(text = " 4.7", style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color(0xFFB00020), // Dark Red
                fontWeight = FontWeight.Bold
            )
        }
    }
}
