package com.example.util

import androidx.annotation.DrawableRes
import com.example.R

object DrawableMapper {
    @DrawableRes
    fun resolveDrawable(imageKey: String): Int {
        return when (imageKey) {
            "img_lahori_logo" -> R.drawable.img_lahori_logo
            "img_hero_platter" -> R.drawable.img_hero_platter
            "img_cat_pizza_extreme" -> R.drawable.img_cat_pizza_extreme
            "img_cat_pizza_traditional" -> R.drawable.img_cat_pizza_traditional
            "img_cat_pizza_premium" -> R.drawable.img_cat_pizza_premium
            "img_cat_burger" -> R.drawable.img_cat_burger
            "img_cat_wraps" -> R.drawable.img_cat_wraps
            "img_cat_fries" -> R.drawable.img_cat_fries
            "img_cat_fried_chicken" -> R.drawable.img_cat_fried_chicken
            "img_cat_sandwiches" -> R.drawable.img_cat_sandwiches
            "img_cat_pasta" -> R.drawable.img_cat_pasta
            "img_cat_drinks" -> R.drawable.img_cat_drinks
            "img_cat_deals" -> R.drawable.img_cat_deals
            else -> R.drawable.img_hero_platter
        }
    }

    val availableImageKeys: List<Pair<String, String>> = listOf(
        "img_hero_platter" to "Cafe Feast Platter",
        "img_cat_pizza_extreme" to "Extreme Pizza",
        "img_cat_pizza_traditional" to "Traditional Pizza",
        "img_cat_pizza_premium" to "Premium Pizza",
        "img_cat_burger" to "Crispy Burger",
        "img_cat_wraps" to "Wraps & Rolls",
        "img_cat_fries" to "Golden Fries",
        "img_cat_fried_chicken" to "Fried Chicken",
        "img_cat_sandwiches" to "Grilled Sandwiches",
        "img_cat_pasta" to "Baked Pasta",
        "img_cat_drinks" to "Cold Drinks",
        "img_cat_deals" to "Special Deals"
    )
}
