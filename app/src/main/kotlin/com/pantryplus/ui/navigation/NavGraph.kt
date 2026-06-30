package com.pantryplus.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.pantryplus.PantryApp
import com.pantryplus.ui.screens.dashboard.DashboardScreen
import com.pantryplus.ui.screens.dashboard.DashboardViewModel
import com.pantryplus.ui.screens.dashboard.DashboardViewModelFactory
import com.pantryplus.ui.screens.food.AddEditFoodScreen
import com.pantryplus.ui.screens.food.FoodListScreen
import com.pantryplus.ui.screens.food.FoodViewModel
import com.pantryplus.ui.screens.food.FoodViewModelFactory
import com.pantryplus.ui.screens.scanner.ScannerScreen
import com.pantryplus.ui.screens.settings.SettingsScreen
import com.pantryplus.ui.screens.settings.SettingsViewModel
import com.pantryplus.ui.screens.settings.SettingsViewModelFactory
import com.pantryplus.ui.screens.tools.AddEditToolScreen
import com.pantryplus.ui.screens.tools.ToolsListScreen
import com.pantryplus.ui.screens.tools.ToolsViewModel
import com.pantryplus.ui.screens.tools.ToolsViewModelFactory

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FoodList : Screen("food_list")
    object AddFood : Screen("add_food?barcode={barcode}&productId={productId}") {
        fun routeWith(barcode: String? = null, productId: Long? = null): String {
            val b = barcode ?: ""
            val p = productId?.toString() ?: ""
            return "add_food?barcode=$b&productId=$p"
        }
    }
    object EditFood : Screen("edit_food/{instanceId}") {
        fun route(instanceId: Long) = "edit_food/$instanceId"
    }
    object ToolsList : Screen("tools_list")
    object AddTool : Screen("add_tool")
    object EditTool : Screen("edit_tool/{toolId}") {
        fun route(toolId: Long) = "edit_tool/$toolId"
    }
    object Scanner : Screen("scanner")
    object Settings : Screen("settings")
}

private data class BottomTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val screen: Screen)

@Composable
fun PantryNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as PantryApp

    val tabs = listOf(
        BottomTab("Dashboard", Icons.Default.Dashboard, Screen.Dashboard),
        BottomTab("Food", Icons.Outlined.Kitchen, Screen.FoodList),
        BottomTab("Tools", Icons.Default.Construction, Screen.ToolsList)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = tabs.any { it.screen.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(app))
                DashboardScreen(vm = vm, onNavigateToFood = { navController.navigate(Screen.FoodList.route) })
            }
            composable(Screen.FoodList.route) {
                val vm: FoodViewModel = viewModel(factory = FoodViewModelFactory(app))
                FoodListScreen(
                    vm = vm,
                    onAddManual = { navController.navigate(Screen.AddFood.routeWith()) },
                    onScan = { navController.navigate(Screen.Scanner.route) },
                    onEdit = { navController.navigate(Screen.EditFood.route(it)) }
                )
            }
            composable(
                route = Screen.AddFood.route,
                arguments = listOf(
                    navArgument("barcode") { type = NavType.StringType; defaultValue = "" },
                    navArgument("productId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val barcode = backStackEntry.arguments?.getString("barcode")?.takeIf { it.isNotEmpty() }
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
                val vm: FoodViewModel = viewModel(factory = FoodViewModelFactory(app))
                AddEditFoodScreen(
                    vm = vm,
                    instanceId = null,
                    prefillBarcode = barcode,
                    prefillProductId = productId,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditFood.route,
                arguments = listOf(navArgument("instanceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val instanceId = backStackEntry.arguments!!.getLong("instanceId")
                val vm: FoodViewModel = viewModel(factory = FoodViewModelFactory(app))
                AddEditFoodScreen(
                    vm = vm,
                    instanceId = instanceId,
                    prefillBarcode = null,
                    prefillProductId = null,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Screen.ToolsList.route) {
                val vm: ToolsViewModel = viewModel(factory = ToolsViewModelFactory(app))
                ToolsListScreen(
                    vm = vm,
                    onAdd = { navController.navigate(Screen.AddTool.route) },
                    onEdit = { navController.navigate(Screen.EditTool.route(it)) },
                    onSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.AddTool.route) {
                val vm: ToolsViewModel = viewModel(factory = ToolsViewModelFactory(app))
                AddEditToolScreen(vm = vm, toolId = null, onDone = { navController.popBackStack() })
            }
            composable(
                route = Screen.EditTool.route,
                arguments = listOf(navArgument("toolId") { type = NavType.LongType })
            ) { backStackEntry ->
                val toolId = backStackEntry.arguments!!.getLong("toolId")
                val vm: ToolsViewModel = viewModel(factory = ToolsViewModelFactory(app))
                AddEditToolScreen(vm = vm, toolId = toolId, onDone = { navController.popBackStack() })
            }
            composable(Screen.Scanner.route) {
                ScannerScreen(
                    onBarcodeScanned = { barcode ->
                        navController.navigate(Screen.AddFood.routeWith(barcode = barcode)) {
                            popUpTo(Screen.Scanner.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(app))
                SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
