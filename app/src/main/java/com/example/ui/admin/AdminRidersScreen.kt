package com.example.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TwoWheeler
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
import com.example.data.local.entities.RiderProfileEntity
import com.example.ui.common.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRidersScreen(
    riders: List<RiderProfileEntity>,
    onApproveRider: (String) -> Unit,
    onRejectRider: (String) -> Unit,
    onToggleActive: (riderId: String, currentStatus: String) -> Unit,
    onRecordPayout: (rider: RiderProfileEntity, amount: Double, notes: String) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedRiderForLicense by remember { mutableStateOf<RiderProfileEntity?>(null) }
    var selectedRiderForPayout by remember { mutableStateOf<RiderProfileEntity?>(null) }
    var payoutAmountStr by remember { mutableStateOf("") }
    var payoutNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Riders (${riders.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("admin_riders_back")) {
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

            if (riders.isEmpty()) {
                EmptyStateView(
                    title = "No rider applications",
                    message = "Delivery riders registering through the Rider portal will appear here for admin approval.",
                    icon = Icons.Default.TwoWheeler
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(riders) { rider ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("rider_card_${rider.riderId}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rider.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Phone: ${rider.phone} • Email: ${rider.email}", fontSize = 12.sp, color = TextSecondary)
                                        Text("Vehicle: ${rider.vehicleNumber}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF0288D1))
                                    }
                                    Surface(
                                        color = if (rider.status == "APPROVED" || rider.status == "ACTIVE") VegGreenLight else Color(0xFFFFF3E0),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = rider.status,
                                            color = if (rider.status == "APPROVED" || rider.status == "ACTIVE") VegGreen else StatusPending,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Deliveries: ${rider.totalDeliveries}", fontSize = 13.sp)
                                    Text("Total Earned: ₹${rider.earnings.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Pending: ₹${rider.pendingPayout.toInt()}", color = BrandOrangeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!rider.licenseUri.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { selectedRiderForLicense = rider },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("View License", fontSize = 11.sp)
                                        }
                                    }

                                    if (rider.status == "PENDING") {
                                        Button(
                                            onClick = { onApproveRider(rider.riderId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                            modifier = Modifier.weight(1f).testTag("approve_rider_${rider.riderId}")
                                        ) {
                                            Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onRejectRider(rider.riderId) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NonVegRed),
                                            modifier = Modifier.weight(1f).testTag("reject_rider_${rider.riderId}")
                                        ) {
                                            Text("Reject", fontSize = 11.sp)
                                        }
                                    } else {
                                        if (rider.pendingPayout > 0) {
                                            Button(
                                                onClick = {
                                                    selectedRiderForPayout = rider
                                                    payoutAmountStr = rider.pendingPayout.toInt().toString()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrangeAccent),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Pay ₹${rider.pendingPayout.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { onToggleActive(rider.riderId, rider.status) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (rider.status == "ACTIVE" || rider.status == "APPROVED") "Deactivate" else "Activate", fontSize = 11.sp)
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

    // License Photo Dialog
    if (selectedRiderForLicense != null) {
        Dialog(onDismissRequest = { selectedRiderForLicense = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rider Driving License", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    selectedRiderForLicense?.licenseUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "License",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { selectedRiderForLicense = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Payout Dialog
    if (selectedRiderForPayout != null) {
        Dialog(onDismissRequest = { selectedRiderForPayout = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Record Rider Payout", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Rider: ${selectedRiderForPayout?.name} (${selectedRiderForPayout?.phone})", fontSize = 12.sp, color = TextSecondary)
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
                        label = { Text("Notes / UPI Ref ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { selectedRiderForPayout = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = payoutAmountStr.toDoubleOrNull() ?: 0.0
                                selectedRiderForPayout?.let { onRecordPayout(it, amount, payoutNotes) }
                                selectedRiderForPayout = null
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
