package com.example.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.AddressEntity
import com.example.data.local.entities.AdminSettingsEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.FoodDeliveryRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCheckoutScreen(
    customer: UserEntity,
    restaurant: SellerProfileEntity?,
    cart: Map<String, FoodDeliveryRepository.CartItem>,
    addresses: List<AddressEntity>,
    selectedAddress: AddressEntity?,
    subtotal: Double,
    deliveryFee: Double,
    discount: Double,
    totalAmount: Double,
    adminSettings: AdminSettingsEntity?,
    onSelectAddress: (AddressEntity) -> Unit,
    onAddNewAddress: (
        title: String, name: String, phone: String, street: String,
        landmark: String, area: String, city: String, pincode: String, isDefault: Boolean
    ) -> Unit,
    onPlaceOrder: (customer: UserEntity, paymentMethod: String, instructions: String) -> Unit,
    onBackClick: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var instructions by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("ONLINE") }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showOnlinePaymentGatewayModal by remember { mutableStateOf(false) }
    var paymentProcessing by remember { mutableStateOf(false) }

    val isCodAvailable = adminSettings?.isCodEnabled ?: true
    val isOnlineAvailable = adminSettings?.isOnlinePaymentEnabled ?: true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("checkout_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceCard,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Amount", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = "₹${totalAmount.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        Button(
                            onClick = {
                                if (selectedPaymentMethod == "ONLINE") {
                                    showOnlinePaymentGatewayModal = true
                                } else {
                                    onPlaceOrder(customer, "COD", instructions)
                                }
                            },
                            enabled = selectedAddress != null && cart.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("confirm_order_button")
                        ) {
                            Text(
                                text = if (selectedPaymentMethod == "ONLINE") "Pay & Place Order" else "Place COD Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceBackground)
                .padding(padding)
        ) {
            // Error banner
            if (!errorMessage.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NonVegRedLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = NonVegRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Customer Summary
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Customer Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${customer.name} • ${customer.phone}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(customer.email, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            // Delivery Address Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delivery Address", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            TextButton(
                                onClick = { showAddAddressDialog = true },
                                modifier = Modifier.testTag("add_new_address_btn")
                            ) {
                                Text("+ Add New", fontWeight = FontWeight.Bold, color = BrandRedPrimary)
                            }
                        }

                        if (addresses.isEmpty()) {
                            Text(
                                text = "No address saved. Please add your delivery address in Ajara.",
                                color = NonVegRed,
                                fontSize = 12.sp
                            )
                        } else {
                            addresses.forEach { addr ->
                                val isSelected = selectedAddress?.id == addr.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            if (isSelected) BrandRedPrimary.copy(alpha = 0.08f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectAddress(addr) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectAddress(addr) },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandRedPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${addr.title}: ${addr.receiverName} (${addr.receiverPhone})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${addr.streetAddress}, ${addr.area}, ${addr.city} - ${addr.pincode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Customer Delivery Instructions
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivery Instructions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            placeholder = { Text("e.g. Leave with security / Ring bell twice / Landmark...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("instructions_input")
                        )
                    }
                }
            }

            // Payment Method Selection
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Method", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (isOnlineAvailable) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = "ONLINE" }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "ONLINE",
                                    onClick = { selectedPaymentMethod = "ONLINE" },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandRedPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Online Payment (UPI, Cards, NetBanking)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Instant confirmation & secure checkout", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        if (isCodAvailable) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = "COD" }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "COD",
                                    onClick = { selectedPaymentMethod = "COD" },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandRedPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cash on Delivery (COD)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Pay cash to delivery rider when food arrives", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Summary breakdown
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Order Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        cart.values.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.quantity}x ${item.product.name}", fontSize = 12.sp, color = TextPrimary)
                                Text("₹${(item.product.price * item.quantity).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${subtotal.toInt()}", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", fontSize = 12.sp, color = TextSecondary)
                            Text(if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}", fontSize = 12.sp)
                        }
                        if (discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", fontSize = 12.sp, color = VegGreen)
                                Text("-₹${discount.toInt()}", fontSize = 12.sp, color = VegGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Final Total", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("₹${totalAmount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = BrandRedPrimary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Add Address Dialog
    if (showAddAddressDialog) {
        AddAddressDialog(
            customerPhone = customer.phone,
            customerName = customer.name,
            onDismiss = { showAddAddressDialog = false },
            onSave = { title, name, phone, street, landmark, area, city, pincode, isDefault ->
                onAddNewAddress(title, name, phone, street, landmark, area, city, pincode, isDefault)
                showAddAddressDialog = false
            }
        )
    }

    // Online Payment Gateway Verification Modal
    if (showOnlinePaymentGatewayModal) {
        Dialog(onDismissRequest = { if (!paymentProcessing) showOnlinePaymentGatewayModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = VegGreen,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Secure Payment Gateway",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Payable Amount: ₹${totalAmount.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRedPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select Payment App or UPI ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedCard(
                        onClick = {
                            paymentProcessing = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BrandOrangeAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Pay via UPI / GPay / PhonePe", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Instant gateway verification", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (paymentProcessing) {
                        CircularProgressIndicator(color = VegGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Verifying transaction with bank...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(1200)
                            showOnlinePaymentGatewayModal = false
                            paymentProcessing = false
                            onPlaceOrder(customer, "ONLINE", instructions)
                        }
                    } else {
                        Button(
                            onClick = {
                                paymentProcessing = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Authorize ₹${totalAmount.toInt()}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAddressDialog(
    customerPhone: String,
    customerName: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String, name: String, phone: String, street: String,
        landmark: String, area: String, city: String, pincode: String, isDefault: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("Home") }
    var name by remember { mutableStateOf(customerName) }
    var phone by remember { mutableStateOf(customerPhone) }
    var street by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Ajara Town") }
    var city by remember { mutableStateOf("Ajara") }
    var pincode by remember { mutableStateOf("416505") }
    var isDefault by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
            ) {
                Text(
                    text = "Add Delivery Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configured service area: Ajara, Maharashtra",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Work", "Other").forEach { t ->
                        FilterChip(
                            selected = title == t,
                            onClick = { title = t },
                            label = { Text(t) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("House / Flat / Street *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { if (it.length <= 6) pincode = it.filter { c -> c.isDigit() } },
                        label = { Text("Pincode *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (street.isNotBlank() && pincode.isNotBlank()) {
                                onSave(title, name, phone, street, landmark, area, city, pincode, isDefault)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary)
                    ) {
                        Text("Save Address")
                    }
                }
            }
        }
    }
}
