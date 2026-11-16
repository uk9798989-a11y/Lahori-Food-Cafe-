package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CafeConstants
import com.example.data.model.CafeSettings
import com.example.data.model.CartItem
import com.example.data.model.Deal
import com.example.data.model.Order
import com.example.data.model.OrderType
import com.example.data.model.PizzaPricingRules
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.repository.CafeOrderGateway
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface OrderSubmissionState {
    data object Idle : OrderSubmissionState
    data object Submitting : OrderSubmissionState
    data class DirectOrderSuccess(val order: Order) : OrderSubmissionState
    data class WhatsAppPending(val orderReference: String, val bannerMessage: String) : OrderSubmissionState
    data class Error(val message: String) : OrderSubmissionState
}

data class ShoppingCartUiState(
    val items: List<CartItem> = emptyList(),
    val totalItemCount: Int = 0,
    val baseItemsSubtotal: Int = 0,
    val toppingsAndExtrasTotal: Int = 0,
    val subtotal: Int = 0,
    val deliveryFee: Int = 0,
    val totalPrice: Int = 0,
    val minDeliveryOrder: Int = CafeConstants.MIN_DELIVERY_ORDER,
    val isFreeDelivery: Boolean = true,
    val isDeliveryEnabled: Boolean = true,
    val isAcceptingOrders: Boolean = true,
    val closureNotice: String = "",
    val meetsMinDelivery: Boolean = true,
    val amountToMinDelivery: Int = 0,
    val orderType: OrderType = OrderType.DELIVERY,
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val specialInstructions: String = "",
    val whatsAppOrderRef: String = "LFC-WA-INIT",
    val submissionState: OrderSubmissionState = OrderSubmissionState.Idle
) {
    val isBusy: Boolean get() = submissionState is OrderSubmissionState.Submitting
}

typealias CartViewModel = ShoppingCartViewModel

class ShoppingCartViewModel(
    private val orderGateway: CafeOrderGateway,
    private val currentUserId: String?,
    defaultUserName: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        recalculateTotals(
            ShoppingCartUiState(
                customerName = defaultUserName.orEmpty(),
                whatsAppOrderRef = generateWhatsAppRef()
            )
        )
    )
    val uiState: StateFlow<ShoppingCartUiState> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private var profilePrefilled = false

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun dismissSubmissionBanner() {
        _uiState.update { it.copy(submissionState = OrderSubmissionState.Idle) }
    }

    fun syncCafeSettings(settings: CafeSettings) {
        _uiState.update { state ->
            val adjustedOrderType = if (!settings.isDeliveryEnabled && state.orderType == OrderType.DELIVERY) {
                OrderType.TAKEAWAY
            } else {
                state.orderType
            }
            recalculateTotals(
                state.copy(
                    minDeliveryOrder = settings.minDeliveryOrder,
                    isFreeDelivery = settings.isFreeDelivery,
                    isDeliveryEnabled = settings.isDeliveryEnabled,
                    isAcceptingOrders = settings.isAcceptingOrders,
                    closureNotice = settings.closureNotice,
                    orderType = adjustedOrderType,
                    deliveryFee = settings.deliveryFee
                ),
                configuredDeliveryFee = settings.deliveryFee
            )
        }
    }

    fun prefillCustomerProfile(profile: UserProfile?, fallbackName: String?) {
        if (profilePrefilled && profile == null) return
        _uiState.update { state ->
            val nextName = state.customerName.ifBlank {
                profile?.displayName?.ifBlank { fallbackName.orEmpty() } ?: fallbackName.orEmpty()
            }
            val nextPhone = state.customerPhone.ifBlank { profile?.phone.orEmpty() }
            val nextAddress = state.deliveryAddress.ifBlank { profile?.savedAddresses?.firstOrNull().orEmpty() }
            if (profile != null) profilePrefilled = true
            state.copy(
                customerName = nextName,
                customerPhone = nextPhone,
                deliveryAddress = nextAddress
            )
        }
    }

    // --- Checkout & Order Form State ---

    fun setCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun setCustomerPhone(phone: String) {
        _uiState.update { it.copy(customerPhone = phone) }
    }

    fun setOrderType(orderType: OrderType, configuredDeliveryFee: Int = _uiState.value.deliveryFee) {
        _uiState.update { state ->
            if (orderType == OrderType.DELIVERY && !state.isDeliveryEnabled) return@update state
            recalculateTotals(state.copy(orderType = orderType), configuredDeliveryFee = configuredDeliveryFee)
        }
    }

    fun setDeliveryAddress(address: String) {
        _uiState.update { it.copy(deliveryAddress = address) }
    }

    fun setSpecialInstructions(instructions: String) {
        _uiState.update { it.copy(specialInstructions = instructions) }
    }

    // --- Cart & Toppings Management ---

    fun addProductToCart(
        product: Product,
        selectedSize: String,
        extraChicken: Boolean,
        extraCheese: Boolean,
        extraCheeseSlice: Boolean,
        selectedSauces: List<String>,
        specialInstructions: String,
        quantity: Int
    ) {
        if (!product.priceVerified || !product.isAvailable) {
            _actionMessage.value = if (!product.priceVerified) {
                "${product.name} is awaiting price verification by cafe admin."
            } else {
                "${product.name} is currently unavailable."
            }
            return
        }
        val safeQty = quantity.coerceAtLeast(1)
        val unitBase = if (product.sizePrices.isNotEmpty()) {
            product.sizePrices[selectedSize] ?: product.basePrice
        } else {
            product.basePrice
        }
        val validSauces = selectedSauces.filter { it in PizzaPricingRules.availableSauces }.distinct().sorted()
        val applyChicken = product.supportsPizzaCustomizations && extraChicken && PizzaPricingRules.extraToppingPrice(selectedSize) != null
        val applyCheese = product.supportsPizzaCustomizations && extraCheese && PizzaPricingRules.extraToppingPrice(selectedSize) != null
        val applySlice = product.supportsExtraCheeseSlice && extraCheeseSlice

        val customizationCharges = calculateCustomizationCharges(
            supportsPizzaCustomizations = product.supportsPizzaCustomizations,
            selectedSize = selectedSize,
            extraChicken = applyChicken,
            extraCheese = applyCheese,
            supportsExtraCheeseSlice = product.supportsExtraCheeseSlice,
            extraCheeseSlice = applySlice,
            selectedSauces = validSauces
        )

        val trimmedNote = specialInstructions.trim()
        val key = buildCartKey(
            itemId = product.id,
            selectedSize = selectedSize,
            extraChicken = applyChicken,
            extraCheese = applyCheese,
            extraCheeseSlice = applySlice,
            selectedSauces = validSauces,
            specialInstructions = trimmedNote
        )

        _uiState.update { state ->
            val updated = state.items.toMutableList()
            val idx = updated.indexOfFirst { it.cartKey == key }
            if (idx >= 0) {
                val prev = updated[idx]
                updated[idx] = prev.copy(quantity = prev.quantity + safeQty)
            } else {
                updated += CartItem(
                    cartKey = key,
                    itemId = product.id,
                    itemType = "PRODUCT",
                    name = product.name,
                    imageKey = product.imageKey,
                    imageUrl = product.imageUrl,
                    selectedSize = selectedSize,
                    extraChicken = applyChicken,
                    extraCheese = applyCheese,
                    extraCheeseSlice = applySlice,
                    selectedSauces = validSauces,
                    specialInstructions = trimmedNote,
                    quantity = safeQty,
                    unitBasePrice = unitBase,
                    customizationCharges = customizationCharges,
                    supportsPizzaCustomizations = product.supportsPizzaCustomizations,
                    supportsExtraCheeseSlice = product.supportsExtraCheeseSlice
                )
            }
            recalculateTotals(
                state.copy(
                    items = updated,
                    whatsAppOrderRef = generateWhatsAppRef(),
                    submissionState = OrderSubmissionState.Idle
                )
            )
        }
        _actionMessage.value = "Added ${product.name} to cart"
    }

    fun addDealToCart(deal: Deal, quantity: Int = 1, specialInstructions: String = "") {
        val safeQty = quantity.coerceAtLeast(1)
        val trimmedNote = specialInstructions.trim()
        val key = "${deal.id}|$trimmedNote"

        _uiState.update { state ->
            val updated = state.items.toMutableList()
            val idx = updated.indexOfFirst { it.cartKey == key }
            if (idx >= 0) {
                val prev = updated[idx]
                updated[idx] = prev.copy(quantity = prev.quantity + safeQty)
            } else {
                updated += CartItem(
                    cartKey = key,
                    itemId = deal.id,
                    itemType = "DEAL",
                    name = "${deal.dealNumber}: ${deal.title}",
                    imageKey = deal.imageKey,
                    imageUrl = deal.imageUrl,
                    selectedSize = "",
                    specialInstructions = trimmedNote,
                    quantity = safeQty,
                    unitBasePrice = deal.price,
                    customizationCharges = 0
                )
            }
            recalculateTotals(
                state.copy(
                    items = updated,
                    whatsAppOrderRef = generateWhatsAppRef(),
                    submissionState = OrderSubmissionState.Idle
                )
            )
        }
        _actionMessage.value = "Added ${deal.dealNumber} to cart"
    }

    fun incrementQuantity(cartKey: String) {
        val currentQty = _uiState.value.items.firstOrNull { it.cartKey == cartKey }?.quantity ?: return
        updateQuantity(cartKey, currentQty + 1)
    }

    fun decrementQuantity(cartKey: String) {
        val currentQty = _uiState.value.items.firstOrNull { it.cartKey == cartKey }?.quantity ?: return
        updateQuantity(cartKey, currentQty - 1)
    }

    fun removeItem(cartKey: String) {
        updateQuantity(cartKey, 0)
    }

    fun updateQuantity(cartKey: String, newQuantity: Int) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            val idx = updated.indexOfFirst { it.cartKey == cartKey }
            if (idx < 0) return@update state
            if (newQuantity <= 0) {
                updated.removeAt(idx)
            } else {
                updated[idx] = updated[idx].copy(quantity = newQuantity)
            }
            recalculateTotals(
                state.copy(
                    items = updated,
                    whatsAppOrderRef = generateWhatsAppRef(),
                    submissionState = OrderSubmissionState.Idle
                )
            )
        }
    }

    fun updateItemToppings(
        cartKey: String,
        extraChicken: Boolean,
        extraCheese: Boolean,
        extraCheeseSlice: Boolean,
        selectedSauces: List<String>
    ) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            val idx = updated.indexOfFirst { it.cartKey == cartKey }
            if (idx < 0) return@update state

            val item = updated[idx]
            val validSauces = selectedSauces.filter { it in PizzaPricingRules.availableSauces }.distinct().sorted()
            val applyChicken = item.supportsPizzaCustomizations && extraChicken && PizzaPricingRules.extraToppingPrice(item.selectedSize) != null
            val applyCheese = item.supportsPizzaCustomizations && extraCheese && PizzaPricingRules.extraToppingPrice(item.selectedSize) != null
            val applySlice = item.supportsExtraCheeseSlice && extraCheeseSlice

            val newCharges = calculateCustomizationCharges(
                supportsPizzaCustomizations = item.supportsPizzaCustomizations,
                selectedSize = item.selectedSize,
                extraChicken = applyChicken,
                extraCheese = applyCheese,
                supportsExtraCheeseSlice = item.supportsExtraCheeseSlice,
                extraCheeseSlice = applySlice,
                selectedSauces = validSauces
            )

            val newKey = buildCartKey(
                itemId = item.itemId,
                selectedSize = item.selectedSize,
                extraChicken = applyChicken,
                extraCheese = applyCheese,
                extraCheeseSlice = applySlice,
                selectedSauces = validSauces,
                specialInstructions = item.specialInstructions
            )

            val duplicateIdx = updated.indexOfFirst { it.cartKey == newKey && it.cartKey != cartKey }
            if (duplicateIdx >= 0) {
                val target = updated[duplicateIdx]
                updated[duplicateIdx] = target.copy(quantity = target.quantity + item.quantity)
                updated.removeAt(idx)
            } else {
                updated[idx] = item.copy(
                    cartKey = newKey,
                    extraChicken = applyChicken,
                    extraCheese = applyCheese,
                    extraCheeseSlice = applySlice,
                    selectedSauces = validSauces,
                    customizationCharges = newCharges
                )
            }

            recalculateTotals(
                state.copy(
                    items = updated,
                    whatsAppOrderRef = generateWhatsAppRef(),
                    submissionState = OrderSubmissionState.Idle
                )
            )
        }
    }

    fun clearCart() {
        _uiState.update { state ->
            recalculateTotals(
                state.copy(
                    items = emptyList(),
                    whatsAppOrderRef = generateWhatsAppRef()
                )
            )
        }
    }

    fun reorderFromOrder(
        order: Order,
        availableProducts: List<Product>,
        availableDeals: List<Deal>
    ) {
        val rebuilt = mutableListOf<CartItem>()
        for (item in order.items) {
            if (item.itemType == "DEAL") {
                val deal = availableDeals.firstOrNull { it.id == item.itemId && it.isActive }
                if (deal != null) {
                    rebuilt += CartItem(
                        cartKey = "${deal.id}|${item.specialInstructions.trim()}",
                        itemId = deal.id,
                        itemType = "DEAL",
                        name = "${deal.dealNumber}: ${deal.title}",
                        imageKey = deal.imageKey,
                        imageUrl = deal.imageUrl,
                        specialInstructions = item.specialInstructions.trim(),
                        quantity = item.quantity.coerceAtLeast(1),
                        unitBasePrice = deal.price,
                        customizationCharges = 0
                    )
                }
            } else {
                val product = availableProducts.firstOrNull {
                    it.id == item.itemId && it.isActive && it.isAvailable && it.priceVerified
                }
                if (product != null) {
                    val base = if (product.sizePrices.isNotEmpty()) {
                        product.sizePrices[item.selectedSize] ?: product.basePrice
                    } else product.basePrice
                    val validSauces = item.selectedSauces.filter { it in PizzaPricingRules.availableSauces }.distinct().sorted()
                    val custom = calculateCustomizationCharges(
                        supportsPizzaCustomizations = product.supportsPizzaCustomizations,
                        selectedSize = item.selectedSize,
                        extraChicken = item.extraChicken,
                        extraCheese = item.extraCheese,
                        supportsExtraCheeseSlice = product.supportsExtraCheeseSlice,
                        extraCheeseSlice = item.extraCheeseSlice,
                        selectedSauces = validSauces
                    )
                    rebuilt += CartItem(
                        cartKey = buildCartKey(
                            itemId = product.id,
                            selectedSize = item.selectedSize,
                            extraChicken = item.extraChicken,
                            extraCheese = item.extraCheese,
                            extraCheeseSlice = item.extraCheeseSlice,
                            selectedSauces = validSauces,
                            specialInstructions = item.specialInstructions.trim()
                        ),
                        itemId = product.id,
                        itemType = "PRODUCT",
                        name = product.name,
                        imageKey = product.imageKey,
                        imageUrl = product.imageUrl,
                        selectedSize = item.selectedSize,
                        extraChicken = item.extraChicken,
                        extraCheese = item.extraCheese,
                        extraCheeseSlice = item.extraCheeseSlice,
                        selectedSauces = validSauces,
                        specialInstructions = item.specialInstructions.trim(),
                        quantity = item.quantity.coerceAtLeast(1),
                        unitBasePrice = base,
                        customizationCharges = custom,
                        supportsPizzaCustomizations = product.supportsPizzaCustomizations,
                        supportsExtraCheeseSlice = product.supportsExtraCheeseSlice
                    )
                }
            }
        }
        if (rebuilt.isEmpty()) {
            _actionMessage.value = "Items from this past order are currently unavailable."
        } else {
            _uiState.update { state ->
                recalculateTotals(
                    state.copy(
                        items = rebuilt,
                        whatsAppOrderRef = generateWhatsAppRef(),
                        submissionState = OrderSubmissionState.Idle
                    )
                )
            }
            _actionMessage.value = "Loaded ${rebuilt.size} item(s) into your cart"
        }
    }

    // --- Direct App Order & WhatsApp Order Execution ---

    fun submitDirectAppOrder(onSuccess: (Order) -> Unit = {}) {
        val state = _uiState.value
        if (currentUserId.isNullOrBlank()) {
            val msg = "Please sign in with Google to submit a Direct App Order, or use WhatsApp Order."
            _uiState.update { it.copy(submissionState = OrderSubmissionState.Error(msg)) }
            _actionMessage.value = msg
            return
        }
        if (state.items.isEmpty()) {
            val msg = "Your cart is empty."
            _uiState.update { it.copy(submissionState = OrderSubmissionState.Error(msg)) }
            _actionMessage.value = msg
            return
        }
        if (!state.isAcceptingOrders) {
            val msg = state.closureNotice.ifBlank { "Lahori Food Cafe is temporarily closed for orders." }
            _uiState.update { it.copy(submissionState = OrderSubmissionState.Error(msg)) }
            _actionMessage.value = msg
            return
        }
        if (state.orderType == OrderType.DELIVERY && !state.meetsMinDelivery) {
            val msg = "Minimum delivery order is Rs. ${state.minDeliveryOrder}. Add Rs. ${state.amountToMinDelivery} more or switch to Takeaway."
            _uiState.update { it.copy(submissionState = OrderSubmissionState.Error(msg)) }
            _actionMessage.value = msg
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submissionState = OrderSubmissionState.Submitting) }
            val result = orderGateway.placeDirectOrder(
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                orderType = state.orderType,
                deliveryAddress = state.deliveryAddress,
                specialInstructions = state.specialInstructions,
                cartItems = state.items
            )
            result.onSuccess { order ->
                _uiState.update { current ->
                    recalculateTotals(
                        current.copy(
                            items = emptyList(),
                            specialInstructions = "",
                            whatsAppOrderRef = generateWhatsAppRef(),
                            submissionState = OrderSubmissionState.DirectOrderSuccess(order)
                        )
                    )
                }
                _actionMessage.value = "Order ${order.orderCode} received!"
                onSuccess(order)
            }.onFailure { err ->
                val msg = err.message ?: "Failed to place order."
                _uiState.update { it.copy(submissionState = OrderSubmissionState.Error(msg)) }
                _actionMessage.value = msg
            }
        }
    }

    fun launchWhatsAppOrder(context: Context) {
        val state = _uiState.value
        if (!state.isAcceptingOrders) {
            _actionMessage.value = state.closureNotice.ifBlank {
                "Lahori Food Cafe is temporarily closed for orders."
            }
            return
        }
        if (state.items.isNotEmpty() && state.orderType == OrderType.DELIVERY && !state.meetsMinDelivery) {
            _actionMessage.value =
                "Minimum delivery order is Rs. ${state.minDeliveryOrder}. Add Rs. ${state.amountToMinDelivery} more or switch to Takeaway."
            return
        }

        val orderRef = state.whatsAppOrderRef
        val nameForMessage = state.customerName.ifBlank { "Customer" }

        val message = WhatsAppHelper.buildOrderMessage(
            orderReference = orderRef,
            customerName = nameForMessage,
            customerPhone = state.customerPhone,
            orderType = state.orderType,
            deliveryAddress = state.deliveryAddress,
            specialInstructions = state.specialInstructions,
            items = state.items,
            subtotal = state.subtotal,
            deliveryFee = state.deliveryFee,
            finalTotal = state.totalPrice
        )

        val launched = WhatsAppHelper.openWhatsAppWithMessage(context, message)
        if (!launched) {
            _actionMessage.value = "Could not open WhatsApp. Please call ${CafeConstants.PHONE_DISPLAY} directly."
            return
        }

        if (!currentUserId.isNullOrBlank() && state.items.isNotEmpty()) {
            viewModelScope.launch {
                orderGateway.recordPendingWhatsAppOrder(
                    orderCode = orderRef,
                    customerName = nameForMessage,
                    customerPhone = state.customerPhone.ifBlank { "Pending" },
                    orderType = state.orderType,
                    deliveryAddress = state.deliveryAddress,
                    specialInstructions = state.specialInstructions,
                    items = state.items,
                    subtotal = state.subtotal,
                    deliveryFee = state.deliveryFee,
                    total = state.totalPrice
                )
            }
        }

        val banner = "WhatsApp opened with Order Ref $orderRef. Please review and send in WhatsApp — your order remains Pending until confirmed by Lahori Food Cafe."
        _uiState.update {
            it.copy(submissionState = OrderSubmissionState.WhatsAppPending(orderRef, banner))
        }
    }

    private fun recalculateTotals(
        state: ShoppingCartUiState,
        configuredDeliveryFee: Int = cachedDeliveryFee
    ): ShoppingCartUiState {
        cachedDeliveryFee = configuredDeliveryFee
        val totalQty = state.items.sumOf { it.quantity }
        val baseSub = state.items.sumOf { it.baseLineTotal }
        val toppingsSub = state.items.sumOf { it.toppingsLineTotal }
        val sub = baseSub + toppingsSub
        val activeDeliveryFee = if (
            state.items.isNotEmpty() &&
            state.orderType == OrderType.DELIVERY &&
            !state.isFreeDelivery
        ) {
            configuredDeliveryFee
        } else {
            0
        }
        val finalTotal = sub + activeDeliveryFee
        val meetsMin = state.orderType != OrderType.DELIVERY || sub >= state.minDeliveryOrder
        val remaining = if (state.orderType == OrderType.DELIVERY) {
            (state.minDeliveryOrder - sub).coerceAtLeast(0)
        } else {
            0
        }
        return state.copy(
            totalItemCount = totalQty,
            baseItemsSubtotal = baseSub,
            toppingsAndExtrasTotal = toppingsSub,
            subtotal = sub,
            deliveryFee = activeDeliveryFee,
            totalPrice = finalTotal,
            meetsMinDelivery = meetsMin,
            amountToMinDelivery = remaining
        )
    }

    private var cachedDeliveryFee: Int = 0

    companion object {
        fun calculateCustomizationCharges(
            supportsPizzaCustomizations: Boolean,
            selectedSize: String,
            extraChicken: Boolean,
            extraCheese: Boolean,
            supportsExtraCheeseSlice: Boolean,
            extraCheeseSlice: Boolean,
            selectedSauces: List<String>
        ): Int {
            var charges = 0
            if (supportsPizzaCustomizations) {
                val toppingRate = PizzaPricingRules.extraToppingPrice(selectedSize) ?: 0
                if (extraChicken) charges += toppingRate
                if (extraCheese) charges += toppingRate
                val validSauceCount = selectedSauces.count { it in PizzaPricingRules.availableSauces }
                charges += validSauceCount * PizzaPricingRules.SAUCE_PRICE
            }
            if (supportsExtraCheeseSlice && extraCheeseSlice) {
                charges += PizzaPricingRules.BURGER_CHEESE_SLICE_PRICE
            }
            return charges
        }

        private fun buildCartKey(
            itemId: String,
            selectedSize: String,
            extraChicken: Boolean,
            extraCheese: Boolean,
            extraCheeseSlice: Boolean,
            selectedSauces: List<String>,
            specialInstructions: String
        ): String {
            return "$itemId|$selectedSize|$extraChicken|$extraCheese|$extraCheeseSlice|${selectedSauces.joinToString(",")}|$specialInstructions"
        }

        private fun generateWhatsAppRef(): String {
            return "LFC-WA-${UUID.randomUUID().toString().uppercase().take(6)}"
        }
    }
}
