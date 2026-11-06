package com.ramzpro.app

import kotlin.math.abs
import kotlin.math.max

object TechnicalIndicators {
    fun ema(values: List<Double>, period: Int): List<Double?> {
        require(period > 0)
        val out = MutableList<Double?>(values.size) { null }
        if (values.size < period) return out
        var current = values.take(period).average()
        out[period - 1] = current
        val alpha = 2.0 / (period + 1)
        for (i in period until values.size) {
            current += alpha * (values[i] - current)
            out[i] = current
        }
        return out
    }

    fun rsi(values: List<Double>, period: Int = 14): List<Double?> {
        require(period > 0)
        val out = MutableList<Double?>(values.size) { null }
        if (values.size <= period) return out
        var gain = 0.0
        var loss = 0.0
        for (i in 1..period) {
            val d = values[i] - values[i - 1]
            if (d > 0) gain += d else loss -= d
        }
        var avgGain = gain / period
        var avgLoss = loss / period
        fun value() = when {
            avgGain == 0.0 && avgLoss == 0.0 -> 50.0
            avgLoss == 0.0 -> 100.0
            else -> 100.0 - 100.0 / (1.0 + avgGain / avgLoss)
        }
        out[period] = value()
        for (i in period + 1 until values.size) {
            val d = values[i] - values[i - 1]
            avgGain = (avgGain * (period - 1) + max(d, 0.0)) / period
            avgLoss = (avgLoss * (period - 1) + max(-d, 0.0)) / period
            out[i] = value()
        }
        return out
    }

    fun atr(candles: List<Candle>, period: Int = 14): List<Double?> {
        require(period > 0)
        val out = MutableList<Double?>(candles.size) { null }
        if (candles.size < period) return out
        val tr = candles.mapIndexed { i, c ->
            if (i == 0) c.high - c.low
            else maxOf(c.high - c.low, abs(c.high - candles[i - 1].close),
                abs(c.low - candles[i - 1].close))
        }
        var current = tr.take(period).average()
        out[period - 1] = current
        for (i in period until candles.size) {
            current = (current * (period - 1) + tr[i]) / period
            out[i] = current
        }
        return out
    }
}
