package com.example.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.data.model.CartItem
import com.example.data.model.LahoriMenuCatalog
import com.example.data.model.Order
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.example.data.model.UserProfile
import com.example.data.repository.CafeOrderGateway
import com.example.ui.theme.LahoriCafeTheme
import com.example.ui.viewmodel.CartViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CheckoutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class FakeOrderGateway : CafeOrderGateway {
        override suspend fun placeDirectOrder(
            customerName: String,
            customerPhone: String,
            orderType: OrderType,
            deliveryAddress: String,
            specialInstructions: String,
            cartItems: List<CartItem>
        ): Result<Order> {
            val total = cartItems.sumOf { it.lineTotal }
            return Result.success(
                Order(
                    id = "ord_ui_1",
                    orderCode = "LFC-1010",
                    status = OrderStatuses.ORDER_RECEIVED,
                    total = total
                )
            )
        }

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
        ): Result<String> = Result.success("wa_1")
    }

    @Test
    fun checkoutScreen_displaysCartViewModelSummary_switchesDeliveryPickup_andShowsWhatsAppButton() {
        val cartViewModel = CartViewModel(
            orderGateway = FakeOrderGateway(),
            currentUserId = "user_test",
            defaultUserName = "Usman"
        )
        val tikkaPizza = LahoriMenuCatalog.products.first { it.id == "prod_trad_tikka" } // M = 950
        cartViewModel.addProductToCart(
            product = tikkaPizza,
            selectedSize = "M",
            extraChicken = true, // +100
            extraCheese = false,
            extraCheeseSlice = false,
            selectedSauces = listOf("Dip Sauce"), // +30
            specialInstructions = "Well done",
            quantity = 1
        )

        composeRule.setContent {
            LahoriCafeTheme {
                CheckoutScreen(
                    cartViewModel = cartViewModel,
                    isSignedIn = true,
                    userProfile = UserProfile(
                        userId = "user_test",
                        displayName = "Usman",
                        phone = "03043689241",
                        savedAddresses = listOf("Main Chowk, Taranda M. Panah")
                    ),
                    onBack = {},
                    onDirectOrderSuccess = {},
                    onBrowseMenu = {},
                    onSignedIn = {}
                )
            }
        }

        // Order summary & total (950 + 100 + 30 = 1080) from CartViewModel
        composeRule.onNodeWithText("1x Tikka Pizza").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Submit Direct App Order • Rs. 1080").assertIsDisplayed()

        // Address field visible in Delivery mode and updates CartViewModel
        composeRule.onNodeWithTag("checkout_address_input")
            .performScrollTo()
            .assertIsDisplayed()
            .performTextReplacement("Sidhu Wali Road, House 14")
        assertEquals("Sidhu Wali Road, House 14", cartViewModel.uiState.value.deliveryAddress)

        // Switch to Pickup (TAKEAWAY) -> updates CartViewModel & shows Self-Pickup Counter info
        composeRule.onNodeWithTag("order_type_takeaway").performScrollTo().performClick()
        assertEquals(OrderType.TAKEAWAY, cartViewModel.uiState.value.orderType)
        composeRule.onNodeWithText("Self-Pickup Counter").performScrollTo().assertIsDisplayed()

        // Sticky bottom actions remain displayed
        composeRule.onNodeWithTag("submit_direct_order_button").assertIsDisplayed()
        composeRule.onNodeWithTag("checkout_whatsapp_order_button").assertIsDisplayed()
    }
}
