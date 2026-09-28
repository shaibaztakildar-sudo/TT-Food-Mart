package com.example.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VegNonVegBadge(
    isVeg: Boolean,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val borderColor = if (isVeg) VegGreen else NonVegRed
    val dotColor = if (isVeg) VegGreen else NonVegRed
    val label = if (isVeg) "VEG" else "NON-VEG"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
        }
        if (showLabel) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = borderColor
            )
        }
    }
}

@Composable
fun OrderStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        "PLACED" -> Triple(Color(0xFFFFF3E0), StatusPending, "Order Placed")
        "ACCEPTED" -> Triple(Color(0xFFE8F5E9), StatusApproved, "Accepted")
        "PREPARING" -> Triple(Color(0xFFE1F5FE), StatusPreparing, "Preparing")
        "READY" -> Triple(Color(0xFFEDE7F6), Color(0xFF5E35B1), "Ready for Pickup")
        "RIDER_ASSIGNED" -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Rider Assigned")
        "PICKED_UP" -> Triple(Color(0xFFF3E5F5), StatusOutForDelivery, "Picked Up")
        "OUT_FOR_DELIVERY" -> Triple(Color(0xFFEDE7F6), StatusOutForDelivery, "Out for Delivery")
        "DELIVERED" -> Triple(Color(0xFFE8F5E9), StatusDelivered, "Delivered")
        "CANCELLED" -> Triple(Color(0xFFFFEBEE), StatusCancelled, "Cancelled")
        else -> Triple(Color(0xFFF5F5F5), Color.DarkGray, status)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
