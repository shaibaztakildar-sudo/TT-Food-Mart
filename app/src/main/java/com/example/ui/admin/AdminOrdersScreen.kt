package com.example.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.RiderProfileEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.common.OrderStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    orders: List<OrderEntity>,
    eligibleRiders: List<RiderProfileEntity>,
    onAssignRider: (orderId: String, rider: RiderProfileEntity) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var orderToAssignRider by remember { mutableStateOf<OrderEntity?>(null) }

    val filteredOrders = remember(orders, selectedFilter) {
        if (selectedFilter == "ALL") orders
        else orders.filter { it.orderStatus == selectedFilter }
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Orders (${orders.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("admin_orders_back")) {
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
            // Status filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All",
                    "PLACED" to "New",
                    "ACCEPTED" to "Accepted",
                    "PREPARING" to "Preparing",
                    "READY" to "Ready",
                    "RIDER_ASSIGNED" to "Assigned",
                    "OUT_FOR_DELIVERY" to "Out for Delivery",
                    "DELIVERED" to "Delivered",
                    "CANCELLED" to "Cancelled"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = NonVegRedLight), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(errorMessage, color = NonVegRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }
            if (!successMessage.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = VegGreenLight), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(successMessage, color = VegGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }

            if (filteredOrders.isEmpty()) {
                EmptyStateView(
                    title = "No orders found",
                    message = "Orders placed by customers will be monitored in real-time here.",
                    icon = Icons.Default.Assignment
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOrders) { order ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("admin_order_${order.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("ORDER #${order.id}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BrandRedPrimary)
                                        Text(dateFormat.format(Date(order.createdAt)), fontSize = 11.sp, color = TextSecondary)
                                    }
                                    OrderStatusBadge(status = order.orderStatus)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Customer: ${order.customerName} (${order.customerPhone})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Restaurant: ${order.restaurantName}", fontSize = 12.sp, color = TextSecondary)
                                Text("Address: ${order.deliveryAddress}", fontSize = 12.sp, color = TextSecondary)
                                if (order.riderName != null) {
                                    Text("Rider: ${order.riderName} (${order.riderPhone})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF0288D1))
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Amount: ₹${order.totalAmount.toInt()} (${order.paymentMethod})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (order.riderId == null && order.orderStatus != "DELIVERED" && order.orderStatus != "CANCELLED") {
                                        Button(
                                            onClick = { orderToAssignRider = order },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Assign Rider", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

    // Manual Assign Rider Dialog
    if (orderToAssignRider != null) {
        Dialog(onDismissRequest = { orderToAssignRider = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Assign Delivery Rider", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Order #${orderToAssignRider?.id}", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (eligibleRiders.isEmpty()) {
                        Text("No approved active riders found in database. Approve riders in the Rider portal first.", color = NonVegRed, fontSize = 12.sp)
                    } else {
                        eligibleRiders.forEach { rider ->
                            Card(
                                onClick = {
                                    orderToAssignRider?.let { onAssignRider(it.id, rider) }
                                    orderToAssignRider = null
                                },
                                colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rider.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${rider.phone} • ${rider.vehicleNumber}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Text("Assign ->", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0288D1))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { orderToAssignRider = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
