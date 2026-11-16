package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CafeConstants
import com.example.data.model.OrderType
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MaroonDark
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ShoppingCartUiState

@Composable
internal fun CheckoutHeaderBar(
    itemCount: Int,
    totalPrice: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(MaroonDark, MaroonPrimary)))
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .testTag("checkout_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to cart",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${CafeConstants.SHORT_NAME} • Review & Place Order",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (itemCount > 0) {
                Surface(
                    color = GoldBright,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$itemCount • Rs. $totalPrice",
                        style = MaterialTheme.typography.labelLarge,
                        color = CharcoalBlack,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun FulfillmentModeSelector(
    selectedType: OrderType,
    isDeliveryEnabled: Boolean,
    minDeliveryOrder: Int,
    isFreeDelivery: Boolean,
    onSelect: (OrderType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Delivery or Pickup",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FulfillmentOptionCard(
                title = "Delivery",
                subtitle = if (!isDeliveryEnabled) "Paused" else if (isFreeDelivery) "Free • Rs.$minDeliveryOrder+" else "Rs.$minDeliveryOrder min",
                icon = Icons.Default.DeliveryDining,
                selected = selectedType == OrderType.DELIVERY,
                enabled = isDeliveryEnabled,
                onClick = { onSelect(OrderType.DELIVERY) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_type_delivery")
            )
            FulfillmentOptionCard(
                title = "Pickup",
                subtitle = "No minimum",
                icon = Icons.Default.Storefront,
                selected = selectedType == OrderType.TAKEAWAY,
                enabled = true,
                onClick = { onSelect(OrderType.TAKEAWAY) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_type_takeaway")
            )
            FulfillmentOptionCard(
                title = "Dine-In",
                subtitle = "Table service",
                icon = Icons.Default.Restaurant,
                selected = selectedType == OrderType.DINE_IN,
                enabled = true,
                onClick = { onSelect(OrderType.DINE_IN) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_type_dine_in")
            )
        }
    }
}

@Composable
private fun FulfillmentOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            selected -> MaroonPrimary
            else -> MaterialTheme.colorScheme.surface
        },
        label = "fulfillment_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> GoldBright
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        },
        label = "fulfillment_border"
    )
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        selected -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) GoldBright else if (enabled) MaroonPrimary else contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun DeliveryMinimumStatusBanner(
    subtotal: Int,
    minDeliveryOrder: Int,
    meetsMinDelivery: Boolean,
    amountToMinDelivery: Int,
    isFreeDelivery: Boolean,
    onSwitchToPickup: () -> Unit
) {
    if (meetsMinDelivery) {
        Surface(
            color = SuccessGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFreeDelivery) "Rs. $minDeliveryOrder min met • FREE Delivery applied" else "Rs. $minDeliveryOrder minimum delivery order met",
                    style = MaterialTheme.typography.labelMedium,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        val progress = (subtotal.toFloat() / minDeliveryOrder.coerceAtLeast(1)).coerceIn(0f, 1f)
        Surface(
            color = WarningAmber.copy(alpha = 0.12f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Rs. $amountToMinDelivery more for Delivery (Min Rs. $minDeliveryOrder).",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    color = WarningAmber,
                    trackColor = WarningAmber.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pickup has no minimum limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onSwitchToPickup) {
                        Text("Switch to Pickup", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun OrderSummaryReceiptCard(uiState: ShoppingCartUiState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                BadgePill(
                    text = "${uiState.totalItemCount} unit(s)",
                    bg = GoldBright.copy(alpha = 0.25f),
                    fg = CharcoalBlack
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            uiState.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodImage(
                        imageKey = item.imageKey,
                        imageUrl = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.quantity}x ${item.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val custom = item.customizationSummary()
                        if (custom.isNotBlank()) {
                            Text(
                                text = custom,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Rs. ${item.lineTotal}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                        if (item.customizationCharges > 0) {
                            Text(
                                text = "+Rs. ${item.toppingsLineTotal} extras",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            SummaryPriceRow("Items Base", "Rs. ${uiState.baseItemsSubtotal}")
            if (uiState.toppingsAndExtrasTotal > 0) {
                SummaryPriceRow("Toppings & Extras", "+ Rs. ${uiState.toppingsAndExtrasTotal}", valueColor = MaroonPrimary)
            }
            SummaryPriceRow("Subtotal", "Rs. ${uiState.subtotal}", isBold = true)
            SummaryPriceRow(
                label = if (uiState.orderType == OrderType.DELIVERY) "Delivery Charges" else "${uiState.orderType.label} Charges",
                value = if (uiState.deliveryFee == 0) "FREE" else "Rs. ${uiState.deliveryFee}",
                valueColor = SuccessGreen,
                isBold = true
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Final Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "Rs. ${uiState.totalPrice}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaroonPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun SummaryPriceRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
internal fun CheckoutBottomBar(
    uiState: ShoppingCartUiState,
    isSignedIn: Boolean,
    onPlaceDirectOrder: () -> Unit,
    onWhatsAppOrder: () -> Unit
) {
    Surface(
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Payable Total (${uiState.orderType.label})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "WhatsApp Ref: ${uiState.whatsAppOrderRef}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Rs. ${uiState.totalPrice}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaroonPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (isSignedIn) {
                Button(
                    onClick = onPlaceDirectOrder,
                    enabled = !uiState.isBusy && uiState.isAcceptingOrders && uiState.meetsMinDelivery && uiState.items.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaroonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_direct_order_button")
                ) {
                    if (uiState.isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Submit Direct App Order • Rs. ${uiState.totalPrice}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            WhatsAppOrderButton(
                text = "WhatsApp Order • ${CafeConstants.PHONE_DISPLAY}",
                onClick = onWhatsAppOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("checkout_whatsapp_order_button")
            )
        }
    }
}

@Composable
internal fun EmptyCheckoutState(
    onBrowseMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaroonPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add pizzas, burgers, rolls, fried chicken, or value deals before checking out.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onBrowseMenu,
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Browse Full Menu", fontWeight = FontWeight.Bold)
            }
        }
    }
}
