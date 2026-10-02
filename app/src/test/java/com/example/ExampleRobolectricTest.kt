package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LeaderboardData
import com.example.game.LevelGenerator
import com.example.ui.BanglaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("মহসিনের তীর", appName)
    }

    @Test
    fun `creator leaderboard info has correct location and infinity points`() {
        val leaderboard = LeaderboardData.getLeaderboard(1, 0, 0)
        val creator = leaderboard[0]
        assertEquals("বাহেরনগর, দিলালপুর, বাজিতপুর, কিশোরগঞ্জ", creator.districtBn)
        assertEquals("∞ (অসীম)", creator.scoreDisplay)
        assertEquals("∞ (অসীম)", creator.starsDisplay)
    }

    @Test
    fun `level generator generates level 1 and high levels correctly and all arrows can exit`() {
        val level1 = LevelGenerator.generateLevel(1)
        assertNotNull(level1)
        assertTrue(level1.arrows.isNotEmpty())
        assertEquals(1, level1.levelNumber)
        assertTrue(level1.arrows.all { it.points.size >= 2 })
        assertTrue(LevelGenerator.canSolveAll(level1.arrows, level1.gridRows, level1.gridCols))

        val level20000 = LevelGenerator.generateLevel(20000)
        assertNotNull(level20000)
        assertTrue(level20000.arrows.isNotEmpty())
        assertEquals(20000, level20000.levelNumber)
        assertTrue(level20000.arrows.all { it.points.size >= 2 })
        assertTrue(LevelGenerator.canSolveAll(level20000.arrows, level20000.gridRows, level20000.gridCols))
    }

    @Test
    fun `bangla utils converts digits correctly`() {
        val result = BanglaUtils.toBanglaDigits(20000)
        assertEquals("২০০০", result.substring(0, 4))
    }
}
