package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.viewmodel.AppTab

@Composable
fun SamjhaRxBottomNavBar(
    currentTab: AppTab,
    selectedLanguage: Language,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("main_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val understandLabel = when (selectedLanguage) {
            Language.URDU -> "نسخہ"
            Language.ROMAN_URDU -> "Samjhein"
            Language.ENGLISH -> "Understand"
        }

        val chatLabel = when (selectedLanguage) {
            Language.URDU -> "چیٹ"
            Language.ROMAN_URDU -> "Chat"
            Language.ENGLISH -> "AI Chat"
        }

        val pharmacistLabel = when (selectedLanguage) {
            Language.URDU -> "فارماسسٹ"
            Language.ROMAN_URDU -> "Review"
            Language.ENGLISH -> "Pharmacist"
        }

        val savedLabel = when (selectedLanguage) {
            Language.URDU -> "محفوظ"
            Language.ROMAN_URDU -> "Saved"
            Language.ENGLISH -> "Saved"
        }

        val accountLabel = when (selectedLanguage) {
            Language.URDU -> "لاک"
            Language.ROMAN_URDU -> "Lock"
            Language.ENGLISH -> "Lock & Privacy"
        }

        NavigationBarItem(
            selected = currentTab == AppTab.UNDERSTAND,
            onClick = { onTabSelected(AppTab.UNDERSTAND) },
            icon = { Icon(Icons.Default.Description, contentDescription = "Understand Tab") },
            label = { Text(understandLabel, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryViolet,
                selectedTextColor = PrimaryViolet,
                indicatorColor = PrimaryVioletLight
            ),
            modifier = Modifier.testTag("nav_tab_understand")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.CHAT,
            onClick = { onTabSelected(AppTab.CHAT) },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "AI Chat Tab") },
            label = { Text(chatLabel, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryViolet,
                selectedTextColor = PrimaryViolet,
                indicatorColor = PrimaryVioletLight
            ),
            modifier = Modifier.testTag("nav_tab_chat")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.PHARMACIST,
            onClick = { onTabSelected(AppTab.PHARMACIST) },
            icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = "Pharmacist Tab") },
            label = { Text(pharmacistLabel, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryViolet,
                selectedTextColor = PrimaryViolet,
                indicatorColor = PrimaryVioletLight
            ),
            modifier = Modifier.testTag("nav_tab_pharmacist")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.SAVED,
            onClick = { onTabSelected(AppTab.SAVED) },
            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved Tab") },
            label = { Text(savedLabel, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryViolet,
                selectedTextColor = PrimaryViolet,
                indicatorColor = PrimaryVioletLight
            ),
            modifier = Modifier.testTag("nav_tab_saved")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.ACCOUNT,
            onClick = { onTabSelected(AppTab.ACCOUNT) },
            icon = { Icon(Icons.Default.Security, contentDescription = "Account Lock Tab") },
            label = { Text(accountLabel, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryViolet,
                selectedTextColor = PrimaryViolet,
                indicatorColor = PrimaryVioletLight
            ),
            modifier = Modifier.testTag("nav_tab_account")
        )
    }
}
