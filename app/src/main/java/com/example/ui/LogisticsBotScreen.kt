package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// Message data model for Chat
data class BotMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsBotScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val drivers by viewModel.drivers.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Chat states
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Missing information calculations
    val missingBank = settings.bankName.isBlank() || settings.accountNumber.isBlank()
    val unpaidOrders = orders.filter { it.status == "UNPAID" || it.status == "PARTIALLY_PAID" }
    val unpaidOrdersCount = unpaidOrders.size
    val totalUnpaidAmount = unpaidOrders.sumOf { it.basePrice }

    val initialWelcomeMsg = remember(isArabic, missingBank, unpaidOrdersCount) {
        val greeting = if (isArabic) {
            "مرحباً بك! أنا مساعدك اللوجستي الذكي 🚛. إليك تدقيق الحالة اليومية لأسطولك:\n\n"
        } else {
            "Hello! I am your smart Logistics Assistant 🚛. Here is your daily fleet audit:\n\n"
        }

        val bankStatus = if (missingBank) {
            if (isArabic) "• ⚠️ **بيانات البنك ناقصة**: لم يتم إدخال تفاصيل الحساب المصرفي للشركة في الإعدادات بعد."
            else "• ⚠️ **Bank Details Missing**: Company bank account information has not been set."
        } else {
            if (isArabic) "• ✅ **بيانات البنك**: الحساب المصرفي مسجل بشكل صحيح."
            else "• ✅ **Bank Details**: Bank account is registered correctly."
        }

        val ordersStatus = if (unpaidOrdersCount > 0) {
            if (isArabic) "• ⚠️ **تحصيل الديون**: لديك $unpaidOrdersCount أمر حركة غير مدفوع أو مدفوع جزئياً بإجمالي ${viewModel.formatCurrency(totalUnpaidAmount)}."
            else "• ⚠️ **Outstanding Collections**: You have $unpaidOrdersCount unpaid or partially paid movement orders totaling ${viewModel.formatCurrency(totalUnpaidAmount)}."
        } else {
            if (isArabic) "• ✅ **الذمم والتحصيل**: جميع أوامر الحركة مدفوعة بالكامل."
            else "• ✅ **Collections**: All movement orders are settled."
        }

        val fleetStatus = if (vehicles.isEmpty() || drivers.isEmpty()) {
            if (isArabic) "• ⚠️ **الأسطول**: ينقصك تسجيل مركبات أو سائقين لتهيئة التشغيل الكامل."
            else "• ⚠️ **Fleet**: You need to register vehicles or drivers to fully configure operations."
        } else {
            if (isArabic) "• ✅ **الأسطول**: الأسطول جاهز للعمل مع ${vehicles.size} مركبة و ${drivers.size} سائق."
            else "• ✅ **Fleet**: Fleet is operational with ${vehicles.size} vehicles and ${drivers.size} drivers."
        }

        val callToAction = if (isArabic) {
            "\n\nيمكنك سؤالي عن المسافات بين المدن الليبية وتكلفة شحن البضائع، أو طلب فحص مالي لحظي لشركتك!"
        } else {
            "\n\nYou can ask me about distances between Libyan cities, freight trip pricing guidelines, or ask for a cash flow audit!"
        }

        greeting + bankStatus + "\n" + ordersStatus + "\n" + fleetStatus + callToAction
    }

    val messages = remember {
        mutableStateListOf(
            BotMessage(text = initialWelcomeMsg, isUser = false)
        )
    }

    // Auto-scroll to bottom of chat when new messages arrive
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Function to parse distance
    fun getDistance(origin: String, destination: String): Int? {
        val o = origin.trim().lowercase()
        val d = destination.trim().lowercase()
        
        fun normalize(city: String): String {
            return when {
                city.contains("طرابلس") || city.contains("tripoli") -> "tripoli"
                city.contains("بنغازي") || city.contains("benghazi") -> "benghazi"
                city.contains("مصراتة") || city.contains("misrata") || city.contains("misurata") -> "misrata"
                city.contains("سبها") || city.contains("sabha") || city.contains("sebha") -> "sabha"
                city.contains("سرت") || city.contains("sirte") || city.contains("surt") -> "sirte"
                city.contains("طبرق") || city.contains("tobruk") || city.contains("tobruch") -> "tobruk"
                city.contains("الخمس") || city.contains("khoms") || city.contains("al khoms") -> "khoms"
                city.contains("زليتن") || city.contains("zliten") -> "zliten"
                city.contains("غريان") || city.contains("gharyan") || city.contains("gharian") -> "gharyan"
                city.contains("الزاوية") || city.contains("zawiya") || city.contains("zawia") -> "zawiya"
                else -> city
            }
        }
        
        val nO = normalize(o)
        val nD = normalize(d)
        if (nO == nD) return 0
        
        val pairs = mapOf(
            setOf("tripoli", "benghazi") to 1000,
            setOf("tripoli", "misrata") to 200,
            setOf("tripoli", "sabha") to 750,
            setOf("tripoli", "sirte") to 450,
            setOf("tripoli", "tobruk") to 1450,
            setOf("tripoli", "khoms") to 120,
            setOf("tripoli", "zliten") to 150,
            setOf("tripoli", "gharyan") to 80,
            setOf("tripoli", "zawiya") to 50,
            
            setOf("benghazi", "misrata") to 800,
            setOf("benghazi", "sabha") to 1050,
            setOf("benghazi", "sirte") to 550,
            setOf("benghazi", "tobruk") to 450,
            setOf("benghazi", "khoms") to 880,
            setOf("benghazi", "zliten") to 850,
            
            setOf("misrata", "sabha") to 650,
            setOf("misrata", "sirte") to 250,
            setOf("misrata", "khoms") to 90,
            setOf("misrata", "zliten") to 60,
            
            setOf("sabha", "sirte") to 550,
            setOf("sabha", "gharyan") to 680,
            
            setOf("sirte", "tobruk") to 1000,
            setOf("khoms", "zliten") to 30
        )
        return pairs[setOf(nO, nD)]
    }

    // Smart Local Fallback Response Generator
    fun generateLocalResponse(query: String): String {
        val lower = query.lowercase().trim()
        
        if (lower.contains("فحص") || lower.contains("نواقص") || lower.contains("remind") || lower.contains("audit") || lower.contains("تحقق") || lower.contains("تقرير")) {
            val bankText = if (missingBank) {
                if (isArabic) "• ⚠️ تفاصيل الحساب البنكي غير مدخلة."
                else "• ⚠️ Bank details are not configured."
            } else {
                if (isArabic) "• ✅ تفاصيل البنك مدخلة بشكل صحيح."
                else "• ✅ Bank details are registered correctly."
            }
            
            val ordersText = if (unpaidOrdersCount > 0) {
                if (isArabic) "• ⚠️ لديك $unpaidOrdersCount أوامر حركة معلقة وغير مدفوعة بالكامل بإجمالي ${viewModel.formatCurrency(totalUnpaidAmount)}."
                else "• ⚠️ You have $unpaidOrdersCount unpaid movement orders totaling ${viewModel.formatCurrency(totalUnpaidAmount)}."
            } else {
                if (isArabic) "• ✅ جميع مستحقات أوامر الحركة مسواة بالكامل."
                else "• ✅ All movement orders are paid."
            }

            val fleetText = if (vehicles.isEmpty() || drivers.isEmpty()) {
                if (isArabic) "• ⚠️ بيانات الأسطول غير كافية (سجل الشاحنات والسائقين)."
                else "• ⚠️ Fleet data incomplete (Add vehicles or drivers)."
            } else {
                if (isArabic) "• ✅ الأسطول يعمل بكفاءة (${vehicles.size} مركبة، ${drivers.size} سائق)."
                else "• ✅ Fleet active (${vehicles.size} vehicles, ${drivers.size} drivers)."
            }

            return if (isArabic) {
                "📋 **تقرير التدقيق اللوجستي المالي الفوري:**\n\n$bankText\n$ordersText\n$fleetText\n\nيرجى استكمال البيانات وإصدار الفواتير مع العملاء لضمان استقرار السيولة النقدية."
            } else {
                "📋 **Real-Time Logistics & Financial Audit Report:**\n\n$bankText\n$ordersText\n$fleetText\n\nPlease enter pending details or settle collections to maintain steady cash-flow."
            }
        }
        
        val cities = listOf(
            "tripoli" to "طرابلس",
            "benghazi" to "بنغازي",
            "misrata" to "مصراتة",
            "sabha" to "سبها",
            "sirte" to "سرت",
            "tobruk" to "طبرق",
            "khoms" to "الخمس",
            "zliten" to "زليتن",
            "gharyan" to "غريان",
            "zawiya" to "الزاوية"
        )
        
        val found = mutableListOf<Pair<String, String>>()
        for (city in cities) {
            if (lower.contains(city.first) || lower.contains(city.second)) {
                found.add(city)
            }
        }
        
        if (found.size >= 2) {
            val c1 = found[0]
            val c2 = found[1]
            val dist = getDistance(c1.first, c2.first)
            if (dist != null) {
                val price = 350.0 + dist * 2.5
                return if (isArabic) {
                    "📍 **تقدير المسافة والأسعار:**\n\n• المسافة التقريبية بين **${c1.second}** و **${c2.second}** هي **$dist كم**.\n• السعر المناسب المقترح لشحن حمولة بينهما: **${viewModel.formatCurrency(price)}**\n*(بواقع 350.00 د.ل كرسوم أساسية وثابتة + 2.50 د.ل لكل كم).* "
                } else {
                    "📍 **Distance & Trip Cost Estimate:**\n\n• Approximate road distance between **${c1.first.replaceFirstChar { it.uppercase() }}** and **${c2.first.replaceFirstChar { it.uppercase() }}** is **$dist km**.\n• Proper trip fee guideline: **${viewModel.formatCurrency(price)}**\n*(Based on 350.00 LYD flat base loading fee + 2.50 LYD per km road tariff).* "
                }
            }
        } else if (found.size == 1) {
            val c = found[0]
            return if (isArabic) {
                "لقد ذكرت مدينة **${c.second}**. يرجى تزويدي بوجهة أخرى (مثال: طرابلس ومصراتة) لأقوم باحتساب المسافة وتكلفة الشحن التقريبية!"
            } else {
                "You mentioned **${c.first.replaceFirstChar { it.uppercase() }}**. Please supply a second location (e.g. Tripoli and Misrata) so I can compute the shipping distance and tariff!"
            }
        }
        
        return if (isArabic) {
            "أنا المساعد اللوجستي لمسار. يسعدني الإجابة عن:\n• المسافات وتكلفة الشحن بين المدن الليبية (مثال: طرابلس وبنغازي).\n• تدقيق تفاصيل العمل المعلقة والنواقص المالية.\n\nتفضل بطرح سؤالك!"
        } else {
            "I am Masar's Logistics Assistant. I can help with:\n• Road distances and shipment costs between Libyan cities (e.g. Tripoli to Misrata).\n• Reminders of missing company information or pending audits.\n\nHow can I help you today?"
        }
    }

    // Call Gemini API via Coroutines REST Post Request
    fun handleSendMessage(query: String) {
        if (query.isBlank()) return
        
        messages.add(BotMessage(text = query, isUser = true))
        inputText = ""
        keyboardController?.hide()
        isTyping = true

        coroutineScope.launch {
            val localResponse = generateLocalResponse(query)
            
            // Check if there is an API Key entered in secrets
            val apiKey = BuildConfig.GEMINI_API_KEY
            val isApiKeyConfigured = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "placeholder"
            
            if (isApiKeyConfigured) {
                val responseText = withContext(Dispatchers.IO) {
                    try {
                        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-Type", "application/json")
                        conn.doOutput = true
                        
                        val systemPrompt = """
                            You are "Masar Logistics Bot", an intelligent AI assistant built for a Trip, Fleet & Logistics Management Application in Libya.
                            Help the user with logistics questions such as road distance, proper trip prices, driver status, or general operations.
                            Keep replies extremely short and direct (under 3-4 sentences), professional, and relevant to Libyan transport.
                            Use Libyan Dinars (د.ل) as the primary currency.
                            Answer in Arabic if the user wrote in Arabic, otherwise English.
                            Use this accurate local information if relevant:
                            - Tripoli to Benghazi is ~1000 km, proper price is ~2850 LYD
                            - Tripoli to Misrata is ~200 km, proper price is ~850 LYD
                            - Tripoli to Sabha is ~750 km, proper price is ~2225 LYD
                            - Misrata to Benghazi is ~800 km, proper price is ~2350 LYD
                            The pricing formula is 350 LYD base + 2.50 LYD per kilometer.
                            Here is the current state of the database:
                            - Missing bank details: $missingBank
                            - Unpaid movement orders: $unpaidOrdersCount (Total unpaid amount: $totalUnpaidAmount)
                            - Vehicles registered: ${vehicles.size}
                            - Drivers registered: ${drivers.size}
                            If they ask for distance/pricing and you can calculate, do so. If they ask about reminders or missing info, summarize the above state.
                        """.trimIndent()

                        val jsonBody = JSONObject().apply {
                            put("contents", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("parts", JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("text", query)
                                        })
                                    })
                                })
                            })
                            put("systemInstruction", JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", systemPrompt)
                                    })
                                })
                            })
                        }

                        val writer = OutputStreamWriter(conn.outputStream)
                        writer.write(jsonBody.toString())
                        writer.flush()
                        writer.close()

                        if (conn.responseCode == 200) {
                            val responseString = conn.inputStream.bufferedReader().use { it.readText() }
                            val jsonResponse = JSONObject(responseString)
                            val candidates = jsonResponse.getJSONArray("candidates")
                            val content = candidates.getJSONObject(0).getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            parts.getJSONObject(0).getString("text")
                        } else {
                            // API Error, fallback to smart rule engine
                            localResponse
                        }
                    } catch (e: Exception) {
                        localResponse
                    }
                }
                isTyping = false
                messages.add(BotMessage(text = responseText, isUser = false))
            } else {
                // No API key or placeholder, instantly respond using local rule engine with a 800ms delay to simulate bot response
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(800)
                }
                isTyping = false
                messages.add(BotMessage(text = localResponse, isUser = false))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom App Bar with Menu Action
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isArabic) "مساعد مسار اللوجستي" else "Masar Assistant Bot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "مساعدك اللحظي لتنظيم الحركة" else "Your real-time operations assistant",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onMenuClicked) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Persistent Urgent Reminder Banner if anything is critical
        if (missingBank || unpaidOrdersCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)), // light red background
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFEE2E2))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Reminders",
                        tint = Color(0xFFDC2626) // Deep red
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "تنبيه معلق يتطلب اهتمامك" else "Pending action required",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B)
                        )
                        val text = if (isArabic) {
                            "${if (missingBank) "• لم يتم إدخال تفاصيل البنك للتحصيل. " else ""}${if (unpaidOrdersCount > 0) "• لديك $unpaidOrdersCount أوامر حركة لم تُسدد قيمتها بالكامل." else ""}"
                        } else {
                            "${if (missingBank) "• Bank details are missing. " else ""}${if (unpaidOrdersCount > 0) "• You have $unpaidOrdersCount unpaid orders." else ""}"
                        }
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message, isArabic = isArabic)
            }

            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "مسار يفكر ويكتب..." else "Masar is typing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick suggestion chips
        HorizontalSuggestionChips(
            isArabic = isArabic,
            onChipClick = { prompt -> handleSendMessage(prompt) }
        )

        // Bottom Input Row
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isArabic) "اسأل عن الأسعار والمسافات أو التنبيهات..." else "Ask about pricing, distance or audits...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                handleSendMessage(inputText)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            handleSendMessage(inputText)
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: BotMessage, isArabic: Boolean) {
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val textColor = if (message.isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (message.isUser) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bubbleColor, shape = bubbleShape)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        
        // Time metadata
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = sdf.format(Date(message.timestamp))
        Text(
            text = timeStr,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
        )
    }
}

@Composable
fun HorizontalSuggestionChips(
    isArabic: Boolean,
    onChipClick: (String) -> Unit
) {
    val suggestions = if (isArabic) {
        listOf(
            "المسافة بين طرابلس وبنغازي",
            "سعر رحلة مصراتة إلى طرابلس",
            "فحص النواقص والتنبيهات المعلقة"
        )
    } else {
        listOf(
            "Distance Tripoli to Benghazi",
            "Trip price Misrata to Tripoli",
            "Audit unpaid orders and details"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(40.dp)
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        suggestions.forEach { suggestion ->
            Surface(
                onClick = { onChipClick(suggestion) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
