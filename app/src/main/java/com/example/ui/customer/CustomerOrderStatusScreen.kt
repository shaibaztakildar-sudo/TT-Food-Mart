package com.example.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import com.example.ui.common.OrderStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderStatusScreen(
    order: OrderEntity,
    items: List<OrderItemEntity>,
    onSubmitReview: (orderId: String, rating: Int, text: String) -> Unit,
    onCancelOrder: (orderId: String, reason: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var rating by remember { mutableStateOf(order.rating ?: 5) }
    var reviewText by remember { mutableStateOf(order.reviewText ?: "") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("order_status_back")) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ORDER #${order.id}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandRedPrimary
                            )
                            Text(text = formattedDate, fontSize = 12.sp, color = TextSecondary)
                        }
                        OrderStatusBadge(status = order.orderStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "From: ${order.restaurantName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Delivering to: ${order.deliveryAddress}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DELIVERY OTP CARD (Displayed prominently for delivery verification)
            if (order.orderStatus != "DELIVERED" && order.orderStatus != "CANCELLED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandOrangeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = BrandOrangeAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DELIVERY VERIFICATION OTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrandOrangeAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = order.deliveryOtp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = 6.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please share this 4-digit code with your delivery partner at the doorstep to verify delivery.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Order Status Timeline
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order Lifecycle Tracking",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val stages = listOf(
                        "PLACED" to "Order Placed",
                        "ACCEPTED" to "Seller Accepted",
                        "PREPARING" to "Kitchen Preparing",
                        "READY" to "Ready for Pickup",
                        "RIDER_ASSIGNED" to if (order.riderName != null) "Rider Assigned (${order.riderName})" else "Rider Assigned",
                        "PICKED_UP" to "Picked Up by Rider",
                        "OUT_FOR_DELIVERY" to "Out for Delivery",
                        "DELIVERED" to "Delivered"
                    )

                    val currentStageIndex = when (order.orderStatus) {
                        "PLACED" -> 0
                        "ACCEPTED" -> 1
                        "PREPARING" -> 2
                        "READY" -> 3
                        "RIDER_ASSIGNED" -> 4
                        "PICKED_UP" -> 5
                        "OUT_FOR_DELIVERY" -> 6
                        "DELIVERED" -> 7
                        "CANCELLED" -> -1
                        else -> 0
                    }

                    if (order.orderStatus == "CANCELLED") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = NonVegRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Order Cancelled", fontWeight = FontWeight.Bold, color = NonVegRed)
                                Text(
                                    text = order.cancellationReason ?: "No reason provided",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        stages.forEachIndexed { index, (stageKey, stageLabel) ->
                            val isCompleted = index <= currentStageIndex
                            val isCurrent = index == currentStageIndex

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isCompleted) VegGreen else Color.LightGray,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stageLabel,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCompleted) TextPrimary else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }

                            if (index < stages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 9.dp)
                                        .width(2.dp)
                                        .height(18.dp)
                                        .background(if (index < currentStageIndex) VegGreen else Color.LightGray)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ordered Items
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Items Ordered", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}x ${item.productName}", fontSize = 13.sp)
                            Text("₹${item.itemTotal.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = TextSecondary, fontSize = 12.sp)
                        Text("₹${order.subtotal.toInt()}", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee", color = TextSecondary, fontSize = 12.sp)
                        Text(if (order.deliveryCharge == 0.0) "FREE" else "₹${order.deliveryCharge.toInt()}", fontSize = 12.sp)
                    }
                    if (order.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount", color = VegGreen, fontSize = 12.sp)
                            Text("-₹${order.discount.toInt()}", color = VegGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Bill Paid", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("₹${order.totalAmount.toInt()} (${order.paymentMethod})", fontWeight = FontWeight.Bold, color = BrandRedPrimary)
                    }
                }
            }

            // Rating & Review section if Delivered
            if (order.orderStatus == "DELIVERED") {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rate & Review Your Food", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { rating = star }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star Stars",
                                        tint = if (star <= rating) BrandYellowStar else Color.LightGray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            label = { Text("Write your feedback...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onSubmitReview(order.id, rating, reviewText) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Submit Review")
                        }
                    }
                }
            }

            // Cancel button if still PLACED or ACCEPTED
            if (order.orderStatus == "PLACED" || order.orderStatus == "ACCEPTED") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NonVegRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NonVegRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_order_button")
                ) {
                    Text("Cancel Order")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order?") },
            text = {
                Column {
                    Text("Are you sure you want to cancel this order?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelOrder(order.id, cancelReason.ifBlank { "Customer requested cancellation" })
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NonVegRed)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No, Keep Order")
                }
            }
        )
    }
}
