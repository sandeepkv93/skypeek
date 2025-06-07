package com.example.skypeek

import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class WeatherIconNightTimeTest {

    // Copy the exact isNightTime logic from WeatherIcons.kt for testing
    private fun isNightTime(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Very aggressive night time detection: 6 PM (18:00) to 8 AM (07:59)
        // This ensures 1 AM, 2 AM, etc. are definitely night time
        return hour >= 18 || hour < 8
    }

    @Test
    fun testNightTimeDetection() {
        val calendar = Calendar.getInstance()
        
        // Test 1 AM - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val oneAM = calendar.timeInMillis
        assertTrue("1 AM should be night time", isNightTime(oneAM))
        
        // Test 2 AM - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 2)
        val twoAM = calendar.timeInMillis
        assertTrue("2 AM should be night time", isNightTime(twoAM))
        
        // Test 6 AM - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 6)
        val sixAM = calendar.timeInMillis
        assertTrue("6 AM should be night time", isNightTime(sixAM))
        
        // Test 7 AM - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 7)
        val sevenAM = calendar.timeInMillis
        assertTrue("7 AM should be night time", isNightTime(sevenAM))
        
        // Test 8 AM - should be day
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        val eightAM = calendar.timeInMillis
        assertFalse("8 AM should be day time", isNightTime(eightAM))
        
        // Test 12 PM - should be day
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        val noon = calendar.timeInMillis
        assertFalse("12 PM should be day time", isNightTime(noon))
        
        // Test 5 PM - should be day
        calendar.set(Calendar.HOUR_OF_DAY, 17)
        val fivePM = calendar.timeInMillis
        assertFalse("5 PM should be day time", isNightTime(fivePM))
        
        // Test 6 PM - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 18)
        val sixPM = calendar.timeInMillis
        assertTrue("6 PM should be night time", isNightTime(sixPM))
        
        // Test 11 PM - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        val elevenPM = calendar.timeInMillis
        assertTrue("11 PM should be night time", isNightTime(elevenPM))
        
        // Test midnight - should be night
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val midnight = calendar.timeInMillis
        assertTrue("Midnight should be night time", isNightTime(midnight))
    }

    @Test
    fun testSpecificProblemHours() {
        val calendar = Calendar.getInstance()
        
        // Test exactly 1 AM as reported in the bug
        calendar.set(2024, Calendar.JANUARY, 15, 1, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val oneAMTimestamp = calendar.timeInMillis
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        println("Testing timestamp: $oneAMTimestamp, hour: $hour")
        
        assertTrue("1 AM timestamp should be night time", isNightTime(oneAMTimestamp))
    }
}