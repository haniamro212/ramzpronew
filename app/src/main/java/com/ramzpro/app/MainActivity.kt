package com.ramzpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.DecimalFormat

private val Background = Color(0xFF090D16)
private val Panel = Color(0xFF141C2B)
private val Green = Color(0xFF31D6A0)
private val Muted = Color(0xFF91A0B7)

private val trackedCoins = listOf(
    "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
    "ADAUSDT", "DOGEUSDT", "AVAXUSDT", "LINKUSDT", "LTCUSDT"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    Surface(Modifier.fillMaxSize(), color = Background) { Dashboard() }
                }
            }
        }
    }
}

@Composable
private fun Dashboard() {
    val api = remember { BinanceMarketData() }
    val scope = rememberCoroutineScope()
    var selectedSymbol by remember { mutableStateOf("BTCUSDT") }
    var selectedPrice by remember { mutableStateOf<Double?>(null) }
    var prices by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var candleCount by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("اضغط تحديث لجلب أسعار العملات") }
    var loading by remember { mutableStateOf(false) }

    fun refreshData() {
        scope.launch {
            loading = true
            message = "جارٍ جلب أسعار Binance..."
            val results = trackedCoins.map { symbol ->
                async { symbol to api.getPrice(symbol) }
            }.awaitAll()
            prices = results.mapNotNull { (symbol, result) ->
                result.getOrNull()?.let { symbol to it.price }
            }.toMap()
            selectedPrice = prices[selectedSymbol]
            val candles = api.getCandles(selectedSymbol, "4h", 500)
            candles.onSuccess { candleCount = it.dropLast(1).size }
            message = if (prices.isNotEmpty()) {
                "تم تحديث \${prices.size} من \${trackedCoins.size} عملات • بيانات قراءة فقط"
            } else {
                "تعذر الاتصال بـ Binance؛ تحقق من الإنترنت"
            }
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("رَمْز برو", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("منصة التحليل الكمي", color = Muted, fontSize = 13.sp)
                }
                Text("BINANCE SPOT", color = Green, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selectedSymbol.removeSuffix("USDT") + " / USDT", color = Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(selectedPrice?.let { "$" + DecimalFormat("#,##0.########").format(it) } ?: "—",
                        color = Green, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(message, color = Muted, fontSize = 12.sp)
                    Button(onClick = { refreshData() }, enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Background),
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (loading) "جارٍ التحديث..." else "تحديث بيانات Binance")
                    }
                }
            }
        }
        item {
            Text("العملات المتاحة", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        items(trackedCoins) { symbol ->
            val coinPrice = prices[symbol]
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    selectedSymbol = symbol
                    selectedPrice = prices[symbol]
                    scope.launch {
                        val candles = api.getCandles(symbol, "4h", 500)
                        candles.onSuccess { candleCount = it.dropLast(1).size }
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedSymbol == symbol) Color(0xFF1B2D39) else Panel
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(symbol.removeSuffix("USDT") + "/USDT", color = Color.White,
                        fontWeight = FontWeight.SemiBold)
                    Text(coinPrice?.let { "$" + DecimalFormat("#,##0.########").format(it) } ?: "—",
                        color = if (coinPrice != null) Green else Muted)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("بيانات التحليل", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("عدد الشموع المكتملة: \${if (candleCount > 0) candleCount else "—"}", color = Muted)
                    Text("الإطار الزمني: 4 ساعات", color = Muted)
                    Text("الإشارات والتحليل الفني قيد التطوير", color = Green, fontSize = 12.sp)
                    Text("قراءة فقط • لا توجد أوامر تداول حقيقية", color = Muted, fontSize = 12.sp)
                }
            }
        }
        item {
            Text("نسخة 0.2.0 • قائمة عملات تجريبية", color = Muted,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                fontSize = 12.sp)
        }
    }
}
