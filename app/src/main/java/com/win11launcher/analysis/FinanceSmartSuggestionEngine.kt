package com.win11launcher.analysis

import com.win11launcher.data.entities.*
import com.win11launcher.data.dao.FinancialPatternDao
import com.win11launcher.data.dao.SmartSuggestionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceSmartSuggestionEngine @Inject constructor(
    private val financialPatternDao: FinancialPatternDao,
    private val smartSuggestionDao: SmartSuggestionDao
) {
    
    suspend fun generateFinancialSuggestions(): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Get existing patterns
        val patterns = financialPatternDao.getRecentPatterns(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)) // Last 30 days
        
        if (patterns.isEmpty()) return suggestions
        
        // Generate different types of suggestions
        suggestions.addAll(generateExpenseCategorizationSuggestions(patterns))
        suggestions.addAll(generateEMITrackingSuggestions(patterns))
        suggestions.addAll(generateBankingAlertsSuggestions(patterns))
        suggestions.addAll(generateRecurringPaymentSuggestions(patterns))
        suggestions.addAll(generateInvestmentTrackingSuggestions(patterns))
        
        return suggestions.sortedByDescending { it.confidenceScore }
    }
    
    suspend fun createInvestmentTrackingRules(): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        val investmentPatterns = financialPatternDao.getPatternsByType(TransactionType.INVESTMENT)
        
        if (investmentPatterns.isNotEmpty()) {
            val apps = investmentPatterns.mapNotNull { it.sourcePackage }.distinct()
            
            apps.forEach { app ->
                val appPatterns = investmentPatterns.filter { it.sourcePackage == app }
                val totalInvested = appPatterns.sumOf { it.amount ?: 0.0 }
                
                suggestions.add(
                    SmartSuggestion(
                        id = UUID.randomUUID().toString(),
                        category = SuggestionCategory.INVESTMENT,
                        subCategory = SuggestionSubCategory.PORTFOLIO_TRACKING,
                        title = "📊 Portfolio Performance Tracker",
                        description = "Track your ${getAppName(app)} notifications in one place",
                        automatedRuleConfig = createInvestmentRuleConfig(app),
                        expectedBenefit = "Monitor ₹${String.format("%.0f", totalInvested)}+ portfolio efficiently",
                        confidenceScore = calculateInvestmentConfidence(appPatterns),
                        priority = SuggestionPriority.HIGH,
                        isFinanceRelated = true,
                        estimatedSavings = 15.0, // 15 minutes daily
                        savingsType = SavingsType.TIME_MINUTES,
                        createdAt = System.currentTimeMillis(),
                        sourcePatterns = JSONArray(appPatterns.map { it.id }).toString(),
                        suggestedFolderName = "Investments",
                        suggestedFolderColor = "#4CAF50",
                        suggestedFolderIcon = "trending_up"
                    )
                )
            }
        }
        
        return suggestions
    }
    
    suspend fun suggestResearchOrganization(): List<SmartSuggestion> {
        // This would analyze research patterns and suggest organization
        // For now, return solar research suggestion as an example
        return listOf(
            SmartSuggestion(
                id = UUID.randomUUID().toString(),
                category = SuggestionCategory.RESEARCH,
                subCategory = SuggestionSubCategory.SOLAR_TECH,
                title = "🔋 Solar Technology Research Hub",
                description = "Capture all solar and renewable energy updates",
                automatedRuleConfig = createSolarResearchRuleConfig(),
                expectedBenefit = "Stay ahead of solar technology trends",
                confidenceScore = 0.85f,
                priority = SuggestionPriority.MEDIUM,
                isFinanceRelated = false,
                estimatedSavings = 20.0,
                savingsType = SavingsType.TIME_MINUTES,
                createdAt = System.currentTimeMillis(),
                suggestedFolderName = "Solar Research",
                suggestedFolderColor = "#FF9800",
                suggestedFolderIcon = "wb_sunny"
            )
        )
    }
    
    private suspend fun generateExpenseCategorizationSuggestions(patterns: List<FinancialPattern>): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Group by category
        val categoryGroups = patterns
            .filter { it.transactionType == TransactionType.DEBIT }
            .groupBy { it.category }
        
        categoryGroups.forEach { (category, categoryPatterns) ->
            if (categoryPatterns.size >= 5) { // Suggest if 5+ transactions in category
                val totalAmount = categoryPatterns.sumOf { it.amount ?: 0.0 }
                
                suggestions.add(
                    SmartSuggestion(
                        id = UUID.randomUUID().toString(),
                        category = SuggestionCategory.FINANCE,
                        subCategory = SuggestionSubCategory.EXPENSE_CATEGORIZATION,
                        title = "💳 Smart ${category.lowercase().replaceFirstChar { it.uppercase() }} Tracking",
                        description = "Auto-organize your ${category.lowercase()} transactions",
                        automatedRuleConfig = createExpenseRuleConfig(category, categoryPatterns),
                        expectedBenefit = "Track ₹${String.format("%.0f", totalAmount)}+ ${category.lowercase()} expenses automatically",
                        confidenceScore = minOf(1.0f, categoryPatterns.size / 20.0f),
                        priority = if (totalAmount > 10000) SuggestionPriority.HIGH else SuggestionPriority.MEDIUM,
                        isFinanceRelated = true,
                        estimatedSavings = 5.0,
                        savingsType = SavingsType.TIME_MINUTES,
                        createdAt = System.currentTimeMillis(),
                        sourcePatterns = JSONArray(categoryPatterns.map { it.id }).toString(),
                        suggestedFolderName = category.lowercase().replaceFirstChar { it.uppercase() },
                        suggestedFolderColor = getCategoryColor(category),
                        suggestedFolderIcon = getCategoryIcon(category)
                    )
                )
            }
        }
        
        return suggestions
    }
    
    private suspend fun generateEMITrackingSuggestions(patterns: List<FinancialPattern>): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        val emiPatterns = patterns.filter { 
            it.transactionType == TransactionType.EMI || 
            it.patternKeywords.contains("emi") ||
            it.patternKeywords.contains("loan") ||
            it.patternKeywords.contains("installment")
        }
        
        if (emiPatterns.size >= 2) {
            val totalEMI = emiPatterns.sumOf { it.amount ?: 0.0 }
            
            suggestions.add(
                SmartSuggestion(
                    id = UUID.randomUUID().toString(),
                    category = SuggestionCategory.FINANCE,
                    subCategory = SuggestionSubCategory.EMI_TRACKING,
                    title = "🏦 EMI & Loan Tracker",
                    description = "Consolidate your loan EMIs and track payments",
                    automatedRuleConfig = createEMIRuleConfig(emiPatterns),
                    expectedBenefit = "Track ₹${String.format("%.0f", totalEMI)} monthly EMIs effortlessly",
                    confidenceScore = minOf(1.0f, emiPatterns.size / 10.0f),
                    priority = SuggestionPriority.HIGH,
                    isFinanceRelated = true,
                    estimatedSavings = 10.0,
                    savingsType = SavingsType.TIME_MINUTES,
                    createdAt = System.currentTimeMillis(),
                    sourcePatterns = JSONArray(emiPatterns.map { it.id }).toString(),
                    suggestedFolderName = "EMI Tracker",
                    suggestedFolderColor = "#F44336",
                    suggestedFolderIcon = "account_balance"
                )
            )
        }
        
        return suggestions
    }
    
    private suspend fun generateBankingAlertsSuggestions(patterns: List<FinancialPattern>): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        val bankGroups = patterns.groupBy { it.bankName }
        
        bankGroups.forEach { (bankName, bankPatterns) ->
            if (bankName != null && bankPatterns.size >= 10) {
                suggestions.add(
                    SmartSuggestion(
                        id = UUID.randomUUID().toString(),
                        category = SuggestionCategory.FINANCE,
                        subCategory = SuggestionSubCategory.BANKING_ALERTS,
                        title = "🏦 $bankName Alerts Hub",
                        description = "Consolidate all your $bankName notifications",
                        automatedRuleConfig = createBankingRuleConfig(bankName, bankPatterns),
                        expectedBenefit = "Never miss important $bankName updates",
                        confidenceScore = minOf(1.0f, bankPatterns.size / 30.0f),
                        priority = SuggestionPriority.MEDIUM,
                        isFinanceRelated = true,
                        estimatedSavings = 5.0,
                        savingsType = SavingsType.TIME_MINUTES,
                        createdAt = System.currentTimeMillis(),
                        sourcePatterns = JSONArray(bankPatterns.map { it.id }).toString(),
                        suggestedFolderName = "$bankName Banking",
                        suggestedFolderColor = "#2196F3",
                        suggestedFolderIcon = "account_balance"
                    )
                )
            }
        }
        
        return suggestions
    }
    
    private suspend fun generateRecurringPaymentSuggestions(patterns: List<FinancialPattern>): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        val recurringPatterns = patterns.filter { it.isRecurring }
        
        if (recurringPatterns.isNotEmpty()) {
            val totalRecurring = recurringPatterns.sumOf { it.amount ?: 0.0 }
            
            suggestions.add(
                SmartSuggestion(
                    id = UUID.randomUUID().toString(),
                    category = SuggestionCategory.FINANCE,
                    subCategory = SuggestionSubCategory.BILL_REMINDERS,
                    title = "📅 Recurring Payments Monitor",
                    description = "Track all your recurring payments and subscriptions",
                    automatedRuleConfig = createRecurringRuleConfig(recurringPatterns),
                    expectedBenefit = "Monitor ₹${String.format("%.0f", totalRecurring)} recurring expenses",
                    confidenceScore = minOf(1.0f, recurringPatterns.size / 15.0f),
                    priority = SuggestionPriority.HIGH,
                    isFinanceRelated = true,
                    estimatedSavings = 8.0,
                    savingsType = SavingsType.TIME_MINUTES,
                    createdAt = System.currentTimeMillis(),
                    sourcePatterns = JSONArray(recurringPatterns.map { it.id }).toString(),
                    suggestedFolderName = "Recurring Payments",
                    suggestedFolderColor = "#9C27B0",
                    suggestedFolderIcon = "repeat"
                )
            )
        }
        
        return suggestions
    }
    
    private suspend fun generateInvestmentTrackingSuggestions(patterns: List<FinancialPattern>): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        val investmentPatterns = patterns.filter { it.category == FinancialCategory.INVESTMENT }
        
        if (investmentPatterns.size >= 3) {
            val totalInvested = investmentPatterns.sumOf { it.amount ?: 0.0 }
            
            suggestions.add(
                SmartSuggestion(
                    id = UUID.randomUUID().toString(),
                    category = SuggestionCategory.INVESTMENT,
                    subCategory = SuggestionSubCategory.PORTFOLIO_TRACKING,
                    title = "📈 Investment Portfolio Hub",
                    description = "Centralize all your investment notifications",
                    automatedRuleConfig = createInvestmentHubRuleConfig(investmentPatterns),
                    expectedBenefit = "Track ₹${String.format("%.0f", totalInvested)}+ investments efficiently",
                    confidenceScore = minOf(1.0f, investmentPatterns.size / 10.0f),
                    priority = SuggestionPriority.HIGH,
                    isFinanceRelated = true,
                    estimatedSavings = 15.0,
                    savingsType = SavingsType.TIME_MINUTES,
                    createdAt = System.currentTimeMillis(),
                    sourcePatterns = JSONArray(investmentPatterns.map { it.id }).toString(),
                    suggestedFolderName = "Investment Portfolio",
                    suggestedFolderColor = "#4CAF50",
                    suggestedFolderIcon = "trending_up"
                )
            )
        }
        
        return suggestions
    }
    
    // Helper methods for rule configuration
    private fun createExpenseRuleConfig(category: String, patterns: List<FinancialPattern>): String {
        val keywords = patterns.flatMap { it.patternKeywords.split(",") }.distinct().take(10)
        val apps = patterns.mapNotNull { it.sourcePackage }.distinct()
        
        return JSONObject().apply {
            put("filterType", "KEYWORD_INCLUDE")
            put("keywords", JSONArray(keywords))
            put("sourceApps", JSONArray(apps))
            put("autoTags", JSONArray(listOf("#${category.lowercase()}", "#expense")))
        }.toString()
    }
    
    private fun createEMIRuleConfig(patterns: List<FinancialPattern>): String {
        return JSONObject().apply {
            put("filterType", "KEYWORD_INCLUDE")
            put("keywords", JSONArray(listOf("EMI", "loan", "installment", "equated monthly")))
            put("autoTags", JSONArray(listOf("#emi", "#loan", "#recurring")))
        }.toString()
    }
    
    private fun createBankingRuleConfig(bankName: String, patterns: List<FinancialPattern>): String {
        val apps = patterns.mapNotNull { it.sourcePackage }.distinct()
        
        return JSONObject().apply {
            put("filterType", "ALL")
            put("sourceApps", JSONArray(apps))
            put("autoTags", JSONArray(listOf("#banking", "#${bankName.lowercase().replace(" ", "")}")))
        }.toString()
    }
    
    private fun createRecurringRuleConfig(patterns: List<FinancialPattern>): String {
        val merchants = patterns.mapNotNull { it.merchant }.distinct()
        
        return JSONObject().apply {
            put("filterType", "KEYWORD_INCLUDE")
            put("keywords", JSONArray(merchants))
            put("autoTags", JSONArray(listOf("#recurring", "#subscription")))
        }.toString()
    }
    
    private fun createInvestmentRuleConfig(app: String): String {
        return JSONObject().apply {
            put("filterType", "ALL")
            put("sourceApps", JSONArray(listOf(app)))
            put("autoTags", JSONArray(listOf("#investment", "#portfolio")))
        }.toString()
    }
    
    private fun createInvestmentHubRuleConfig(patterns: List<FinancialPattern>): String {
        val apps = patterns.mapNotNull { it.sourcePackage }.distinct()
        
        return JSONObject().apply {
            put("filterType", "ALL")
            put("sourceApps", JSONArray(apps))
            put("autoTags", JSONArray(listOf("#investment", "#portfolio", "#sip")))
        }.toString()
    }
    
    private fun createSolarResearchRuleConfig(): String {
        return JSONObject().apply {
            put("filterType", "KEYWORD_INCLUDE")
            put("keywords", JSONArray(listOf("solar panels", "photovoltaic", "solar efficiency", "renewable energy", "clean energy", "battery storage")))
            put("autoTags", JSONArray(listOf("#solar", "#research", "#renewable")))
        }.toString()
    }
    
    // Helper methods
    private fun getAppName(packageName: String): String {
        return when (packageName) {
            "com.zerodha.kite3" -> "Zerodha"
            "com.groww.groww_android" -> "Groww"
            "com.kuvera.kuvera" -> "Kuvera"
            "com.etmoney.android" -> "ET Money"
            "com.paytm.money" -> "Paytm Money"
            else -> "Investment App"
        }
    }
    
    private fun calculateInvestmentConfidence(patterns: List<FinancialPattern>): Float {
        val frequency = minOf(1.0f, patterns.size / 10.0f)
        val consistency = if (patterns.size > 1) {
            val amounts = patterns.mapNotNull { it.amount }
            if (amounts.isNotEmpty()) {
                val avg = amounts.average()
                val variance = amounts.map { (it - avg) * (it - avg) }.average()
                1.0f - minOf(1.0f, (variance / (avg * avg)).toFloat())
            } else 0.5f
        } else 0.5f
        
        return frequency * 0.6f + consistency * 0.4f
    }
    
    private fun getCategoryColor(category: String): String {
        return when (category) {
            FinancialCategory.GROCERIES -> "#8BC34A"
            FinancialCategory.FUEL -> "#FF5722"
            FinancialCategory.ENTERTAINMENT -> "#E91E63"
            FinancialCategory.SHOPPING -> "#9C27B0"
            FinancialCategory.FOOD_DELIVERY -> "#FF9800"
            FinancialCategory.TRANSPORTATION -> "#2196F3"
            FinancialCategory.UTILITIES -> "#607D8B"
            FinancialCategory.HEALTHCARE -> "#F44336"
            FinancialCategory.EDUCATION -> "#3F51B5"
            FinancialCategory.INSURANCE -> "#795548"
            else -> "#757575"
        }
    }
    
    private fun getCategoryIcon(category: String): String {
        return when (category) {
            FinancialCategory.GROCERIES -> "shopping_cart"
            FinancialCategory.FUEL -> "local_gas_station"
            FinancialCategory.ENTERTAINMENT -> "movie"
            FinancialCategory.SHOPPING -> "shopping_bag"
            FinancialCategory.FOOD_DELIVERY -> "restaurant"
            FinancialCategory.TRANSPORTATION -> "directions_car"
            FinancialCategory.UTILITIES -> "electrical_services"
            FinancialCategory.HEALTHCARE -> "local_hospital"
            FinancialCategory.EDUCATION -> "school"
            FinancialCategory.INSURANCE -> "security"
            else -> "folder"
        }
    }
}