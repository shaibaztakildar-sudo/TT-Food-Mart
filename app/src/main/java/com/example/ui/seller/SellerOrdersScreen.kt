package com.example.ui.seller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.OrderEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.common.OrderStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    orders: List<OrderEntity>,
    onAcceptOrder: (orderId: String) -> Unit,
    onRejectOrder: (orderId: String, reason: String) -> Unit,
    onStartPreparing: (orderId: String) -> Unit,
    onMarkReady: (orderId: String) -> Unit,
    onPrintSticker: (OrderEntity) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var orderToReject by remember { mutableStateOf<OrderEntity?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> orders
            "PLACED" -> orders.filter { it.orderStatus == "PLACED" }
            "ACCEPTED" -> orders.filter { it.orderStatus == "ACCEPTED" }
            "PREPARING" -> orders.filter { it.orderStatus == "PREPARING" }
            "READY" -> orders.filter { it.orderStatus == "READY" || it.orderStatus == "RIDER_ASSIGNED" }
            "DELIVERED" -> orders.filter { it.orderStatus == "DELIVERED" }
            "CANCELLED" -> orders.filter { it.orderStatus == "CANCELLED" }
            else -> orders
        }
    }

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("seller_orders_back")) {
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
            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All (${orders.size})",
                    "PLACED" to "New (${orders.count { it.orderStatus == "PLACED" }})",
                    "ACCEPTED" to "Accepted",
                    "PREPARING" to "Preparing",
                    "READY" to "Ready",
                    "DELIVERED" to "Delivered",
                    "CANCELLED" to "Cancelled"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandRedPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NonVegRedLight),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(errorMessage, color = NonVegRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }
            if (!successMessage.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VegGreenLight),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(successMessage, color = VegGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }

            if (filteredOrders.isEmpty()) {
                EmptyStateView(
                    title = "No orders yet",
                    message = "Customer orders will appear here in real-time as they are placed.",
                    icon = Icons.Default.ReceiptLong
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("seller_order_${order.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Order ID and Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ORDER #${order.id}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = BrandRedPrimary
                                        )
                                        Text(
                                            text = dateFormat.format(Date(order.createdAt)),
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    OrderStatusBadge(status = order.orderStatus)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                // Customer info
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${order.customerName} • ${order.customerPhone}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = order.deliveryAddress,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (order.customerInstructions.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Note: ${order.customerInstructions}",
                                        fontSize = 12.sp,
                                        color = BrandOrangeAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Amount & Payment Info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Bill: ₹${order.totalAmount.toInt()} (${order.paymentMethod})",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Payment: ${order.paymentStatus}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.paymentStatus == "PAID") VegGreen else StatusPending
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons Row:
                                // PRINT STICKER is ALWAYS clearly visible for any active order!
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // PRINT STICKER BUTTON (Very important)
                                    OutlinedButton(
                                        onClick = { onPrintSticker(order) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("print_sticker_${order.id}")
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Print Sticker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    when (order.orderStatus) {
                                        "PLACED" -> {
                                            OutlinedButton(
                                                onClick = { orderToReject = order },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NonVegRed),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, NonVegRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("reject_order_${order.id}")
                                            ) {
                                                Text("Reject", fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = { onAcceptOrder(order.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("accept_order_${order.id}")
                                            ) {
                                                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        "ACCEPTED" -> {
                                            Button(
                                                onClick = { onStartPreparing(order.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusPreparing),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("start_prep_${order.id}")
                                            ) {
                                                Text("Start Preparing", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        "PREPARING" -> {
                                            Button(
                                                onClick = { onMarkReady(order.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("mark_ready_${order.id}")
                                            ) {
                                                Text("Mark Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        "READY" -> {
                                            Surface(
                                                color = Color(0xFFEDE7F6),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Waiting for Rider Pickup",
                                                    color = Color(0xFF5E35B1),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Dialog
    if (orderToReject != null) {
        AlertDialog(
            onDismissRequest = { orderToReject = null },
            title = { Text("Reject Order #${orderToReject?.id}") },
            text = {
                Column {
                    Text("Please specify why the order cannot be fulfilled:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. Item out of stock / Kitchen closing") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        orderToReject?.let { onRejectOrder(it.id, rejectReason.ifBlank { "Restaurant busy" }) }
                        orderToReject = null
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NonVegRed)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
