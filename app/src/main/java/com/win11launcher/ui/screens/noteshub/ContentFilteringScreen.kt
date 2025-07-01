package com.win11launcher.ui.screens.noteshub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.win11launcher.data.models.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentFilteringScreen(
    selectedApps: Set<String>,
    filterType: FilterType,
    onFilterTypeChanged: (FilterType) -> Unit,
    keywords: List<String>,
    onKeywordsChanged: (List<String>) -> Unit,
    excludeKeywords: List<String>,
    onExcludeKeywordsChanged: (List<String>) -> Unit,
    regexPattern: String,
    onRegexPatternChanged: (String) -> Unit,
    caseSensitive: Boolean,
    onCaseSensitiveChanged: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var newKeyword by remember { mutableStateOf("") }
    var newExcludeKeyword by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp) // Extra bottom padding for taskbar
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column {
            Text(
                text = "Content Filtering",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Define what notifications should be converted to notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Selected apps summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tracking ${selectedApps.size} apps",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter type selection
        Text(
            text = "Filter Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTypeOption(
                title = "All Notifications",
                description = "Track every notification from selected apps",
                isSelected = filterType == FilterType.ALL,
                onClick = { onFilterTypeChanged(FilterType.ALL) }
            )
            
            FilterTypeOption(
                title = "Include Keywords",
                description = "Only notifications containing specific words",
                isSelected = filterType == FilterType.KEYWORD_INCLUDE,
                onClick = { onFilterTypeChanged(FilterType.KEYWORD_INCLUDE) }
            )
            
            FilterTypeOption(
                title = "Exclude Keywords",
                description = "All notifications except those with specific words",
                isSelected = filterType == FilterType.KEYWORD_EXCLUDE,
                onClick = { onFilterTypeChanged(FilterType.KEYWORD_EXCLUDE) }
            )
            
            FilterTypeOption(
                title = "Advanced Pattern",
                description = "Use regular expressions for complex matching",
                isSelected = filterType == FilterType.REGEX,
                onClick = { onFilterTypeChanged(FilterType.REGEX) }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter configuration based on type
        when (filterType) {
            FilterType.ALL -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All notifications from selected apps will be tracked",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            FilterType.KEYWORD_INCLUDE -> {
                KeywordSection(
                    title = "Include Keywords",
                    description = "Notifications containing these words will be tracked",
                    keywords = keywords,
                    newKeyword = newKeyword,
                    onNewKeywordChanged = { newKeyword = it },
                    onAddKeyword = {
                        if (newKeyword.isNotBlank() && !keywords.contains(newKeyword.trim())) {
                            onKeywordsChanged(keywords + newKeyword.trim())
                            newKeyword = ""
                        }
                    },
                    onRemoveKeyword = { keyword ->
                        onKeywordsChanged(keywords - keyword)
                    }
                )
            }
            
            FilterType.KEYWORD_EXCLUDE -> {
                KeywordSection(
                    title = "Exclude Keywords",
                    description = "Notifications containing these words will be ignored",
                    keywords = excludeKeywords,
                    newKeyword = newExcludeKeyword,
                    onNewKeywordChanged = { newExcludeKeyword = it },
                    onAddKeyword = {
                        if (newExcludeKeyword.isNotBlank() && !excludeKeywords.contains(newExcludeKeyword.trim())) {
                            onExcludeKeywordsChanged(excludeKeywords + newExcludeKeyword.trim())
                            newExcludeKeyword = ""
                        }
                    },
                    onRemoveKeyword = { keyword ->
                        onExcludeKeywordsChanged(excludeKeywords - keyword)
                    }
                )
            }
            
            FilterType.REGEX -> {
                RegexSection(
                    pattern = regexPattern,
                    onPatternChanged = onRegexPatternChanged
                )
            }
        }
        
        // Case sensitivity option (for keyword filters)
        if (filterType == FilterType.KEYWORD_INCLUDE || filterType == FilterType.KEYWORD_EXCLUDE) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = caseSensitive,
                    onCheckedChange = onCaseSensitiveChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Case sensitive matching",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun FilterTypeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun KeywordSection(
    title: String,
    description: String,
    keywords: List<String>,
    newKeyword: String,
    onNewKeywordChanged: (String) -> Unit,
    onAddKeyword: () -> Unit,
    onRemoveKeyword: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Add keyword input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newKeyword,
                onValueChange = onNewKeywordChanged,
                placeholder = { Text("Enter keyword...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            IconButton(
                onClick = onAddKeyword,
                enabled = newKeyword.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add keyword"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Keywords list
        if (keywords.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(keywords) { keyword ->
                    KeywordChip(
                        keyword = keyword,
                        onRemove = { onRemoveKeyword(keyword) }
                    )
                }
            }
        } else {
            Text(
                text = "No keywords added yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RegexSection(
    pattern: String,
    onPatternChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Regular Expression Pattern",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Use regex patterns for advanced matching (e.g., \"\\d+%\" for percentages)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = pattern,
            onValueChange = onPatternChanged,
            placeholder = { Text("Enter regex pattern...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

@Composable
private fun KeywordChip(
    keyword: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = keyword,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}