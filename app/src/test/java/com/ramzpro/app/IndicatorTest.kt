package com.ramzpro.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class IndicatorTest {
    @Test fun emaHasExpectedWarmupAndOutput() {
        val result = TechnicalIndicators.ema(listOf(1.0, 2.0, 3.0, 4.0), 3)
        assertEquals(null, result[1])
        assertNotNull(result[2])
        assertEquals(3.0, result[2]!!, 0.000001)
    }

    @Test fun rsiReturnsNeutralForFlatMarket() {
        val result = TechnicalIndicators.rsi(List(20) { 10.0 }, 14)
        assertEquals(50.0, result.last()!!, 0.000001)
    }
}
