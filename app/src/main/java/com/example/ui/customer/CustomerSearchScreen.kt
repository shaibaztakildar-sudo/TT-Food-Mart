package com.example.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.common.VegNonVegBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSearchScreen(
    searchQuery: String,
    searchResults: List<ProductEntity>,
    restaurants: List<SellerProfileEntity>,
    onQueryChange: (String) -> Unit,
    onAddToCart: (ProductEntity, SellerProfileEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search dishes, biryani, thali...") },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_query_input")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("search_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceBackground)
                .padding(padding)
        ) {
            if (searchQuery.isBlank()) {
                EmptyStateView(
                    title = "Search food in Ajara",
                    message = "Type a dish or category name above to find available dishes from local restaurants.",
                    icon = Icons.Default.Search
                )
            } else if (searchResults.isEmpty()) {
                EmptyStateView(
                    title = "No dishes found",
                    message = "No matching dishes found for \"$searchQuery\". Try another food keyword.",
                    icon = Icons.Default.Search
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { product ->
                        val matchingRestaurant = restaurants.firstOrNull { it.sellerId == product.sellerId }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    VegNonVegBadge(isVeg = product.isVeg)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("₹${product.price.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (matchingRestaurant != null) {
                                        Text(matchingRestaurant.restaurantName, fontSize = 11.sp, color = BrandOrangeAccent)
                                    }
                                }

                                if (!product.imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUri,
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Button(
                                    onClick = {
                                        if (matchingRestaurant != null) {
                                            onAddToCart(product, matchingRestaurant)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
