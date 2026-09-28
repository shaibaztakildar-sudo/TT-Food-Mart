package com.example.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entities.SellerProfileEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSellersScreen(
    sellers: List<SellerProfileEntity>,
    onApproveSeller: (String) -> Unit,
    onRejectSeller: (String) -> Unit,
    onSuspendSeller: (String) -> Unit,
    onActivateSeller: (String) -> Unit,
    onRecordPayout: (seller: SellerProfileEntity, amount: Double, notes: String) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedSellerForDoc by remember { mutableStateOf<SellerProfileEntity?>(null) }
    var selectedSellerForPayout by remember { mutableStateOf<SellerProfileEntity?>(null) }
    var payoutAmountStr by remember { mutableStateOf("") }
    var payoutNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurant Partners (${sellers.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("admin_sellers_back")) {
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

            if (sellers.isEmpty()) {
                EmptyStateView(
                    title = "No restaurant applications",
                    message = "When restaurants apply through the Seller portal, their applications and documents will appear here for review.",
                    icon = Icons.Default.Storefront
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sellers) { seller ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("seller_card_${seller.sellerId}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = seller.restaurantName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Owner: ${seller.ownerName} • ${seller.phone}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    StatusChip(status = seller.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Address: ${seller.address}", fontSize = 12.sp, color = TextSecondary)
                                Text(text = "Email: ${seller.email}", fontSize = 12.sp, color = TextSecondary)
                                Text(text = "Bank: ${seller.bankName} (A/C: ${seller.bankAccount}, IFSC: ${seller.bankIfsc})", fontSize = 12.sp, color = TextSecondary)
                                Text(text = "PAN: ${seller.panNumber}", fontSize = 12.sp, color = TextSecondary)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Earnings: ₹${seller.earnings.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Pending: ₹${seller.pendingPayout.toInt()}", color = BrandOrangeAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Paid: ₹${seller.paidPayout.toInt()}", color = VegGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!seller.documentProofUri.isNullOrBlank() || !seller.imageUri.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { selectedSellerForDoc = seller },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Documents", fontSize = 11.sp)
                                        }
                                    }

                                    if (seller.status == "PENDING") {
                                        Button(
                                            onClick = { onApproveSeller(seller.sellerId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                            modifier = Modifier.weight(1f).testTag("approve_seller_${seller.sellerId}")
                                        ) {
                                            Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onRejectSeller(seller.sellerId) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NonVegRed),
                                            modifier = Modifier.weight(1f).testTag("reject_seller_${seller.sellerId}")
                                        ) {
                                            Text("Reject", fontSize = 11.sp)
                                        }
                                    } else if (seller.status == "APPROVED") {
                                        if (seller.pendingPayout > 0) {
                                            Button(
                                                onClick = {
                                                    selectedSellerForPayout = seller
                                                    payoutAmountStr = seller.pendingPayout.toInt().toString()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrangeAccent),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Pay ₹${seller.pendingPayout.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { onSuspendSeller(seller.sellerId) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NonVegRed),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Suspend", fontSize = 11.sp)
                                        }
                                    } else if (seller.status == "SUSPENDED" || seller.status == "REJECTED") {
                                        Button(
                                            onClick = { onActivateSeller(seller.sellerId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reactivate", fontSize = 11.sp)
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

    // Documents Viewer Dialog
    if (selectedSellerForDoc != null) {
        Dialog(onDismissRequest = { selectedSellerForDoc = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Uploaded Business Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    selectedSellerForDoc?.documentProofUri?.let { uri ->
                        Text("Business/FSSAI Proof:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        AsyncImage(
                            model = uri,
                            contentDescription = "Document",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    selectedSellerForDoc?.imageUri?.let { uri ->
                        Text("Restaurant Profile Banner:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        AsyncImage(
                            model = uri,
                            contentDescription = "Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { selectedSellerForDoc = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Payout Dialog
    if (selectedSellerForPayout != null) {
        Dialog(onDismissRequest = { selectedSellerForPayout = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Record Seller Payout", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Beneficiary: ${selectedSellerForPayout?.restaurantName} (${selectedSellerForPayout?.bankAccount})", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = payoutAmountStr,
                        onValueChange = { payoutAmountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Payout Amount (₹)") },
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = payoutNotes,
                        onValueChange = { payoutNotes = it },
                        label = { Text("Notes / Bank Ref ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { selectedSellerForPayout = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = payoutAmountStr.toDoubleOrNull() ?: 0.0
                                selectedSellerForPayout?.let { onRecordPayout(it, amount, payoutNotes) }
                                selectedSellerForPayout = null
                                payoutAmountStr = ""
                                payoutNotes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VegGreen)
                        ) {
                            Text("Record Payout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg) = when (status) {
        "APPROVED" -> VegGreenLight to VegGreen
        "PENDING" -> Color(0xFFFFF3E0) to StatusPending
        "SUSPENDED", "REJECTED" -> NonVegRedLight to NonVegRed
        else -> Color(0xFFEEEEEE) to Color.DarkGray
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = status,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
