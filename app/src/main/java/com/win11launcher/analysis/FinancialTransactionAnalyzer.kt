package com.win11launcher.analysis

import com.win11launcher.data.entities.FinancialPattern
import com.win11launcher.data.entities.TransactionType
import com.win11launcher.data.entities.FinancialCategory
import com.win11launcher.data.entities.Frequency
import com.win11launcher.data.entities.TimePattern
import com.win11launcher.models.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

class FinancialTransactionAnalyzer {
    
    private val bankingApps = setOf(
        "com.sbi.lotza", // SBI YONO
        "com.csam.icici.bank.imobile", // iMobile Pay
        "com.snapwork.hdfc", // HDFC Bank MobileBanking
        "com.axis.mobile", // Axis Mobile
        "com.pnb.onlite", // PNB ONE
        "net.one97.paytm", // Paytm
        "com.phonepe.app", // PhonePe
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "in.amazon.mShop.android.shopping", // Amazon Pay
        "com.mobikwik_new", // MobiKwik
        "com.freecharge.android", // FreeCharge
        "com.application.airtel.money" // Airtel Money
    )
    
    private val investmentApps = setOf(
        "com.zerodha.kite3", // Zerodha Kite
        "com.groww.groww_android", // Groww
        "com.kuvera.kuvera", // Kuvera
        "com.etmoney.android", // ET Money
        "com.paytm.money", // Paytm Money
        "com.icicidirect.mobileapp", // ICICI Direct
        "com.hdfcsec.hdfcsecmobileapp", // HDFC Securities
        "com.upstox.pro" // Upstox
    )
    
    private val amountPatterns = listOf(
        Pattern.compile("₹\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Rs\\.?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("INR\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Amount:?\\s*₹?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
    )
    
    private val transactionKeywords = mapOf(
        TransactionType.DEBIT to listOf("debited", "paid", "withdrawn", "deducted", "charged", "spent"),
        TransactionType.CREDIT to listOf("credited", "received", "deposited", "refunded", "cashback"),
        TransactionType.EMI to listOf("emi", "installment", "loan", "equated monthly", "monthly payment"),
        TransactionType.INVESTMENT to listOf("mutual fund", "sip", "investment", "portfolio", "nav", "dividend"),
        TransactionType.TRANSFER to listOf("transferred", "sent", "upi", "imps", "neft", "rtgs"),
        TransactionType.BILL_PAYMENT to listOf("bill", "utility", "electricity", "gas", "water", "mobile", "broadband"),
        TransactionType.REFUND to listOf("refund", "reversed", "cancelled", "returned")
    )
    
    private val categoryKeywords = mapOf(
        FinancialCategory.GROCERIES to listOf("bigbasket", "grofers", "blinkit", "dunzo", "grocery", "vegetables", "fruits"),
        FinancialCategory.FOOD_DELIVERY to listOf("swiggy", "zomato", "uber eats", "food", "restaurant", "pizza", "burger"),
        FinancialCategory.FUEL to listOf("petrol", "diesel", "fuel", "iocl", "bpcl", "hpcl", "gas station"),
        FinancialCategory.SHOPPING to listOf("amazon", "flipkart", "myntra", "ajio", "shopping", "purchase"),
        FinancialCategory.ENTERTAINMENT to listOf("netflix", "hotstar", "prime", "spotify", "movie", "cinema", "entertainment"),
        FinancialCategory.TRANSPORTATION to listOf("uber", "ola", "taxi", "metro", "bus", "train", "transport"),
        FinancialCategory.UTILITIES to listOf("electricity", "water", "gas", "internet", "broadband", "mobile", "utility"),
        FinancialCategory.HEALTHCARE to listOf("medical", "hospital", "pharmacy", "doctor", "health", "medicine"),
        FinancialCategory.EDUCATION to listOf("school", "college", "university", "course", "education", "tuition"),
        FinancialCategory.INSURANCE to listOf("insurance", "premium", "policy", "lic", "health insurance")
    )
    
    private val bankNamePatterns = mapOf(
        "State Bank" to listOf("sbi", "state bank"),
        "HDFC Bank" to listOf("hdfc"),
        "ICICI Bank" to listOf("icici"),
        "Axis Bank" to listOf("axis"),
        "Punjab National Bank" to listOf("pnb", "punjab national"),
        "Bank of Baroda" to listOf("bob", "bank of baroda"),
        "Canara Bank" to listOf("canara"),
        "Union Bank" to listOf("union bank"),
        "Indian Bank" to listOf("indian bank"),
        "Central Bank" to listOf("central bank")
    )
    
    fun analyzeNotification(notification: AppNotification): FinancialPattern? {
        val content = "${notification.title} ${notification.content}".lowercase()
        
        // Check if this is a financial notification
        if (!isFinancialNotification(notification)) {
            return null
        }
        
        val transactionType = detectTransactionType(content)
        val amount = extractAmount(content)
        val merchant = extractMerchant(content, notification)
        val category = detectCategory(content, merchant, notification.packageName)
        val bankName = detectBankName(content, notification.packageName)
        val timePattern = detectTimePattern(notification.timestamp)
        val keywords = extractKeywords(content)
        
        return FinancialPattern(
            id = UUID.randomUUID().toString(),
            transactionType = transactionType,
            amount = amount,
            merchant = merchant,
            category = category,
            bankName = bankName,
            frequency = Frequency.IRREGULAR, // Will be updated by pattern matching
            timePattern = timePattern,
            isRecurring = false, // Will be updated by pattern matching
            lastSeen = notification.timestamp,
            confidence = calculateConfidence(content, transactionType, amount, category),
            sourcePackage = notification.packageName,
            patternKeywords = keywords,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            occurrenceCount = 1
        )
    }
    
    fun analyzeTransactionPatterns(notifications: List<AppNotification>): Flow<List<FinancialPattern>> = flow {
        val patterns = mutableListOf<FinancialPattern>()
        
        notifications.forEach { notification ->
            analyzeNotification(notification)?.let { pattern ->
                patterns.add(pattern)
            }
        }
        
        emit(patterns)
    }
    
    fun detectRecurringPayments(patterns: List<FinancialPattern>): List<RecurringPaymentSuggestion> {
        val suggestions = mutableListOf<RecurringPaymentSuggestion>()
        
        // Group patterns by merchant and amount
        val groupedPatterns = patterns
            .filter { it.amount != null && it.merchant != null }
            .groupBy { "${it.merchant}_${it.amount}" }
        
        groupedPatterns.forEach { (key, patternGroup) ->
            if (patternGroup.size >= 2) {
                val timeIntervals = patternGroup
                    .sortedBy { it.lastSeen }
                    .zipWithNext { a, b -> b.lastSeen - a.lastSeen }
                
                val avgInterval = timeIntervals.average()
                val isRegular = timeIntervals.all { abs(it - avgInterval) < avgInterval * 0.1 }
                
                if (isRegular) {
                    val frequency = when {
                        avgInterval < 7 * 24 * 60 * 60 * 1000 -> Frequency.WEEKLY
                        avgInterval < 35 * 24 * 60 * 60 * 1000 -> Frequency.MONTHLY
                        else -> Frequency.YEARLY
                    }
                    
                    suggestions.add(
                        RecurringPaymentSuggestion(
                            merchant = patternGroup.first().merchant!!,
                            amount = patternGroup.first().amount!!,
                            frequency = frequency,
                            confidence = calculateRecurringConfidence(patternGroup, timeIntervals),
                            category = patternGroup.first().category,
                            lastSeen = patternGroup.maxOf { it.lastSeen }
                        )
                    )
                }
            }
        }
        
        return suggestions.sortedByDescending { it.confidence }
    }
    
    fun categorizeExpenses(patterns: List<FinancialPattern>): List<ExpenseCategorySuggestion> {
        val categoryGroups = patterns
            .filter { it.transactionType == TransactionType.DEBIT && it.amount != null }
            .groupBy { it.category }
        
        return categoryGroups.map { (category, categoryPatterns) ->
            val totalAmount = categoryPatterns.sumOf { it.amount ?: 0.0 }
            val avgAmount = totalAmount / categoryPatterns.size
            val frequency = categoryPatterns.size
            
            ExpenseCategorySuggestion(
                category = category,
                totalAmount = totalAmount,
                averageAmount = avgAmount,
                transactionCount = frequency,
                topMerchants = categoryPatterns
                    .mapNotNull { it.merchant }
                    .groupingBy { it }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                    .map { it.first },
                confidence = minOf(1.0f, frequency / 10.0f)
            )
        }.sortedByDescending { it.totalAmount }
    }
    
    fun identifyInvestmentOpportunities(patterns: List<FinancialPattern>): List<InvestmentTrackingSuggestion> {
        val investmentPatterns = patterns.filter { 
            it.transactionType == TransactionType.INVESTMENT || 
            investmentApps.contains(it.sourcePackage)
        }
        
        val suggestions = mutableListOf<InvestmentTrackingSuggestion>()
        
        // Group by investment app
        investmentPatterns.groupBy { it.sourcePackage }.forEach { (packageName, appPatterns) ->
            val totalInvested = appPatterns.sumOf { it.amount ?: 0.0 }
            val frequency = appPatterns.size
            
            suggestions.add(
                InvestmentTrackingSuggestion(
                    appPackage = packageName ?: "",
                    appName = "Investment App",
                    totalInvested = totalInvested,
                    transactionCount = frequency,
                    averageAmount = if (frequency > 0) totalInvested / frequency else 0.0,
                    lastActivity = appPatterns.maxOf { it.lastSeen },
                    confidence = minOf(1.0f, frequency / 5.0f)
                )
            )
        }
        
        return suggestions.sortedByDescending { it.totalInvested }
    }
    
    private fun isFinancialNotification(notification: AppNotification): Boolean {
        val content = "${notification.title} ${notification.content}".lowercase()
        
        // Check if from banking/payment app
        if (bankingApps.contains(notification.packageName) || 
            investmentApps.contains(notification.packageName)) {
            return true
        }
        
        // Check for financial keywords
        val financialKeywords = listOf(
            "amount", "₹", "rs.", "inr", "paid", "received", "balance", 
            "transaction", "payment", "transfer", "credited", "debited"
        )
        
        return financialKeywords.any { content.contains(it) }
    }
    
    private fun detectTransactionType(content: String): String {
        transactionKeywords.forEach { (type, keywords) ->
            if (keywords.any { content.contains(it) }) {
                return type
            }
        }
        return TransactionType.DEBIT // Default
    }
    
    private fun extractAmount(content: String): Double? {
        amountPatterns.forEach { pattern ->
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                return try {
                    matcher.group(1).replace(",", "").toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
        return null
    }
    
    private fun extractMerchant(content: String, notification: AppNotification): String? {
        // Try to extract merchant from common patterns
        val merchantPatterns = listOf(
            Pattern.compile("at\\s+([A-Za-z0-9\\s]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("to\\s+([A-Za-z0-9\\s]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("from\\s+([A-Za-z0-9\\s]+)", Pattern.CASE_INSENSITIVE)
        )
        
        merchantPatterns.forEach { pattern ->
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                val merchant = matcher.group(1).trim()
                if (merchant.length > 2 && merchant.length < 50) {
                    return merchant
                }
            }
        }
        
        return null
    }
    
    private fun detectCategory(content: String, merchant: String?, appName: String): String {
        // Check merchant-based categorization first
        merchant?.let { merchantName ->
            categoryKeywords.forEach { (category, keywords) ->
                if (keywords.any { merchantName.lowercase().contains(it) }) {
                    return category
                }
            }
        }
        
        // Check content-based categorization
        categoryKeywords.forEach { (category, keywords) ->
            if (keywords.any { content.contains(it) }) {
                return category
            }
        }
        
        // Check app-based categorization
        when {
            investmentApps.any { content.contains(it) } -> return FinancialCategory.INVESTMENT
            content.contains("fuel") || content.contains("petrol") -> return FinancialCategory.FUEL
            else -> return FinancialCategory.UNKNOWN
        }
    }
    
    private fun detectBankName(content: String, appName: String): String? {
        bankNamePatterns.forEach { (bankName, patterns) ->
            if (patterns.any { content.contains(it) || appName.lowercase().contains(it) }) {
                return bankName
            }
        }
        return null
    }
    
    private fun detectTimePattern(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 6..11 -> TimePattern.MORNING
            in 12..17 -> TimePattern.AFTERNOON
            in 18..21 -> TimePattern.EVENING
            else -> TimePattern.NIGHT
        }
    }
    
    private fun extractKeywords(content: String): String {
        val words = content.split("\\s+".toRegex())
            .filter { it.length > 3 }
            .map { it.lowercase().replace("[^a-z0-9]".toRegex(), "") }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(10)
        
        return words.joinToString(",")
    }
    
    private fun calculateConfidence(
        content: String, 
        transactionType: String, 
        amount: Double?, 
        category: String
    ): Float {
        var confidence = 0.5f
        
        // Boost confidence for amount detection
        if (amount != null) confidence += 0.3f
        
        // Boost confidence for specific transaction type detection
        if (transactionType != TransactionType.DEBIT) confidence += 0.1f
        
        // Boost confidence for category detection
        if (category != FinancialCategory.UNKNOWN) confidence += 0.1f
        
        return minOf(1.0f, confidence)
    }
    
    private fun calculateRecurringConfidence(
        patterns: List<FinancialPattern>,
        intervals: List<Long>
    ): Float {
        val consistency = 1.0f - (intervals.map { abs(it - intervals.average()) }.average() / intervals.average()).toFloat()
        val frequency = minOf(1.0f, patterns.size / 12.0f) // Max confidence at 12 occurrences
        
        return minOf(1.0f, consistency * 0.7f + frequency * 0.3f)
    }
}

// Suggestion data classes
data class RecurringPaymentSuggestion(
    val merchant: String,
    val amount: Double,
    val frequency: String,
    val confidence: Float,
    val category: String,
    val lastSeen: Long
)

data class ExpenseCategorySuggestion(
    val category: String,
    val totalAmount: Double,
    val averageAmount: Double,
    val transactionCount: Int,
    val topMerchants: List<String>,
    val confidence: Float
)

data class InvestmentTrackingSuggestion(
    val appPackage: String,
    val appName: String,
    val totalInvested: Double,
    val transactionCount: Int,
    val averageAmount: Double,
    val lastActivity: Long,
    val confidence: Float
)