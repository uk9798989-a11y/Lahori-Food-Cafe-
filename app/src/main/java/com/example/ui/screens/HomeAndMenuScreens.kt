package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Banner
import com.example.data.model.CafeConstants
import com.example.data.model.CafeSettings
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Deal
import com.example.data.model.OrderType
import com.example.data.model.PizzaPricingRules
import com.example.data.model.Product
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MaroonDark
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppGreen

@Composable
fun HomeScreen(
    userName: String?,
    categories: List<Category>,
    products: List<Product>,
    deals: List<Deal>,
    banners: List<Banner>,
    settings: CafeSettings,
    cartCount: Int,
    unreadNotificationCount: Int,
    onNavigateToMenu: (categoryId: String?, query: String) -> Unit,
    onNavigateToDeals: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddDealToCart: (Deal) -> Unit,
    onWhatsAppQuickOrder: (customerName: String, phone: String, details: String, orderType: OrderType, address: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showQuickWhatsAppDialog by remember { mutableStateOf(false) }

    val popularProducts = remember(products) { products.filter { it.isPopular && it.isAvailable }.take(8) }
    val newArrivals = remember(products) { products.filter { it.isNewArrival && it.isAvailable }.take(8) }
    val featuredDeals = remember(deals) { deals.filter { it.isFeatured }.ifEmpty { deals.take(5) } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branded Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(MaroonDark, MaroonPrimary))
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF7F8F2),
                            border = BorderStroke(2.dp, GoldBright),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_lahori_logo),
                                contentDescription = "Lahori Food Cafe Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = CafeConstants.BUSINESS_NAME,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Welcome, ${userName?.takeIf { it.isNotBlank() } ?: "Food Lover"}!",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldBright
                            )
                        }

                        IconButton(onClick = onNavigateToNotifications) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(containerColor = GoldBright, contentColor = CharcoalBlack) {
                                            Text("$unreadNotificationCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateToCart,
                            modifier = Modifier.testTag("home_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = GoldBright, contentColor = CharcoalBlack) {
                                            Text("$cartCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping Cart",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (settings.isAcceptingOrders) SuccessGreen else MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (settings.isAcceptingOrders) "OPEN • ${settings.openingHours}" else "TEMPORARILY CLOSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = GoldBright,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { onNavigateToLocation() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (settings.isFreeDelivery) "Free Delivery (Min Rs. ${settings.minDeliveryOrder})" else "Min Rs. ${settings.minDeliveryOrder}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CharcoalBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Pizza, Zinger, Roll, Deals...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onNavigateToMenu(null, searchQuery) }) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = "Go to Search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = GoldBright,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_field")
                    )

                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onNavigateToMenu(null, searchQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldBright, contentColor = CharcoalBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Search \"$searchQuery\" in Menu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Promotional Banners Pager
        if (banners.isNotEmpty()) {
            item {
                val pagerState = rememberPagerState(pageCount = { banners.size })
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp
                    ) { page ->
                        val banner = banners[page]
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                                .clickable {
                                    if (banner.targetRoute == "deals") onNavigateToDeals()
                                    else onNavigateToMenu(null, "")
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                FoodImage(
                                    imageKey = banner.imageKey,
                                    imageUrl = "",
                                    contentDescription = banner.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(CharcoalBlack.copy(alpha = 0.88f), MaroonPrimary.copy(alpha = 0.55f), Color.Transparent)
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(16.dp)
                                        .fillMaxWidth(0.75f)
                                ) {
                                    BadgePill(banner.badgeText, GoldBright, CharcoalBlack)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = banner.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = banner.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Order Now & WhatsApp Order CTAs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigateToMenu(null, "") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("order_now_button")
                ) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Now", fontWeight = FontWeight.Bold)
                }

                WhatsAppOrderButton(
                    text = "WhatsApp Order",
                    onClick = {
                        if (cartCount > 0) {
                            onNavigateToCart()
                        } else {
                            showQuickWhatsAppDialog = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("home_whatsapp_order_button")
                )
            }
        }

        // Food Categories Horizontal Strip
        item {
            Column {
                SectionHeader(
                    title = "Food Categories",
                    actionLabel = "Full Menu",
                    onAction = { onNavigateToMenu(null, "") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .width(114.dp)
                                .clickable { onNavigateToMenu(cat.id, "") }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FoodImage(
                                    imageKey = cat.imageKey,
                                    imageUrl = "",
                                    contentDescription = cat.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Special Deals Carousel
        item {
            Column {
                SectionHeader(
                    title = "Special Deals",
                    actionLabel = "All ${deals.size} Deals",
                    onAction = onNavigateToDeals
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredDeals, key = { it.id }) { deal ->
                        Box(modifier = Modifier.width(305.dp)) {
                            DealCard(
                                deal = deal,
                                onAddToCart = { onAddDealToCart(deal) },
                                onWhatsAppDeal = {
                                    onAddDealToCart(deal)
                                    onNavigateToCart()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Popular Products
        item {
            Column {
                SectionHeader(
                    title = "Popular at Lahori Cafe",
                    actionLabel = "See All",
                    onAction = { onNavigateToMenu(null, "") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    popularProducts.take(4).forEach { prod ->
                        ProductCard(product = prod, onClick = { onProductClick(prod) })
                    }
                }
            }
        }

        // New Arrivals
        if (newArrivals.isNotEmpty()) {
            item {
                Column {
                    SectionHeader(
                        title = "New Arrivals",
                        actionLabel = "Explore",
                        onAction = { onNavigateToMenu(null, "") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(newArrivals, key = { it.id }) { prod ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, GoldBright.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { onProductClick(prod) }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    FoodImage(
                                        imageKey = prod.imageKey,
                                        imageUrl = prod.imageUrl,
                                        contentDescription = prod.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BadgePill("NEW ARRIVAL", MaroonPrimary, Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Rs. ${prod.startingPrice()}", style = MaterialTheme.typography.titleMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cafe Address Quick Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToLocation() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Visit or Call Us", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(CafeConstants.LOCATION_ADDRESS, style = MaterialTheme.typography.bodySmall)
                        Text("Phone / WhatsApp: ${CafeConstants.PHONE_DISPLAY}", style = MaterialTheme.typography.labelMedium, color = MaroonPrimary)
                    }
                }
            }
        }
    }

    if (showQuickWhatsAppDialog) {
        QuickWhatsAppOrderDialog(
            defaultName = userName ?: "",
            onDismiss = { showQuickWhatsAppDialog = false },
            onSend = { name, phone, details, orderType, address ->
                showQuickWhatsAppDialog = false
                onWhatsAppQuickOrder(name, phone, details, orderType, address)
            }
        )
    }
}

@Composable
private fun QuickWhatsAppOrderDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onSend: (String, String, String, OrderType, String) -> Unit
) {
    var name by remember { mutableStateOf(defaultName) }
    var phone by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var orderType by remember { mutableStateOf(OrderType.DELIVERY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick WhatsApp Order", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Send your order directly to Lahori Food Cafe (${CafeConstants.WHATSAPP_NUMBER}). Add items to Cart for automatic itemized totals, or enter your request below:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrderType.entries.forEach { type ->
                        FilterChip(
                            selected = orderType == type,
                            onClick = { orderType = type },
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                if (orderType == OrderType.DELIVERY) {
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Delivery Address") })
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Order Items & Instructions") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(name, phone, details, orderType, address) },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Text("Open WhatsApp")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAction) {
            Text(actionLabel, color = MaroonPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuScreen(
    categories: List<Category>,
    products: List<Product>,
    initialCategoryId: String?,
    initialQuery: String,
    onProductClick: (Product) -> Unit
) {
    var selectedGroup by remember { mutableStateOf("ALL") }
    var selectedCategoryId by remember(initialCategoryId) { mutableStateOf(initialCategoryId) }
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }

    val filteredCategories = remember(categories, selectedGroup) {
        if (selectedGroup == "ALL") categories else categories.filter { it.group == selectedGroup }
    }

    val filteredProducts = remember(products, selectedGroup, selectedCategoryId, query) {
        products.filter { p ->
            val groupMatch = selectedGroup == "ALL" || p.group == selectedGroup
            val catMatch = selectedCategoryId == null || p.categoryId == selectedCategoryId
            val queryMatch = query.isBlank() ||
                p.name.contains(query, ignoreCase = true) ||
                p.categoryName.contains(query, ignoreCase = true) ||
                p.description.contains(query, ignoreCase = true)
            groupMatch && catMatch && queryMatch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Complete Digital Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search all pizzas, burgers, rolls, pastas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = GoldBright,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All Menu", "PIZZA" to "Pizza Menu", "FAST_FOOD" to "Fast Food").forEach { (key, label) ->
                        FilterChip(
                            selected = selectedGroup == key,
                            onClick = {
                                selectedGroup = key
                                selectedCategoryId = null
                            },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldBright,
                                selectedLabelColor = CharcoalBlack,
                                containerColor = MaroonDark,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("All Categories") }
                )
            }
            items(filteredCategories, key = { it.id }) { cat ->
                FilterChip(
                    selected = selectedCategoryId == cat.id,
                    onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                    label = { Text(cat.name) }
                )
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching menu items found.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { prod ->
                    ProductCard(product = prod, onClick = { onProductClick(prod) })
                }
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (
        product: Product,
        selectedSize: String,
        extraChicken: Boolean,
        extraCheese: Boolean,
        extraCheeseSlice: Boolean,
        selectedSauces: List<String>,
        specialInstructions: String,
        quantity: Int
    ) -> Unit
) {
    val orderedSizes = remember(product) { product.orderedSizes() }
    var selectedSize by remember(product) {
        mutableStateOf(orderedSizes.firstOrNull()?.first ?: "")
    }
    var extraChicken by remember { mutableStateOf(false) }
    var extraCheese by remember { mutableStateOf(false) }
    var extraCheeseSlice by remember { mutableStateOf(false) }
    val selectedSauces = remember { mutableStateListOf<String>() }
    var specialInstructions by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    val baseUnitPrice = if (orderedSizes.isNotEmpty()) {
        orderedSizes.firstOrNull { it.first == selectedSize }?.second ?: product.basePrice
    } else {
        product.basePrice
    }

    val toppingRate = if (product.supportsPizzaCustomizations) {
        PizzaPricingRules.extraToppingPrice(selectedSize)
    } else null

    val extraChickenCharge = if (extraChicken && toppingRate != null) toppingRate else 0
    val extraCheeseCharge = if (extraCheese && toppingRate != null) toppingRate else 0
    val cheeseSliceCharge = if (product.supportsExtraCheeseSlice && extraCheeseSlice) PizzaPricingRules.BURGER_CHEESE_SLICE_PRICE else 0
    val saucesCharge = if (product.supportsPizzaCustomizations) selectedSauces.size * PizzaPricingRules.SAUCE_PRICE else 0
    val totalAdditional = extraChickenCharge + extraCheeseCharge + cheeseSliceCharge + saucesCharge
    val finalTotal = (baseUnitPrice + totalAdditional) * quantity

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            FoodImage(
                imageKey = product.imageKey,
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Black.copy(alpha = 0.65f))))
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                BadgePill(product.categoryName, GoldBright, CharcoalBlack)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (product.isAvailable) SuccessGreen.copy(alpha = 0.14f) else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (product.isAvailable) "Available Today" else "Currently Unavailable",
                        color = if (product.isAvailable) SuccessGreen else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (product.priceVerified) {
                    Text(
                        text = "Rs. $baseUnitPrice",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    BadgePill("Price Pending Admin Verification", WarningAmber.copy(alpha = 0.2f), WarningAmber)
                }
            }

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!product.priceVerified) {
                Surface(
                    color = WarningAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarningAmber)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This drink's price is not listed on the printed flyer and is awaiting Admin verification. Please contact 0304-3689241 or ask via WhatsApp.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Size Selector (only when product has sizes)
            if (orderedSizes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        orderedSizes.forEach { (sizeLabel, price) ->
                            val isSelected = selectedSize == sizeLabel
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSize = sizeLabel
                                    if (PizzaPricingRules.extraToppingPrice(sizeLabel) == null) {
                                        extraChicken = false
                                        extraCheese = false
                                    }
                                },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(sizeLabel, fontWeight = FontWeight.Bold)
                                        Text("Rs. $price", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaroonPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Pizza Customizations (ONLY for Pizza items)
            if (product.supportsPizzaCustomizations) {
                HorizontalDivider()
                Text("Pizza Customizations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                if (toppingRate != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { extraChicken = !extraChicken },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = extraChicken, onCheckedChange = { extraChicken = it })
                                Text("Extra Chicken ($selectedSize)", modifier = Modifier.weight(1f))
                                Text("+ Rs. $toppingRate", fontWeight = FontWeight.Bold, color = MaroonPrimary)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { extraCheese = !extraCheese },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = extraCheese, onCheckedChange = { extraCheese = it })
                                Text("Extra Cheese ($selectedSize)", modifier = Modifier.weight(1f))
                                Text("+ Rs. $toppingRate", fontWeight = FontWeight.Bold, color = MaroonPrimary)
                            }
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaroonPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extra Chicken/Cheese pricing for $selectedSize is not printed on the flyer (left for cafe admin verification). You may note your request in Special Instructions.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Text("Add Sauces (Rs. ${PizzaPricingRules.SAUCE_PRICE} each)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        PizzaPricingRules.availableSauces.forEach { sauce ->
                            val checked = sauce in selectedSauces
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (checked) selectedSauces.remove(sauce) else selectedSauces.add(sauce)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        if (it) selectedSauces.add(sauce) else selectedSauces.remove(sauce)
                                    }
                                )
                                Text(sauce, modifier = Modifier.weight(1f))
                                Text("+ Rs. ${PizzaPricingRules.SAUCE_PRICE}", fontWeight = FontWeight.SemiBold, color = MaroonPrimary)
                            }
                        }
                    }
                }
            }

            // Burger Extra Cheese Slice (ONLY for Burgers)
            if (product.supportsExtraCheeseSlice) {
                HorizontalDivider()
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { extraCheeseSlice = !extraCheeseSlice }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = extraCheeseSlice, onCheckedChange = { extraCheeseSlice = it })
                        Text("Extra Cheese Slice", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Text("+ Rs. ${PizzaPricingRules.BURGER_CHEESE_SLICE_PRICE}", fontWeight = FontWeight.Bold, color = MaroonPrimary)
                    }
                }
            }

            // Additional Charges breakdown
            if (totalAdditional > 0) {
                Surface(
                    color = GoldBright.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customization Charges:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("+ Rs. $totalAdditional", style = MaterialTheme.typography.titleSmall, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = specialInstructions,
                onValueChange = { specialInstructions = it },
                label = { Text("Special Instructions (optional)") },
                placeholder = { Text("e.g., Less spicy, well done, extra napkins") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bottom Add to Cart bar
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                    }
                    Text("$quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { quantity++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                    }
                }

                Button(
                    onClick = {
                        onAddToCart(
                            product,
                            selectedSize,
                            extraChicken,
                            extraCheese,
                            extraCheeseSlice,
                            selectedSauces.toList(),
                            specialInstructions,
                            quantity
                        )
                    },
                    enabled = product.isAvailable && product.priceVerified,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("add_to_cart_button")
                ) {
                    Text(
                        text = if (product.priceVerified) "Add to Cart • Rs. $finalTotal" else "Pending Price",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DealsScreen(
    deals: List<Deal>,
    onAddDealToCart: (Deal) -> Unit,
    onWhatsAppDeal: (Deal) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(deals, search) {
        if (search.isBlank()) deals
        else deals.filter {
            it.dealNumber.contains(search, ignoreCase = true) ||
                it.itemsSummary.contains(search, ignoreCase = true) ||
                it.title.contains(search, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Special Deals & Birthday Feast",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Official Deal 1 to Deal 19 + Birthday Deal • Additional charges apply on Signature & Crusty flavours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldBright
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Filter deals (e.g. Zinger, Pizza, Birthday)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = GoldBright,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { deal ->
                DealCard(
                    deal = deal,
                    onAddToCart = { onAddDealToCart(deal) },
                    onWhatsAppDeal = { onWhatsAppDeal(deal) }
                )
            }
        }
    }
}
