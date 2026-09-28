package com.example.ui.seller

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entities.ProductEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.common.VegNonVegBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductsScreen(
    products: List<ProductEntity>,
    onAddProduct: (name: String, desc: String, price: Double, category: String, isVeg: Boolean, imageUri: String?) -> Unit,
    onUpdateProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onToggleAvailability: (productId: String, current: Boolean) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("products_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandRedPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Dish", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceBackground)
                .padding(padding)
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NonVegRedLight),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(errorMessage, color = NonVegRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }
            if (!successMessage.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VegGreenLight),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(successMessage, color = VegGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }

            if (products.isEmpty()) {
                EmptyStateView(
                    title = "Your menu is empty",
                    message = "Tap 'Add New Dish' below to upload food photos and add items to your restaurant menu.",
                    icon = Icons.Default.RestaurantMenu,
                    actionLabel = "Add First Dish",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_row_${product.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!product.imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUri,
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        VegNonVegBadge(isVeg = product.isVeg)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = product.category,
                                            fontSize = 11.sp,
                                            color = BrandOrangeAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "₹${product.price.toInt()}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    // Available Switch
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (product.isAvailable) "In Stock" else "Sold Out",
                                            fontSize = 11.sp,
                                            color = if (product.isAvailable) VegGreen else NonVegRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Switch(
                                            checked = product.isAvailable,
                                            onCheckedChange = { onToggleAvailability(product.id, product.isAvailable) },
                                            modifier = Modifier.testTag("switch_avail_${product.id}")
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = { editingProduct = product }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { onDeleteProduct(product.id) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NonVegRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        ProductFormDialog(
            title = "Add New Dish",
            initialProduct = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, desc, price, cat, isVeg, img ->
                onAddProduct(name, desc, price, cat, isVeg, img)
                showAddDialog = false
            }
        )
    }

    // Edit Product Dialog
    if (editingProduct != null) {
        ProductFormDialog(
            title = "Edit Dish",
            initialProduct = editingProduct,
            onDismiss = { editingProduct = null },
            onSave = { name, desc, price, cat, isVeg, img ->
                editingProduct?.let {
                    onUpdateProduct(it.copy(name = name, description = desc, price = price, category = cat, isVeg = isVeg, imageUri = img))
                }
                editingProduct = null
            }
        )
    }
}

@Composable
private fun ProductFormDialog(
    title: String,
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, price: Double, category: String, isVeg: Boolean, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var priceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.price.toInt().toString() else "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Main Course") }
    var isVeg by remember { mutableStateOf(initialProduct?.isVeg ?: true) }
    var imageUri by remember { mutableStateOf<Uri?>(initialProduct?.imageUri?.let { Uri.parse(it) }) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val categories = listOf("Main Course", "Biryani", "Thali", "Starters", "Snacks", "Beverages", "Dessert")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dish_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("dish_desc_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Price in ₹ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dish_price_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Veg / Non-Veg Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    FilterChip(
                        selected = isVeg,
                        onClick = { isVeg = true },
                        label = { Text("Veg") },
                        leadingIcon = { VegNonVegBadge(isVeg = true) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !isVeg,
                        onClick = { isVeg = false },
                        label = { Text("Non-Veg") },
                        leadingIcon = { VegNonVegBadge(isVeg = false) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upload Dish Photo from phone gallery
                OutlinedCard(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandRedPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (imageUri != null) "Photo Selected (Tap to change)" else "Upload Dish Photo from Gallery",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && price > 0) {
                                onSave(name, description, price, category, isVeg, imageUri?.toString())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                        modifier = Modifier.testTag("save_dish_btn")
                    ) {
                        Text("Save Dish")
                    }
                }
            }
        }
    }
}
