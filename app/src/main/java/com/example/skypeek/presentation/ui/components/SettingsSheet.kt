package com.example.skypeek.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skypeek.presentation.ui.theme.WeatherColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    onAddLocation: () -> Unit,
    onManageLocations: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White.copy(alpha = 0.95f),
        contentColor = Color.Black,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Header
            Text(
                text = "Weather",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Menu items
            SettingsMenuItem(
                icon = Icons.Default.Add,
                title = "Add Location",
                subtitle = "Search for a city or zipcode",
                onClick = {
                    onAddLocation()
                    onDismiss()
                }
            )
            
            SettingsMenuItem(
                icon = Icons.Default.List,
                title = "Manage Locations",
                subtitle = "Reorder or remove saved locations",
                onClick = {
                    onManageLocations()
                    onDismiss()
                }
            )
            
            Divider(
                color = Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            SettingsMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "Temperature units, notifications",
                onClick = {
                    onSettings()
                    onDismiss()
                }
            )
            
            SettingsMenuItem(
                icon = Icons.Default.Info,
                title = "About SkyPeek",
                subtitle = "Version and information",
                onClick = {
                    onAbout()
                    onDismiss()
                }
            )
            
            // Bottom padding for safe area
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF007AFF), // iOS Blue
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Go",
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
} 