package com.ramzpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch
import java.text.DecimalFormat

private val Background = Color(0xFF090D16)
private val Panel = Color(0xFF141C2B)
private val Green = Color(0xFF31D6A0)
private val Muted = Color(0xFF91A0B7)

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
    var price by remember { mutableStateOf<Double?>(null) }
    var candleCount by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("لم يتم الاتصال بعد") }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("رَمْز برو", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("منصة التحليل الكمي", color = Muted, fontSize = 13.sp)
            }
            Text("BINANCE SPOT", color = Green, fontWeight = FontWeight.Bold)
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BTC / USDT", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(price?.let { "$" + DecimalFormat("#,##0.00").format(it) } ?: "—",
                    color = Green, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(message, color = Muted, fontSize = 12.sp)
                Button(onClick = {
                    scope.launch {
                        loading = true
                        message = "جارٍ جلب البيانات..."
                        val ticker = api.getPrice()
                        val candles = api.getCandles()
                        ticker.onSuccess { price = it.price }
                        candles.onSuccess { candleCount = it.dropLast(1).size }
                        message = if (ticker.isSuccess && candles.isSuccess)
                            "اتصال ناجح • تم التحقق من ترتيب الشموع" else
                            "تعذر جلب البيانات؛ تحقق من الاتصال وحاول مجدداً"
                        loading = false
                    }
                }, enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Background),
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (loading) "جارٍ التحديث..." else "تحديث بيانات Binance")
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("بيانات التحليل", color = Color.White, fontWeight = FontWeight.Bold)
                Text("عدد الشموع المكتملة: ${if (candleCount > 0) candleCount else "—"}",
                    color = Muted)
                Text("الإطار الزمني: 4 ساعات", color = Muted)
                Text("قراءة فقط • لا توجد أوامر تداول حقيقية", color = Green, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("نسخة تأسيسية 0.1.0", color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
