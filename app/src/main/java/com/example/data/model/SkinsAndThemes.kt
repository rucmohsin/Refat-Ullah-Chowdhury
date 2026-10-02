package com.example.data.model

object SkinsAndThemes {

    val arrowSkins = listOf(
        ArrowSkin(
            id = "skin_black_classic",
            nameBn = "ক্লাসিক কালো তীর",
            descriptionBn = "গেমের ঐতিহ্যবাহী মসৃণ কালো তীর, নিখুঁত নিশানা ও স্পষ্ট অবয়ব।",
            priceCoins = 0,
            primaryColorHex = 0xFF212121,
            accentColorHex = 0xFF424242,
            isUnlocked = true
        ),
        ArrowSkin(
            id = "skin_gold_trim",
            nameBn = "সোনালী রাজকীয় তীর",
            descriptionBn = "উজ্জ্বল খাঁটি সোনার কারুকাজ, বিজয়ের রাজকীয় উজ্জ্বল প্রতীক।",
            priceCoins = 150,
            primaryColorHex = 0xFFFFB300, // Brilliant Pure Golden Yellow
            accentColorHex = 0xFFFFD700
        ),
        ArrowSkin(
            id = "skin_bamboo_folk",
            nameBn = "ঐতিহ্যবাহী বাঁশের তীর",
            descriptionBn = "বাংলার লোকজ কুটির শিল্পের বাঁশের ধনুর্বিদ্যার উজ্জ্বল স্মারক।",
            priceCoins = 250,
            primaryColorHex = 0xFF8D6E63, // Rich Warm Bamboo Brown
            accentColorHex = 0xFF6D4C41
        ),
        ArrowSkin(
            id = "skin_mayurpankhi",
            nameBn = "ময়ূরপঙ্খী তীর",
            descriptionBn = "ময়ূরের রূপালী নীল ও উজ্জ্বল পান্না রঙের অপূর্ব সংমিশ্রণ।",
            priceCoins = 350,
            primaryColorHex = 0xFF00B0FF, // Brilliant Peacock Sky Blue
            accentColorHex = 0xFF00E5FF
        ),
        ArrowSkin(
            id = "skin_crimson_ruby",
            nameBn = "লাল পলাশ তীর",
            descriptionBn = "বসন্তের ফুটন্ত পলাশ ও লাল রঙের উজ্জ্বল দীপ্তিময় তীর।",
            priceCoins = 450,
            primaryColorHex = 0xFFD50000, // Vivid Crimson Scarlet Red
            accentColorHex = 0xFFFF1744
        ),
        ArrowSkin(
            id = "skin_silver_lightning",
            nameBn = "রৌপ্য বিদ্যুৎ তীর",
            descriptionBn = "আকাশের বিদ্যুতের মতো চকচকে রূপালী ইস্পাত ফলা।",
            priceCoins = 600,
            primaryColorHex = 0xFF78909C, // Bright Shiny Silver Steel
            accentColorHex = 0xFFB0BEC5
        ),
        ArrowSkin(
            id = "skin_terracotta",
            nameBn = "লোকজ পোড়ামাটি তীর",
            descriptionBn = "পাহাড়পুর ও কান্তজির ঐতিহাসিক পোড়ামাটির উজ্জ্বল শিল্পশৈলী।",
            priceCoins = 750,
            primaryColorHex = 0xFFFF5722, // Fiery Terracotta Orange
            accentColorHex = 0xFFFF8A65
        ),
        ArrowSkin(
            id = "skin_royal_purple",
            nameBn = "রাজকীয় জামরুল তীর",
            descriptionBn = "অভিজাত অমীমাংসিত রহস্যের অপূর্ব গাঢ় বেগুনি আভা।",
            priceCoins = 900,
            primaryColorHex = 0xFFAA00FF, // Royal Vivid Purple
            accentColorHex = 0xFFD500F9
        ),
        ArrowSkin(
            id = "skin_emerald_jade",
            nameBn = "পান্না সবুজ তীর",
            descriptionBn = "প্রকৃতির সতেজ চিরহরিৎ পান্না রত্নের চোখজুড়ানো দ্যুতি।",
            priceCoins = 1000,
            primaryColorHex = 0xFF00C853, // Lush Emerald Jade Green
            accentColorHex = 0xFF69F0AE
        )
    )

    val boardThemes = listOf(
        BoardTheme(
            id = "theme_emerald",
            nameBn = "শ্যামল বাংলা",
            descriptionBn = "বাংলার রূপসী শ্যামল প্রকৃতির সবুজ ও সতেজ পরিবেশ।",
            backgroundStartHex = 0xFF0A2E1D,
            backgroundEndHex = 0xFF04170E,
            cardBackgroundHex = 0xFF103D27,
            gridBorderHex = 0xFF1B5E20,
            isUnlocked = true,
            priceCoins = 0
        ),
        BoardTheme(
            id = "theme_terracotta",
            nameBn = "গ্রামীন পোড়ামাটি",
            descriptionBn = "মাটির ঘর ও বাংলার সনাতন ঐতিহ্যের উষ্ণ মাটির ছোঁয়া।",
            backgroundStartHex = 0xFF3E2723,
            backgroundEndHex = 0xFF1B0000,
            cardBackgroundHex = 0xFF4E342E,
            gridBorderHex = 0xFF8D6E63,
            isUnlocked = false,
            priceCoins = 200
        ),
        BoardTheme(
            id = "theme_meghna",
            nameBn = "মেঘনা নদীর ঢেউ",
            descriptionBn = "শান্ত নদীর নীল জলরাশি ও দূর দিগন্তের মোহনা।",
            backgroundStartHex = 0xFF0D233A,
            backgroundEndHex = 0xFF05101A,
            cardBackgroundHex = 0xFF143657,
            gridBorderHex = 0xFF1976D2,
            isUnlocked = false,
            priceCoins = 300
        ),
        BoardTheme(
            id = "theme_sundarbans",
            nameBn = "রাতের সুন্দরবন",
            descriptionBn = "রহস্যময় ম্যানগ্রোভ অরণ্যের রাতের মায়াবী গভীরতা।",
            backgroundStartHex = 0xFF121B15,
            backgroundEndHex = 0xFF080D0A,
            cardBackgroundHex = 0xFF1A2920,
            gridBorderHex = 0xFF2E7D32,
            isUnlocked = false,
            priceCoins = 400
        ),
        BoardTheme(
            id = "theme_boishakh",
            nameBn = "শুভ নববর্ষ",
            descriptionBn = "উৎসবমুখর বৈশাখী মেলার আনন্দঘন লাল ও সাদার আমেজ।",
            backgroundStartHex = 0xFF3E1015,
            backgroundEndHex = 0xFF1D0508,
            cardBackgroundHex = 0xFF5C1920,
            gridBorderHex = 0xFFD32F2F,
            isUnlocked = false,
            priceCoins = 500
        )
    )

    fun getSkinById(id: String): ArrowSkin {
        return arrowSkins.find { it.id == id } ?: arrowSkins.first()
    }

    fun getThemeById(id: String): BoardTheme {
        return boardThemes.find { it.id == id } ?: boardThemes.first()
    }
}
