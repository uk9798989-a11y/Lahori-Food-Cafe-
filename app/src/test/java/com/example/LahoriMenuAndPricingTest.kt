package com.example

import com.example.data.model.CafeConstants
import com.example.data.model.CartItem
import com.example.data.model.LahoriMenuCatalog
import com.example.data.model.OrderType
import com.example.data.model.PizzaPricingRules
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LahoriMenuAndPricingTest {

    @Test
    fun catalog_preservesAll20DealsWithExactOriginalPrices() {
        val deals = LahoriMenuCatalog.deals
        assertEquals(20, deals.size)
        for (n in 1..19) {
            assertTrue("Missing DEAL $n", deals.any { it.dealNumber == "DEAL $n" })
        }
        assertTrue("Missing BIRTHDAY DEAL", deals.any { it.dealNumber == "BIRTHDAY DEAL" })

        val deal1 = deals.first { it.dealNumber == "DEAL 1" }
        assertEquals(400, deal1.price)
        assertEquals("1 Regular Drink", deal1.drinkInfo)

        val birthday = deals.first { it.dealNumber == "BIRTHDAY DEAL" }
        assertEquals(7070, birthday.price)
        assertTrue(birthday.itemsSummary.contains("1 Pound Cake"))
        assertEquals("2 (1.5 Liter) Drinks", birthday.drinkInfo)
    }

    @Test
    fun catalog_preservesPizzaAndFastFoodPricingRulesAndVerificationFlags() {
        val lahoriSpecial = LahoriMenuCatalog.products.first { it.id == "prod_trad_lahori_special" }
        assertEquals(mapOf("S" to 600, "M" to 1070, "L" to 1590, "XL" to 2300), lahoriSpecial.sizePrices)
        assertTrue(lahoriSpecial.supportsPizzaCustomizations)

        val doubleSeekh = LahoriMenuCatalog.products.first { it.id == "prod_prem_double_seekh" }
        assertEquals(mapOf("M" to 1400, "L" to 1850, "XL" to 2700), doubleSeekh.sizePrices)

        val zinger = LahoriMenuCatalog.products.first { it.id == "prod_brg_zinger" }
        assertEquals(350, zinger.basePrice)
        assertTrue(zinger.supportsExtraCheeseSlice)

        // Soft drinks had no printed prices on the flyer and must remain flagged for Admin Verification
        val drinks = LahoriMenuCatalog.products.filter { it.categoryId == "cat_soft_drinks" }
        assertEquals(7, drinks.size)
        drinks.forEach { drink ->
            assertFalse("${drink.name} must not invent a price", drink.priceVerified)
            assertEquals(0, drink.basePrice)
        }

        // Pizza customization rules match flyer
        assertEquals(70, PizzaPricingRules.extraToppingPrice("S"))
        assertEquals(100, PizzaPricingRules.extraToppingPrice("M"))
        assertEquals(170, PizzaPricingRules.extraToppingPrice("L"))
        assertNull(PizzaPricingRules.extraToppingPrice("XL"))
        assertEquals(30, PizzaPricingRules.SAUCE_PRICE)
        assertEquals(50, PizzaPricingRules.BURGER_CHEESE_SLICE_PRICE)
        assertEquals(4, PizzaPricingRules.availableSauces.size)
    }

    @Test
    fun whatsAppOrderMessage_includesAllRequiredFieldsAndCustomizations() {
        val pizzaItem = CartItem(
            cartKey = "k1",
            itemId = "prod_trad_tikka",
            itemType = "PRODUCT",
            name = "Tikka Pizza",
            imageKey = "img_cat_pizza_traditional",
            selectedSize = "M",
            extraChicken = true,
            extraCheese = true,
            selectedSauces = listOf("Dip Sauce", "Garlic Sauce"),
            specialInstructions = "Well baked",
            quantity = 2,
            unitBasePrice = 950,
            customizationCharges = 100 + 100 + 60 // Extra chicken (100) + extra cheese (100) + 2 sauces (60)
        )
        assertEquals(1210, pizzaItem.unitTotal)
        assertEquals(2420, pizzaItem.lineTotal)

        val msg = WhatsAppHelper.buildOrderMessage(
            orderReference = "LFC-WA-4821",
            customerName = "Bilal Ahmed",
            customerPhone = "03001234567",
            orderType = OrderType.DELIVERY,
            deliveryAddress = "Main Chowk, Sidhu Wali Road",
            specialInstructions = "Call on arrival",
            items = listOf(pizzaItem),
            subtotal = 2420,
            deliveryFee = 0,
            finalTotal = 2420
        )

        assertTrue(msg.contains(CafeConstants.BUSINESS_NAME))
        assertTrue(msg.contains("LFC-WA-4821"))
        assertTrue(msg.contains("Bilal Ahmed"))
        assertTrue(msg.contains("03001234567"))
        assertTrue(msg.contains("Delivery"))
        assertTrue(msg.contains("Main Chowk, Sidhu Wali Road"))
        assertTrue(msg.contains("2x Tikka Pizza [M]"))
        assertTrue(msg.contains("Extra Chicken"))
        assertTrue(msg.contains("Extra Cheese"))
        assertTrue(msg.contains("Dip Sauce, Garlic Sauce"))
        assertTrue(msg.contains("Well baked"))
        assertTrue(msg.contains("Call on arrival"))
        assertTrue(msg.contains("FREE"))
        assertTrue(msg.contains("Rs. 2420"))
        assertNotNull(CafeConstants.WHATSAPP_NUMBER)
        assertEquals("+923043689241", CafeConstants.WHATSAPP_NUMBER)
    }
}
