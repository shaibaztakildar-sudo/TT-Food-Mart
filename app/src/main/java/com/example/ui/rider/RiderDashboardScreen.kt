package com.example.ui.rider

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.RiderProfileEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.common.OrderStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderDashboardScreen(
    rider: RiderProfileEntity?,
    orders: List<OrderEntity>,
    selectedOrderForOtp: OrderEntity?,
    onAcceptOrder: (OrderEntity) -> Unit,
    onPickUpOrder: (orderId: String) -> Unit,
    onOutForDelivery: (orderId: String) -> Unit,
    onPromptOtp: (OrderEntity) -> Unit,
    onVerifyOtp: (orderId: String, otp: String) -> Unit,
    onDismissOtp: () -> Unit,
    onLogoutClick: () -> Unit,
    errorMessage: String?,
    successMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeDeliveries = remember(orders, rider?.riderId) {
        orders.filter { it.riderId == rider?.riderId && it.orderStatus != "DELIVERED" && it.orderStatus != "CANCELLED" }
    }
    val availableOrders = remember(orders) {
        orders.filter { it.orderStatus == "READY" && it.riderId == null }
    }
    val completedDeliveries = remember(orders, rider?.riderId) {
        orders.filter { it.riderId == rider?.riderId && it.orderStatus == "DELIVERED" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(rider?.name ?: "Rider Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Vehicle: ${rider?.vehicleNumber ?: "N/A"} • Ajara, MH",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick, modifier = Modifier.testTag("rider_logout_btn")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = NonVegRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceBackground)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Stats Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Deliveries", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "${rider?.totalDeliveries ?: 0}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF0288D1)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Earnings", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "₹${(rider?.earnings ?: 0.0).toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = VegGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pending Payout", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "₹${(rider?.pendingPayout ?: 0.0).toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = BrandOrangeAccent
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Alerts
            if (!errorMessage.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NonVegRedLight),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(errorMessage, color = NonVegRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                    }
                }
            }
            if (!successMessage.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VegGreenLight),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(successMessage, color = VegGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                    }
                }
            }

            // Active Assigned Deliveries
            item {
                Text(
                    text = "My Active Deliveries (${activeDeliveries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (activeDeliveries.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "No active deliveries in progress. Accept available orders below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(activeDeliveries) { order ->
                    RiderOrderCard(
                        order = order,
                        isAssignedToMe = true,
                        onAccept = {},
                        onPickUp = { onPickUpOrder(order.id) },
                        onOutForDelivery = { onOutForDelivery(order.id) },
                        onDeliver = { onPromptOtp(order) },
                        onNavigate = { openMapsNavigation(context, order.deliveryAddress) }
                    )
                }
            }

            // Available Orders Ready For Pickup
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Available Orders (${availableOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (availableOrders.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No orders ready right now",
                        message = "New food orders from restaurants in Ajara will appear here once marked ready for pickup.",
                        icon = Icons.Default.DeliveryDining
                    )
                }
            } else {
                items(availableOrders) { order ->
                    RiderOrderCard(
                        order = order,
                        isAssignedToMe = false,
                        onAccept = { onAcceptOrder(order) },
                        onPickUp = {},
                        onOutForDelivery = {},
                        onDeliver = {},
                        onNavigate = { openMapsNavigation(context, order.restaurantAddress) }
                    )
                }
            }

            // Completed Deliveries History
            if (completedDeliveries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Completed Deliveries (${completedDeliveries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(completedDeliveries) { order ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ORDER #${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(order.deliveryAddress, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                            OrderStatusBadge(status = "DELIVERED")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // DELIVERY OTP MODAL
    if (selectedOrderForOtp != null) {
        DeliveryOtpVerificationDialog(
            order = selectedOrderForOtp,
            onDismiss = onDismissOtp,
            onVerify = { otp -> onVerifyOtp(selectedOrderForOtp.id, otp) }
        )
    }
}

@Composable
private fun RiderOrderCard(
    order: OrderEntity,
    isAssignedToMe: Boolean,
    onAccept: () -> Unit,
    onPickUp: () -> Unit,
    onOutForDelivery: () -> Unit,
    onDeliver: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("rider_order_${order.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDER #${order.id}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = BrandRedPrimary
                )
                OrderStatusBadge(status = order.orderStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Restaurant Pickup Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandOrangeAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pickup: ${order.restaurantName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = order.restaurantAddress,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(start = 22.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Customer Delivery Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Deliver to: ${order.customerName} (${order.customerPhone})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = order.deliveryAddress,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(start = 22.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bill & Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bill: ₹${order.totalAmount.toInt()} (${order.paymentMethod})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (order.paymentMethod == "COD") "COLLECT CASH" else "PAID ONLINE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (order.paymentMethod == "COD") StatusPending else VegGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("rider_nav_${order.id}")
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Map / Nav", fontSize = 12.sp)
                }

                if (!isAssignedToMe) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("rider_accept_${order.id}")
                    ) {
                        Text("Accept Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    when (order.orderStatus) {
                        "RIDER_ASSIGNED" -> {
                            Button(
                                onClick = onPickUp,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPreparing),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("rider_pickup_${order.id}")
                            ) {
                                Text("Pick Up Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        "PICKED_UP" -> {
                            Button(
                                onClick = onOutForDelivery,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("rider_out_${order.id}")
                            ) {
                                Text("Out for Delivery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        "OUT_FOR_DELIVERY" -> {
                            Button(
                                onClick = onDeliver,
                                colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("rider_deliver_${order.id}")
                            ) {
                                Text("Deliver with OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryOtpVerificationDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onVerify: (otp: String) -> Unit
) {
    var enteredOtp by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = VegGreen,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Verify Customer OTP",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Ask customer for the 4-digit OTP shown on their screen for Order #${order.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = {
                        if (it.length <= 4) enteredOtp = it.filter { c -> c.isDigit() }
                    },
                    label = { Text("4-digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f).testTag("rider_otp_field")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (enteredOtp.length == 4) {
                                onVerify(enteredOtp)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                        modifier = Modifier.weight(1f).testTag("rider_verify_otp_btn")
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun openMapsNavigation(context: Context, locationQuery: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(locationQuery))
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to browser or generic map handler
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(locationQuery)))
            context.startActivity(genericIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
