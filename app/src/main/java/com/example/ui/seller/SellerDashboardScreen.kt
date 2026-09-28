package com.example.ui.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SellerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    profile: SellerProfileEntity?,
    orders: List<OrderEntity>,
    onNavigate: (SellerScreen) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val newOrdersCount = orders.count { it.orderStatus == "PLACED" }
    val preparingOrdersCount = orders.count { it.orderStatus == "ACCEPTED" || it.orderStatus == "PREPARING" }
    val readyOrdersCount = orders.count { it.orderStatus == "READY" }
    val completedOrdersCount = orders.count { it.orderStatus == "DELIVERED" }
    val cancelledOrdersCount = orders.count { it.orderStatus == "CANCELLED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = profile?.restaurantName ?: "Restaurant Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Ajara, Maharashtra • Seller Portal",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick, modifier = Modifier.testTag("seller_logout_btn")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = NonVegRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceCard) {
                NavigationBarItem(
                    selected = true,
                    onClick = { onNavigate(SellerScreen.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(SellerScreen.ORDERS) },
                    icon = {
                        BadgedBox(badge = {
                            if (newOrdersCount > 0) {
                                Badge { Text("$newOrdersCount") }
                            }
                        }) {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                        }
                    },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(SellerScreen.PRODUCTS) },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
                    label = { Text("Menu") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(SellerScreen.EARNINGS) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Payouts") }
                )
            }
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
            // New Orders Urgent Alert Banner
            if (newOrdersCount > 0) {
                Card(
                    onClick = { onNavigate(SellerScreen.ORDERS) },
                    colors = CardDefaults.cardColors(containerColor = BrandRedPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_orders_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$newOrdersCount NEW ORDER(S) RECEIVED",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Accept & print thermal stickers now",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Financial Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Earnings Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Earnings", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "₹${(profile?.earnings ?: 0.0).toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        Column {
                            Text("Pending Payout", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "₹${(profile?.pendingPayout ?: 0.0).toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandOrangeAccent
                            )
                        }
                        Column {
                            Text("Paid Out", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "₹${(profile?.paidPayout ?: 0.0).toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = VegGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Status Pipeline Grid
            Text(
                text = "Order Pipeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusCounterCard(
                    title = "New Orders",
                    count = newOrdersCount,
                    icon = Icons.Default.NewReleases,
                    color = BrandRedPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(SellerScreen.ORDERS) }
                )
                StatusCounterCard(
                    title = "Preparing",
                    count = preparingOrdersCount,
                    icon = Icons.Default.SoupKitchen,
                    color = StatusPreparing,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(SellerScreen.ORDERS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusCounterCard(
                    title = "Ready for Rider",
                    count = readyOrdersCount,
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF5E35B1),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(SellerScreen.ORDERS) }
                )
                StatusCounterCard(
                    title = "Delivered",
                    count = completedOrdersCount,
                    icon = Icons.Default.DoneAll,
                    color = VegGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(SellerScreen.ORDERS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusCounterCard(
                    title = "Cancelled",
                    count = cancelledOrdersCount,
                    icon = Icons.Default.Cancel,
                    color = NonVegRed,
                    modifier = Modifier.weight(0.5f),
                    onClick = { onNavigate(SellerScreen.ORDERS) }
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Menu Management Shortcut
            Card(
                onClick = { onNavigate(SellerScreen.PRODUCTS) },
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(BrandOrangeAccent.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = BrandOrangeAccent)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manage Menu & Dishes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Add dishes, set prices & toggle availability", fontSize = 12.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun StatusCounterCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}
