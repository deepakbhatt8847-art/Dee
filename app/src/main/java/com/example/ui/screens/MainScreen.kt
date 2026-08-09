package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.AusPostRed
import com.example.ui.theme.DarkSurface
import com.example.ui.viewmodel.ScannerViewModel
import com.example.ui.viewmodel.StreetViewModel

enum class NavigationTab(
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    LIVE_SCAN("Live Scan", Icons.Default.QrCodeScanner, "tab_live_scan"),
    MANAGE_STREETS("Manage Streets", Icons.Default.FormatListNumbered, "tab_manage_streets"),
    BULK_DATA("Bulk Data", Icons.Default.ImportExport, "tab_bulk_data")
}

@Composable
fun MainScreen(
    streetViewModel: StreetViewModel,
    scannerViewModel: ScannerViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = Color.White
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AusPostRed,
                            selectedTextColor = AusPostRed,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = AusPostRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> CameraScannerScreen(
                    scannerViewModel = scannerViewModel,
                    snackbarHostState = snackbarHostState
                )
                1 -> StreetListScreen(
                    streetViewModel = streetViewModel,
                    scannerViewModel = scannerViewModel,
                    snackbarHostState = snackbarHostState
                )
                2 -> BulkImportExportScreen(
                    streetViewModel = streetViewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
