package com.example.ui.common

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThermalStickerDialog(
    order: OrderEntity,
    items: List<OrderItemEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp)
                .testTag("thermal_sticker_dialog"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thermal Sticker Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_sticker_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Thermal Paper Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFFFDF8), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 380.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "TT FOOD DELIVERY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "FAST • FRESH • LOCAL",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ORDER ID: ${order.id}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Date: $formattedDate",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Restaurant: ${order.restaurantName}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Customer Section
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "CUSTOMER DETAILS:",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Name: ${order.customerName}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Phone: ${order.customerPhone}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Address: ${order.deliveryAddress}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                            if (order.customerInstructions.isNotBlank()) {
                                Text(
                                    text = "Notes: ${order.customerInstructions}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Items Section
                        Text(
                            text = "ORDER ITEMS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ITEM", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text("QTY x PRICE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text("TOTAL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                        }

                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val vegPrefix = if (item.isVeg) "[V]" else "[NV]"
                                Text(
                                    text = "$vegPrefix ${item.productName}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                    text = "${item.quantity} x ₹${item.price.toInt()}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = "₹${item.itemTotal.toInt()}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Bill summary
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                            Text("₹${order.subtotal.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                            Text("₹${order.deliveryCharge.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                        }
                        if (order.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                                Text("-₹${order.discount.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "TOTAL BILL:",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            Text(
                                "₹${order.totalAmount.toInt()}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Payment Info
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Mode: ${order.paymentMethod}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                            Text("Status: ${order.paymentStatus}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        // Barcode visual simulation
                        Text(
                            text = "|||||| | |||| ||| ||||||| || ||||||",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "* ${order.id} *",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Thank you for choosing TT Food!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action buttons: Print Sticker & Share Sticker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareStickerAsText(context, order, items, formattedDate)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_sticker_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Sticker")
                    }

                    Button(
                        onClick = {
                            printThermalReceipt(context, order, items, formattedDate)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_sticker_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Sticker")
                    }
                }
            }
        }
    }
}

@Composable
private fun DottedDivider() {
    Text(
        text = "--------------------------------",
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = Color.DarkGray
    )
}

private fun shareStickerAsText(
    context: Context,
    order: OrderEntity,
    items: List<OrderItemEntity>,
    formattedDate: String
) {
    val sb = StringBuilder()
    sb.appendLine("================================")
    sb.appendLine("      TT FOOD DELIVERY          ")
    sb.appendLine("    FAST • FRESH • LOCAL        ")
    sb.appendLine("================================")
    sb.appendLine("ORDER ID: ${order.id}")
    sb.appendLine("Date: $formattedDate")
    sb.appendLine("Restaurant: ${order.restaurantName}")
    sb.appendLine("--------------------------------")
    sb.appendLine("CUSTOMER DETAILS:")
    sb.appendLine("Name: ${order.customerName}")
    sb.appendLine("Phone: ${order.customerPhone}")
    sb.appendLine("Address: ${order.deliveryAddress}")
    if (order.customerInstructions.isNotBlank()) {
        sb.appendLine("Note: ${order.customerInstructions}")
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("ITEMS:")
    items.forEach { item ->
        val veg = if (item.isVeg) "[V]" else "[NV]"
        sb.appendLine("$veg ${item.productName} x ${item.quantity} = ₹${item.itemTotal.toInt()}")
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("Subtotal: ₹${order.subtotal.toInt()}")
    sb.appendLine("Delivery Fee: ₹${order.deliveryCharge.toInt()}")
    if (order.discount > 0) {
        sb.appendLine("Discount: -₹${order.discount.toInt()}")
    }
    sb.appendLine("TOTAL BILL: ₹${order.totalAmount.toInt()}")
    sb.appendLine("Payment: ${order.paymentMethod} (${order.paymentStatus})")
    sb.appendLine("================================")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Order Sticker"))
}

private fun printThermalReceipt(
    context: Context,
    order: OrderEntity,
    items: List<OrderItemEntity>,
    formattedDate: String
) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val itemsHtml = items.joinToString("") { item ->
            val veg = if (item.isVeg) "(V)" else "(NV)"
            "<tr><td>$veg ${item.productName}</td><td align='center'>${item.quantity}</td><td align='right'>₹${item.itemTotal.toInt()}</td></tr>"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: monospace; font-size: 12px; width: 280px; margin: 0 auto; padding: 10px; }
                    .center { text-align: center; }
                    .bold { font-weight: bold; }
                    .line { border-top: 1px dashed #000; margin: 6px 0; }
                    table { width: 100%; border-collapse: collapse; }
                </style>
            </head>
            <body>
                <div class='center bold' style='font-size:16px;'>TT FOOD DELIVERY</div>
                <div class='center'>FAST • FRESH • LOCAL</div>
                <div class='line'></div>
                <div><b>ORDER ID: ${order.id}</b></div>
                <div>Date: $formattedDate</div>
                <div>Restaurant: ${order.restaurantName}</div>
                <div class='line'></div>
                <div class='bold'>CUSTOMER:</div>
                <div>${order.customerName} (${order.customerPhone})</div>
                <div>${order.deliveryAddress}</div>
                <div class='line'></div>
                <table>
                    <tr><th align='left'>Item</th><th>Qty</th><th align='right'>Total</th></tr>
                    $itemsHtml
                </table>
                <div class='line'></div>
                <table>
                    <tr><td>Subtotal</td><td align='right'>₹${order.subtotal.toInt()}</td></tr>
                    <tr><td>Delivery Charge</td><td align='right'>₹${order.deliveryCharge.toInt()}</td></tr>
                    ${if (order.discount > 0) "<tr><td>Discount</td><td align='right'>-₹${order.discount.toInt()}</td></tr>" else ""}
                    <tr class='bold' style='font-size:14px;'><td>TOTAL BILL</td><td align='right'>₹${order.totalAmount.toInt()}</td></tr>
                </table>
                <div class='line'></div>
                <div>Payment: ${order.paymentMethod} [${order.paymentStatus}]</div>
                <div class='center' style='margin-top:10px;'>* Thank You *</div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter("TT_Order_${order.id}")
                printManager.print("TT_Order_${order.id}", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
