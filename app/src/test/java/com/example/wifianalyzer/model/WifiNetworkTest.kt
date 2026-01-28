package com.example.wifianalyzer.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiNetworkTest {

    @Test
    fun `signalZone returns HOT when level is greater than or equal to -50`() {
        // Edge case: exactly -50
        val networkHot = createNetworkWithLevel(-50)
        assertEquals(SignalZone.HOT, networkHot.signalZone)
        
        // Normal hot case: -40
        val networkHot2 = createNetworkWithLevel(-40)
        assertEquals(SignalZone.HOT, networkHot2.signalZone)
    }

    @Test
    fun `signalZone returns WARM when level is between -50 and -60`() {
        // Edge case: exactly -60
        val networkWarm = createNetworkWithLevel(-60)
        assertEquals(SignalZone.WARM, networkWarm.signalZone)
        
        // Normal warm case: -55
        val networkWarm2 = createNetworkWithLevel(-55)
        assertEquals(SignalZone.WARM, networkWarm2.signalZone)
        
        // Boundary check: -51 is Warm
        val networkWarm3 = createNetworkWithLevel(-51)
        assertEquals(SignalZone.WARM, networkWarm3.signalZone)
    }

    @Test
    fun `signalZone returns FAIR when level is between -60 and -70`() {
        // Edge case: exactly -70
        val networkFair = createNetworkWithLevel(-70)
        assertEquals(SignalZone.FAIR, networkFair.signalZone)
        
        // Normal fair case: -65
        val networkFair2 = createNetworkWithLevel(-65)
        assertEquals(SignalZone.FAIR, networkFair2.signalZone)
    }

    @Test
    fun `signalZone returns COLD when level is less than -70`() {
        // Normal colde case: -71
        val networkCold = createNetworkWithLevel(-71)
        assertEquals(SignalZone.COLD, networkCold.signalZone)
        
        // Very cold
        val networkCold2 = createNetworkWithLevel(-90)
        assertEquals(SignalZone.COLD, networkCold2.signalZone)
    }

    private fun createNetworkWithLevel(level: Int): WifiNetwork {
        return WifiNetwork(
            ssid = "TestNet",
            bssid = "00:00:00:00:00:00",
            level = level,
            frequency = 2412,
            capabilities = "",
            timestamp = System.currentTimeMillis(),
            channelWidth = 20,
            centerFreq0 = 0,
            centerFreq1 = 0
        )
    }
}
