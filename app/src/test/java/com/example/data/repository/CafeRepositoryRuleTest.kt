package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CartItem
import com.example.data.model.OrderStatuses
import com.example.data.model.OrderType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CafeRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_and_placeDirectOrder_recalculatesServerPrices() = runBlocking {
        val uid = signInTestUser("customer_${UUID.randomUUID().toString().take(6)}@test.com")
        val repository = CafeRepository(firestore)

        val profileRes = withTimeout(5000L) {
            repository.ensureUserProfile(uid, "Hamza Ali", "hamza@test.com")
        }
        assertTrue(profileRes.isSuccess)

        // Even if client passes tampered unitBasePrice = 1, repository must verify & recompute Rs. 1520 (1450 + 70? Wait: M has 100 topping)
        val tamperedItem = CartItem(
            cartKey = "test_pizza_m",
            itemId = "prod_ext_peri_peri",
            itemType = "PRODUCT",
            name = "Extreme Peri Peri",
            imageKey = "img_cat_pizza_extreme",
            selectedSize = "M",
            extraChicken = true,
            extraCheese = false,
            selectedSauces = listOf("Garlic Sauce"),
            quantity = 1,
            unitBasePrice = 1, // tampered client value
            customizationCharges = 0
        )

        val orderResult = withTimeout(5000L) {
            repository.placeDirectOrder(
                customerName = "Hamza Ali",
                customerPhone = "03043689241",
                orderType = OrderType.DELIVERY,
                deliveryAddress = "Main Chowk, Taranda Muhammad Panah",
                specialInstructions = "Extra crispy",
                cartItems = listOf(tamperedItem)
            )
        }
        assertTrue("Order placement failed: ${orderResult.exceptionOrNull()}", orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        // Extreme Peri Peri M = 1450, Extra Chicken M = 100, Garlic Sauce = 30 -> 1580
        assertEquals(1580, order.subtotal)
        assertEquals(1580, order.total)
        assertEquals(OrderStatuses.ORDER_RECEIVED, order.status)
    }

    @Test
    fun placeDirectOrder_belowMinDelivery700_isRejected() = runBlocking {
        val uid = signInTestUser("minorder_${UUID.randomUUID().toString().take(6)}@test.com")
        val repository = CafeRepository(firestore)
        withTimeout(5000L) { repository.ensureUserProfile(uid, "Zain", "zain@test.com") }

        val cheapItem = CartItem(
            cartKey = "test_zinger",
            itemId = "prod_brg_zinger",
            itemType = "PRODUCT",
            name = "Zinger Burger",
            imageKey = "img_cat_burger",
            quantity = 1,
            unitBasePrice = 350,
            customizationCharges = 0
        )

        val result = withTimeout(5000L) {
            repository.placeDirectOrder(
                customerName = "Zain",
                customerPhone = "03001112233",
                orderType = OrderType.DELIVERY,
                deliveryAddress = "Sidhu Wali Road",
                specialInstructions = "",
                cartItems = listOf(cheapItem)
            )
        }
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: result.exceptionOrNull().toString()
        assertTrue("Expected minimum delivery 700 message, got: $msg", msg.contains("700"))
    }

    @Test
    fun observeUserOrders_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = CafeRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(3000L) {
                repository.observeUserOrders("some_uid").first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull("Expected PERMISSION_DENIED exception", caught)
        val firestoreEx = caught!!.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    @Test
    fun observeUserOrders_crossUserIsolation_enforced() = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID().toString().take(6)}@test.com")
        val aliceRepo = CafeRepository(firestore)
        withTimeout(5000L) {
            aliceRepo.recordPendingWhatsAppOrder(
                orderCode = "WA-${UUID.randomUUID().toString().take(6)}",
                customerName = "Alice",
                customerPhone = "03001234567",
                orderType = OrderType.TAKEAWAY,
                deliveryAddress = "",
                specialInstructions = "",
                items = listOf(
                    CartItem(
                        cartKey = "d1",
                        itemId = "deal_01",
                        itemType = "DEAL",
                        name = "DEAL 1",
                        imageKey = "img_cat_burger",
                        quantity = 1,
                        unitBasePrice = 400,
                        customizationCharges = 0
                    )
                ),
                subtotal = 400,
                deliveryFee = 0,
                total = 400
            )
        }

        // Bob tries to observe Alice's orders -> PERMISSION_DENIED
        signInTestUser("bob_${UUID.randomUUID().toString().take(6)}@test.com")
        val bobRepo = CafeRepository(firestore)
        var caught: Throwable? = null
        try {
            withTimeout(3000L) {
                bobRepo.observeUserOrders(aliceUid).first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull(caught)
        val firestoreEx = caught!!.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }
}
