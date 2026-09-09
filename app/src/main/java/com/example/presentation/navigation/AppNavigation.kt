package com.example.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.service.CrewDispatchService
import com.example.di.ServiceLocator
import com.example.domain.model.UserRole
import com.example.presentation.admin.AdminOverviewScreen
import com.example.presentation.auth.AuthScreen
import com.example.presentation.client.booking.BookShootWizardScreen
import com.example.presentation.client.booking.ConfirmationScreen
import com.example.presentation.client.booking.SearchingCrewScreen
import com.example.presentation.client.bookings.ClientBookingsScreen
import com.example.presentation.client.home.ClientHomeScreen
import com.example.presentation.common.BookingChatScreen
import com.example.presentation.common.BookingDetailScreen
import com.example.presentation.common.MessagesOverviewScreen
import com.example.presentation.common.NotificationDialog
import com.example.presentation.components.FameBookTopBar
import com.example.presentation.crew.home.CrewHomeScreen
import com.example.presentation.crew.requests.CrewRequestsScreen
import com.example.presentation.crew.request.CrewIncomingRequestViewModel
import com.example.presentation.crew.request.IncomingShootRequestOverlay
import com.example.presentation.profile.ProfileScreen
import com.example.presentation.splash.OnboardingScreen
import com.example.presentation.splash.SplashScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ObsidianBlack
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
  object Splash : Screen("splash")
  object Onboarding : Screen("onboarding")
  object Auth : Screen("auth")
  object MainHome : Screen("main_home")
  object CrewRequests : Screen("crew_requests")
  object MyBookings : Screen("my_bookings")
  object Messages : Screen("messages")
  object Profile : Screen("profile")
  object BookWizard : Screen("book_wizard")
  object SearchingCrew : Screen("searching_crew/{bookingId}") {
    fun createRoute(bookingId: String) = "searching_crew/$bookingId"
  }
  object Confirmation : Screen("confirmation/{bookingId}") {
    fun createRoute(bookingId: String) = "confirmation/$bookingId"
  }
  object BookingDetail : Screen("booking_detail/{bookingId}") {
    fun createRoute(bookingId: String) = "booking_detail/$bookingId"
  }
  object BookingChat : Screen("booking_chat/{bookingId}") {
    fun createRoute(bookingId: String) = "booking_chat/$bookingId"
  }
}

@Composable
fun AppNavigation(
  navController: NavHostController = rememberNavController(),
  initialBookingId: String? = null,
  onBookingIdHandled: () -> Unit = {}
) {
  val userRepository = ServiceLocator.userRepository
  val bookingRepository = ServiceLocator.bookingRepository
  val crewRepository = ServiceLocator.crewRepository
  val messageRepository = ServiceLocator.messageRepository
  val notificationRepository = ServiceLocator.notificationRepository

  val currentUser by userRepository.currentUser.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  var showNotificationDialog by remember { mutableStateOf(false) }
  var isRestoringSession by remember { mutableStateOf(true) }
  var splashDone by remember { mutableStateOf(false) }

  // Restore saved Supabase session once so reopening the app skips sign-in.
  LaunchedEffect(Unit) {
    try {
      userRepository.restoreSession()
    } catch (_: Exception) {
      // Stay signed out; user can sign in manually.
    } finally {
      isRestoringSession = false
    }
  }

  // Deep link handling from background notification tap
  LaunchedEffect(initialBookingId) {
    if (!initialBookingId.isNullOrBlank()) {
      navController.navigate(Screen.BookingDetail.createRoute(initialBookingId))
      onBookingIdHandled()
    }
  }

  // Manage persistent Crew Dispatch Service in background when crew member is Available
  LaunchedEffect(currentUser?.id, currentUser?.role) {
    val user = currentUser
    if (user?.role == UserRole.CREW) {
      crewRepository.getCrewProfileByUserId(user.id).collectLatest { profile ->
        if (profile?.isAvailable == true) {
          CrewDispatchService.startService(context, user.id, user.name)
        } else {
          CrewDispatchService.stopService(context)
        }
      }
    } else {
      CrewDispatchService.stopService(context)
    }
  }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val isBottomBarVisible = currentRoute in listOf(
    Screen.MainHome.route,
    Screen.CrewRequests.route,
    Screen.MyBookings.route,
    Screen.Messages.route,
    Screen.Profile.route
  ) && currentUser != null

  Scaffold(
    containerColor = ObsidianBlack,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (isBottomBarVisible && currentUser != null) {
        FameBookTopBar(
          currentUser = currentUser,
          onNotificationClick = { showNotificationDialog = true }
        )
      }
    },
    bottomBar = {
      if (isBottomBarVisible && currentUser != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xF2131622),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x33FFFFFF)),
            shadowElevation = 16.dp,
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("floating_liquid_glass_nav_bar")
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      Color(0x22FFFFFF),
                      Color(0x00000000)
                    )
                  )
                )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Tab 1: Home
                FloatingNavItem(
                  selected = currentRoute == Screen.MainHome.route,
                  icon = Icons.Default.Home,
                  label = "Home",
                  testTag = "nav_tab_home",
                  onClick = {
                    if (currentRoute != Screen.MainHome.route) {
                      navController.navigate(Screen.MainHome.route) {
                        popUpTo(Screen.MainHome.route) { inclusive = true }
                      }
                    }
                  }
                )

                if (currentUser?.role == UserRole.CREW) {
                  // Crew Tab 2: Requests
                  FloatingNavItem(
                    selected = currentRoute == Screen.CrewRequests.route,
                    icon = Icons.Default.Videocam,
                    label = "Requests",
                    testTag = "nav_tab_requests",
                    onClick = {
                      if (currentRoute != Screen.CrewRequests.route) {
                        navController.navigate(Screen.CrewRequests.route)
                      }
                    }
                  )

                  // Crew Tab 3: Schedule
                  FloatingNavItem(
                    selected = currentRoute == Screen.MyBookings.route,
                    icon = Icons.Default.CalendarToday,
                    label = "Schedule",
                    testTag = "nav_tab_schedule",
                    onClick = {
                      if (currentRoute != Screen.MyBookings.route) {
                        navController.navigate(Screen.MyBookings.route)
                      }
                    }
                  )
                } else {
                  // Client Tab 2: Bookings
                  FloatingNavItem(
                    selected = currentRoute == Screen.MyBookings.route,
                    icon = Icons.Default.CalendarToday,
                    label = "Bookings",
                    testTag = "nav_tab_bookings",
                    onClick = {
                      if (currentRoute != Screen.MyBookings.route) {
                        navController.navigate(Screen.MyBookings.route)
                      }
                    }
                  )
                }

                // Tab: Messages
                FloatingNavItem(
                  selected = currentRoute == Screen.Messages.route,
                  icon = Icons.Default.Chat,
                  label = "Messages",
                  testTag = "nav_tab_messages",
                  onClick = {
                    if (currentRoute != Screen.Messages.route) {
                      navController.navigate(Screen.Messages.route)
                    }
                  }
                )

                // Tab: Profile
                FloatingNavItem(
                  selected = currentRoute == Screen.Profile.route,
                  icon = Icons.Default.Person,
                  label = "Profile",
                  testTag = "nav_tab_profile",
                  onClick = {
                    if (currentRoute != Screen.Profile.route) {
                      navController.navigate(Screen.Profile.route)
                    }
                  }
                )
              }
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
      ) {
        composable(Screen.Splash.route) {
          SplashScreen(onFinish = { splashDone = true })

          // Wait for both the splash animation and session restore before routing.
          // Otherwise a slow network restore loses the race and forces sign-in again.
          LaunchedEffect(splashDone, isRestoringSession, currentUser) {
            if (splashDone && !isRestoringSession) {
              if (currentUser != null) {
                navController.navigate(Screen.MainHome.route) {
                  popUpTo(Screen.Splash.route) { inclusive = true }
                }
              } else {
                navController.navigate(Screen.Auth.route) {
                  popUpTo(Screen.Splash.route) { inclusive = true }
                }
              }
            }
          }
        }

        composable(Screen.Onboarding.route) {
          OnboardingScreen(
            onComplete = {
              navController.navigate(Screen.Auth.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
              }
            }
          )
        }

        composable(Screen.Auth.route) {
          AuthScreen(
            userRepository = userRepository,
            onLoginSuccess = { user ->
              navController.navigate(Screen.MainHome.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
              }
            }
          )
        }

        // Main Tab 1: Home (Client / Crew / Admin)
        composable(Screen.MainHome.route) {
          val user = currentUser
          if (user == null) {
            LaunchedEffect(Unit) {
              navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
              }
            }
          } else {
            when (user.role) {
              UserRole.CLIENT -> {
                ClientHomeScreen(
                  currentUser = user,
                  bookingRepository = bookingRepository,
                  crewRepository = crewRepository,
                  onBookShootClick = { navController.navigate(Screen.BookWizard.route) },
                  onActiveRequestClick = { id -> navController.navigate(Screen.SearchingCrew.createRoute(id)) },
                  onBookingClick = { id -> navController.navigate(Screen.BookingDetail.createRoute(id)) },
                  onOpenChat = { id -> navController.navigate(Screen.BookingChat.createRoute(id)) }
                )
              }
              UserRole.CREW -> {
                CrewHomeScreen(
                  currentUser = user,
                  bookingRepository = bookingRepository,
                  crewRepository = crewRepository,
                  snackbarHostState = snackbarHostState,
                  onBookingClick = { id -> navController.navigate(Screen.BookingDetail.createRoute(id)) },
                  onOpenChat = { id -> navController.navigate(Screen.BookingChat.createRoute(id)) }
                )
              }
              UserRole.ADMIN -> {
                AdminOverviewScreen(
                  currentUser = user,
                  bookingRepository = bookingRepository,
                  crewRepository = crewRepository,
                  userRepository = userRepository,
                  onBookingClick = { id -> navController.navigate(Screen.BookingDetail.createRoute(id)) }
                )
              }
            }
          }
        }

        // Main Tab 2: Bookings (or Schedule for Crew)
        composable(Screen.MyBookings.route) {
          val user = currentUser
          if (user != null) {
            ClientBookingsScreen(
              currentUser = user,
              bookingRepository = bookingRepository,
              onBookingClick = { id -> navController.navigate(Screen.BookingDetail.createRoute(id)) },
              onOpenChat = { id -> navController.navigate(Screen.BookingChat.createRoute(id)) },
              onBookShootClick = { navController.navigate(Screen.BookWizard.route) }
            )
          }
        }

        // Crew Tab: Requests
        composable(Screen.CrewRequests.route) {
          val user = currentUser
          if (user != null) {
            CrewRequestsScreen(
              currentUser = user,
              bookingRepository = bookingRepository,
              crewRepository = crewRepository,
              snackbarHostState = snackbarHostState,
              onBookingClick = { id -> navController.navigate(Screen.BookingDetail.createRoute(id)) }
            )
          }
        }

        // Main Tab 3: Messages
        composable(Screen.Messages.route) {
          val user = currentUser
          if (user != null) {
            MessagesOverviewScreen(
              currentUser = user,
              bookingRepository = bookingRepository,
              onOpenChat = { id -> navController.navigate(Screen.BookingChat.createRoute(id)) }
            )
          }
        }

        // Main Tab 4: Profile
        composable(Screen.Profile.route) {
          val user = currentUser
          if (user != null) {
            ProfileScreen(
              currentUser = user,
              userRepository = userRepository,
              crewRepository = crewRepository,
              onLogout = {
                navController.navigate(Screen.Auth.route) {
                  popUpTo(0) { inclusive = true }
                }
              }
            )
          }
        }

        // BOOKING WIZARD
        composable(Screen.BookWizard.route) {
          val user = currentUser
          if (user != null) {
            BookShootWizardScreen(
              currentUser = user,
              onBackClick = { navController.popBackStack() },
              onRequestCrewSubmit = { newBooking ->
                scope.launch {
                  val result = bookingRepository.createBooking(newBooking)
                  if (result.isSuccess) {
                    val created = result.getOrThrow()
                    navController.navigate(Screen.SearchingCrew.createRoute(created.id)) {
                      popUpTo(Screen.BookWizard.route) { inclusive = true }
                    }
                  } else {
                    snackbarHostState.showSnackbar("Failed to submit request: ${result.exceptionOrNull()?.message}")
                  }
                }
              }
            )
          }
        }

        // SEARCHING RADAR SCREEN
        composable(
          route = Screen.SearchingCrew.route,
          arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
          val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
          val user = currentUser
          if (user != null) {
            SearchingCrewScreen(
              bookingId = bookingId,
              currentUser = user,
              bookingRepository = bookingRepository,
              onConfirmed = { confirmedBooking ->
                navController.navigate(Screen.Confirmation.createRoute(confirmedBooking.id)) {
                  popUpTo(Screen.SearchingCrew.createRoute(bookingId)) { inclusive = true }
                }
              },
              onCancelled = {
                scope.launch {
                  bookingRepository.cancelBooking(bookingId)
                  navController.navigate(Screen.MainHome.route) {
                    popUpTo(Screen.MainHome.route) { inclusive = true }
                  }
                }
              },
              onDismissToHome = {
                navController.navigate(Screen.MainHome.route) {
                  popUpTo(Screen.MainHome.route) { inclusive = true }
                }
              }
            )
          }
        }

        // CONFIRMATION SCREEN
        composable(
          route = Screen.Confirmation.route,
          arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
          val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
          val user = currentUser
          val bookingFlow = remember(bookingId) { bookingRepository.getBooking(bookingId) }
          val booking by bookingFlow.collectAsState(initial = null)

          if (booking != null && user != null) {
            ConfirmationScreen(
              booking = booking!!,
              currentUser = user,
              onOpenChat = { navController.navigate(Screen.BookingChat.createRoute(bookingId)) },
              onViewBookingDetails = { navController.navigate(Screen.BookingDetail.createRoute(bookingId)) },
              onBackToHome = {
                navController.navigate(Screen.MainHome.route) {
                  popUpTo(Screen.MainHome.route) { inclusive = true }
                }
              }
            )
          }
        }

        // BOOKING DETAIL SCREEN
        composable(
          route = Screen.BookingDetail.route,
          arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
          val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
          val user = currentUser
          if (user != null) {
            BookingDetailScreen(
              bookingId = bookingId,
              currentUser = user,
              bookingRepository = bookingRepository,
              onBackClick = { navController.popBackStack() },
              onOpenChat = { id -> navController.navigate(Screen.BookingChat.createRoute(id)) }
            )
          }
        }

        // PRODUCTION CHAT SCREEN
        composable(
          route = Screen.BookingChat.route,
          arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
          val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
          val user = currentUser
          if (user != null) {
            BookingChatScreen(
              bookingId = bookingId,
              currentUser = user,
              bookingRepository = bookingRepository,
              messageRepository = messageRepository,
              onBackClick = { navController.popBackStack() }
            )
          }
        }
      }

      // FLOATING INCOMING SHOOT REQUEST OVERLAY FOR CREW
      if (currentUser?.role == UserRole.CREW) {
        val crewUser = currentUser!!
        val incomingRequestViewModel = remember(crewUser.id) {
          CrewIncomingRequestViewModel(
            crewUserId = crewUser.id,
            bookingRepository = bookingRepository,
            crewRepository = crewRepository,
            notificationRepository = notificationRepository
          )
        }

        IncomingShootRequestOverlay(
          viewModel = incomingRequestViewModel,
          onViewShoot = { bookingId ->
            navController.navigate(Screen.BookingDetail.createRoute(bookingId))
          }
        )
      }
    }

    if (showNotificationDialog) {
      NotificationDialog(
        userId = currentUser?.id ?: "",
        notificationRepository = notificationRepository,
        onDismiss = { showNotificationDialog = false },
        onNotificationClick = { bookingId ->
          showNotificationDialog = false
          if (bookingId != null) {
            navController.navigate(Screen.BookingDetail.createRoute(bookingId))
          }
        }
      )
    }
  }
}

/**
 * Floating Liquid Glass Navigation Item with smooth capsule highlight
 */
@Composable
private fun FloatingNavItem(
  selected: Boolean,
  icon: ImageVector,
  label: String,
  testTag: String,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1f,
    animationSpec = tween(durationMillis = 100),
    label = "nav_item_scale"
  )

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = if (selected) Color(0x33FFB800) else Color.Transparent,
    border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f)) else null,
    modifier = Modifier
      .scale(scale)
      .clip(RoundedCornerShape(20.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(
        horizontal = if (selected) 14.dp else 10.dp,
        vertical = 8.dp
      ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) AmberGold else Color(0xFF8E95A5),
        modifier = Modifier.size(20.dp)
      )
      if (selected) {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = AmberGold
        )
      }
    }
  }
}
