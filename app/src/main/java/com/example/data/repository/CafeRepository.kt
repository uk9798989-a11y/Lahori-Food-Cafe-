package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.Banner
import com.example.data.model.CafeConstants
import com.example.data.model.CafeSettings
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Deal
import com.example.data.model.LahoriMenuCatalog
import com.example.data.model.Order
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderSource
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.example.data.model.PizzaPricingRules
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface CafeOrderGateway {
    suspend fun placeDirectOrder(
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        specialInstructions: String,
        cartItems: List<CartItem>
    ): Result<Order>

    suspend fun recordPendingWhatsAppOrder(
        orderCode: String,
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        specialInstructions: String,
        items: List<CartItem>,
        subtotal: Int,
        deliveryFee: Int,
        total: Int
    ): Result<String>
}

class CafeRepository(private val db: FirebaseFirestore) : CafeOrderGateway {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in with Google to continue.")
    }

    suspend fun checkIsAdmin(uid: String, email: String?, emailVerified: Boolean): Boolean {
        if (emailVerified && email.equals(CafeConstants.BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)) {
            return true
        }
        return try {
            db.collection("admins").document(uid).get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    // --- Real-Time Streams (Only collected when authenticated!) ---

    fun observeCategories(includeInactive: Boolean = false): Flow<List<Category>> = flow {
        val path = "categories"
        val query = if (includeInactive) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("isActive", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.displayOrder }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeProducts(includeInactive: Boolean = false): Flow<List<Product>> = flow {
        val path = "products"
        val query = if (includeInactive) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("isActive", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.displayOrder }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeDeals(includeInactive: Boolean = false): Flow<List<Deal>> = flow {
        val path = "deals"
        val query = if (includeInactive) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("isActive", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(Deal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.displayOrder }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeBanners(includeInactive: Boolean = false): Flow<List<Banner>> = flow {
        val path = "banners"
        val query = if (includeInactive) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("isActive", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(Banner::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.displayOrder }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeSettings(): Flow<CafeSettings> = flow {
        val path = "settings/${CafeConstants.SETTINGS_DOC_ID}"
        emitAll(
            db.collection("settings").document(CafeConstants.SETTINGS_DOC_ID)
                .snapshots()
                .map { snap ->
                    snap.toObject(CafeSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: CafeSettings()
                }
                .catch { e ->
                    logStreamError(e, OperationType.GET, path)
                    throw e
                }
        )
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { e ->
                    logStreamError(e, OperationType.GET, path)
                    throw e
                }
        )
    }

    fun observeUserOrders(userId: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeAllOrdersAdmin(): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeUserNotifications(userId: String): Flow<List<AppNotification>> = flow {
        val path = "notifications"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    fun observeAdminNotifications(): Flow<List<AppNotification>> = flow {
        val path = "notifications"
        emitAll(
            db.collection(path)
                .whereEqualTo("targetRole", "ADMIN")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { e ->
                    logStreamError(e, OperationType.LIST, path)
                    throw e
                }
        )
    }

    // --- Profile Operations ---

    suspend fun ensureUserProfile(userId: String, displayName: String, email: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to userId,
                    "displayName" to displayName.ifBlank { "Lahori Cafe Guest" },
                    "email" to email,
                    "phone" to "",
                    "savedAddresses" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        phone: String,
        savedAddresses: List<String>
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.trim().ifBlank { "Customer" },
                    "email" to (Firebase.auth.currentUser?.email ?: ""),
                    "phone" to phone.trim(),
                    "savedAddresses" to savedAddresses.take(10),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "displayName" to displayName.trim().ifBlank { "Customer" },
                    "phone" to phone.trim(),
                    "savedAddresses" to savedAddresses.take(10),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Authoritative Server-Verified Order Placement ---

    override suspend fun placeDirectOrder(
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        specialInstructions: String,
        cartItems: List<CartItem>
    ): Result<Order> {
        val uid = requireUserId()
        if (cartItems.isEmpty()) return Result.failure(IllegalArgumentException("Your cart is empty."))
        if (customerName.isBlank()) return Result.failure(IllegalArgumentException("Please enter your name."))
        if (customerPhone.trim().length < 7) return Result.failure(IllegalArgumentException("Please enter a valid phone number."))
        if (orderType == OrderType.DELIVERY && deliveryAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your delivery address."))
        }

        return try {
            val settingsSnap = db.collection("settings").document(CafeConstants.SETTINGS_DOC_ID).get().await()
            val settings = settingsSnap.toObject(CafeSettings::class.java) ?: CafeSettings()

            if (!settings.isAcceptingOrders) {
                return Result.failure(IllegalStateException(
                    settings.closureNotice.ifBlank { "Lahori Food Cafe is temporarily not accepting orders." }
                ))
            }
            if (orderType == OrderType.DELIVERY && !settings.isDeliveryEnabled) {
                return Result.failure(IllegalStateException("Delivery is currently unavailable. Please choose Takeaway or Dine-In."))
            }

            // Recompute every item price from authoritative Firestore records (never trust client totals)
            val verifiedItems = mutableListOf<OrderLineItem>()
            var computedSubtotal = 0

            for (cartItem in cartItems) {
                if (cartItem.quantity <= 0) continue
                if (cartItem.itemType == "DEAL") {
                    val dealSnap = db.collection("deals").document(cartItem.itemId).get().await()
                    val deal = if (dealSnap.exists()) {
                        dealSnap.toObject(Deal::class.java)
                    } else {
                        LahoriMenuCatalog.deals.firstOrNull { it.id == cartItem.itemId }
                    } ?: return Result.failure(IllegalStateException("Deal ${cartItem.name} is no longer available."))

                    if (!deal.isActive) {
                        return Result.failure(IllegalStateException("${deal.title} is currently inactive."))
                    }
                    val unitPrice = deal.price
                    val lineTotal = unitPrice * cartItem.quantity
                    computedSubtotal += lineTotal
                    verifiedItems += OrderLineItem(
                        itemId = deal.id,
                        itemType = "DEAL",
                        name = "${deal.dealNumber}: ${deal.title}",
                        selectedSize = "",
                        specialInstructions = cartItem.specialInstructions.take(200),
                        quantity = cartItem.quantity,
                        unitPrice = unitPrice,
                        customizationCharges = 0,
                        lineTotal = lineTotal
                    )
                } else {
                    val prodSnap = db.collection("products").document(cartItem.itemId).get().await()
                    val product = if (prodSnap.exists()) {
                        prodSnap.toObject(Product::class.java)
                    } else {
                        LahoriMenuCatalog.products.firstOrNull { it.id == cartItem.itemId }
                    } ?: return Result.failure(IllegalStateException("Product ${cartItem.name} is no longer available."))

                    if (!product.isActive || !product.isAvailable) {
                        return Result.failure(IllegalStateException("${product.name} is currently unavailable."))
                    }
                    if (!product.priceVerified) {
                        return Result.failure(IllegalStateException("${product.name} is awaiting price verification by cafe admin."))
                    }

                    val unitBase = if (product.sizePrices.isNotEmpty()) {
                        product.sizePrices[cartItem.selectedSize]
                            ?: return Result.failure(IllegalStateException("Invalid size ${cartItem.selectedSize} for ${product.name}."))
                    } else {
                        product.basePrice
                    }

                    var extraCharges = 0
                    if (product.supportsPizzaCustomizations) {
                        val toppingRate = PizzaPricingRules.extraToppingPrice(cartItem.selectedSize)
                        if (cartItem.extraChicken) {
                            extraCharges += toppingRate
                                ?: return Result.failure(IllegalStateException("Extra Chicken price for ${cartItem.selectedSize} requires cafe verification."))
                        }
                        if (cartItem.extraCheese) {
                            extraCharges += toppingRate
                                ?: return Result.failure(IllegalStateException("Extra Cheese price for ${cartItem.selectedSize} requires cafe verification."))
                        }
                        val validSauces = cartItem.selectedSauces.filter { it in PizzaPricingRules.availableSauces }
                        extraCharges += validSauces.size * PizzaPricingRules.SAUCE_PRICE
                    }
                    if (product.supportsExtraCheeseSlice && cartItem.extraCheeseSlice) {
                        extraCharges += PizzaPricingRules.BURGER_CHEESE_SLICE_PRICE
                    }

                    val lineTotal = (unitBase + extraCharges) * cartItem.quantity
                    computedSubtotal += lineTotal
                    verifiedItems += OrderLineItem(
                        itemId = product.id,
                        itemType = "PRODUCT",
                        name = product.name,
                        selectedSize = cartItem.selectedSize,
                        extraChicken = cartItem.extraChicken,
                        extraCheese = cartItem.extraCheese,
                        extraCheeseSlice = cartItem.extraCheeseSlice,
                        selectedSauces = cartItem.selectedSauces,
                        specialInstructions = cartItem.specialInstructions.take(200),
                        quantity = cartItem.quantity,
                        unitPrice = unitBase,
                        customizationCharges = extraCharges,
                        lineTotal = lineTotal
                    )
                }
            }

            if (orderType == OrderType.DELIVERY && computedSubtotal < settings.minDeliveryOrder) {
                return Result.failure(IllegalStateException(
                    "Minimum delivery order is Rs. ${settings.minDeliveryOrder}. Add Rs. ${settings.minDeliveryOrder - computedSubtotal} more or choose Takeaway."
                ))
            }

            val computedDeliveryFee = if (orderType == OrderType.DELIVERY && !settings.isFreeDelivery) {
                settings.deliveryFee
            } else 0
            val computedTotal = computedSubtotal + computedDeliveryFee

            val orderId = "ord_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            val orderCode = "LFC-${(1000..9999).random()}"

            val itemMaps = verifiedItems.map { item ->
                mapOf(
                    "itemId" to item.itemId,
                    "itemType" to item.itemType,
                    "name" to item.name,
                    "selectedSize" to item.selectedSize,
                    "extraChicken" to item.extraChicken,
                    "extraCheese" to item.extraCheese,
                    "extraCheeseSlice" to item.extraCheeseSlice,
                    "selectedSauces" to item.selectedSauces,
                    "specialInstructions" to item.specialInstructions,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitPrice,
                    "customizationCharges" to item.customizationCharges,
                    "lineTotal" to item.lineTotal
                )
            }

            val orderPayload = mapOf(
                "userId" to uid,
                "orderCode" to orderCode,
                "orderSource" to OrderSource.APP.firestoreValue,
                "orderType" to orderType.firestoreValue,
                "status" to OrderStatuses.ORDER_RECEIVED,
                "customerName" to customerName.trim().take(80),
                "customerPhone" to customerPhone.trim().take(25),
                "deliveryAddress" to deliveryAddress.trim().take(300),
                "specialInstructions" to specialInstructions.trim().take(400),
                "items" to itemMaps,
                "subtotal" to computedSubtotal,
                "deliveryFee" to computedDeliveryFee,
                "total" to computedTotal,
                "priceVerified" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()
            val orderRef = db.collection("orders").document(orderId)
            batch.set(orderRef, orderPayload)

            val adminNotifRef = db.collection("notifications").document("notif_adm_${UUID.randomUUID().toString().take(8)}")
            batch.set(
                adminNotifRef,
                mapOf(
                    "userId" to "ADMIN",
                    "senderId" to uid,
                    "targetRole" to "ADMIN",
                    "orderId" to orderId,
                    "orderCode" to orderCode,
                    "title" to "New App Order $orderCode",
                    "message" to "${customerName.trim()} placed a ${orderType.label} order for Rs. $computedTotal.",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            val custNotifRef = db.collection("notifications").document("notif_cust_${UUID.randomUUID().toString().take(8)}")
            batch.set(
                custNotifRef,
                mapOf(
                    "userId" to uid,
                    "senderId" to uid,
                    "targetRole" to "CUSTOMER",
                    "orderId" to orderId,
                    "orderCode" to orderCode,
                    "title" to "Order Received ($orderCode)",
                    "message" to "Your order for Rs. $computedTotal has been sent to Lahori Food Cafe.",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()

            Result.success(
                Order(
                    id = orderId,
                    userId = uid,
                    orderCode = orderCode,
                    orderSource = OrderSource.APP.firestoreValue,
                    orderType = orderType.firestoreValue,
                    status = OrderStatuses.ORDER_RECEIVED,
                    customerName = customerName.trim(),
                    customerPhone = customerPhone.trim(),
                    deliveryAddress = deliveryAddress.trim(),
                    specialInstructions = specialInstructions.trim(),
                    items = verifiedItems,
                    subtotal = computedSubtotal,
                    deliveryFee = computedDeliveryFee,
                    total = computedTotal,
                    priceVerified = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "orders")
            Result.failure(e)
        }
    }

    /**
     * Records a pending WhatsApp draft using deterministic orderCode ID so repeated taps
     * never create duplicate orders. Clearly marked as Pending WhatsApp Confirmation.
     */
    override suspend fun recordPendingWhatsAppOrder(
        orderCode: String,
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        specialInstructions: String,
        items: List<CartItem>,
        subtotal: Int,
        deliveryFee: Int,
        total: Int
    ): Result<String> {
        val uid = requireUserId()
        val safeId = "wa_${orderCode.replace(Regex("[^a-zA-Z0-9_-]"), "_")}"
        val docRef = db.collection("orders").document(safeId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                return Result.success(safeId)
            }
            val itemMaps = items.map { item ->
                mapOf(
                    "itemId" to item.itemId,
                    "itemType" to item.itemType,
                    "name" to item.name,
                    "selectedSize" to item.selectedSize,
                    "extraChicken" to item.extraChicken,
                    "extraCheese" to item.extraCheese,
                    "extraCheeseSlice" to item.extraCheeseSlice,
                    "selectedSauces" to item.selectedSauces,
                    "specialInstructions" to item.specialInstructions,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitBasePrice,
                    "customizationCharges" to item.customizationCharges,
                    "lineTotal" to item.lineTotal
                )
            }
            val payload = mapOf(
                "userId" to uid,
                "orderCode" to orderCode,
                "orderSource" to OrderSource.WHATSAPP.firestoreValue,
                "orderType" to orderType.firestoreValue,
                "status" to OrderStatuses.PENDING_WHATSAPP,
                "customerName" to customerName.trim().ifBlank { "Customer" }.take(80),
                "customerPhone" to customerPhone.trim().ifBlank { "Pending" }.take(25),
                "deliveryAddress" to deliveryAddress.trim().take(300),
                "specialInstructions" to specialInstructions.trim().take(400),
                "items" to itemMaps,
                "subtotal" to subtotal,
                "deliveryFee" to deliveryFee,
                "total" to total,
                "priceVerified" to true,
                "adminNote" to "Initiated via WhatsApp Click-to-Chat. Awaiting cafe confirmation.",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(safeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun cancelOrderCustomer(orderId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("orders").document(orderId)
        return try {
            docRef.update(
                mapOf(
                    "status" to OrderStatuses.CANCELLED,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("notifications").document(notificationId)
        return try {
            docRef.update("isRead", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Admin Operations ---

    suspend fun seedInitialCatalogIfEmpty(): Result<Unit> {
        requireUserId()
        return try {
            val existingCategories = db.collection("categories").limit(1).get().await()
            if (existingCategories.isEmpty) {
                val batch1 = db.batch()
                LahoriMenuCatalog.categories.forEach { cat ->
                    batch1.set(
                        db.collection("categories").document(cat.id),
                        mapOf(
                            "name" to cat.name,
                            "group" to cat.group,
                            "displayOrder" to cat.displayOrder,
                            "imageKey" to cat.imageKey,
                            "isActive" to cat.isActive,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                }
                LahoriMenuCatalog.banners.forEach { banner ->
                    batch1.set(
                        db.collection("banners").document(banner.id),
                        mapOf(
                            "title" to banner.title,
                            "subtitle" to banner.subtitle,
                            "badgeText" to banner.badgeText,
                            "imageKey" to banner.imageKey,
                            "targetRoute" to banner.targetRoute,
                            "isActive" to banner.isActive,
                            "displayOrder" to banner.displayOrder,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                }
                batch1.set(
                    db.collection("settings").document(CafeConstants.SETTINGS_DOC_ID),
                    mapOf(
                        "minDeliveryOrder" to CafeConstants.MIN_DELIVERY_ORDER,
                        "deliveryFee" to 0,
                        "isFreeDelivery" to true,
                        "isDeliveryEnabled" to true,
                        "isAcceptingOrders" to true,
                        "openingHours" to "12:00 PM - 2:00 AM Daily",
                        "closureNotice" to "",
                        "whatsappNumber" to CafeConstants.WHATSAPP_NUMBER,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                batch1.commit().await()
            }

            val existingProducts = db.collection("products").limit(1).get().await()
            if (existingProducts.isEmpty) {
                LahoriMenuCatalog.products.chunked(40).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { p ->
                        batch.set(
                            db.collection("products").document(p.id),
                            productToMap(p, isCreate = true),
                            SetOptions.merge()
                        )
                    }
                    batch.commit().await()
                }
            }

            val existingDeals = db.collection("deals").limit(1).get().await()
            if (existingDeals.isEmpty) {
                val batch = db.batch()
                LahoriMenuCatalog.deals.forEach { d ->
                    batch.set(
                        db.collection("deals").document(d.id),
                        dealToMap(d, isCreate = true),
                        SetOptions.merge()
                    )
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "catalog_seed")
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatusAdmin(order: Order, newStatus: String, adminNote: String = ""): Result<Unit> {
        val adminUid = requireUserId()
        val docRef = db.collection("orders").document(order.id)
        return try {
            val batch = db.batch()
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (adminNote.isNotBlank()) {
                updates["adminNote"] = adminNote.take(300)
            }
            batch.update(docRef, updates)

            val notifMessage = when (newStatus) {
                OrderStatuses.ORDER_CONFIRMED -> "Your order ${order.orderCode} has been confirmed by Lahori Food Cafe."
                OrderStatuses.PREPARING -> "Our kitchen has started preparing your order ${order.orderCode}!"
                OrderStatuses.READY -> "Your order ${order.orderCode} is fresh and ready!"
                OrderStatuses.OUT_FOR_DELIVERY -> "Rider is on the way with your order ${order.orderCode}!"
                OrderStatuses.COMPLETED -> "Order ${order.orderCode} completed. Thank you for dining with Lahori Food Cafe!"
                OrderStatuses.CANCELLED -> "Order ${order.orderCode} was cancelled. ${adminNote.ifBlank { "Contact 0304-3689241 for details." }}"
                else -> "Order ${order.orderCode} status updated to $newStatus."
            }

            val notifRef = db.collection("notifications").document("notif_st_${UUID.randomUUID().toString().take(8)}")
            batch.set(
                notifRef,
                mapOf(
                    "userId" to order.userId,
                    "senderId" to adminUid,
                    "targetRole" to "CUSTOMER",
                    "orderId" to order.id,
                    "orderCode" to order.orderCode,
                    "title" to "Order ${order.orderCode}: $newStatus",
                    "message" to notifMessage.take(300),
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordConfirmedWhatsAppOrderAdmin(
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        summaryText: String,
        totalAmount: Int
    ): Result<Unit> {
        val adminUid = requireUserId()
        val orderId = "wa_adm_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val orderCode = "LFC-WA-${(1000..9999).random()}"
        val docRef = db.collection("orders").document(orderId)
        return try {
            val payload = mapOf(
                "userId" to adminUid,
                "orderCode" to orderCode,
                "orderSource" to OrderSource.WHATSAPP.firestoreValue,
                "orderType" to orderType.firestoreValue,
                "status" to OrderStatuses.ORDER_CONFIRMED,
                "customerName" to customerName.trim().take(80),
                "customerPhone" to customerPhone.trim().take(25),
                "deliveryAddress" to deliveryAddress.trim().take(300),
                "specialInstructions" to summaryText.trim().take(400),
                "items" to listOf(
                    mapOf(
                        "itemId" to "whatsapp_manual",
                        "itemType" to "CUSTOM",
                        "name" to summaryText.trim().ifBlank { "Confirmed WhatsApp Order" }.take(120),
                        "selectedSize" to "",
                        "quantity" to 1,
                        "unitPrice" to totalAmount,
                        "customizationCharges" to 0,
                        "lineTotal" to totalAmount
                    )
                ),
                "subtotal" to totalAmount,
                "deliveryFee" to 0,
                "total" to totalAmount,
                "priceVerified" to true,
                "adminNote" to "Manually recorded & confirmed from WhatsApp.",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveProduct(product: Product, isNew: Boolean): Result<Unit> {
        requireUserId()
        val id = if (isNew || product.id.isBlank()) {
            "prod_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        } else product.id
        val docRef = db.collection("products").document(id)
        return try {
            if (isNew) {
                docRef.set(productToMap(product.copy(id = id), isCreate = true)).await()
            } else {
                docRef.update(productToMap(product, isCreate = false)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleProductAvailability(product: Product): Result<Unit> {
        requireUserId()
        val docRef = db.collection("products").document(product.id)
        return try {
            docRef.update(
                mapOf(
                    "isAvailable" to !product.isAvailable,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveDeal(deal: Deal, isNew: Boolean): Result<Unit> {
        requireUserId()
        val id = if (isNew || deal.id.isBlank()) {
            "deal_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        } else deal.id
        val docRef = db.collection("deals").document(id)
        return try {
            if (isNew) {
                docRef.set(dealToMap(deal.copy(id = id), isCreate = true)).await()
            } else {
                docRef.update(dealToMap(deal, isCreate = false)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleDealActive(deal: Deal): Result<Unit> {
        requireUserId()
        val docRef = db.collection("deals").document(deal.id)
        return try {
            docRef.update(
                mapOf(
                    "isActive" to !deal.isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveCategory(category: Category, isNew: Boolean): Result<Unit> {
        requireUserId()
        val id = if (isNew || category.id.isBlank()) {
            "cat_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        } else category.id
        val docRef = db.collection("categories").document(id)
        return try {
            val payload = mutableMapOf<String, Any>(
                "name" to category.name.trim(),
                "group" to category.group,
                "displayOrder" to category.displayOrder,
                "imageKey" to category.imageKey,
                "isActive" to category.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isNew) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveSettings(settings: CafeSettings): Result<Unit> {
        requireUserId()
        val docRef = db.collection("settings").document(CafeConstants.SETTINGS_DOC_ID)
        return try {
            val payload = mapOf(
                "minDeliveryOrder" to settings.minDeliveryOrder,
                "deliveryFee" to settings.deliveryFee,
                "isFreeDelivery" to settings.isFreeDelivery,
                "isDeliveryEnabled" to settings.isDeliveryEnabled,
                "isAcceptingOrders" to settings.isAcceptingOrders,
                "openingHours" to settings.openingHours.trim(),
                "closureNotice" to settings.closureNotice.trim(),
                "whatsappNumber" to settings.whatsappNumber.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    private fun productToMap(p: Product, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to p.name.trim(),
            "categoryId" to p.categoryId,
            "categoryName" to p.categoryName,
            "group" to p.group,
            "description" to p.description.trim(),
            "basePrice" to p.basePrice,
            "sizePrices" to p.sizePrices,
            "supportsPizzaCustomizations" to p.supportsPizzaCustomizations,
            "supportsExtraCheeseSlice" to p.supportsExtraCheeseSlice,
            "priceVerified" to p.priceVerified,
            "isAvailable" to p.isAvailable,
            "isActive" to p.isActive,
            "isPopular" to p.isPopular,
            "isNewArrival" to p.isNewArrival,
            "imageKey" to p.imageKey,
            "imageUrl" to p.imageUrl,
            "displayOrder" to p.displayOrder,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }

    private fun dealToMap(d: Deal, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "dealNumber" to d.dealNumber.trim(),
            "title" to d.title.trim(),
            "itemsSummary" to d.itemsSummary.trim(),
            "drinkInfo" to d.drinkInfo.trim(),
            "price" to d.price,
            "note" to d.note.trim(),
            "imageKey" to d.imageKey,
            "imageUrl" to d.imageUrl,
            "isActive" to d.isActive,
            "isFeatured" to d.isFeatured,
            "displayOrder" to d.displayOrder,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }

    private fun logStreamError(error: Throwable, op: OperationType, path: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception) ?: error
        if (Firebase.auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
