package com.smartkrishi.presentation.faq

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// 🎨 Professional Color Scheme
private val PrimaryGreen = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF388E3C)
private val AccentGreen = Color(0xFF66BB6A)
private val LightBackground = Color(0xFFF5F5F5)
private val CardBackground = Color(0xFFFFFFFF)

// 📝 FAQ Data Model
data class FAQCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val questions: List<FAQItem>
)

data class FAQItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val faqCategories = getFAQCategories()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FAQ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightBackground)
        ) {
            // 🔍 Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search questions...",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = SecondaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = SecondaryGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 📂 Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(faqCategories) { category ->
                    val index = faqCategories.indexOf(category)
                    val isSelected = selectedCategory == index

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = index },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) Color.White else category.color
                                )
                                Text(
                                    category.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = category.color.copy(alpha = 0.1f),
                            labelColor = category.color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) category.color else category.color.copy(alpha = 0.3f),
                            selectedBorderColor = category.color,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        ),
                        modifier = Modifier.height(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ❓ Questions List
            val filteredQuestions = if (searchQuery.isEmpty()) {
                faqCategories[selectedCategory].questions
            } else {
                faqCategories.flatMap { it.questions }.filter {
                    it.question.contains(searchQuery, ignoreCase = true) ||
                            it.answer.contains(searchQuery, ignoreCase = true)
                }
            }

            if (filteredQuestions.isEmpty()) {
                EmptySearchState()
            } else {
                // Show category name if searching
                if (searchQuery.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = faqCategories[selectedCategory].color.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = faqCategories[selectedCategory].color,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Found ${filteredQuestions.size} result${if (filteredQuestions.size > 1) "s" else ""}",
                                fontSize = 14.sp,
                                color = faqCategories[selectedCategory].color,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredQuestions) { faqItem ->
                        FAQCard(
                            question = faqItem.question,
                            answer = faqItem.answer,
                            categoryColor = faqCategories[selectedCategory].color
                        )
                    }
                }
            }
        }
    }
}

// 🗂️ FAQ Card with Expandable Answer
@Composable
private fun FAQCard(
    question: String,
    answer: String,
    categoryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        question,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = categoryColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Answer (Expandable)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            answer,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// 🔍 Empty Search State
@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Gray.copy(alpha = 0.25f)
            )
            Text(
                "No results found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                "Try a different search term",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

// 📚 FAQ Data
private fun getFAQCategories(): List<FAQCategory> {
    return listOf(
        // 🌱 General Category
        FAQCategory(
            title = "General",
            icon = Icons.Outlined.Info,
            color = Color(0xFF1976D2),
            questions = listOf(
                FAQItem(
                    question = "What is Smart Krishi?",
                    answer = "Smart Krishi is an AI-powered agricultural platform that helps farmers manage their farms efficiently using modern technology. It provides real-time monitoring, disease detection, market prices, and expert farming advice."
                ),
                FAQItem(
                    question = "How do I get started with Smart Krishi?",
                    answer = "Simply sign up with your phone number or email, complete your farmer profile, add your farm details, and you're ready to start! The app will guide you through each step."
                ),
                FAQItem(
                    question = "Is Smart Krishi free to use?",
                    answer = "Yes! Smart Krishi offers a free version with essential features. Premium features like advanced analytics and unlimited disease scans are available through our subscription plans."
                ),
                FAQItem(
                    question = "Which languages are supported?",
                    answer = "Smart Krishi currently supports English, Hindi, and several regional languages. You can change the language anytime from the settings."
                ),
                FAQItem(
                    question = "Can I use Smart Krishi offline?",
                    answer = "Some features like viewing saved farm data work offline. However, AI disease detection, market prices, and Krishi Mitri chat require an internet connection."
                )
            )
        ),

        // 🚜 Farm Management Category
        FAQCategory(
            title = "Farm",
            icon = Icons.Outlined.Agriculture,
            color = Color(0xFF388E3C),
            questions = listOf(
                FAQItem(
                    question = "How do I add a new farm?",
                    answer = "Tap the 'Add Farm' button on the home screen, enter your farm details including name, location, crop type, and acreage. You can also add photos and set up sensors if available."
                ),
                FAQItem(
                    question = "Can I manage multiple farms?",
                    answer = "Yes! You can add and manage unlimited farms in your account. Switch between farms easily from the home screen."
                ),
                FAQItem(
                    question = "How do I edit my farm details?",
                    answer = "Long press on any farm card, select 'Edit Farm Details', make your changes, and save. You can update crop type, acreage, location, and other information."
                ),
                FAQItem(
                    question = "What happens if I delete a farm?",
                    answer = "Deleting a farm permanently removes all associated data including sensor history, logs, and analytics. This action cannot be undone, so please be careful!"
                ),
                FAQItem(
                    question = "How do I track different crops on the same farm?",
                    answer = "Currently, each farm supports one primary crop type. If you grow multiple crops, we recommend creating separate farms for each major crop for better tracking and insights."
                )
            )
        ),

        // 🦠 Disease Detection Category
        FAQCategory(
            title = "Disease",
            icon = Icons.Outlined.LocalFlorist,
            color = Color(0xFFE65100),
            questions = listOf(
                FAQItem(
                    question = "How does disease detection work?",
                    answer = "Our AI analyzes photos of your plant leaves using advanced machine learning. Simply take a clear photo of the affected leaf, and within seconds you'll get disease identification and treatment recommendations."
                ),
                FAQItem(
                    question = "Which crops are supported for disease detection?",
                    answer = "We currently support disease detection for major crops including rice, wheat, corn, tomato, potato, cotton, and many more. The list is constantly expanding!"
                ),
                FAQItem(
                    question = "How accurate is the disease detection?",
                    answer = "Our AI model has 90-95% accuracy based on extensive training data. However, for serious issues, we always recommend consulting with local agricultural experts."
                ),
                FAQItem(
                    question = "What should I do after detecting a disease?",
                    answer = "The app provides detailed treatment plans including immediate steps, organic treatments, chemical solutions, and prevention strategies. Follow the recommendations and monitor your crop regularly."
                ),
                FAQItem(
                    question = "Can I save disease detection results?",
                    answer = "Yes! All your disease detection results are automatically saved in your farm history. You can access them anytime from the Logs section."
                )
            )
        ),

        // 🤖 Krishi Mitri Category
        FAQCategory(
            title = "AI Chat",
            icon = Icons.Outlined.Chat,
            color = Color(0xFF7CB342),
            questions = listOf(
                FAQItem(
                    question = "What is Krishi Mitri?",
                    answer = "Krishi Mitri is your personal AI farming assistant powered by ChatGPT. Ask any agriculture-related question and get expert advice on farming practices, crop care, pest control, and more."
                ),
                FAQItem(
                    question = "What kind of questions can I ask?",
                    answer = "You can ask about crop cultivation, soil management, irrigation, fertilizers, pest control, weather impact, market trends, government schemes, and any other farming-related topics."
                ),
                FAQItem(
                    question = "Is Krishi Mitri available 24/7?",
                    answer = "Yes! Krishi Mitri is available round the clock to answer your farming questions. Get instant expert advice whenever you need it."
                ),
                FAQItem(
                    question = "Can Krishi Mitri provide local recommendations?",
                    answer = "Yes! Krishi Mitri considers your location, crop type, and local conditions when providing recommendations. Make sure your profile and farm details are complete for best results."
                ),
                FAQItem(
                    question = "Is my conversation with Krishi Mitri private?",
                    answer = "Absolutely! All conversations are private and encrypted. We never share your personal farming data with third parties."
                )
            )
        ),

        // 💰 Market & Schemes Category
        FAQCategory(
            title = "Market",
            icon = Icons.Outlined.TrendingUp,
            color = Color(0xFFD32F2F),
            questions = listOf(
                FAQItem(
                    question = "How often are market prices updated?",
                    answer = "Market prices are updated in real-time from government and market sources. You can view live prices for major crops, vegetables, and commodities across different mandis."
                ),
                FAQItem(
                    question = "Can I compare prices across different markets?",
                    answer = "Yes! The Market Price feature allows you to compare prices across multiple mandis and markets to help you make better selling decisions."
                ),
                FAQItem(
                    question = "What government schemes are available?",
                    answer = "We provide comprehensive information about all major agricultural schemes including PM-KISAN, crop insurance, subsidies, loans, and state-specific programs. Check the Govt Schemes section regularly for updates."
                ),
                FAQItem(
                    question = "How do I apply for government schemes?",
                    answer = "Each scheme listing includes detailed eligibility criteria, required documents, and step-by-step application instructions. Some schemes also have direct links to official portals."
                ),
                FAQItem(
                    question = "Can I get price alerts for my crops?",
                    answer = "Premium users can set up custom price alerts. You'll receive notifications when prices for your selected crops reach your target threshold."
                )
            )
        ),

        // ⚙️ Technical Support Category
        FAQCategory(
            title = "Support",
            icon = Icons.Outlined.Settings,
            color = Color(0xFF5E35B1),
            questions = listOf(
                FAQItem(
                    question = "Why is the app not working properly?",
                    answer = "First, check your internet connection. Then try closing and reopening the app. If issues persist, clear app cache from your phone settings or reinstall the app."
                ),
                FAQItem(
                    question = "How do I reset my password?",
                    answer = "On the login screen, tap 'Forgot Password', enter your registered email or phone number, and follow the instructions sent to you."
                ),
                FAQItem(
                    question = "My disease detection is taking too long. What should I do?",
                    answer = "Disease detection usually takes 5-10 seconds. If it's taking longer, check your internet connection. Try taking a clearer photo with good lighting and retry."
                ),
                FAQItem(
                    question = "How do I contact customer support?",
                    answer = "You can reach our support team via the in-app chat, email at support@smartkrishi.com, or call our helpline. We typically respond within 24 hours."
                ),
                FAQItem(
                    question = "Is my data secure?",
                    answer = "Yes! We use bank-level encryption to protect your data. Your farm information, photos, and personal details are stored securely and never shared without your permission."
                ),
                FAQItem(
                    question = "How do I delete my account?",
                    answer = "Go to Profile > Settings > Account > Delete Account. Please note this action is permanent and will delete all your data including farm records and history."
                )
            )
        )
    )
}
