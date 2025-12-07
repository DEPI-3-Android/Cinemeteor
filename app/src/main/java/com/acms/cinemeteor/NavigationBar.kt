package com.example.compose.snippets.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.acms.cinemeteor.MoviesHomeScreen
import com.acms.cinemeteor.ProfileDesign
import com.acms.cinemeteor.FavoriteActivityDesign
import com.acms.cinemeteor.R
import com.acms.cinemeteor.ui.theme.CinemeteorTheme

enum class Destination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int
) {
    HOME("home", R.string.nav_home, Icons.Rounded.Home, R.string.nav_home),
    FAVOURITE("favourite", R.string.nav_favourites, Icons.Rounded.FavoriteBorder, R.string.nav_favourites),
    Profile("profile", R.string.nav_profile, Icons.Rounded.Person, R.string.nav_profile);

    companion object {
        fun fromRoute(route: String) = entries.firstOrNull { it.route == route } ?: HOME
    }

}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> MoviesHomeScreen()
                    Destination.FAVOURITE -> FavoriteActivityDesign()
                    Destination.Profile -> ProfileDesign()
                }
            }
        }
    }
}


@Preview()
@Composable
fun NavigationBarBottom(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME

    CinemeteorTheme {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    val currentDestination = Destination.fromRoute(currentRoute ?: "")

                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary,
                                indicatorColor = Color(0x1FE21220)
                            ),
                            selected = destination == currentDestination,
                            onClick = {
                                if (destination != currentDestination) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = stringResource(destination.contentDescription)
                                )
                            },
                            label = { Text(stringResource(destination.label)) }
                        )
                    }
                }
            }

        ) { contentPadding ->
            AppNavHost(
                navController,
                startDestination,
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}


/*@Preview()
@Composable
fun NavigationBarBottom(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
    CinemeteorTheme {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    Destination.entries.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary,
                                indicatorColor = Color(0x1FE21220)
                            ),

                                    selected = selectedDestination == index,
                            onClick = {
                                if (selectedDestination != index) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    selectedDestination = index
                                }
                            }
                            ,
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.contentDescription
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }

        ) { contentPadding ->
            AppNavHost(navController, startDestination, modifier = Modifier.padding(contentPadding))
        }
    }
}*/


