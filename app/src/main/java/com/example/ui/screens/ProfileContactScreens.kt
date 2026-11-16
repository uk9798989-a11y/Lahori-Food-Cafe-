package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.CafeConstants
import com.example.data.model.CafeSettings
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.SignInResult
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.WhatsAppGreen
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    isSignedIn: Boolean,
    isAdmin: Boolean,
    userProfile: UserProfile?,
    userDisplayName: String?,
    userEmail: String?,
    unreadNotifications: Int,
    onSaveProfile: (name: String, phone: String, addresses: List<String>) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenContactLocation: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onSignOut: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var displayName by remember(userProfile, userDisplayName) {
        mutableStateOf(userProfile?.displayName?.ifBlank { userDisplayName ?: "" } ?: (userDisplayName ?: ""))
    }
    var phone by remember(userProfile) { mutableStateOf(userProfile?.phone ?: "") }
    val addresses = remember(userProfile) {
        mutableStateListOf<String>().apply {
            addAll(userProfile?.savedAddresses.orEmpty())
        }
    }
    var newAddress by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = GoldBright,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = CharcoalBlack,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSignedIn) (displayName.ifBlank { "Lahori Cafe Customer" }) else "Guest Mode",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSignedIn) (userEmail ?: "") else "Sign in with Google to save addresses & track orders",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldBright
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (!isSignedIn) {
                Button(
                    onClick = {
                        val act = context as? Activity ?: return@Button
                        scope.launch {
                            if (AuthRepository.signInWithGoogleInteractive(act) is SignInResult.Success) {
                                onSignedIn()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profile_google_sign_in")
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Google", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Account & Saved Addresses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Saved Delivery Addresses", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                addresses.forEachIndexed { idx, addr ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(addr, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { addresses.removeAt(idx) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Address")
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Add Delivery Address") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newAddress.isNotBlank() && addresses.size < 10) {
                                addresses.add(newAddress.trim())
                                newAddress = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldBright, contentColor = CharcoalBlack),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                Button(
                    onClick = { onSaveProfile(displayName, phone, addresses.toList()) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Save Profile & Addresses", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()

            // Quick Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenNotifications() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaroonPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Notifications", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    if (unreadNotifications > 0) {
                        BadgePill("$unreadNotifications NEW", GoldBright, CharcoalBlack)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenContactLocation() }
                    .testTag("open_contact_location"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MaroonPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contact Us & Cafe Location", fontWeight = FontWeight.SemiBold)
                        Text("Main Chowk, Sidhu Wali Road • 0304-3689241", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Admin Dashboard Portal (Protected)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAdminDashboard() }
                    .testTag("open_admin_dashboard"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                border = BorderStroke(1.dp, GoldBright)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldBright)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cafe Owner Admin Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (isAdmin) "Authorized Admin Access Active" else "Protected — Admin Account Required",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldBright
                        )
                    }
                }
            }

            if (isSignedIn) {
                OutlinedButton(
                    onClick = onSignOut,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Order Notifications", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No notifications yet. Order status updates will appear here.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!notif.isRead) onMarkRead(notif.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else GoldBright.copy(alpha = 0.16f)
                        ),
                        border = BorderStroke(1.dp, if (notif.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f) else GoldBright)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (!notif.isRead) BadgePill("NEW", MaroonPrimary, Color.White)
                            }
                            Text(notif.message, style = MaterialTheme.typography.bodySmall)
                            val dt = notif.createdAt?.toDate()?.let { formatter.format(it) } ?: "Just now"
                            Text(dt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactAndLocationScreen(
    settings: CafeSettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(color = MaroonPrimary, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Contact Us & Location", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8F2)),
                border = BorderStroke(2.dp, GoldBright)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lahori_logo),
                        contentDescription = "Lahori Food Cafe Logo",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(CafeConstants.BUSINESS_NAME, style = MaterialTheme.typography.headlineMedium, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (settings.isFreeDelivery) "Free Delivery (Min Order Rs. ${settings.minDeliveryOrder})" else "Min Delivery Rs. ${settings.minDeliveryOrder}",
                        style = MaterialTheme.typography.labelLarge,
                        color = CharcoalBlack
                    )
                }
            }

            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaroonPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cafe Address", fontWeight = FontWeight.Bold)
                            Text(CafeConstants.LOCATION_ADDRESS, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaroonPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Opening Hours", fontWeight = FontWeight.Bold)
                            Text(settings.openingHours, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = MaroonPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Official Phone & WhatsApp", fontWeight = FontWeight.Bold)
                            Text("${CafeConstants.PHONE_DISPLAY} (${CafeConstants.WHATSAPP_NUMBER})", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { WhatsAppHelper.dialCafe(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Cafe", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        WhatsAppHelper.openWhatsAppWithMessage(
                            context,
                            "Assalam-o-Alaikum Lahori Food Cafe! I would like to inquire / place an order."
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { WhatsAppHelper.openCafeOnMaps(context) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldBright, contentColor = CharcoalBlack),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open in Google Maps", fontWeight = FontWeight.Bold)
            }

            Text("Follow Us on Social Media (${CafeConstants.SOCIAL_HANDLE})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { WhatsAppHelper.openUrl(context, CafeConstants.FACEBOOK_URL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Facebook", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { WhatsAppHelper.openUrl(context, CafeConstants.INSTAGRAM_URL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Instagram", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { WhatsAppHelper.openUrl(context, CafeConstants.TIKTOK_URL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TikTok", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
