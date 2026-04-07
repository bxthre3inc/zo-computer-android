package com.bxthre3.zocomputer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bxthre3.design.atoms.BX3Button
import com.bxthre3.design.atoms.BX3ButtonVariant
import com.bxthre3.design.atoms.BX3Text
import com.bxthre3.design.molecules.BX3InputField
import com.bxthre3.design.organisms.navbar.BX3NavBar
import com.bxthre3.design.organisms.navbar.NavItem
import com.bxthre3.design.theme.BX3Theme
import com.bxthre3.design.theme.ThemeVariant
import com.bxthre3.zocomputer.ota.OTAUpdateManager

class MainActivity : ComponentActivity() {
    
    private lateinit var otaManager: OTAUpdateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        otaManager = OTAUpdateManager(this)
        otaManager.checkForUpdate()
        
        setContent {
            BX3Theme(
                initialTheme = ThemeVariant.AgentOS,
                content = { MainScreen() }
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        otaManager.unregister()
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf("dashboard") }
    var handle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val navItems = listOf(
        NavItem("dashboard", "Dashboard"),
        NavItem("agents", "Agents"),
        NavItem("tasks", "Tasks"),
        NavItem("settings", "Settings")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BX3Text(
            text = "Zo Computer",
            size = 28.sp,
            weight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        BX3Text(
            text = "Access your AI workforce",
            size = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        BX3InputField(
            value = handle,
            onValueChange = { handle = it },
            label = "Handle",
            hint = "yourname",
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        BX3InputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        BX3Button(
            text = "Connect",
            onClick = { },
            fullWidth = true,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        BX3Button(
            text = "Sign Up",
            onClick = { },
            variant = BX3ButtonVariant.SECONDARY,
            fullWidth = true
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        BX3NavBar(
            items = navItems,
            selectedItem = selectedTab,
            onItemSelect = { selectedTab = it }
        )
    }
}