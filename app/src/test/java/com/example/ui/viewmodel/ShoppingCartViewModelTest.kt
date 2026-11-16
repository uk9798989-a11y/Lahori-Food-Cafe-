package com.example.ui.viewmodel

import com.example.data.model.CafeSettings
import com.example.data.model.CartItem
import com.example.data.model.LahoriMenuCatalog
import com.example.data.model.Order
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.example.data.repository.CafeOrderGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingCartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeOrderGateway : CafeOrderGateway {
        var lastSubmittedItems: List<CartItem> = emptyList()
        var shouldFail: Boolean = false

        override suspend fun placeDirectOrder(
            customerName: String,
            customerPhone: String,
            orderType: OrderType,
            deliveryAddress: String,
            specialInstructions: String,
            cartItems: List<CartItem>
        ): Result<Order> {
            lastSubmittedItems = cartItems
            if (shouldFail) {
                return Result.failure(IllegalStateException("Server rejected order"))
            }
            val sub = cartItems.sumOf { it.lineTotal }
            return Result.success(
                Order(
                    id = "ord_test_1",
                    userId = "user_1",
                    orderCode = "LFC-9999",
                    orderType = orderType.firestoreValue,
                    status = OrderStatuses.ORDER_RECEIVED,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    deliveryAddress = deliveryAddress,
                    subtotal = sub,
                    deliveryFee = 0,
                    total = sub
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
        ): Result<String> = Result.success("wa_$orderCode")
    }

    private lateinit var gateway: FakeOrderGateway
    private lateinit var viewModel: ShoppingCartViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        gateway = FakeOrderGateway()
        viewModel = ShoppingCartViewModel(
            orderGateway = gateway,
            currentUserId = "user_1",
            defaultUserName = "Ayesha Khan"
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addPizzaAndBurgerWithToppings_andQuantityUpdates_calculatesAccurateTotals() {
        val tikkaPizza = LahoriMenuCatalog.products.first { it.id == "prod_trad_tikka" } // M = 950
        val zingerBurger = LahoriMenuCatalog.products.first { it.id == "prod_brg_zinger" } // 350

        // Add 2x Medium Tikka Pizza with Extra Chicken (+100), Extra Cheese (+100), 2 sauces (+60)
        // Per unit: base 950 + toppings 260 = 1210; for qty 2 -> base 1900 + toppings 520 = 2420
        viewModel.addProductToCart(
            product = tikkaPizza,
            selectedSize = "M",
            extraChicken = true,
            extraCheese = true,
            extraCheeseSlice = false,
            selectedSauces = listOf("Garlic Sauce", "Dip Sauce"),
            specialInstructions = "",
            quantity = 2
        )

        var state = viewModel.uiState.value
        assertEquals(2, state.totalItemCount)
        assertEquals(1900, state.baseItemsSubtotal)
        assertEquals(520, state.toppingsAndExtrasTotal)
        assertEquals(2420, state.subtotal)
        assertEquals(2420, state.totalPrice)
        assertTrue(state.meetsMinDelivery)

        // Add 1x Zinger Burger with Extra Cheese Slice (+50) -> unit 400
        viewModel.addProductToCart(
            product = zingerBurger,
            selectedSize = "",
            extraChicken = false,
            extraCheese = false,
            extraCheeseSlice = true,
            selectedSauces = emptyList(),
            specialInstructions = "",
            quantity = 1
        )

        state = viewModel.uiState.value
        assertEquals(3, state.totalItemCount)
        assertEquals(1900 + 350, state.baseItemsSubtotal)
        assertEquals(520 + 50, state.toppingsAndExtrasTotal)
        assertEquals(2820, state.subtotal)
        assertEquals(2820, state.totalPrice)

        // Decrement pizza quantity to 1 -> base 950 + 350 = 1300, toppings 260 + 50 = 310, total 1610
        val pizzaKey = state.items.first { it.itemId == "prod_trad_tikka" }.cartKey
        viewModel.decrementQuantity(pizzaKey)

        state = viewModel.uiState.value
        assertEquals(2, state.totalItemCount)
        assertEquals(1300, state.baseItemsSubtotal)
        assertEquals(310, state.toppingsAndExtrasTotal)
        assertEquals(1610, state.totalPrice)

        // Decrement burger quantity to 0 -> removes burger from cart
        val burgerKey = state.items.first { it.itemId == "prod_brg_zinger" }.cartKey
        viewModel.decrementQuantity(burgerKey)

        state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(1, state.totalItemCount)
        assertEquals(950, state.baseItemsSubtotal)
        assertEquals(260, state.toppingsAndExtrasTotal)
        assertEquals(1210, state.totalPrice)
    }

    @Test
    fun updateItemToppings_recalculatesChargesAndMergesDuplicateItems() {
        val zinger = LahoriMenuCatalog.products.first { it.id == "prod_brg_zinger" } // 350

        // Add plain Zinger
        viewModel.addProductToCart(zinger, "", false, false, false, emptyList(), "", 1)
        // Add Zinger with Extra Cheese Slice (+50)
        viewModel.addProductToCart(zinger, "", false, false, true, emptyList(), "", 1)

        var state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals(750, state.totalPrice) // 350 + 400

        // Enable Extra Cheese Slice on the plain Zinger -> merges into 2x Zinger + Extra Cheese Slice (800)
        val plainKey = state.items.first { !it.extraCheeseSlice }.cartKey
        viewModel.updateItemToppings(
            cartKey = plainKey,
            extraChicken = false,
            extraCheese = false,
            extraCheeseSlice = true,
            selectedSauces = emptyList()
        )

        state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(2, state.totalItemCount)
        assertEquals(700, state.baseItemsSubtotal)
        assertEquals(100, state.toppingsAndExtrasTotal)
        assertEquals(800, state.totalPrice)
    }

    @Test
    fun orderState_enforcesMinDeliveryAndTransitionsOnDirectOrderSubmit() = runTest {
        val singleZinger = LahoriMenuCatalog.products.first { it.id == "prod_brg_zinger" } // 350
        viewModel.addProductToCart(singleZinger, "", false, false, false, emptyList(), "", 1)

        viewModel.syncCafeSettings(CafeSettings(minDeliveryOrder = 700, deliveryFee = 50, isFreeDelivery = false))
        viewModel.setCustomerName("Bilal")
        viewModel.setCustomerPhone("03043689241")
        viewModel.setDeliveryAddress("Main Chowk, Taranda Muhammad Panah")

        // Delivery with 350 < 700 -> blocked
        var state = viewModel.uiState.value
        assertFalse(state.meetsMinDelivery)
        assertEquals(350, state.amountToMinDelivery)
        assertEquals(50, state.deliveryFee)
        assertEquals(400, state.totalPrice)

        viewModel.submitDirectAppOrder()
        assertTrue(viewModel.uiState.value.submissionState is OrderSubmissionState.Error)

        // Increment quantity to 2 -> 700 >= 700, meets minimum!
        val key = state.items.first().cartKey
        viewModel.incrementQuantity(key)
        state = viewModel.uiState.value
        assertTrue(state.meetsMinDelivery)
        assertEquals(0, state.amountToMinDelivery)
        assertEquals(750, state.totalPrice)

        // Switch to Takeaway -> deliveryFee drops to 0, total 700
        viewModel.setOrderType(OrderType.TAKEAWAY, configuredDeliveryFee = 50)
        assertEquals(0, viewModel.uiState.value.deliveryFee)
        assertEquals(700, viewModel.uiState.value.totalPrice)

        var successOrder: Order? = null
        viewModel.submitDirectAppOrder { successOrder = it }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.submissionState is OrderSubmissionState.DirectOrderSuccess)
        assertEquals("LFC-9999", successOrder?.orderCode)
        assertEquals(0, viewModel.uiState.value.items.size)
        assertEquals(0, viewModel.uiState.value.totalPrice)
    }

    @Test
    fun reorderFromOrder_reconstructsItemsWithToppings() {
        val pastOrder = Order(
            id = "ord_past",
            items = listOf(
                OrderLineItem(
                    itemId = "prod_trad_fajita",
                    itemType = "PRODUCT",
                    name = "Fajita Pizza",
                    selectedSize = "L", // 1450
                    extraChicken = true, // +170
                    selectedSauces = listOf("Peri Peri Sauce"), // +30
                    quantity = 2
                ),
                OrderLineItem(
                    itemId = "deal_01",
                    itemType = "DEAL",
                    name = "DEAL 1",
                    quantity = 1
                )
            )
        )

        viewModel.reorderFromOrder(pastOrder, LahoriMenuCatalog.products, LahoriMenuCatalog.deals)
        val state = viewModel.uiState.value
        assertEquals(3, state.totalItemCount)
        assertEquals(1450 * 2 + 400, state.baseItemsSubtotal)
        assertEquals((170 + 30) * 2, state.toppingsAndExtrasTotal)
        assertEquals(3700, state.totalPrice)
    }
}
