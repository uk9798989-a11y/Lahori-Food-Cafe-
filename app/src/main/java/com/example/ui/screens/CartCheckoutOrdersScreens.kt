package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderSource
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.example.data.model.PizzaPricingRules
import com.example.data.repository.AuthRepository
import com.example.data.repository.SignInResult
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.ShoppingCartUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CartScreen(
    uiState: ShoppingCartUiState,
    onIncrementQty: (String) -> Unit,
    onDecrementQty: (String) -> Unit,
    onUpdateItemToppings: (CartItem, extraChicken: Boolean, extraCheese: Boolean, extraCheeseSlice: Boolean, selectedSauces: List<String>) -> Unit,
    onClearCart: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onBrowseMenu: () -> Unit
) {
    val cartItems = uiState.items
    val subtotal = uiState.subtotal
    val minOrder = uiState.minDeliveryOrder

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shopping Cart",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (cartItems.isNotEmpty()) {
                        Text(
                            text = "${uiState.totalItemCount} item(s) • Total Rs. ${uiState.totalPrice}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldBright
                        )
                    }
                }
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("Clear", color = GoldBright, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaroonPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your cart is empty", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Explore Lahori Food Cafe's pizzas, burgers, rolls, fried chicken, and 20 value deals.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onBrowseMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Browse Full Menu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val progress = (subtotal.toFloat() / minOrder.coerceAtLeast(1)).coerceIn(0f, 1f)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (subtotal >= minOrder) SuccessGreen.copy(alpha = 0.12f) else GoldBright.copy(alpha = 0.16f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    tint = if (subtotal >= minOrder) SuccessGreen else MaroonPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (subtotal >= minOrder) {
                                        if (uiState.isFreeDelivery) "Minimum Rs. $minOrder reached — FREE Delivery unlocked!"
                                        else "Minimum Rs. $minOrder reached for Delivery!"
                                    } else {
                                        "Add Rs. ${minOrder - subtotal} more for Delivery (Min Rs. $minOrder). Takeaway & Dine-In have no minimum."
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                color = if (subtotal >= minOrder) SuccessGreen else MaroonPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }

                items(cartItems, key = { it.cartKey }) { item ->
                    val pizzaToppingRate = if (item.supportsPizzaCustomizations) {
                        PizzaPricingRules.extraToppingPrice(item.selectedSize)
                    } else null

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FoodImage(
                                    imageKey = item.imageKey,
                                    imageUrl = item.imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    val summary = item.customizationSummary()
                                    if (summary.isNotBlank()) {
                                        Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Rs. ${item.lineTotal}", style = MaterialTheme.typography.titleMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                                    if (item.customizationCharges > 0) {
                                        Text(
                                            text = "Base Rs. ${item.baseLineTotal} + Toppings/Extras Rs. ${item.toppingsLineTotal}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onDecrementQty(item.cartKey) },
                                        modifier = Modifier.testTag("cart_decrement_${item.itemId}")
                                    ) {
                                        Icon(
                                            if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                            contentDescription = "Decrease"
                                        )
                                    }
                                    Text("${item.quantity}", fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { onIncrementQty(item.cartKey) },
                                        modifier = Modifier.testTag("cart_increment_${item.itemId}")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }

                            if (pizzaToppingRate != null) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item {
                                        FilterChip(
                                            selected = item.extraChicken,
                                            onClick = {
                                                onUpdateItemToppings(
                                                    item,
                                                    !item.extraChicken,
                                                    item.extraCheese,
                                                    item.extraCheeseSlice,
                                                    item.selectedSauces
                                                )
                                            },
                                            label = { Text("Extra Chicken (+Rs. $pizzaToppingRate)", style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaroonPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = item.extraCheese,
                                            onClick = {
                                                onUpdateItemToppings(
                                                    item,
                                                    item.extraChicken,
                                                    !item.extraCheese,
                                                    item.extraCheeseSlice,
                                                    item.selectedSauces
                                                )
                                            },
                                            label = { Text("Extra Cheese (+Rs. $pizzaToppingRate)", style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaroonPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                    items(PizzaPricingRules.availableSauces) { sauce ->
                                        val selected = sauce in item.selectedSauces
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                val nextSauces = if (selected) {
                                                    item.selectedSauces - sauce
                                                } else {
                                                    item.selectedSauces + sauce
                                                }
                                                onUpdateItemToppings(
                                                    item,
                                                    item.extraChicken,
                                                    item.extraCheese,
                                                    item.extraCheeseSlice,
                                                    nextSauces
                                                )
                                            },
                                            label = { Text("$sauce (+Rs. ${PizzaPricingRules.SAUCE_PRICE})", style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaroonPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            } else if (item.supportsExtraCheeseSlice) {
                                FilterChip(
                                    selected = item.extraCheeseSlice,
                                    onClick = {
                                        onUpdateItemToppings(
                                            item,
                                            item.extraChicken,
                                            item.extraCheese,
                                            !item.extraCheeseSlice,
                                            item.selectedSauces
                                        )
                                    },
                                    label = {
                                        Text(
                                            "Extra Cheese Slice (+Rs. ${PizzaPricingRules.BURGER_CHEESE_SLICE_PRICE})",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaroonPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Items Base Total", style = MaterialTheme.typography.bodyMedium)
                        Text("Rs. ${uiState.baseItemsSubtotal}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    if (uiState.toppingsAndExtrasTotal > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Toppings & Extras", style = MaterialTheme.typography.bodyMedium, color = MaroonPrimary)
                            Text("+ Rs. ${uiState.toppingsAndExtrasTotal}", style = MaterialTheme.typography.bodyMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Rs. ${uiState.subtotal}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Charges", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (uiState.deliveryFee == 0) "FREE" else "Rs. ${uiState.deliveryFee}",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Rs. ${uiState.totalPrice}", style = MaterialTheme.typography.headlineMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Button(
                        onClick = onProceedToCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_checkout_button")
                    ) {
                        Text("Proceed to Checkout (App or WhatsApp)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersScreen(
    isSignedIn: Boolean,
    orders: List<Order>,
    onCancelOrder: (String) -> Unit,
    onReorder: (Order) -> Unit,
    onGoToCart: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Order Tracking & History", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Live updates for Direct App Orders & WhatsApp Orders", style = MaterialTheme.typography.bodySmall, color = GoldBright)
            }
        }

        if (!isSignedIn) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(64.dp))
                    Text("Sign in to Track Orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Sign in with Google to view your persistent order history, live kitchen progress, and one-tap reorder.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            val act = context as? Activity ?: return@Button
                            scope.launch {
                                if (AuthRepository.signInWithGoogleInteractive(act) is SignInResult.Success) {
                                    onSignedIn()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaroonPrimary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No orders yet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Your Direct App Orders and recorded WhatsApp Orders will appear here.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    val isWhatsApp = order.orderSource == OrderSource.WHATSAPP.firestoreValue
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(order.orderCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    val dateStr = order.createdAt?.toDate()?.let { formatter.format(it) } ?: "Just now"
                                    Text("$dateStr • ${OrderType.fromString(order.orderType).label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill(order.status)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BadgePill(
                                    text = if (isWhatsApp) "WHATSAPP ORDER" else "DIRECT APP ORDER",
                                    bg = if (isWhatsApp) WhatsAppGreen.copy(alpha = 0.16f) else GoldBright.copy(alpha = 0.25f),
                                    fg = if (isWhatsApp) WhatsAppGreen else CharcoalBlack
                                )
                            }

                            if (order.status == OrderStatuses.PENDING_WHATSAPP) {
                                Text(
                                    "Waiting for Lahori Food Cafe to confirm your WhatsApp message.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WhatsAppGreen
                                )
                            } else if (order.status != OrderStatuses.CANCELLED) {
                                OrderTrackingStepper(currentStatus = order.status)
                            }

                            HorizontalDivider()

                            order.items.forEach { item ->
                                val sizeStr = if (item.selectedSize.isNotBlank()) " (${item.selectedSize})" else ""
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.quantity}x ${item.name}$sizeStr", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("Rs. ${item.lineTotal}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total: Rs. ${order.total}", style = MaterialTheme.typography.titleMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (order.status in listOf(OrderStatuses.ORDER_RECEIVED, OrderStatuses.PENDING_WHATSAPP)) {
                                        OutlinedButton(
                                            onClick = { onCancelOrder(order.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            onReorder(order)
                                            onGoToCart()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldBright, contentColor = CharcoalBlack)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reorder", fontWeight = FontWeight.Bold)
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

@Composable
private fun OrderTrackingStepper(currentStatus: String) {
    val steps = OrderStatuses.TRACKING_STEPS
    val currentIndex = steps.indexOf(currentStatus).coerceAtLeast(0)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(steps.size) { idx ->
            val reached = idx <= currentIndex
            Surface(
                color = if (reached) SuccessGreen.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = if (idx == currentIndex) BorderStroke(1.dp, SuccessGreen) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (reached) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = steps[idx],
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                        color = if (reached) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
