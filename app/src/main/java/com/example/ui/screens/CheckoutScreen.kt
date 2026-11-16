package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CafeConstants
import com.example.data.model.OrderType
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.SignInResult
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.CartViewModel
import com.example.ui.viewmodel.OrderSubmissionState
import com.example.ui.viewmodel.ShoppingCartUiState
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    isSignedIn: Boolean,
    userProfile: UserProfile?,
    onBack: () -> Unit,
    onDirectOrderSuccess: () -> Unit,
    onBrowseMenu: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

    CheckoutScreenContent(
        uiState = uiState,
        isSignedIn = isSignedIn,
        savedAddresses = userProfile?.savedAddresses.orEmpty(),
        onCustomerNameChange = cartViewModel::setCustomerName,
        onCustomerPhoneChange = cartViewModel::setCustomerPhone,
        onOrderTypeChange = { type -> cartViewModel.setOrderType(type) },
        onDeliveryAddressChange = cartViewModel::setDeliveryAddress,
        onSpecialInstructionsChange = cartViewModel::setSpecialInstructions,
        onPlaceDirectOrder = {
            cartViewModel.submitDirectAppOrder { onDirectOrderSuccess() }
        },
        onWhatsAppOrder = { cartViewModel.launchWhatsAppOrder(context) },
        onDismissSubmissionBanner = cartViewModel::dismissSubmissionBanner,
        onBack = onBack,
        onBrowseMenu = onBrowseMenu,
        onSignedIn = onSignedIn
    )
}

@Composable
fun CheckoutScreenContent(
    uiState: ShoppingCartUiState,
    isSignedIn: Boolean,
    savedAddresses: List<String>,
    onCustomerNameChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onOrderTypeChange: (OrderType) -> Unit,
    onDeliveryAddressChange: (String) -> Unit,
    onSpecialInstructionsChange: (String) -> Unit,
    onPlaceDirectOrder: () -> Unit,
    onWhatsAppOrder: () -> Unit,
    onDismissSubmissionBanner: () -> Unit,
    onBack: () -> Unit,
    onBrowseMenu: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var authError by remember { mutableStateOf<String?>(null) }

    val cartItems = uiState.items
    val pendingBanner = (uiState.submissionState as? OrderSubmissionState.WhatsAppPending)?.bannerMessage
    val submissionError = (uiState.submissionState as? OrderSubmissionState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CheckoutHeaderBar(
            itemCount = uiState.totalItemCount,
            totalPrice = uiState.totalPrice,
            onBack = onBack
        )

        if (cartItems.isEmpty()) {
            EmptyCheckoutState(
                onBrowseMenu = onBrowseMenu,
                modifier = Modifier.weight(1f)
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.isAcceptingOrders) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.closureNotice.ifBlank {
                                "Lahori Food Cafe is temporarily closed for online orders."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = submissionError != null) {
                submissionError?.let { errorMsg ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onDismissSubmissionBanner) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss error", tint = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = pendingBanner != null) {
                pendingBanner?.let { banner ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WhatsAppGreen.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, WhatsAppGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = WhatsAppGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pending WhatsApp Confirmation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onDismissSubmissionBanner) {
                                    Text("Dismiss", color = WhatsAppGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(banner, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Section 1: Delivery / Pickup Selection
            FulfillmentModeSelector(
                selectedType = uiState.orderType,
                isDeliveryEnabled = uiState.isDeliveryEnabled,
                minDeliveryOrder = uiState.minDeliveryOrder,
                isFreeDelivery = uiState.isFreeDelivery,
                onSelect = onOrderTypeChange
            )

            // Section 2: Customer Info & Animated Delivery Address / Pickup Info
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (uiState.orderType == OrderType.DELIVERY) "Contact & Delivery Address" else "Contact & Pickup Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = uiState.customerName,
                        onValueChange = onCustomerNameChange,
                        label = { Text("Full Name *") },
                        placeholder = { Text("Enter your name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaroonPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )

                    OutlinedTextField(
                        value = uiState.customerPhone,
                        onValueChange = onCustomerPhoneChange,
                        label = { Text("Phone / WhatsApp Number *") },
                        placeholder = { Text(CafeConstants.PHONE_DISPLAY) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaroonPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )

                    AnimatedVisibility(
                        visible = uiState.orderType == OrderType.DELIVERY,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (savedAddresses.isNotEmpty()) {
                                Text("Saved Addresses", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(savedAddresses) { addr ->
                                        FilterChip(
                                            selected = uiState.deliveryAddress == addr,
                                            onClick = { onDeliveryAddressChange(addr) },
                                            label = { Text(addr, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaroonPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = uiState.deliveryAddress,
                                onValueChange = onDeliveryAddressChange,
                                label = { Text("Delivery Address *") },
                                placeholder = { Text("House / Street / Chak / Mohalla, Taranda M. Panah") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaroonPrimary) },
                                minLines = 2,
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_address_input")
                            )

                            DeliveryMinimumStatusBanner(
                                subtotal = uiState.subtotal,
                                minDeliveryOrder = uiState.minDeliveryOrder,
                                meetsMinDelivery = uiState.meetsMinDelivery,
                                amountToMinDelivery = uiState.amountToMinDelivery,
                                isFreeDelivery = uiState.isFreeDelivery,
                                onSwitchToPickup = { onOrderTypeChange(OrderType.TAKEAWAY) }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.orderType != OrderType.DELIVERY,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            color = GoldBright.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GoldBright.copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.orderType == OrderType.TAKEAWAY) Icons.Default.Storefront else Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaroonPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (uiState.orderType == OrderType.TAKEAWAY) "Self-Pickup Counter" else "Dine-In Service",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary
                                    )
                                    Text(
                                        text = CafeConstants.LOCATION_ADDRESS,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.specialInstructions,
                        onValueChange = onSpecialInstructionsChange,
                        label = { Text("Order Notes / Kitchen Instructions (optional)") },
                        placeholder = { Text("E.g. Extra spicy, ring bell on arrival...") },
                        leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = MaroonPrimary) },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 3: Order Summary Receipt Card
            OrderSummaryReceiptCard(uiState = uiState)

            // Section 4: Google Sign-In card for Direct App Ordering when Guest
            if (!isSignedIn) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaroonPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Direct App Ordering & Live Tracking",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                        Text(
                            text = "Sign in with Google to submit in-app orders with real-time kitchen tracking, or tap WhatsApp Order below as a guest.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (authError != null) {
                            Text(authError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                val act = context as? Activity ?: return@Button
                                scope.launch {
                                    when (val r = AuthRepository.signInWithGoogleInteractive(act)) {
                                        is SignInResult.Success -> onSignedIn()
                                        is SignInResult.Error -> authError = r.message
                                        is SignInResult.Cancelled -> Unit
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Google for Direct Order", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Section 5: Pinned Checkout Action Bar (Total + Direct App Order + WhatsApp Order)
        CheckoutBottomBar(
            uiState = uiState,
            isSignedIn = isSignedIn,
            onPlaceDirectOrder = onPlaceDirectOrder,
            onWhatsAppOrder = onWhatsAppOrder
        )
    }
}
