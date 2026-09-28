package com.example.ui.admin

import androidx.compose.foundation.background
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
import com.example.data.local.entities.RiderProfileEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    customers: List<UserEntity>,
    sellers: List<SellerProfileEntity>,
    riders: List<RiderProfileEntity>,
    orders: List<OrderEntity>,
    revenue: Double,
    onNavigate: (AdminScreen) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingSellers = sellers.count { it.status == "PENDING" }
    val pendingRiders = riders.count { it.status == "PENDING" }
    val newOrders = orders.count { it.orderStatus == "PLACED" }
    val completedOrders = orders.count { it.orderStatus == "DELIVERED" }
    val cancelledOrders = orders.count { it.orderStatus == "CANCELLED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TT Food Administration", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Ajara Service Area • Master Control", fontSize = 11.sp, color = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick, modifier = Modifier.testTag("admin_logout_btn")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = NonVegRed)
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
            // Revenue Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Total Platform Delivered Revenue", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${revenue.toInt()}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Completed Orders: $completedOrders", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                        Text("Cancelled Orders: $cancelledOrders", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pending Approvals Alert
            if (pendingSellers > 0 || pendingRiders > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandOrangeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = BrandOrangeAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pending Admin Approvals", fontWeight = FontWeight.Bold, color = BrandOrangeAccent)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (pendingSellers > 0) {
                            TextButton(onClick = { onNavigate(AdminScreen.SELLERS) }) {
                                Text("• $pendingSellers Restaurant application(s) awaiting approval ->", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                        if (pendingRiders > 0) {
                            TextButton(onClick = { onNavigate(AdminScreen.RIDERS) }) {
                                Text("• $pendingRiders Delivery Rider application(s) awaiting approval ->", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Key Metrics 2x2
            Text("Core Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Total Orders",
                    value = "${orders.size}",
                    sub = "$newOrders New",
                    icon = Icons.Default.Receipt,
                    accentColor = BrandRedPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AdminScreen.ORDERS) }
                )
                AdminMetricCard(
                    title = "Customers",
                    value = "${customers.size}",
                    sub = "Registered",
                    icon = Icons.Default.People,
                    accentColor = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AdminScreen.CUSTOMERS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Restaurants",
                    value = "${sellers.size}",
                    sub = "$pendingSellers Pending",
                    icon = Icons.Default.Storefront,
                    accentColor = BrandOrangeAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AdminScreen.SELLERS) }
                )
                AdminMetricCard(
                    title = "Riders",
                    value = "${riders.size}",
                    sub = "$pendingRiders Pending",
                    icon = Icons.Default.DeliveryDining,
                    accentColor = VegGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AdminScreen.RIDERS) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Management Navigation Grid
            Text("Administration Portals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            AdminNavButton("Orders Management & Live Lifecycle", Icons.Default.Assignment, "admin_nav_orders") { onNavigate(AdminScreen.ORDERS) }
            Spacer(modifier = Modifier.height(8.dp))
            AdminNavButton("Restaurant Approvals & Documents", Icons.Default.Store, "admin_nav_sellers") { onNavigate(AdminScreen.SELLERS) }
            Spacer(modifier = Modifier.height(8.dp))
            AdminNavButton("Rider Approvals & Order Assignment", Icons.Default.TwoWheeler, "admin_nav_riders") { onNavigate(AdminScreen.RIDERS) }
            Spacer(modifier = Modifier.height(8.dp))
            AdminNavButton("Customer Accounts Management", Icons.Default.Group, "admin_nav_customers") { onNavigate(AdminScreen.CUSTOMERS) }
            Spacer(modifier = Modifier.height(8.dp))
            AdminNavButton("Coupons & Discount Engine", Icons.Default.LocalOffer, "admin_nav_coupons") { onNavigate(AdminScreen.COUPONS) }
            Spacer(modifier = Modifier.height(8.dp))
            AdminNavButton("Delivery Charges & Service Area (Ajara)", Icons.Default.Tune, "admin_nav_settings") { onNavigate(AdminScreen.SETTINGS) }
            Spacer(modifier = Modifier.height(8.dp))
            AdminNavButton("Seller & Rider Payout Logs", Icons.Default.Paid, "admin_nav_payouts") { onNavigate(AdminScreen.PAYOUTS) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = TextSecondary)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(sub, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminNavButton(
    title: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BrandRedPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
        }
    }
}
