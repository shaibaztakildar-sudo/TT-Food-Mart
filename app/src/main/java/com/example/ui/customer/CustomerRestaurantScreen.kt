package com.example.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.data.repository.FoodDeliveryRepository
import com.example.ui.common.EmptyStateView
import com.example.ui.common.VegNonVegBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRestaurantScreen(
    restaurant: SellerProfileEntity,
    products: List<ProductEntity>,
    cart: Map<String, FoodDeliveryRepository.CartItem>,
    onAddToCart: (ProductEntity) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onViewCartClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val restaurantProducts = remember(products, restaurant.sellerId) {
        products.filter { it.sellerId == restaurant.sellerId && it.isAvailable }
    }

    val totalItemsInCart = remember(cart) {
        cart.values.sumOf { it.quantity }
    }
    val cartTotal = remember(cart) {
        cart.values.sumOf { it.product.price * it.quantity }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurant.restaurantName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("rest_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        bottomBar = {
            if (totalItemsInCart > 0) {
                Surface(
                    color = BrandRedPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewCartClick() }
                        .testTag("rest_view_cart_bar"),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalItemsInCart ITEMS IN CART",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${cartTotal.toInt()}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "View Cart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceBackground)
                .padding(padding)
        ) {
            // Restaurant Banner Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        if (!restaurant.imageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = restaurant.imageUri,
                                contentDescription = restaurant.restaurantName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = restaurant.restaurantName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = restaurant.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(restaurant.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                if (restaurant.rating > 0) {
                                    Surface(color = VegGreen, shape = RoundedCornerShape(4.dp)) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = String.format("%.1f", restaurant.rating),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Menu Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            if (restaurantProducts.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No dishes listed yet",
                        message = "This restaurant has not added any dishes to their menu yet.",
                        icon = Icons.Default.MenuBook
                    )
                }
            } else {
                items(restaurantProducts) { product ->
                    val quantity = cart[product.id]?.quantity ?: 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("menu_item_${product.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                VegNonVegBadge(isVeg = product.isVeg, showLabel = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${product.price.toInt()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (product.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = product.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (!product.imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUri,
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (quantity > 0) {
                                    Surface(
                                        color = BrandRedPrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandRedPrimary)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onRemoveFromCart(product.id) },
                                                modifier = Modifier.size(28.dp).testTag("decrease_qty_${product.id}")
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrandRedPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = "$quantity",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = BrandRedPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                            IconButton(
                                                onClick = { onAddToCart(product) },
                                                modifier = Modifier.size(28.dp).testTag("increase_qty_${product.id}")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandRedPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { onAddToCart(product) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("add_item_${product.id}")
                                    ) {
                                        Text("ADD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
