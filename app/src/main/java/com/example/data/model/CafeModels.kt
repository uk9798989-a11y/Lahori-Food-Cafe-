package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

object CafeConstants {
    const val BUSINESS_NAME = "LAHORI FOOD CAFE"
    const val SHORT_NAME = "Lahori Cafe"
    const val LOCATION_ADDRESS = "Main Chowk, Sidhu Wali Road, Taranda Muhammad Panah, Punjab, Pakistan"
    const val PHONE_DISPLAY = "0304-3689241"
    const val WHATSAPP_NUMBER = "+923043689241"
    const val WHATSAPP_DIGITS = "923043689241"
    const val MIN_DELIVERY_ORDER = 700
    const val SOCIAL_HANDLE = "LAHORI CAFÉ TMP"
    const val FACEBOOK_URL = "https://www.facebook.com/search/top?q=LAHORI%20CAFE%20TMP"
    const val INSTAGRAM_URL = "https://www.instagram.com/explore/search/keyword/?q=LAHORI%20CAFE%20TMP"
    const val TIKTOK_URL = "https://www.tiktok.com/search?q=LAHORI%20CAFE%20TMP"
    const val BOOTSTRAPPED_ADMIN_EMAIL = "uk9798989@gmail.com"
    const val SETTINGS_DOC_ID = "cafe_config"
}

enum class OrderType(val label: String, val firestoreValue: String) {
    DELIVERY("Delivery", "DELIVERY"),
    TAKEAWAY("Takeaway", "TAKEAWAY"),
    DINE_IN("Dine-In", "DINE_IN");

    companion object {
        fun fromString(value: String): OrderType =
            entries.firstOrNull { it.firestoreValue == value } ?: DELIVERY
    }
}

enum class OrderSource(val label: String, val firestoreValue: String) {
    APP("Direct App Order", "APP"),
    WHATSAPP("WhatsApp Order", "WHATSAPP");

    companion object {
        fun fromString(value: String): OrderSource =
            entries.firstOrNull { it.firestoreValue == value } ?: APP
    }
}

object OrderStatuses {
    const val PENDING_WHATSAPP = "Pending WhatsApp Confirmation"
    const val ORDER_RECEIVED = "Order Received"
    const val ORDER_CONFIRMED = "Order Confirmed"
    const val PREPARING = "Preparing"
    const val READY = "Ready"
    const val OUT_FOR_DELIVERY = "Out for Delivery"
    const val COMPLETED = "Completed"
    const val CANCELLED = "Cancelled"

    val TRACKING_STEPS = listOf(
        ORDER_RECEIVED,
        ORDER_CONFIRMED,
        PREPARING,
        READY,
        OUT_FOR_DELIVERY,
        COMPLETED
    )

    val ALL_ADMIN_STATUSES = listOf(
        PENDING_WHATSAPP,
        ORDER_RECEIVED,
        ORDER_CONFIRMED,
        PREPARING,
        READY,
        OUT_FOR_DELIVERY,
        COMPLETED,
        CANCELLED
    )
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val savedAddresses: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    val group: String = "FAST_FOOD",
    val displayOrder: Int = 0,
    val imageKey: String = "img_hero_platter",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val group: String = "FAST_FOOD",
    val description: String = "",
    val basePrice: Int = 0,
    val sizePrices: Map<String, Int> = emptyMap(),
    val supportsPizzaCustomizations: Boolean = false,
    val supportsExtraCheeseSlice: Boolean = false,
    val priceVerified: Boolean = true,
    val isAvailable: Boolean = true,
    val isActive: Boolean = true,
    val isPopular: Boolean = false,
    val isNewArrival: Boolean = false,
    val imageKey: String = "img_hero_platter",
    val imageUrl: String = "",
    val displayOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun orderedSizes(): List<Pair<String, Int>> {
        if (sizePrices.isEmpty()) return emptyList()
        val order = listOf("S", "M", "L", "XL", "5P", "10P", "HALF", "FULL")
        return sizePrices.entries
            .sortedBy { entry ->
                val idx = order.indexOf(entry.key.uppercase())
                if (idx >= 0) idx else 100
            }
            .map { it.key to it.value }
    }

    fun startingPrice(): Int {
        return if (sizePrices.isNotEmpty()) sizePrices.values.minOrNull() ?: basePrice else basePrice
    }
}

@IgnoreExtraProperties
data class Deal(
    @DocumentId val id: String = "",
    val dealNumber: String = "",
    val title: String = "",
    val itemsSummary: String = "",
    val drinkInfo: String = "",
    val price: Int = 0,
    val note: String = "",
    val imageKey: String = "img_cat_deals",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val displayOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Banner(
    @DocumentId val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val badgeText: String = "",
    val imageKey: String = "img_hero_platter",
    val targetRoute: String = "deals",
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CafeSettings(
    @DocumentId val id: String = CafeConstants.SETTINGS_DOC_ID,
    val minDeliveryOrder: Int = CafeConstants.MIN_DELIVERY_ORDER,
    val deliveryFee: Int = 0,
    val isFreeDelivery: Boolean = true,
    val isDeliveryEnabled: Boolean = true,
    val isAcceptingOrders: Boolean = true,
    val openingHours: String = "12:00 PM - 2:00 AM Daily",
    val closureNotice: String = "",
    val whatsappNumber: String = CafeConstants.WHATSAPP_NUMBER,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class OrderLineItem(
    val itemId: String = "",
    val itemType: String = "PRODUCT",
    val name: String = "",
    val selectedSize: String = "",
    val extraChicken: Boolean = false,
    val extraCheese: Boolean = false,
    val extraCheeseSlice: Boolean = false,
    val selectedSauces: List<String> = emptyList(),
    val specialInstructions: String = "",
    val quantity: Int = 1,
    val unitPrice: Int = 0,
    val customizationCharges: Int = 0,
    val lineTotal: Int = 0
)

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderCode: String = "",
    val orderSource: String = OrderSource.APP.firestoreValue,
    val orderType: String = OrderType.DELIVERY.firestoreValue,
    val status: String = OrderStatuses.ORDER_RECEIVED,
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val specialInstructions: String = "",
    val items: List<OrderLineItem> = emptyList(),
    val subtotal: Int = 0,
    val deliveryFee: Int = 0,
    val total: Int = 0,
    val priceVerified: Boolean = true,
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderId: String = "",
    val targetRole: String = "CUSTOMER",
    val orderId: String = "",
    val orderCode: String = "",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CartItem(
    val cartKey: String,
    val itemId: String,
    val itemType: String, // "PRODUCT" or "DEAL"
    val name: String,
    val imageKey: String,
    val imageUrl: String = "",
    val selectedSize: String = "",
    val extraChicken: Boolean = false,
    val extraCheese: Boolean = false,
    val extraCheeseSlice: Boolean = false,
    val selectedSauces: List<String> = emptyList(),
    val specialInstructions: String = "",
    val quantity: Int = 1,
    val unitBasePrice: Int,
    val customizationCharges: Int,
    val supportsPizzaCustomizations: Boolean = false,
    val supportsExtraCheeseSlice: Boolean = false
) {
    val unitTotal: Int get() = unitBasePrice + customizationCharges
    val baseLineTotal: Int get() = unitBasePrice * quantity
    val toppingsLineTotal: Int get() = customizationCharges * quantity
    val lineTotal: Int get() = unitTotal * quantity

    fun customizationSummary(): String {
        val parts = mutableListOf<String>()
        if (selectedSize.isNotBlank()) parts += "Size: $selectedSize"
        if (extraChicken) parts += "Extra Chicken"
        if (extraCheese) parts += "Extra Cheese"
        if (extraCheeseSlice) parts += "Extra Cheese Slice (+Rs. 50)"
        if (selectedSauces.isNotEmpty()) parts += "Sauces: ${selectedSauces.joinToString(", ")}"
        if (specialInstructions.isNotBlank()) parts += "Note: $specialInstructions"
        return parts.joinToString(" • ")
    }
}

object PizzaPricingRules {
    val availableSauces = listOf("Dip Sauce", "Special Sauce", "Peri Peri Sauce", "Garlic Sauce")
    const val SAUCE_PRICE = 30
    const val BURGER_CHEESE_SLICE_PRICE = 50

    fun extraToppingPrice(size: String): Int? {
        return when (size.uppercase()) {
            "S" -> 70
            "M" -> 100
            "L" -> 170
            else -> null // XL topping price is not printed on the flyer; left for admin verification
        }
    }
}
