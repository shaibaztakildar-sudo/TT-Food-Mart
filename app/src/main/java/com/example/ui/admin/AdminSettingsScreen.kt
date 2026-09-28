package com.example.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AdminSettingsEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    settings: AdminSettingsEntity?,
    onSaveSettings: (AdminSettingsEntity) -> Unit,
    onBackClick: () -> Unit,
    successMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var fixedFeeStr by remember(settings) { mutableStateOf(settings?.fixedDeliveryFee?.toInt()?.toString() ?: "30") }
    var freeThresholdStr by remember(settings) { mutableStateOf(settings?.freeDeliveryThreshold?.toInt()?.toString() ?: "499") }
    var commissionStr by remember(settings) { mutableStateOf(settings?.platformCommissionPercent?.toInt()?.toString() ?: "10") }
    var serviceAreaName by remember(settings) { mutableStateOf(settings?.serviceAreaName ?: "Ajara, Maharashtra") }
    var servicePincodes by remember(settings) { mutableStateOf(settings?.servicePincodes ?: "416505,416506,416502") }
    var isCodEnabled by remember(settings) { mutableStateOf(settings?.isCodEnabled ?: true) }
    var isOnlineEnabled by remember(settings) { mutableStateOf(settings?.isOnlinePaymentEnabled ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Platform & Delivery Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("admin_settings_back")) {
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
            if (!successMessage.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = VegGreenLight), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Text(successMessage, color = VegGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }

            // 1. Service Area Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationCity, contentDescription = null, tint = BrandRedPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("1. Service Area Restrictions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Orders outside this configured service area and pincodes are strictly rejected during address validation.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = serviceAreaName,
                        onValueChange = { serviceAreaName = it },
                        label = { Text("Primary City & Region") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = servicePincodes,
                        onValueChange = { servicePincodes = it },
                        label = { Text("Eligible Pincodes (comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Delivery Charges
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = BrandOrangeAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("2. Configurable Delivery Fee", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fixedFeeStr,
                        onValueChange = { fixedFeeStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Standard Delivery Fee (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = freeThresholdStr,
                        onValueChange = { freeThresholdStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Free Delivery Threshold (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Payment Methods & Commission
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = VegGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("3. Payment Gateways & Commission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Cash on Delivery (COD)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Allows customers to pay in cash", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = isCodEnabled, onCheckedChange = { isCodEnabled = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Online Payments", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("UPI / Cards gateway processing", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = isOnlineEnabled, onCheckedChange = { isOnlineEnabled = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = commissionStr,
                        onValueChange = { commissionStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Platform Commission on Orders (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("%") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val fixed = fixedFeeStr.toDoubleOrNull() ?: 30.0
                    val freeT = freeThresholdStr.toDoubleOrNull() ?: 499.0
                    val comm = commissionStr.toDoubleOrNull() ?: 10.0
                    val updated = AdminSettingsEntity(
                        id = 1,
                        fixedDeliveryFee = fixed,
                        freeDeliveryThreshold = freeT,
                        platformCommissionPercent = comm,
                        serviceAreaName = serviceAreaName.trim(),
                        servicePincodes = servicePincodes.trim(),
                        isCodEnabled = isCodEnabled,
                        isOnlinePaymentEnabled = isOnlineEnabled,
                        isSmsGatewayConfigured = true,
                        isPaymentGatewayConfigured = true
                    )
                    onSaveSettings(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_btn")
            ) {
                Text("Save Platform Settings", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
