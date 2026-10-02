package com.example.data.model

object LeaderboardData {

    private val creatorEntry = LeaderboardEntry(
        rank = 1,
        nameBn = "রেফাত উল্লাহ চৌধুরী মহসিন (স্রষ্টা)",
        level = 20000,
        score = Int.MAX_VALUE,
        stars = Int.MAX_VALUE,
        isPlayer = false,
        districtBn = "বাহেরনগর, দিলালপুর, বাজিতপুর, কিশোরগঞ্জ",
        scoreDisplay = "∞ (অসীম)",
        starsDisplay = "∞ (অসীম)"
    )

    private val otherTopPlayers = listOf(
        LeaderboardEntry(2, "তানভীর আহমেদ সীমান্ত", 18420, 2190000, 55260, false, "ঢাকা"),
        LeaderboardEntry(3, "নুসরাত জাহান স্বর্ণা", 17200, 1980000, 51600, false, "সিলেট"),
        LeaderboardEntry(4, "ফারহান কবির শান্ত", 15600, 1785000, 46800, false, "রাজশাহী"),
        LeaderboardEntry(5, "মেহজাবীন সুলতানা", 14350, 1620000, 43050, false, "খুলনা"),
        LeaderboardEntry(6, "আবরার ফাহিম সৌমিক", 12900, 1450000, 38700, false, "বরিশাল"),
        LeaderboardEntry(7, "সাদিয়া ইসলাম রিয়া", 11200, 1280000, 33600, false, "রংপুর"),
        LeaderboardEntry(8, "মাহমুদুর রহমান শুভ", 9850, 1120000, 29550, false, "ময়মনসিংহ"),
        LeaderboardEntry(9, "তাসনিম জান্নাত এশা", 8400, 950000, 25200, false, "কুমিল্লা"),
        LeaderboardEntry(10, "আরিফুল ইসলাম রাফি", 7100, 810000, 21300, false, "গাজীপুর"),
        LeaderboardEntry(11, "সাবরিনা নওশীন মীম", 5800, 670000, 17400, false, "বগুড়া"),
        LeaderboardEntry(12, "নাজমুল হুদা সৈকত", 4600, 530000, 13800, false, "দিনাজপুর"),
        LeaderboardEntry(13, "ইশরাত জাহান তমা", 3400, 395000, 10200, false, "কক্সবাজার"),
        LeaderboardEntry(14, "রাকিবুল হাসান জিসান", 2200, 255000, 6600, false, "যশোর"),
        LeaderboardEntry(15, "আতিয়া চৌধুরী", 1200, 140000, 3600, false, "ফরিদপুর")
    )

    fun getLeaderboard(
        userLevel: Int,
        userScore: Int,
        userStars: Int,
        userName: String = "বাঙালি তীরন্দাজ",
        userDistrict: String = "কিশোরগঞ্জ"
    ): List<LeaderboardEntry> {
        val userEntry = LeaderboardEntry(
            rank = 0,
            nameBn = "$userName (আপনি)",
            level = userLevel,
            score = maxOf(userScore, userLevel * 120),
            stars = maxOf(userStars, userLevel * 3),
            isPlayer = true,
            districtBn = userDistrict
        )

        // Creator Refat Ullah Chowdhury Mohsin always stays at Rank 1 with Infinity points
        val restSorted = (otherTopPlayers + userEntry).sortedByDescending { it.score }
        val mappedRest = restSorted.mapIndexed { index, entry ->
            entry.copy(rank = index + 2)
        }

        return listOf(creatorEntry) + mappedRest
    }
}
