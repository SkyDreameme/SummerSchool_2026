package com.apex.karting.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apex.karting.ui.screens.auth.RegistrationScreen
import com.apex.karting.ui.screens.booking.BookingScreen
import com.apex.karting.ui.screens.bookings.BookingDetailsScreen
import com.apex.karting.ui.screens.bookings.MyBookingsScreen
import com.apex.karting.ui.screens.profile.ProfileScreen
import com.apex.karting.ui.screens.slots.SlotDetailsScreen
import com.apex.karting.ui.screens.slots.SlotListScreen

object Routes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val SLOT_DETAILS = "slot/{slotId}"
    const val BOOKING = "booking/{slotId}"
    const val BOOKING_DETAILS = "booking_details/{bookingId}"

    fun slotDetails(slotId: String) = "slot/$slotId"
    fun booking(slotId: String) = "booking/$slotId"
    fun bookingDetails(bookingId: String) = "booking_details/$bookingId"
}

@Composable
fun ApexNavHost(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.AUTH) {
            RegistrationScreen(
                onLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainTabs(
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSlotClick = { slotId -> navController.navigate(Routes.slotDetails(slotId)) },
                onBookingClick = { bookingId -> navController.navigate(Routes.bookingDetails(bookingId)) }
            )
        }

        composable(
            route = Routes.SLOT_DETAILS,
            arguments = listOf(navArgument("slotId") { type = NavType.StringType })
        ) { entry ->
            val slotId = entry.arguments?.getString("slotId") ?: return@composable
            SlotDetailsScreen(
                slotId = slotId,
                onBack = { navController.popBackStack() },
                onBook = { navController.navigate(Routes.booking(it)) }
            )
        }

        composable(
            route = Routes.BOOKING,
            arguments = listOf(navArgument("slotId") { type = NavType.StringType })
        ) { entry ->
            val slotId = entry.arguments?.getString("slotId") ?: return@composable
            BookingScreen(
                slotId = slotId,
                onBack = { navController.popBackStack() },
                onGoToSchedule = { navController.popBackStack(Routes.MAIN, false) },
                onGoToBookings = { navController.popBackStack(Routes.MAIN, false) }
            )
        }

        composable(
            route = Routes.BOOKING_DETAILS,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { entry ->
            val bookingId = entry.arguments?.getString("bookingId") ?: return@composable
            BookingDetailsScreen(
                bookingId = bookingId,
                onBack = { navController.popBackStack() },
                onCancelled = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainTabs(
    onLogout: () -> Unit,
    onSlotClick: (String) -> Unit,
    onBookingClick: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Расписание", "Мои заезды", "Профиль")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.CalendarMonth
                                    1 -> Icons.Default.List
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> SlotListScreen(
                onSlotClick = onSlotClick,
                modifier = Modifier.padding(padding)
            )
            1 -> MyBookingsScreen(
                onBookingClick = onBookingClick,
                onGoToSchedule = { selectedTab = 0 },
                modifier = Modifier.padding(padding)
            )
            2 -> ProfileScreen(
                onLogout = onLogout,
                modifier = Modifier.padding(padding)
            )
        }
    }
}