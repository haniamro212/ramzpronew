package com.ramzpro.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TickerPrice(val symbol: String, val price: Double)

/** Public, read-only Binance Spot API. No API key and no order execution. */
class BinanceMarketData {
    private val client = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getPrice(symbol: String = "BTCUSDT"): Result<TickerPrice> =
        withContext(Dispatchers.IO) {
            runCatching {
                val clean = symbol.uppercase().filter(Char::isLetterOrDigit)
                require(clean.isNotBlank()) { "Invalid symbol" }
                val request = Request.Builder()
                    .url("https://api.binance.com/api/v3/ticker/price?symbol=$clean")
                    .build()
                client.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "Binance HTTP ${response.code}" }
                    val json = JSONObject(response.body?.string().orEmpty())
                    TickerPrice(json.getString("symbol"), json.getString("price").toDouble())
                }
            }
        }

    suspend fun getCandles(
        symbol: String = "BTCUSDT",
        interval: String = "4h",
        limit: Int = 500
    ): Result<List<Candle>> = withContext(Dispatchers.IO) {
        runCatching {
            require(interval in setOf("5m", "15m", "30m", "1h", "4h", "1d", "1w"))
            require(limit in 1..1000)
            val clean = symbol.uppercase().filter(Char::isLetterOrDigit)
            require(clean.isNotBlank())
            val request = Request.Builder()
                .url("https://api.binance.com/api/v3/klines?symbol=$clean&interval=$interval&limit=$limit")
                .build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Binance HTTP ${response.code}" }
                val rows = JSONArray(response.body?.string().orEmpty())
                val candles = (0 until rows.length()).map { i ->
                    val row = rows.getJSONArray(i)
                    Candle(row.getLong(0), row.getString(1).toDouble(),
                        row.getString(2).toDouble(), row.getString(3).toDouble(),
                        row.getString(4).toDouble(), row.getString(5).toDouble(),
                        row.getLong(6))
                }
                require(candles.zipWithNext().all { (a, b) -> a.openTime < b.openTime }) {
                    "Candles are unordered or duplicated"
                }
                candles
            }
        }
    }
}
