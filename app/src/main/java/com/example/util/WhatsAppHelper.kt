package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.data.model.CafeConstants
import com.example.data.model.CartItem
import com.example.data.model.OrderType

object WhatsAppHelper {

    fun buildOrderMessage(
        orderReference: String,
        customerName: String,
        customerPhone: String,
        orderType: OrderType,
        deliveryAddress: String,
        specialInstructions: String,
        items: List<CartItem>,
        subtotal: Int,
        deliveryFee: Int,
        finalTotal: Int
    ): String {
        val sb = StringBuilder()
        sb.appendLine("*${CafeConstants.BUSINESS_NAME} — WHATSAPP ORDER*")
        sb.appendLine("Order Ref: $orderReference")
        sb.appendLine("--------------------------------")
        sb.appendLine("*Customer:* ${customerName.ifBlank { "Customer" }}")
        sb.appendLine("*Phone:* ${customerPhone.ifBlank { "Not provided" }}")
        sb.appendLine("*Order Type:* ${orderType.label}")
        if (orderType == OrderType.DELIVERY && deliveryAddress.isNotBlank()) {
            sb.appendLine("*Delivery Address:* $deliveryAddress")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("*Order Items:*")
        if (items.isEmpty()) {
            sb.appendLine("• (Custom / Quick Inquiry)")
        } else {
            items.forEachIndexed { index, item ->
                val sizeTag = if (item.selectedSize.isNotBlank()) " [${item.selectedSize}]" else ""
                sb.appendLine("${index + 1}. ${item.quantity}x ${item.name}$sizeTag — Rs. ${item.lineTotal}")
                val customParts = mutableListOf<String>()
                if (item.extraChicken) customParts += "Extra Chicken"
                if (item.extraCheese) customParts += "Extra Cheese"
                if (item.extraCheeseSlice) customParts += "Extra Cheese Slice"
                if (item.selectedSauces.isNotEmpty()) {
                    customParts += "Sauces: ${item.selectedSauces.joinToString(", ")}"
                }
                if (item.specialInstructions.isNotBlank()) {
                    customParts += "Item Note: ${item.specialInstructions}"
                }
                if (customParts.isNotEmpty()) {
                    sb.appendLine("   _Customizations:_ ${customParts.joinToString(" | ")}")
                }
            }
        }
        if (specialInstructions.isNotBlank()) {
            sb.appendLine("--------------------------------")
            sb.appendLine("*Special Instructions:* $specialInstructions")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("*Subtotal:* Rs. $subtotal")
        sb.appendLine(
            if (deliveryFee == 0) "*Delivery Charges:* FREE"
            else "*Delivery Charges:* Rs. $deliveryFee"
        )
        sb.appendLine("*Final Total:* Rs. $finalTotal")
        sb.appendLine("--------------------------------")
        sb.append("Please confirm this order. Thank you!")
        return sb.toString()
    }

    fun openWhatsAppWithMessage(context: Context, message: String): Boolean {
        val encoded = Uri.encode(message)
        val waUri = Uri.parse("https://wa.me/${CafeConstants.WHATSAPP_DIGITS}?text=$encoded")

        val intent = Intent(Intent.ACTION_VIEW, waUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Prefer installed WhatsApp or WhatsApp Business package when available
        val pm = context.packageManager
        val preferredPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
        for (pkg in preferredPackages) {
            val isInstalled = try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                true
            } catch (_: Exception) {
                false
            }
            if (isInstalled) {
                intent.setPackage(pkg)
                break
            }
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // Fallback without explicit package
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun dialCafe(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${CafeConstants.PHONE_DISPLAY}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openCafeOnMaps(context: Context): Boolean {
        val query = Uri.encode("${CafeConstants.BUSINESS_NAME}, ${CafeConstants.LOCATION_ADDRESS}")
        val geoUri = Uri.parse("geo:0,0?q=$query")
        val mapsWebUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, geoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, mapsWebUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun openUrl(context: Context, url: String): Boolean {
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (_: Exception) {
            false
        }
    }
}
