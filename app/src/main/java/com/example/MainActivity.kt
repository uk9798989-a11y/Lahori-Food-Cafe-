package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.OrderType
import com.example.data.model.Product
import com.example.data.repository.AuthRepository
import com.example.data.repository.CafeRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.ContactAndLocationScreen
import com.example.ui.screens.DealsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.LahoriCafeTheme
import com.example.ui.viewmodel.CafeViewModel
import com.example.ui.viewmodel.ShoppingCartViewModel
import com.example.util.LocalNotificationHelper
import com.example.util.WhatsAppHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

sealed interface AppDestination {
    data object Splash : AppDestination
    data object AuthGate : AppDestination
    data object Home : AppDestination
    data class Menu(val categoryId: String? = null, val query: String = "") : AppDestination
    data object Deals : AppDestination
    data object Cart : AppDestination
    data object Checkout : AppDestination
    data object Orders : AppDestination
    data object Profile : AppDestination
    data class ProductDetails(val product: Product) : AppDestination
    data object Notifications : AppDestination
    data object ContactLocation : AppDestination
    data object AdminDashboard : AppDestination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LocalNotificationHelper.ensureChannel(this)

        setContent {
            LahoriCafeTheme {
                LahoriFoodCafeApp()
            }
        }
    }
}

@Composable
fun LahoriFoodCafeApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by AuthRepository.authStateFlow(Firebase.auth)
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var showSplash by remember { mutableStateOf(true) }
    var guestMode by remember { mutableStateOf(false) }

    // Request POST_NOTIFICATIONS runtime permission on Android 13+ when signed in
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* handled gracefully */ }
    )
    LaunchedEffect(Unit) {
        AuthRepository.attemptAutoSignIn(context)
    }
    LaunchedEffect(currentUser) {
        if (currentUser != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    if (currentUser == null && !guestMode) {
        AuthGateScreen(
            onSignedIn = { guestMode = false },
            onContinueAsGuest = { guestMode = true }
        )
        return
    }

    val uid = currentUser?.uid
    val sessionKey = uid ?: "guest"

    val repository = remember(uid) {
        val dbId = context.getString(R.string.firestore_database_id)
        CafeRepository(FirebaseFirestore.getInstance(dbId))
    }

    val viewModel: CafeViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                CafeViewModel(
                    repository = repository,
                    appContext = app.applicationContext,
                    currentUserId = uid,
                    userDisplayName = currentUser?.displayName,
                    userEmail = currentUser?.email,
                    emailVerified = currentUser?.isEmailVerified ?: false
                )
            }
        }
    )

    val cartViewModel: ShoppingCartViewModel = viewModel(
        key = "cart_$sessionKey",
        factory = viewModelFactory {
            initializer {
                ShoppingCartViewModel(
                    orderGateway = repository,
                    currentUserId = uid,
                    defaultUserName = currentUser?.displayName
                )
            }
        }
    )

    MainAppShell(
        viewModel = viewModel,
        cartViewModel = cartViewModel,
        isSignedIn = currentUser != null,
        onSignOut = {
            scope.launch {
                AuthRepository.signOut(context)
                guestMode = false
            }
        },
        onSignedIn = { guestMode = false }
    )
}

@Composable
private fun MainAppShell(
    viewModel: CafeViewModel,
    cartViewModel: ShoppingCartViewModel,
    isSignedIn: Boolean,
    onSignOut: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val deals by viewModel.deals.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val allOrdersAdmin by viewModel.allOrdersAdmin.collectAsStateWithLifecycle()
    val allProductsAdmin by viewModel.allProductsAdmin.collectAsStateWithLifecycle()
    val allDealsAdmin by viewModel.allDealsAdmin.collectAsStateWithLifecycle()
    val allCategoriesAdmin by viewModel.allCategoriesAdmin.collectAsStateWithLifecycle()
    val adminNotifications by viewModel.adminNotifications.collectAsStateWithLifecycle()
    val cafeActionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartActionMessage by cartViewModel.actionMessage.collectAsStateWithLifecycle()

    LaunchedEffect(settings) {
        cartViewModel.syncCafeSettings(settings)
    }
    LaunchedEffect(userProfile, viewModel.userDisplayName) {
        cartViewModel.prefillCustomerProfile(userProfile, viewModel.userDisplayName)
    }

    var currentDest by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
    var previousDest by remember { mutableStateOf<AppDestination>(AppDestination.Home) }

    fun navigateTo(dest: AppDestination) {
        if (currentDest != dest) {
            previousDest = currentDest
            currentDest = dest
        }
    }

    LaunchedEffect(cafeActionMessage) {
        cafeActionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(cartActionMessage) {
        cartActionMessage?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearActionMessage()
        }
    }

    if (currentDest !is AppDestination.Home) {
        BackHandler {
            currentDest = when (currentDest) {
                is AppDestination.ProductDetails -> previousDest
                is AppDestination.Checkout -> AppDestination.Cart
                is AppDestination.Notifications,
                is AppDestination.ContactLocation,
                is AppDestination.AdminDashboard -> AppDestination.Profile
                else -> AppDestination.Home
            }
        }
    }

    val cartCount = cartUiState.totalItemCount
    val unreadCount = userNotifications.count { !it.isRead }
    val showBottomNav = currentDest in listOf(
        AppDestination.Home,
        AppDestination.Deals,
        AppDestination.Cart,
        AppDestination.Orders,
        AppDestination.Profile
    ) || currentDest is AppDestination.Menu

    val navItems = listOf(
        Triple("Home", Icons.Default.Home, AppDestination.Home),
        Triple("Menu", Icons.Default.RestaurantMenu, AppDestination.Menu()),
        Triple("Deals", Icons.Default.LocalOffer, AppDestination.Deals),
        Triple("Cart", Icons.Default.ShoppingCart, AppDestination.Cart),
        Triple("Orders", Icons.Default.ReceiptLong, AppDestination.Orders),
        Triple("Account", Icons.Default.Person, AppDestination.Profile)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomNav && !useRail) {
                    NavigationBar {
                        navItems.forEach { (label, icon, dest) ->
                            val selected = (currentDest::class == dest::class)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest) },
                                icon = {
                                    if (label == "Cart" && cartCount > 0) {
                                        BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                            Icon(icon, contentDescription = label)
                                        }
                                    } else {
                                        Icon(icon, contentDescription = label)
                                    }
                                },
                                label = { Text(label, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showBottomNav && useRail) {
                    NavigationRail {
                        navItems.forEach { (label, icon, dest) ->
                            val selected = (currentDest::class == dest::class)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest) },
                                icon = {
                                    if (label == "Cart" && cartCount > 0) {
                                        BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                            Icon(icon, contentDescription = label)
                                        }
                                    } else {
                                        Icon(icon, contentDescription = label)
                                    }
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()) {
                        when (val dest = currentDest) {
                            is AppDestination.Home -> HomeScreen(
                                userName = userProfile?.displayName ?: viewModel.userDisplayName,
                                categories = categories,
                                products = products,
                                deals = deals,
                                banners = banners,
                                settings = settings,
                                cartCount = cartCount,
                                unreadNotificationCount = unreadCount,
                                onNavigateToMenu = { catId, q -> navigateTo(AppDestination.Menu(catId, q)) },
                                onNavigateToDeals = { navigateTo(AppDestination.Deals) },
                                onNavigateToCart = { navigateTo(AppDestination.Cart) },
                                onNavigateToNotifications = { navigateTo(AppDestination.Notifications) },
                                onNavigateToLocation = { navigateTo(AppDestination.ContactLocation) },
                                onProductClick = { p -> navigateTo(AppDestination.ProductDetails(p)) },
                                onAddDealToCart = { d -> cartViewModel.addDealToCart(d) },
                                onWhatsAppQuickOrder = { name, phone, details, orderType, addr ->
                                    val msg = WhatsAppHelper.buildOrderMessage(
                                        orderReference = cartUiState.whatsAppOrderRef,
                                        customerName = name,
                                        customerPhone = phone,
                                        orderType = orderType,
                                        deliveryAddress = addr,
                                        specialInstructions = details,
                                        items = emptyList(),
                                        subtotal = 0,
                                        deliveryFee = 0,
                                        finalTotal = 0
                                    )
                                    WhatsAppHelper.openWhatsAppWithMessage(context, msg)
                                }
                            )

                            is AppDestination.Menu -> MenuScreen(
                                categories = categories,
                                products = products,
                                initialCategoryId = dest.categoryId,
                                initialQuery = dest.query,
                                onProductClick = { p -> navigateTo(AppDestination.ProductDetails(p)) }
                            )

                            is AppDestination.Deals -> DealsScreen(
                                deals = deals,
                                onAddDealToCart = { d -> cartViewModel.addDealToCart(d) },
                                onWhatsAppDeal = { d ->
                                    cartViewModel.addDealToCart(d)
                                    navigateTo(AppDestination.Checkout)
                                }
                            )

                            is AppDestination.ProductDetails -> ProductDetailScreen(
                                product = dest.product,
                                onBack = { currentDest = previousDest },
                                onAddToCart = { prod, size, chicken, cheese, slice, sauces, note, qty ->
                                    cartViewModel.addProductToCart(prod, size, chicken, cheese, slice, sauces, note, qty)
                                    currentDest = previousDest
                                }
                            )

                            is AppDestination.Cart -> CartScreen(
                                uiState = cartUiState,
                                onIncrementQty = { k -> cartViewModel.incrementQuantity(k) },
                                onDecrementQty = { k -> cartViewModel.decrementQuantity(k) },
                                onUpdateItemToppings = { item, ch, cheese, slice, sauces ->
                                    cartViewModel.updateItemToppings(item.cartKey, ch, cheese, slice, sauces)
                                },
                                onClearCart = { cartViewModel.clearCart() },
                                onProceedToCheckout = { navigateTo(AppDestination.Checkout) },
                                onBrowseMenu = { navigateTo(AppDestination.Menu()) }
                            )

                            is AppDestination.Checkout -> CheckoutScreen(
                                cartViewModel = cartViewModel,
                                isSignedIn = isSignedIn,
                                userProfile = userProfile,
                                onBack = { currentDest = AppDestination.Cart },
                                onDirectOrderSuccess = { currentDest = AppDestination.Orders },
                                onBrowseMenu = { navigateTo(AppDestination.Menu()) },
                                onSignedIn = onSignedIn
                            )

                            is AppDestination.Orders -> OrdersScreen(
                                isSignedIn = isSignedIn,
                                orders = userOrders,
                                onCancelOrder = { id -> viewModel.cancelMyOrder(id) },
                                onReorder = { o -> cartViewModel.reorderFromOrder(o, products, deals) },
                                onGoToCart = { currentDest = AppDestination.Cart },
                                onSignedIn = onSignedIn
                            )

                            is AppDestination.Profile -> ProfileScreen(
                                isSignedIn = isSignedIn,
                                isAdmin = isAdmin,
                                userProfile = userProfile,
                                userDisplayName = viewModel.userDisplayName,
                                userEmail = viewModel.userEmail,
                                unreadNotifications = unreadCount,
                                onSaveProfile = { n, p, addrs -> viewModel.updateProfile(n, p, addrs) },
                                onOpenNotifications = { navigateTo(AppDestination.Notifications) },
                                onOpenContactLocation = { navigateTo(AppDestination.ContactLocation) },
                                onOpenAdminDashboard = { navigateTo(AppDestination.AdminDashboard) },
                                onSignOut = onSignOut,
                                onSignedIn = onSignedIn
                            )

                            is AppDestination.Notifications -> NotificationsScreen(
                                notifications = userNotifications,
                                onMarkRead = { id -> viewModel.markNotificationRead(id) },
                                onBack = { currentDest = AppDestination.Profile }
                            )

                            is AppDestination.ContactLocation -> ContactAndLocationScreen(
                                settings = settings,
                                onBack = { currentDest = AppDestination.Profile }
                            )

                            is AppDestination.AdminDashboard -> AdminDashboardScreen(
                                isSignedIn = isSignedIn,
                                isAdmin = isAdmin,
                                userEmail = viewModel.userEmail,
                                orders = allOrdersAdmin,
                                products = allProductsAdmin,
                                deals = allDealsAdmin,
                                categories = allCategoriesAdmin,
                                settings = settings,
                                notifications = adminNotifications,
                                salesSummary = viewModel.computeSalesSummary(allOrdersAdmin),
                                onBack = { currentDest = AppDestination.Profile },
                                onUpdateOrderStatus = { o, st, note -> viewModel.adminUpdateOrderStatus(o, st, note) },
                                onRecordWhatsAppOrder = { n, p, t, a, s, tot ->
                                    viewModel.adminRecordWhatsAppOrder(n, p, t, a, s, tot)
                                },
                                onSaveProduct = { p, isNew -> viewModel.adminSaveProduct(p, isNew) },
                                onToggleProductAvailable = { p -> viewModel.adminToggleProductAvailability(p) },
                                onSaveDeal = { d, isNew -> viewModel.adminSaveDeal(d, isNew) },
                                onToggleDealActive = { d -> viewModel.adminToggleDealActive(d) },
                                onSaveCategory = { c, isNew -> viewModel.adminSaveCategory(c, isNew) },
                                onSaveSettings = { s -> viewModel.adminSaveSettings(s) }
                            )

                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}
