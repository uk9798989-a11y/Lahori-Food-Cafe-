package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AppNotification
import com.example.data.model.CafeConstants
import com.example.data.model.CafeSettings
import com.example.data.model.Category
import com.example.data.model.Deal
import com.example.data.model.Order
import com.example.data.model.OrderSource
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.example.data.model.Product
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.SalesSummary
import com.example.util.DrawableMapper

@Composable
fun AdminDashboardScreen(
    isSignedIn: Boolean,
    isAdmin: Boolean,
    userEmail: String?,
    orders: List<Order>,
    products: List<Product>,
    deals: List<Deal>,
    categories: List<Category>,
    settings: CafeSettings,
    notifications: List<AppNotification>,
    salesSummary: SalesSummary,
    onBack: () -> Unit,
    onUpdateOrderStatus: (Order, String, String) -> Unit,
    onRecordWhatsAppOrder: (String, String, OrderType, String, String, Int) -> Unit,
    onSaveProduct: (Product, Boolean) -> Unit,
    onToggleProductAvailable: (Product) -> Unit,
    onSaveDeal: (Deal, Boolean) -> Unit,
    onToggleDealActive: (Deal) -> Unit,
    onSaveCategory: (Category, Boolean) -> Unit,
    onSaveSettings: (CafeSettings) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = CharcoalBlack, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldBright)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Admin Dashboard", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(CafeConstants.BUSINESS_NAME, style = MaterialTheme.typography.labelSmall, color = GoldBright)
                }
            }
        }

        if (!isSignedIn || !isAdmin) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(64.dp))
                    Text("Protected Admin Area", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (!isSignedIn) {
                            "Sign in with your authorized owner Google account (${CafeConstants.BOOTSTRAPPED_ADMIN_EMAIL}) to access the Admin Dashboard."
                        } else {
                            "Signed in as ${userEmail ?: "customer"}. Only authorized cafe administrators can manage orders, menu prices, deals, and delivery settings."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)) {
                        Text("Return to Customer App")
                    }
                }
            }
            return
        }

        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Overview", "Orders (${orders.size})", "Products", "Deals", "Sales & Settings")

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaroonPrimary
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminOverviewTab(salesSummary, products, notifications)
            1 -> AdminOrdersTab(orders, onUpdateOrderStatus, onRecordWhatsAppOrder)
            2 -> AdminProductsTab(products, categories, onSaveProduct, onToggleProductAvailable, onSaveCategory)
            3 -> AdminDealsTab(deals, onSaveDeal, onToggleDealActive)
            4 -> AdminSalesAndSettingsTab(salesSummary, settings, onSaveSettings)
        }
    }
}

@Composable
private fun AdminOverviewTab(
    summary: SalesSummary,
    products: List<Product>,
    notifications: List<AppNotification>
) {
    val popularProducts = remember(products) { products.filter { it.isPopular }.take(5) }
    val pendingPriceProducts = remember(products) { products.filter { !it.priceVerified } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Today's Orders", "${summary.todayOrders}", MaroonPrimary, Modifier.weight(1f))
                MetricCard("Pending Orders", "${summary.pendingOrders}", WarningAmber, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Completed", "${summary.completedOrders}", SuccessGreen, Modifier.weight(1f))
                MetricCard("Today's Sales", "Rs. ${summary.todaySales}", CharcoalBlack, Modifier.weight(1f))
            }
        }
        item {
            MetricCard("Total Revenue (Verified)", "Rs. ${summary.totalRevenue}", MaroonPrimary, Modifier.fillMaxWidth())
        }

        if (pendingPriceProducts.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.14f)),
                    border = BorderStroke(1.dp, WarningAmber),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "${pendingPriceProducts.size} menu item(s) (Soft Drinks) have unverified flyer prices. Go to Products tab to set their PKR prices.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (notifications.isNotEmpty()) {
            item {
                Text("Recent Admin Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(notifications.take(5), key = { it.id }) { n ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(n.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(n.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Text("Popular Products", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(popularProducts, key = { it.id }) { prod ->
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(prod.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text("Rs. ${prod.startingPrice()}", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<Order>,
    onUpdateOrderStatus: (Order, String, String) -> Unit,
    onRecordWhatsAppOrder: (String, String, OrderType, String, String, Int) -> Unit
) {
    var filterSource by remember { mutableStateOf("ALL") }
    var showRecordWaDialog by remember { mutableStateOf(false) }

    val filtered = remember(orders, filterSource) {
        when (filterSource) {
            "APP" -> orders.filter { it.orderSource == OrderSource.APP.firestoreValue }
            "WHATSAPP" -> orders.filter { it.orderSource == OrderSource.WHATSAPP.firestoreValue }
            "PENDING" -> orders.filter { it.status !in listOf(OrderStatuses.COMPLETED, OrderStatuses.CANCELLED) }
            else -> orders
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                items(listOf("ALL" to "All", "PENDING" to "Active", "APP" to "App", "WHATSAPP" to "WhatsApp")) { (k, label) ->
                    FilterChip(
                        selected = filterSource == k,
                        onClick = { filterSource = k },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showRecordWaDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log WA", style = MaterialTheme.typography.labelMedium)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { order ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${order.orderCode} • ${order.customerName}", fontWeight = FontWeight.Bold)
                                Text("${order.customerPhone} • ${OrderType.fromString(order.orderType).label}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill(order.status)
                        }
                        BadgePill(
                            text = if (order.orderSource == OrderSource.WHATSAPP.firestoreValue) "WHATSAPP SOURCE" else "DIRECT APP SOURCE",
                            bg = if (order.orderSource == OrderSource.WHATSAPP.firestoreValue) WhatsAppGreen.copy(alpha = 0.16f) else GoldBright.copy(alpha = 0.25f),
                            fg = if (order.orderSource == OrderSource.WHATSAPP.firestoreValue) WhatsAppGreen else CharcoalBlack
                        )
                        if (order.deliveryAddress.isNotBlank()) {
                            Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (order.specialInstructions.isNotBlank()) {
                            Text("Note: ${order.specialInstructions}", style = MaterialTheme.typography.bodySmall, color = MaroonPrimary)
                        }
                        HorizontalDivider()
                        order.items.forEach { item ->
                            Text("${item.quantity}x ${item.name} ${if (item.selectedSize.isNotBlank()) "[${item.selectedSize}]" else ""} — Rs. ${item.lineTotal}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Total: Rs. ${order.total}", style = MaterialTheme.typography.titleMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)

                        // Quick Status Update Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(OrderStatuses.ALL_ADMIN_STATUSES) { status ->
                                FilterChip(
                                    selected = order.status == status,
                                    onClick = { if (order.status != status) onUpdateOrderStatus(order, status, "") },
                                    label = { Text(status, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecordWaDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var summary by remember { mutableStateOf("") }
        var totalStr by remember { mutableStateOf("") }
        var orderType by remember { mutableStateOf(OrderType.DELIVERY) }

        AlertDialog(
            onDismissRequest = { showRecordWaDialog = false },
            title = { Text("Record Confirmed WhatsApp Order") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OrderType.entries.forEach { t ->
                            FilterChip(selected = orderType == t, onClick = { orderType = t }, label = { Text(t.label) })
                        }
                    }
                    if (orderType == OrderType.DELIVERY) {
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
                    }
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Order Items Summary") })
                    OutlinedTextField(
                        value = totalStr,
                        onValueChange = { totalStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Confirmed Total (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = totalStr.toIntOrNull() ?: 0
                        if (name.isNotBlank() && amt > 0) {
                            onRecordWhatsAppOrder(name, phone.ifBlank { "WhatsApp" }, orderType, address, summary, amt)
                            showRecordWaDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) { Text("Record & Confirm") }
            },
            dismissButton = { TextButton(onClick = { showRecordWaDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminProductsTab(
    products: List<Product>,
    categories: List<Category>,
    onSaveProduct: (Product, Boolean) -> Unit,
    onToggleProductAvailable: (Product) -> Unit,
    onSaveCategory: (Category, Boolean) -> Unit
) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val defaultCat = categories.firstOrNull()
                    editingProduct = Product(
                        categoryId = defaultCat?.id ?: "cat_burgers",
                        categoryName = defaultCat?.name ?: "Burgers",
                        group = defaultCat?.group ?: "FAST_FOOD",
                        imageKey = defaultCat?.imageKey ?: "img_cat_burger"
                    )
                    isCreatingNew = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Product")
            }
            OutlinedButton(
                onClick = { showNewCategoryDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Category")
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { prod ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    border = if (!prod.priceVerified) BorderStroke(1.dp, WarningAmber) else null
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        FoodImage(
                            imageKey = prod.imageKey,
                            imageUrl = prod.imageUrl,
                            contentDescription = prod.name,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (prod.priceVerified) "${prod.categoryName} • Rs. ${prod.startingPrice()}" else "${prod.categoryName} • Needs Price Verification",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (prod.priceVerified) MaterialTheme.colorScheme.onSurfaceVariant else WarningAmber
                            )
                        }
                        Switch(
                            checked = prod.isAvailable,
                            onCheckedChange = { onToggleProductAvailable(prod) }
                        )
                        IconButton(onClick = {
                            editingProduct = prod
                            isCreatingNew = false
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                        }
                    }
                }
            }
        }
    }

    editingProduct?.let { prod ->
        ProductEditorDialog(
            product = prod,
            categories = categories,
            isNew = isCreatingNew,
            onDismiss = { editingProduct = null },
            onSave = { updated ->
                onSaveProduct(updated, isCreatingNew)
                editingProduct = null
            }
        )
    }

    if (showNewCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var group by remember { mutableStateOf("FAST_FOOD") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Add Menu Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Category Name") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = group == "PIZZA", onClick = { group = "PIZZA" }, label = { Text("PIZZA") })
                        FilterChip(selected = group == "FAST_FOOD", onClick = { group = "FAST_FOOD" }, label = { Text("FAST FOOD") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (catName.isNotBlank()) {
                        onSaveCategory(
                            Category(
                                name = catName.trim(),
                                group = group,
                                displayOrder = categories.size + 1,
                                imageKey = if (group == "PIZZA") "img_cat_pizza_traditional" else "img_hero_platter"
                            ),
                            true
                        )
                        showNewCategoryDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProductEditorDialog(
    product: Product,
    categories: List<Category>,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var description by remember { mutableStateOf(product.description) }
    var basePriceStr by remember { mutableStateOf(product.basePrice.toString()) }
    var selectedCatId by remember { mutableStateOf(product.categoryId) }
    var imageKey by remember { mutableStateOf(product.imageKey) }
    var imageUrl by remember { mutableStateOf(product.imageUrl) }
    var priceVerified by remember { mutableStateOf(product.priceVerified) }
    var isPopular by remember { mutableStateOf(product.isPopular) }
    var isAvailable by remember { mutableStateOf(product.isAvailable) }
    var supportsPizza by remember { mutableStateOf(product.supportsPizzaCustomizations) }

    // Android Photo Picker (Google Play policy compliant — zero broad storage permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { imageUrl = it.toString() } }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add Product" else "Edit ${product.name}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(
                    value = basePriceStr,
                    onValueChange = { basePriceStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Base Price (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Category", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Photo")
                    }
                    if (imageUrl.isNotBlank()) {
                        TextButton(onClick = { imageUrl = "" }) { Text("Clear Photo") }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DrawableMapper.availableImageKeys) { (key, label) ->
                        FilterChip(
                            selected = imageKey == key && imageUrl.isBlank(),
                            onClick = {
                                imageKey = key
                                imageUrl = ""
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = priceVerified, onCheckedChange = { priceVerified = it })
                    Text("Price Verified for Customer Ordering")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    Text("Available")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPopular, onCheckedChange = { isPopular = it })
                    Text("Featured as Popular")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = supportsPizza, onCheckedChange = { supportsPizza = it })
                    Text("Pizza Toppings & Sauces")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cat = categories.firstOrNull { it.id == selectedCatId }
                    val newPrice = basePriceStr.toIntOrNull() ?: product.basePrice
                    onSave(
                        product.copy(
                            name = name.trim(),
                            description = description.trim(),
                            basePrice = newPrice,
                            categoryId = cat?.id ?: product.categoryId,
                            categoryName = cat?.name ?: product.categoryName,
                            group = cat?.group ?: product.group,
                            imageKey = imageKey,
                            imageUrl = imageUrl,
                            priceVerified = priceVerified && newPrice > 0,
                            isAvailable = isAvailable,
                            isPopular = isPopular,
                            supportsPizzaCustomizations = supportsPizza
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminDealsTab(
    deals: List<Deal>,
    onSaveDeal: (Deal, Boolean) -> Unit,
    onToggleDealActive: (Deal) -> Unit
) {
    var editingDeal by remember { mutableStateOf<Deal?>(null) }
    var isNew by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
                editingDeal = Deal(dealNumber = "DEAL ${deals.size + 1}", imageKey = "img_cat_deals")
                isNew = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create New Deal")
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(deals, key = { it.id }) { deal ->
                Card(shape = RoundedCornerShape(14.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${deal.dealNumber} • Rs. ${deal.price}", fontWeight = FontWeight.Bold, color = MaroonPrimary)
                            Text("${deal.itemsSummary} + ${deal.drinkInfo}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = deal.isActive, onCheckedChange = { onToggleDealActive(deal) })
                        IconButton(onClick = {
                            editingDeal = deal
                            isNew = false
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Deal")
                        }
                    }
                }
            }
        }
    }

    editingDeal?.let { deal ->
        var dealNum by remember { mutableStateOf(deal.dealNumber) }
        var title by remember { mutableStateOf(deal.title) }
        var items by remember { mutableStateOf(deal.itemsSummary) }
        var drink by remember { mutableStateOf(deal.drinkInfo) }
        var priceStr by remember { mutableStateOf(deal.price.toString()) }
        var imageUrl by remember { mutableStateOf(deal.imageUrl) }

        val picker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> uri?.let { imageUrl = it.toString() } }
        )

        AlertDialog(
            onDismissRequest = { editingDeal = null },
            title = { Text(if (isNew) "Create Deal" else "Edit ${deal.dealNumber}") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dealNum, onValueChange = { dealNum = it }, label = { Text("Deal Number") })
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Deal Title") })
                    OutlinedTextField(value = items, onValueChange = { items = it }, label = { Text("Food Items & Quantities") })
                    OutlinedTextField(value = drink, onValueChange = { drink = it }, label = { Text("Drink Size") })
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Deal Price (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedButton(
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Deal Image")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = priceStr.toIntOrNull() ?: deal.price
                        if (dealNum.isNotBlank() && items.isNotBlank() && p > 0) {
                            onSaveDeal(
                                deal.copy(
                                    dealNumber = dealNum.trim(),
                                    title = title.ifBlank { dealNum }.trim(),
                                    itemsSummary = items.trim(),
                                    drinkInfo = drink.trim(),
                                    price = p,
                                    imageUrl = imageUrl
                                ),
                                isNew
                            )
                            editingDeal = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                ) { Text("Save Deal") }
            },
            dismissButton = { TextButton(onClick = { editingDeal = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminSalesAndSettingsTab(
    summary: SalesSummary,
    settings: CafeSettings,
    onSaveSettings: (CafeSettings) -> Unit
) {
    var minOrderStr by remember(settings) { mutableStateOf(settings.minDeliveryOrder.toString()) }
    var deliveryFeeStr by remember(settings) { mutableStateOf(settings.deliveryFee.toString()) }
    var isFreeDelivery by remember(settings) { mutableStateOf(settings.isFreeDelivery) }
    var isDeliveryEnabled by remember(settings) { mutableStateOf(settings.isDeliveryEnabled) }
    var isAcceptingOrders by remember(settings) { mutableStateOf(settings.isAcceptingOrders) }
    var openingHours by remember(settings) { mutableStateOf(settings.openingHours) }
    var closureNotice by remember(settings) { mutableStateOf(settings.closureNotice) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sales & Revenue Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReportRow("Daily Sales", "Rs. ${summary.todaySales}")
                ReportRow("Weekly Sales (7 days)", "Rs. ${summary.weeklySales}")
                ReportRow("Monthly Sales (30 days)", "Rs. ${summary.monthlySales}")
                ReportRow("Total Revenue", "Rs. ${summary.totalRevenue}")
                HorizontalDivider()
                ReportRow("Total Orders", "${summary.totalOrders}")
                ReportRow("Completed Orders", "${summary.completedOrders}")
                ReportRow("Cancelled Orders", "${summary.cancelledOrders}")
                ReportRow("Direct App vs WhatsApp", "${summary.appOrdersCount} App / ${summary.whatsappOrdersCount} WhatsApp")
            }
        }

        HorizontalDivider()
        Text("Delivery & Cafe Operations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = minOrderStr,
            onValueChange = { minOrderStr = it.filter { c -> c.isDigit() } },
            label = { Text("Minimum Delivery Order (PKR)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = deliveryFeeStr,
            onValueChange = { deliveryFeeStr = it.filter { c -> c.isDigit() } },
            label = { Text("Delivery Fee when not Free (PKR)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Free Delivery Active", fontWeight = FontWeight.SemiBold)
            Switch(checked = isFreeDelivery, onCheckedChange = { isFreeDelivery = it })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Delivery Service Enabled", fontWeight = FontWeight.SemiBold)
            Switch(checked = isDeliveryEnabled, onCheckedChange = { isDeliveryEnabled = it })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Accepting Online Orders", fontWeight = FontWeight.SemiBold)
            Switch(checked = isAcceptingOrders, onCheckedChange = { isAcceptingOrders = it })
        }
        OutlinedTextField(
            value = openingHours,
            onValueChange = { openingHours = it },
            label = { Text("Opening Hours") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = closureNotice,
            onValueChange = { closureNotice = it },
            label = { Text("Temporary Closure Notice (shown when closed)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSaveSettings(
                    settings.copy(
                        minDeliveryOrder = minOrderStr.toIntOrNull() ?: CafeConstants.MIN_DELIVERY_ORDER,
                        deliveryFee = deliveryFeeStr.toIntOrNull() ?: 0,
                        isFreeDelivery = isFreeDelivery,
                        isDeliveryEnabled = isDeliveryEnabled,
                        isAcceptingOrders = isAcceptingOrders,
                        openingHours = openingHours.ifBlank { "12:00 PM - 2:00 AM Daily" },
                        closureNotice = closureNotice
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Save Cafe Settings", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaroonPrimary, fontWeight = FontWeight.Bold)
    }
}
