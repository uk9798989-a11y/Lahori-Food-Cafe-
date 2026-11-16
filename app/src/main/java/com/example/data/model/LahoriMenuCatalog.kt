package com.example.data.model

/**
 * Authoritative menu catalog extracted 1:1 from the official uploaded Lahori Food Cafe menus.
 * Items without printed prices (e.g. Soft Drinks, XL pizza extra topping) are explicitly flagged
 * with priceVerified = false for admin verification rather than guessing prices.
 */
object LahoriMenuCatalog {

    val categories: List<Category> = listOf(
        Category(id = "cat_extreme_pizza", name = "Extreme Pizza", group = "PIZZA", displayOrder = 1, imageKey = "img_cat_pizza_extreme", isActive = true),
        Category(id = "cat_traditional_pizza", name = "Traditional Pizza", group = "PIZZA", displayOrder = 2, imageKey = "img_cat_pizza_traditional", isActive = true),
        Category(id = "cat_premium_pizza", name = "Premium Pizza", group = "PIZZA", displayOrder = 3, imageKey = "img_cat_pizza_premium", isActive = true),
        Category(id = "cat_burgers", name = "Burgers", group = "FAST_FOOD", displayOrder = 4, imageKey = "img_cat_burger", isActive = true),
        Category(id = "cat_wraps_rolls", name = "Wraps & Rolls", group = "FAST_FOOD", displayOrder = 5, imageKey = "img_cat_wraps", isActive = true),
        Category(id = "cat_fries", name = "Fries", group = "FAST_FOOD", displayOrder = 6, imageKey = "img_cat_fries", isActive = true),
        Category(id = "cat_fried_chicken", name = "Fried Chicken", group = "FAST_FOOD", displayOrder = 7, imageKey = "img_cat_fried_chicken", isActive = true),
        Category(id = "cat_sandwiches", name = "Sandwiches", group = "FAST_FOOD", displayOrder = 8, imageKey = "img_cat_sandwiches", isActive = true),
        Category(id = "cat_pastas", name = "Pastas", group = "FAST_FOOD", displayOrder = 9, imageKey = "img_cat_pasta", isActive = true),
        Category(id = "cat_soft_drinks", name = "Soft Drinks", group = "FAST_FOOD", displayOrder = 10, imageKey = "img_cat_drinks", isActive = true)
    )

    val products: List<Product> = listOf(
        // --- EXTREME PIZZA (M, L) ---
        pizza("prod_ext_peri_peri", "Extreme Peri Peri", "cat_extreme_pizza", "Extreme Pizza", "img_cat_pizza_extreme",
            "Loaded extreme crust pizza topped with spicy Peri Peri chicken & signature cheese.",
            mapOf("M" to 1450, "L" to 1950), order = 1, popular = true),
        pizza("prod_ext_malai_boti", "Extreme Malai Boti", "cat_extreme_pizza", "Extreme Pizza", "img_cat_pizza_extreme",
            "Rich creamy Malai Boti chunks baked on our heavy extreme crust.",
            mapOf("M" to 1450, "L" to 1950), order = 2),
        pizza("prod_ext_kababish", "Extreme Kababish", "cat_extreme_pizza", "Extreme Pizza", "img_cat_pizza_extreme",
            "Smoky Pakistani kabab topping with mozzarella blend on extreme crust.",
            mapOf("M" to 1450, "L" to 1950), order = 3),
        pizza("prod_ext_crispy", "Extreme Crispy", "cat_extreme_pizza", "Extreme Pizza", "img_cat_pizza_extreme",
            "Crunchy crispy chicken fillets layered with melted cheese and sauces.",
            mapOf("M" to 1450, "L" to 1950), order = 4, newArrival = true),
        pizza("prod_pizza_stick", "Pizza Stick", "cat_extreme_pizza", "Extreme Pizza", "img_cat_pizza_extreme",
            "Golden baked stuffed pizza sticks served hot with signature flavor.",
            mapOf("M" to 900, "L" to 1400), order = 5),

        // --- TRADITIONAL PIZZA (S, M, L, XL) ---
        pizza("prod_trad_lahori_special", "Lahori Special Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "House chef special pizza crafted with Lahori Food Cafe signature toppings.",
            mapOf("S" to 600, "M" to 1070, "L" to 1590, "XL" to 2300), order = 6, popular = true),
        pizza("prod_trad_malai_boti", "Malai Boti Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Tender spiced Malai Boti chicken with bell peppers and creamy sauce.",
            mapOf("S" to 590, "M" to 1050, "L" to 1550, "XL" to 2100), order = 7),
        pizza("prod_trad_nawabi", "Nawabi Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Royal spiced chicken cubes with onions, olives, and rich cheese.",
            mapOf("S" to 550, "M" to 950, "L" to 1450, "XL" to 2100), order = 8),
        pizza("prod_trad_fajita", "Fajita Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Classic chicken fajita seasoning with capsicum, onions, and mozzarella.",
            mapOf("S" to 550, "M" to 950, "L" to 1450, "XL" to 2150), order = 9, popular = true),
        pizza("prod_trad_tikka", "Tikka Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Chargrilled desi chicken tikka flavor on freshly baked pizza dough.",
            mapOf("S" to 550, "M" to 950, "L" to 1450, "XL" to 2150), order = 10, popular = true),
        pizza("prod_trad_creamy_mughlai", "Creamy Mughlai", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Velvety Mughlai white sauce base topped with tender chicken and herbs.",
            mapOf("S" to 550, "M" to 950, "L" to 1450, "XL" to 2150), order = 11),
        pizza("prod_trad_bonfire", "Bonfire Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Fiery smoky chicken chunks with jalapenos and bold spices.",
            mapOf("S" to 590, "M" to 1000, "L" to 1490, "XL" to 2150), order = 12),
        pizza("prod_trad_shahi", "Shahi Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Savory spiced chicken and vegetables baked in rich mozzarella.",
            mapOf("S" to 590, "M" to 1000, "L" to 1490, "XL" to 2150), order = 13),
        pizza("prod_trad_crispy", "Crispy Pizza", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Topped with golden crispy chicken bites and tangy pizza sauce.",
            mapOf("S" to 600, "M" to 1150, "L" to 1600, "XL" to 2200), order = 14),
        pizza("prod_trad_hot_spicy", "Hot & Spicy", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "For spice lovers — zesty marinated chicken, jalapenos, and chili flakes.",
            mapOf("S" to 590, "M" to 1000, "L" to 1490, "XL" to 2150), order = 15),
        pizza("prod_trad_chicken_supreme", "Chicken Supreme", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "Loaded with chicken, mushrooms, olives, capsicum, and sweet corn.",
            mapOf("S" to 590, "M" to 1000, "L" to 1490, "XL" to 2150), order = 16),
        pizza("prod_trad_peri_tikka", "Peri Peri Tikka Fusion", "cat_traditional_pizza", "Traditional Pizza", "img_cat_pizza_traditional",
            "A fiery fusion of Peri Peri drizzle and classic desi Chicken Tikka.",
            mapOf("S" to 590, "M" to 1000, "L" to 1490, "XL" to 2150), order = 17, newArrival = true),

        // --- PREMIUM PIZZA ---
        pizza("prod_prem_bihari_kabab", "Bihari Kabab", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Authentic smoky Bihari Kabab strips with onions and creamy drizzle.",
            mapOf("S" to 690, "M" to 1190, "L" to 1690, "XL" to 2600), order = 18, popular = true),
        pizza("prod_prem_makhni_kabab", "Makhni Kabab", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Rich buttery Makhni gravy base topped with spiced seekh kabab.",
            mapOf("S" to 690, "M" to 1190, "L" to 1690, "XL" to 2600), order = 19),
        pizza("prod_prem_crown_crust", "Crown Crust", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Iconic crown-shaped pockets stuffed with cheese & chicken around the rim.",
            mapOf("S" to 690, "M" to 1190, "L" to 1690, "XL" to 2600), order = 20, popular = true),
        pizza("prod_prem_kabab_crust", "Kabab Crust", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Seekh kabab baked directly into the outer crust with melted mozzarella.",
            mapOf("S" to 690, "M" to 1190, "L" to 1690, "XL" to 2600), order = 21),
        pizza("prod_prem_gola_kabab", "Gola Kabab", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Juicy Gola Kabab medallions paired with smoky BBQ and cheese.",
            mapOf("S" to 690, "M" to 1190, "L" to 1690, "XL" to 2600), order = 22),
        pizza("prod_prem_cheese_crust", "Cheese Crust", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Golden mozzarella-stuffed crust pizza available in M, L, and XL.",
            mapOf("M" to 1190, "L" to 1690, "XL" to 2600), order = 23),
        pizza("prod_prem_royal_crust", "Royal Crust", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Signature stuffed crust layered with gourmet chicken and creamy sauces.",
            mapOf("M" to 1190, "L" to 1690, "XL" to 2600), order = 24, newArrival = true),
        pizza("prod_prem_double_seekh", "Double Seekh Kabab", "cat_premium_pizza", "Premium Pizza", "img_cat_pizza_premium",
            "Double layer of juicy seekh kababs with extra cheese on stuffed crust.",
            mapOf("M" to 1400, "L" to 1850, "XL" to 2700), order = 25),

        // --- BURGERS ---
        burger("prod_brg_lahori_sig", "Lahori Signature Burger", 450, "Our house signature fillet burger with special Lahori sauce and melted cheese.", 26, popular = true, newArrival = true),
        burger("prod_brg_zinger", "Zinger Burger", 350, "Crispy golden fried chicken thigh fillet with lettuce and mayo in a toasted bun.", 27, popular = true),
        burger("prod_brg_jalapeno", "Jalapeno Burger", 400, "Crispy zinger fillet topped with spicy sliced jalapenos and zesty sauce.", 28),
        burger("prod_brg_double_decker", "Double Decker Burger", 600, "Two thick crispy chicken fillets stacked high with cheese and signature sauce.", 29),
        burger("prod_brg_tower", "Tower Burger", 550, "Crispy zinger fillet, crispy hash brown, cheese slice, and spicy mayo.", 30),
        burger("prod_brg_patty", "Patty Burger", 300, "Tender seasoned chicken patty burger with fresh crisp veggies.", 31),
        burger("prod_brg_beef", "Beef Burger", 450, "Juicy flame-grilled beef patty with caramelized onions and burger sauce.", 32),
        burger("prod_brg_chicken", "Chicken Burger", 180, "Classic light chicken patty burger with lettuce and creamy mayo.", 33),

        // --- WRAPS & ROLLS ---
        fastFood("prod_wrap_lahori_special", "Lahori Special Roll", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "House special chicken paratha roll packed with signature Lahori sauces.", 400, 34, popular = true),
        fastFood("prod_wrap_zinger_roll", "Zinger Roll", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Crunchy zinger strips wrapped in warm flatbread with garlic mayo.", 350, 35),
        fastFood("prod_wrap_zinger_shawarma", "Zinger Shawarma", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Crispy zinger fillet folded inside warm pita bread with pickled veggies.", 330, 36),
        fastFood("prod_wrap_bihari_roll", "Bihari Kabab Roll", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Smoky Bihari kabab rolled with onions and chutney in paratha.", 350, 37),
        fastFood("prod_wrap_grill", "Grill Wrap", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Smoky chargrilled chicken wrap with fresh salad and creamy dressing.", 450, 38, newArrival = true),
        fastFood("prod_wrap_tikka_paratha", "Tikka Paratha Roll", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Spiced chicken tikka chunks wrapped in flaky crispy paratha.", 250, 39),
        fastFood("prod_wrap_malai_boti", "Malai Boti Roll", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Creamy melt-in-mouth malai boti wrapped in fresh paratha.", 280, 40),
        fastFood("prod_wrap_chicken_shawarma", "Chicken Shawarma", "cat_wraps_rolls", "Wraps & Rolls", "img_cat_wraps",
            "Traditional spiced chicken shawarma in soft pita bread.", 180, 41),

        // --- FRIES ---
        fastFood("prod_fries_single", "Single Serving Fries", "cat_fries", "Fries", "img_cat_fries",
            "Individual portion of hot golden crispy French fries.", 150, 42),
        fastFood("prod_fries_plain", "Plain Fries", "cat_fries", "Fries", "img_cat_fries",
            "Classic salted crispy fries portion.", 250, 43),
        fastFood("prod_fries_masala", "Masala Fries", "cat_fries", "Fries", "img_cat_fries",
            "Crispy fries tossed in tangy Lahori chaat masala.", 350, 44),
        fastFood("prod_fries_loaded_small", "Loaded Fries Small", "cat_fries", "Fries", "img_cat_fries",
            "Fries topped with melted cheese sauce, chicken chunks, and jalapenos (Small).", 430, 45),
        fastFood("prod_fries_loaded_large", "Loaded Fries Large", "cat_fries", "Fries", "img_cat_fries",
            "Generous platter of loaded cheese & chicken fries (Large).", 700, 46, popular = true),
        fastFood("prod_fries_crunchy_half", "Crunchy Loaded Half", "cat_fries", "Fries", "img_cat_fries",
            "Crispy zinger bits over cheesy loaded fries (Half).", 450, 47),
        fastFood("prod_fries_crunchy_full", "Crunchy Loaded Full", "cat_fries", "Fries", "img_cat_fries",
            "Full platter of crunchy zinger bits over cheesy loaded fries.", 750, 48, newArrival = true),

        // --- FRIED CHICKEN ---
        sizedFastFood("prod_fc_crispy_wings", "Crispy Wings", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Golden crunch-breaded chicken wings fried fresh to order.", mapOf("5P" to 320, "10P" to 600), 49, popular = true),
        sizedFastFood("prod_fc_grilled_wings", "Grilled Wings", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Flame-grilled marinated chicken wings with herb glaze.", mapOf("5P" to 350, "10P" to 600), 50),
        sizedFastFood("prod_fc_smoky_wings", "Smoky Wings", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Wings tossed in rich smoky BBQ glaze.", mapOf("5P" to 350, "10P" to 600), 51),
        sizedFastFood("prod_fc_creamy_wings", "Creamy Wings", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Crispy wings glazed in garlic parmesan cream sauce.", mapOf("5P" to 350, "10P" to 600), 52),
        sizedFastFood("prod_fc_peri_wings", "Peri Peri Wings", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Spicy tangy Peri Peri glazed chicken wings.", mapOf("5P" to 350, "10P" to 600), 53),
        sizedFastFood("prod_fc_hot_shots", "Hot Shots", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Bite-sized crispy spiced chicken popcorn shots.", mapOf("5P" to 250, "10P" to 450), 54),
        sizedFastFood("prod_fc_nuggets", "Nuggets", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Tender golden chicken nuggets served with dip.", mapOf("5P" to 300, "10P" to 600), 55),
        fastFood("prod_fc_leg_broast", "Crispy Leg Broast", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Crispy southern-fried leg quarter broast.", 350, 56),
        fastFood("prod_fc_chest_broast", "Crispy Chest Broast", "cat_fried_chicken", "Fried Chicken", "img_cat_fried_chicken",
            "Crispy golden chest quarter broast.", 400, 57),

        // --- SANDWICHES ---
        fastFood("prod_sand_lahori_special", "Lahori Special Sandwich", "cat_sandwiches", "Sandwiches", "img_cat_sandwiches",
            "Signature triple-layer grilled sandwich with chicken, egg, cheese & fries.", 500, 58, popular = true),
        fastFood("prod_sand_club", "Club Sandwich", "cat_sandwiches", "Sandwiches", "img_cat_sandwiches",
            "Classic toasted club sandwich with chicken, lettuce, tomato, and mayo.", 450, 59),
        fastFood("prod_sand_chicken", "Chicken Sandwich", "cat_sandwiches", "Sandwiches", "img_cat_sandwiches",
            "Grilled seasoned chicken fillet sandwich on toasted bread.", 450, 60),
        fastFood("prod_sand_malai_panini", "Malai Panini Sandwich", "cat_sandwiches", "Sandwiches", "img_cat_sandwiches",
            "Pressed panini stuffed with creamy malai chicken and melted cheese.", 450, 61, newArrival = true),

        // --- PASTAS (HALF, FULL) ---
        sizedFastFood("prod_pasta_lahori_special", "Lahori Special Pasta", "cat_pastas", "Pastas", "img_cat_pasta",
            "Baked cheesy penne pasta with Lahori special chicken and house sauce.", mapOf("HALF" to 500, "FULL" to 800), 62, popular = true),
        sizedFastFood("prod_pasta_crunchy", "Crunchy Pasta", "cat_pastas", "Pastas", "img_cat_pasta",
            "Creamy baked pasta crowned with crispy fried chicken strips.", mapOf("HALF" to 500, "FULL" to 800), 63),
        sizedFastFood("prod_pasta_flaming", "Flaming Pasta", "cat_pastas", "Pastas", "img_cat_pasta",
            "Spicy red-chili baked pasta loaded with jalapenos and mozzarella.", mapOf("HALF" to 450, "FULL" to 700), 64),
        sizedFastFood("prod_pasta_creamy", "Creamy Pasta", "cat_pastas", "Pastas", "img_cat_pasta",
            "Smooth Alfredo-style creamy chicken pasta topped with baked cheese.", mapOf("HALF" to 450, "FULL" to 700), 65),

        // --- SOFT DRINKS (Prices not printed on flyer -> left for Admin Verification!) ---
        unpricedDrink("prod_drink_regular", "Regular Cold Drink", "Chilled regular carbonated soft drink.", 66),
        unpricedDrink("prod_drink_500ml", "Cold Drink 500 ml", "Chilled 500 ml soft drink bottle.", 67),
        unpricedDrink("prod_drink_1l", "1 Liter Drink", "1 Liter family soft drink bottle.", 68),
        unpricedDrink("prod_drink_15l", "1.5 Liter Drink", "1.5 Liter family soft drink bottle.", 69),
        unpricedDrink("prod_drink_jumbo", "Jumbo Liter Drink", "Jumbo size cold drink bottle.", 70),
        unpricedDrink("prod_drink_water_small", "Small Water", "Chilled small mineral water bottle.", 71),
        unpricedDrink("prod_drink_water_large", "Large Water", "Chilled large mineral water bottle.", 72)
    )

    val deals: List<Deal> = listOf(
        Deal(id = "deal_01", dealNumber = "DEAL 1", title = "Deal 1 — Zinger Solo", itemsSummary = "1 Zinger Burger", drinkInfo = "1 Regular Drink", price = 400, imageKey = "img_cat_burger", isFeatured = true, displayOrder = 1),
        Deal(id = "deal_02", dealNumber = "DEAL 2", title = "Deal 2 — Zinger Trio", itemsSummary = "3 Zinger Burgers", drinkInfo = "1 Liter Drink", price = 1190, imageKey = "img_cat_burger", isFeatured = true, displayOrder = 2),
        Deal(id = "deal_03", dealNumber = "DEAL 3", title = "Deal 3 — Small Pizza & Wings", itemsSummary = "1 Small Pizza • 4 Pc Wings", drinkInfo = "1 Half Drink", price = 850, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 3),
        Deal(id = "deal_04", dealNumber = "DEAL 4", title = "Deal 4 — Medium Pizza & Wings", itemsSummary = "1 Medium Pizza • 6 Hot Wings", drinkInfo = "1 Liter Drink", price = 1450, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", isFeatured = true, displayOrder = 4),
        Deal(id = "deal_05", dealNumber = "DEAL 5", title = "Deal 5 — Large Pizza & Wings", itemsSummary = "1 Large Pizza • 8 Pc Hot Wings", drinkInfo = "1 Liter Drink", price = 2070, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 5),
        Deal(id = "deal_06", dealNumber = "DEAL 6", title = "Deal 6 — 5 Zinger Feast", itemsSummary = "5 Zinger Burgers", drinkInfo = "1.5 Liter Drink", price = 1900, imageKey = "img_cat_burger", displayOrder = 6),
        Deal(id = "deal_07", dealNumber = "DEAL 7", title = "Deal 7 — Large & Medium Duo", itemsSummary = "1 Large Pizza • 1 Medium Pizza", drinkInfo = "1.5 Liter Drink", price = 2550, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 7),
        Deal(id = "deal_08", dealNumber = "DEAL 8", title = "Deal 8 — Twin Small Pizzas", itemsSummary = "2 Small Pizzas", drinkInfo = "1 Liter Drink", price = 1240, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 8),
        Deal(id = "deal_09", dealNumber = "DEAL 9", title = "Deal 9 — Twin Medium Pizzas", itemsSummary = "2 Medium Pizzas", drinkInfo = "1.5 Liter Drink", price = 2050, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 9),
        Deal(id = "deal_10", dealNumber = "DEAL 10", title = "Deal 10 — Large Pizza Combo", itemsSummary = "1 Large Pizza", drinkInfo = "1 Liter Drink", price = 1580, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 10),
        Deal(id = "deal_11", dealNumber = "DEAL 11", title = "Deal 11 — Wings & Nuggets", itemsSummary = "5 Hot Wings • 5 Nuggets", drinkInfo = "1 Half Drink", price = 650, imageKey = "img_cat_fried_chicken", displayOrder = 11),
        Deal(id = "deal_12", dealNumber = "DEAL 12", title = "Deal 12 — Mega Wings & Nuggets", itemsSummary = "10 Hot Wings • 10 Nuggets", drinkInfo = "1 Liter Drink", price = 1280, imageKey = "img_cat_fried_chicken", displayOrder = 12),
        Deal(id = "deal_13", dealNumber = "DEAL 13", title = "Deal 13 — Triple Craving", itemsSummary = "1 Small Pizza • 1 Zinger Burger • 5 Hot Wings", drinkInfo = "1 Liter Drink", price = 1380, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_hero_platter", isFeatured = true, displayOrder = 13),
        Deal(id = "deal_14", dealNumber = "DEAL 14", title = "Deal 14 — Pizza & Zinger", itemsSummary = "1 Medium Pizza • 1 Zinger Burger", drinkInfo = "1 Liter Drink", price = 1440, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_hero_platter", displayOrder = 14),
        Deal(id = "deal_15", dealNumber = "DEAL 15", title = "Deal 15 — Party Pizza & Wings", itemsSummary = "1 Large Pizza • 10 Hot Wings", drinkInfo = "1 Liter Drink", price = 2150, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 15),
        Deal(id = "deal_16", dealNumber = "DEAL 16", title = "Deal 16 — Double Zinger & Wings", itemsSummary = "2 Zinger Burgers • 5 Hot Wings", drinkInfo = "2 Regular Drinks", price = 1160, imageKey = "img_cat_burger", displayOrder = 16),
        Deal(id = "deal_17", dealNumber = "DEAL 17", title = "Deal 17 — Twin Large Pizzas", itemsSummary = "2 Large Pizzas", drinkInfo = "1.5 Liter Drink", price = 3050, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_cat_pizza_traditional", displayOrder = 17),
        Deal(id = "deal_18", dealNumber = "DEAL 18", title = "Deal 18 — Medium Extreme Combo", itemsSummary = "1 Medium Extreme Pizza", drinkInfo = "1 Liter Drink", price = 1550, imageKey = "img_cat_pizza_extreme", isFeatured = true, displayOrder = 18),
        Deal(id = "deal_19", dealNumber = "DEAL 19", title = "Deal 19 — Large Extreme Combo", itemsSummary = "1 Large Extreme Pizza", drinkInfo = "1 (1.5 Liter) Drink", price = 2050, imageKey = "img_cat_pizza_extreme", displayOrder = 19),
        Deal(id = "deal_birthday", dealNumber = "BIRTHDAY DEAL", title = "Birthday Celebration Feast", itemsSummary = "2 Large Pizza • 4 Zinger Burgers • 10 Hot Wings • 10 Nuggets • 2 Fries • 1 Pound Cake", drinkInfo = "2 (1.5 Liter) Drinks", price = 7070, note = "Additional charges apply on Signature & Crusty flavours.", imageKey = "img_hero_platter", isFeatured = true, displayOrder = 20)
    )

    val banners: List<Banner> = listOf(
        Banner(
            id = "banner_free_delivery",
            title = "Free Delivery Across Taranda M. Panah",
            subtitle = "Order Rs. 700+ for complimentary doorstep delivery from Main Chowk.",
            badgeText = "FREE DELIVERY",
            imageKey = "img_hero_platter",
            targetRoute = "menu",
            displayOrder = 1
        ),
        Banner(
            id = "banner_special_deals",
            title = "20 Value Deals Starting Rs. 400",
            subtitle = "Explore Deal 1 to Deal 19 plus our 1-Pound Cake Birthday Deal.",
            badgeText = "HOT DEALS",
            imageKey = "img_cat_deals",
            targetRoute = "deals",
            displayOrder = 2
        ),
        Banner(
            id = "banner_extreme_pizza",
            title = "Extreme & Premium Stuffed Crusts",
            subtitle = "Bihari Kabab, Crown Crust, Royal Crust & Extreme Peri Peri.",
            badgeText = "CHEF SPECIAL",
            imageKey = "img_cat_pizza_premium",
            targetRoute = "menu",
            displayOrder = 3
        )
    )

    private fun pizza(
        id: String,
        name: String,
        categoryId: String,
        categoryName: String,
        imageKey: String,
        description: String,
        sizes: Map<String, Int>,
        order: Int,
        popular: Boolean = false,
        newArrival: Boolean = false
    ) = Product(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        group = "PIZZA",
        description = description,
        basePrice = sizes.values.minOrNull() ?: 0,
        sizePrices = sizes,
        supportsPizzaCustomizations = true,
        supportsExtraCheeseSlice = false,
        priceVerified = true,
        isAvailable = true,
        isActive = true,
        isPopular = popular,
        isNewArrival = newArrival,
        imageKey = imageKey,
        displayOrder = order
    )

    private fun burger(
        id: String,
        name: String,
        price: Int,
        description: String,
        order: Int,
        popular: Boolean = false,
        newArrival: Boolean = false
    ) = Product(
        id = id,
        name = name,
        categoryId = "cat_burgers",
        categoryName = "Burgers",
        group = "FAST_FOOD",
        description = description,
        basePrice = price,
        sizePrices = emptyMap(),
        supportsPizzaCustomizations = false,
        supportsExtraCheeseSlice = true,
        priceVerified = true,
        isAvailable = true,
        isActive = true,
        isPopular = popular,
        isNewArrival = newArrival,
        imageKey = "img_cat_burger",
        displayOrder = order
    )

    private fun fastFood(
        id: String,
        name: String,
        categoryId: String,
        categoryName: String,
        imageKey: String,
        description: String,
        price: Int,
        order: Int,
        popular: Boolean = false,
        newArrival: Boolean = false
    ) = Product(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        group = "FAST_FOOD",
        description = description,
        basePrice = price,
        sizePrices = emptyMap(),
        supportsPizzaCustomizations = false,
        supportsExtraCheeseSlice = false,
        priceVerified = true,
        isAvailable = true,
        isActive = true,
        isPopular = popular,
        isNewArrival = newArrival,
        imageKey = imageKey,
        displayOrder = order
    )

    private fun sizedFastFood(
        id: String,
        name: String,
        categoryId: String,
        categoryName: String,
        imageKey: String,
        description: String,
        sizes: Map<String, Int>,
        order: Int,
        popular: Boolean = false
    ) = Product(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        group = "FAST_FOOD",
        description = description,
        basePrice = sizes.values.minOrNull() ?: 0,
        sizePrices = sizes,
        supportsPizzaCustomizations = false,
        supportsExtraCheeseSlice = false,
        priceVerified = true,
        isAvailable = true,
        isActive = true,
        isPopular = popular,
        isNewArrival = false,
        imageKey = imageKey,
        displayOrder = order
    )

    private fun unpricedDrink(
        id: String,
        name: String,
        description: String,
        order: Int
    ) = Product(
        id = id,
        name = name,
        categoryId = "cat_soft_drinks",
        categoryName = "Soft Drinks",
        group = "FAST_FOOD",
        description = description,
        basePrice = 0,
        sizePrices = emptyMap(),
        supportsPizzaCustomizations = false,
        supportsExtraCheeseSlice = false,
        priceVerified = false,
        isAvailable = true,
        isActive = true,
        isPopular = false,
        isNewArrival = false,
        imageKey = "img_cat_drinks",
        displayOrder = order
    )
}
