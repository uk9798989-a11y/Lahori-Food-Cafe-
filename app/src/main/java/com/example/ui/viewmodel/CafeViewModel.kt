package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.Banner
import com.example.data.model.CafeSettings
import com.example.data.model.Category
import com.example.data.model.Deal
import com.example.data.model.LahoriMenuCatalog
import com.example.data.model.Order
import com.example.data.model.OrderSource
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.repository.CafeRepository
import com.example.util.LocalNotificationHelper
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class SalesSummary(
    val todayOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalOrders: Int = 0,
    val todaySales: Int = 0,
    val weeklySales: Int = 0,
    val monthlySales: Int = 0,
    val totalRevenue: Int = 0,
    val appOrdersCount: Int = 0,
    val whatsappOrdersCount: Int = 0
)

class CafeViewModel(
    private val repository: CafeRepository,
    private val appContext: Context,
    val currentUserId: String?,
    val userDisplayName: String?,
    val userEmail: String?,
    val emailVerified: Boolean
) : ViewModel() {

    private val isSignedIn: Boolean = !currentUserId.isNullOrBlank()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // Catalog streams: live Firestore when signed in, authoritative local catalog when guest
    val categories: StateFlow<List<Category>> = (
        if (isSignedIn) {
            repository.observeCategories(includeInactive = false)
                .map { if (it.isEmpty()) LahoriMenuCatalog.categories else it }
                .catch { emit(LahoriMenuCatalog.categories) }
        } else {
            flowOf(LahoriMenuCatalog.categories)
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LahoriMenuCatalog.categories)

    val products: StateFlow<List<Product>> = (
        if (isSignedIn) {
            repository.observeProducts(includeInactive = false)
                .map { if (it.isEmpty()) LahoriMenuCatalog.products else it }
                .catch { emit(LahoriMenuCatalog.products) }
        } else {
            flowOf(LahoriMenuCatalog.products)
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LahoriMenuCatalog.products)

    val deals: StateFlow<List<Deal>> = (
        if (isSignedIn) {
            repository.observeDeals(includeInactive = false)
                .map { if (it.isEmpty()) LahoriMenuCatalog.deals else it }
                .catch { emit(LahoriMenuCatalog.deals) }
        } else {
            flowOf(LahoriMenuCatalog.deals)
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LahoriMenuCatalog.deals)

    val banners: StateFlow<List<Banner>> = (
        if (isSignedIn) {
            repository.observeBanners(includeInactive = false)
                .map { if (it.isEmpty()) LahoriMenuCatalog.banners else it }
                .catch { emit(LahoriMenuCatalog.banners) }
        } else {
            flowOf(LahoriMenuCatalog.banners)
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LahoriMenuCatalog.banners)

    val settings: StateFlow<CafeSettings> = (
        if (isSignedIn) {
            repository.observeSettings().catch { emit(CafeSettings()) }
        } else {
            flowOf(CafeSettings())
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CafeSettings())

    val userProfile: StateFlow<UserProfile?> = (
        if (currentUserId != null) {
            repository.observeUserProfile(currentUserId).catch { emit(null) }
        } else {
            flowOf(null)
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val userOrders: StateFlow<List<Order>> = (
        if (currentUserId != null) {
            repository.observeUserOrders(currentUserId).catch { emit(emptyList()) }
        } else {
            flowOf(emptyList())
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userNotifications: StateFlow<List<AppNotification>> = (
        if (currentUserId != null) {
            repository.observeUserNotifications(currentUserId).catch { emit(emptyList()) }
        } else {
            flowOf(emptyList())
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Admin streams (active only after admin verification)
    private val _allOrdersAdmin = MutableStateFlow<List<Order>>(emptyList())
    val allOrdersAdmin: StateFlow<List<Order>> = _allOrdersAdmin.asStateFlow()

    private val _allProductsAdmin = MutableStateFlow<List<Product>>(LahoriMenuCatalog.products)
    val allProductsAdmin: StateFlow<List<Product>> = _allProductsAdmin.asStateFlow()

    private val _allDealsAdmin = MutableStateFlow<List<Deal>>(LahoriMenuCatalog.deals)
    val allDealsAdmin: StateFlow<List<Deal>> = _allDealsAdmin.asStateFlow()

    private val _allCategoriesAdmin = MutableStateFlow<List<Category>>(LahoriMenuCatalog.categories)
    val allCategoriesAdmin: StateFlow<List<Category>> = _allCategoriesAdmin.asStateFlow()

    private val _adminNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val adminNotifications: StateFlow<List<AppNotification>> = _adminNotifications.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val seenNotificationIds = mutableSetOf<String>()
    private var initialNotifSyncDone = false

    init {
        if (currentUserId != null) {
            viewModelScope.launch {
                repository.ensureUserProfile(
                    currentUserId,
                    userDisplayName ?: "Lahori Cafe Guest",
                    userEmail ?: ""
                )
                val admin = repository.checkIsAdmin(currentUserId, userEmail, emailVerified)
                _isAdmin.value = admin
                if (admin) {
                    repository.seedInitialCatalogIfEmpty()
                    startAdminStreams()
                }
            }
            viewModelScope.launch {
                userNotifications.collect { list ->
                    notifyNewUnread(list)
                }
            }
        }
    }

    private fun startAdminStreams() {
        viewModelScope.launch {
            repository.observeAllOrdersAdmin()
                .catch { /* logged in repo */ }
                .collect { _allOrdersAdmin.value = it }
        }
        viewModelScope.launch {
            repository.observeProducts(includeInactive = true)
                .catch { /* logged in repo */ }
                .collect { if (it.isNotEmpty()) _allProductsAdmin.value = it }
        }
        viewModelScope.launch {
            repository.observeDeals(includeInactive = true)
                .catch { /* logged in repo */ }
                .collect { if (it.isNotEmpty()) _allDealsAdmin.value = it }
        }
        viewModelScope.launch {
            repository.observeCategories(includeInactive = true)
                .catch { /* logged in repo */ }
                .collect { if (it.isNotEmpty()) _allCategoriesAdmin.value = it }
        }
        viewModelScope.launch {
            repository.observeAdminNotifications()
                .catch { /* logged in repo */ }
                .collect { list ->
                    _adminNotifications.value = list
                    notifyNewUnread(list)
                }
        }
    }

    private fun notifyNewUnread(list: List<AppNotification>) {
        if (!initialNotifSyncDone) {
            list.forEach { seenNotificationIds += it.id }
            initialNotifSyncDone = true
            return
        }
        list.filter { !it.isRead && it.id.isNotBlank() && it.id !in seenNotificationIds }
            .forEach { notif ->
                seenNotificationIds += notif.id
                LocalNotificationHelper.showNotification(
                    appContext,
                    notif.id.hashCode(),
                    notif.title,
                    notif.message
                )
            }
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun updateProfile(name: String, phone: String, addresses: List<String>) {
        if (currentUserId == null) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateUserProfile(name, phone, addresses)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) "Profile updated" else (res.exceptionOrNull()?.message ?: "Update failed")
        }
    }

    fun cancelMyOrder(orderId: String) {
        viewModelScope.launch {
            val res = repository.cancelOrderCustomer(orderId)
            _actionMessage.value = if (res.isSuccess) "Order cancelled" else (res.exceptionOrNull()?.message ?: "Could not cancel order")
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch { repository.markNotificationRead(notificationId) }
    }

    // --- Admin Actions ---

    fun adminUpdateOrderStatus(order: Order, newStatus: String, note: String = "") {
        viewModelScope.launch {
            val res = repository.updateOrderStatusAdmin(order, newStatus, note)
            _actionMessage.value = if (res.isSuccess) "Order ${order.orderCode} -> $newStatus" else (res.exceptionOrNull()?.message ?: "Update failed")
        }
    }

    fun adminRecordWhatsAppOrder(
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        summaryText: String,
        totalAmount: Int
    ) {
        viewModelScope.launch {
            val res = repository.recordConfirmedWhatsAppOrderAdmin(
                customerName, customerPhone, orderType, deliveryAddress, summaryText, totalAmount
            )
            _actionMessage.value = if (res.isSuccess) "WhatsApp order recorded & confirmed" else (res.exceptionOrNull()?.message ?: "Failed")
        }
    }

    fun adminSaveProduct(product: Product, isNew: Boolean) {
        viewModelScope.launch {
            val res = repository.saveProduct(product, isNew)
            _actionMessage.value = if (res.isSuccess) "Saved ${product.name}" else (res.exceptionOrNull()?.message ?: "Save failed")
        }
    }

    fun adminToggleProductAvailability(product: Product) {
        viewModelScope.launch {
            val res = repository.toggleProductAvailability(product)
            if (res.isFailure) _actionMessage.value = res.exceptionOrNull()?.message
        }
    }

    fun adminSaveDeal(deal: Deal, isNew: Boolean) {
        viewModelScope.launch {
            val res = repository.saveDeal(deal, isNew)
            _actionMessage.value = if (res.isSuccess) "Saved ${deal.dealNumber}" else (res.exceptionOrNull()?.message ?: "Save failed")
        }
    }

    fun adminToggleDealActive(deal: Deal) {
        viewModelScope.launch {
            val res = repository.toggleDealActive(deal)
            if (res.isFailure) _actionMessage.value = res.exceptionOrNull()?.message
        }
    }

    fun adminSaveCategory(category: Category, isNew: Boolean) {
        viewModelScope.launch {
            val res = repository.saveCategory(category, isNew)
            _actionMessage.value = if (res.isSuccess) "Saved category ${category.name}" else (res.exceptionOrNull()?.message ?: "Failed")
        }
    }

    fun adminSaveSettings(newSettings: CafeSettings) {
        viewModelScope.launch {
            val res = repository.saveSettings(newSettings)
            _actionMessage.value = if (res.isSuccess) "Cafe settings saved" else (res.exceptionOrNull()?.message ?: "Save failed")
        }
    }

    fun computeSalesSummary(orders: List<Order>): SalesSummary {
        val nowCal = Calendar.getInstance()
        val todayStart = (nowCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000L

        val weekStart = todayStart - 6 * 86400L
        val monthStart = todayStart - 29 * 86400L

        var todayOrders = 0
        var pending = 0
        var completed = 0
        var cancelled = 0
        var todaySales = 0
        var weeklySales = 0
        var monthlySales = 0
        var totalRevenue = 0
        var appCount = 0
        var waCount = 0

        for (o in orders) {
            val sec = (o.createdAt ?: Timestamp.now()).seconds
            if (sec >= todayStart) todayOrders++
            if (o.orderSource == OrderSource.WHATSAPP.firestoreValue) waCount++ else appCount++

            when (o.status) {
                OrderStatuses.COMPLETED -> {
                    completed++
                    totalRevenue += o.total
                    if (sec >= todayStart) todaySales += o.total
                    if (sec >= weekStart) weeklySales += o.total
                    if (sec >= monthStart) monthlySales += o.total
                }
                OrderStatuses.CANCELLED -> cancelled++
                else -> {
                    pending++
                    // Count confirmed active non-pending-WA orders in today's active sales if non-cancelled?
                    // Keep revenue strict on non-cancelled vs completed: let's count completed + active confirmed in revenue, or completed only.
                    // Counting completed separately and active revenue clearly:
                    if (o.status != OrderStatuses.PENDING_WHATSAPP) {
                        totalRevenue += o.total
                        if (sec >= todayStart) todaySales += o.total
                        if (sec >= weekStart) weeklySales += o.total
                        if (sec >= monthStart) monthlySales += o.total
                    }
                }
            }
        }
        return SalesSummary(
            todayOrders = todayOrders,
            pendingOrders = pending,
            completedOrders = completed,
            cancelledOrders = cancelled,
            totalOrders = orders.size,
            todaySales = todaySales,
            weeklySales = weeklySales,
            monthlySales = monthlySales,
            totalRevenue = totalRevenue,
            appOrdersCount = appCount,
            whatsappOrdersCount = waCount
        )
    }
}
