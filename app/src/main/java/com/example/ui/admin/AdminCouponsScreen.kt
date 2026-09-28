package com.example.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.CouponEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponsScreen(
    coupons: List<CouponEntity>,
    onAddCoupon: (code: String, title: String, discountPercent: Int, maxDiscount: Double, minOrder: Double) -> Unit,
    onToggleActive: (code: String, current: Boolean) -> Unit,
    onDeleteCoupon: (code: String) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coupon Engine (${coupons.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("admin_coupons_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandRedPrimary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_coupon_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Coupon")
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
                Card(colors = CardDefaults.cardColors(containerColor = NonVegRedLight), modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(errorMessage, color = NonVegRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }
            if (!successMessage.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = VegGreenLight), modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(successMessage, color = VegGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }

            if (coupons.isEmpty()) {
                EmptyStateView(
                    title = "No coupons configured",
                    message = "Create promotional discounts for customers in Ajara using the '+' button.",
                    icon = Icons.Default.LocalOffer,
                    actionLabel = "Create First Coupon",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(coupons) { coupon ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("coupon_row_${coupon.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = coupon.code,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = BrandRedPrimary
                                    )
                                    Text(
                                        text = "${coupon.discountPercent}% OFF up to ₹${coupon.maxDiscountAmount.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Min order: ₹${coupon.minOrderAmount.toInt()} • ${coupon.title}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = coupon.isActive,
                                        onCheckedChange = { onToggleActive(coupon.code, coupon.isActive) }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(onClick = { onDeleteCoupon(coupon.code) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NonVegRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCouponDialog(
            onDismiss = { showAddDialog = false },
            onSave = { code, title, pct, maxDisc, minOrd ->
                onAddCoupon(code, title, pct, maxDisc, minOrd)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddCouponDialog(
    onDismiss: () -> Unit,
    onSave: (code: String, title: String, discountPercent: Int, maxDiscount: Double, minOrder: Double) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var pctStr by remember { mutableStateOf("20") }
    var maxDiscStr by remember { mutableStateOf("50") }
    var minOrderStr by remember { mutableStateOf("199") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Create New Coupon", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code (e.g. TTFIRST) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description (e.g. Welcome Offer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pctStr,
                        onValueChange = { pctStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Discount % *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxDiscStr,
                        onValueChange = { maxDiscStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Max ₹ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = minOrderStr,
                    onValueChange = { minOrderStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Min Order Value (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pct = pctStr.toIntOrNull() ?: 0
                            val maxD = maxDiscStr.toDoubleOrNull() ?: 0.0
                            val minO = minOrderStr.toDoubleOrNull() ?: 0.0
                            if (code.isNotBlank() && pct > 0) {
                                onSave(code, title, pct, maxD, minO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
