package com.example.data.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.data.notification.IncomingRequestNotificationHelper
import com.example.di.ServiceLocator
import com.example.domain.model.RequestEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service that listens for real-time shoot request events
 * and displays incoming request notifications when crew is active and available.
 */
class CrewDispatchService : Service {

  constructor() : super()

  companion object {
    const val ACTION_START_DISPATCH = "com.example.ACTION_START_DISPATCH"
    const val ACTION_STOP_DISPATCH = "com.example.ACTION_STOP_DISPATCH"

    const val EXTRA_CREW_USER_ID = "extra_crew_user_id"
    const val EXTRA_CREW_NAME = "extra_crew_name"

    fun startService(context: Context, crewUserId: String, crewName: String) {
      val intent = Intent(context, CrewDispatchService::class.java).apply {
        action = ACTION_START_DISPATCH
        putExtra(EXTRA_CREW_USER_ID, crewUserId)
        putExtra(EXTRA_CREW_NAME, crewName)
      }
      try {
        ContextCompat.startForegroundService(context, intent)
      } catch (e: Exception) {
        // Fallback if background restrictions apply
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, CrewDispatchService::class.java).apply {
        action = ACTION_STOP_DISPATCH
      }
      try {
        context.startService(intent)
      } catch (e: Exception) {
        // Handled
      }
    }
  }

  private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
  private var eventListenerJob: Job? = null
  private var availabilityJob: Job? = null
  private var crewUserId: String = ""
  private var crewName: String = "Crew Member"

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_STOP_DISPATCH -> {
        stopForegroundCompat()
        stopSelf()
        return START_NOT_STICKY
      }

      ACTION_START_DISPATCH, null -> {
        intent?.getStringExtra(EXTRA_CREW_USER_ID)?.let { crewUserId = it }
        intent?.getStringExtra(EXTRA_CREW_NAME)?.let { crewName = it }

        val notification = IncomingRequestNotificationHelper.buildDispatchForegroundNotification(
          this,
          crewName
        )
        try {
          startForeground(IncomingRequestNotificationHelper.NOTIFICATION_ID_DISPATCH_SERVICE, notification)
        } catch (e: Exception) {
          // Handled
        }

        startListening()
      }
    }

    return START_STICKY
  }

  private fun startListening() {
    eventListenerJob?.cancel()
    availabilityJob?.cancel()

    // Monitor availability - stop service if crew goes offline
    availabilityJob = serviceScope.launch {
      ServiceLocator.crewRepository.getCrewProfileByUserId(crewUserId).collectLatest { profile ->
        val isAvailable = profile?.isAvailable ?: true
        if (!isAvailable) {
          stopForegroundCompat()
          stopSelf()
        }
      }
    }

    // Monitor incoming request events
    eventListenerJob = serviceScope.launch {
      ServiceLocator.notificationRepository.requestEvents.collectLatest { event ->
        when (event) {
          is RequestEvent.NewShootRequest -> {
            IncomingRequestNotificationHelper.showIncomingShootNotification(
              context = this@CrewDispatchService,
              request = event.request,
              crewUserId = crewUserId
            )
          }

          is RequestEvent.RequestAssigned -> {
            if (event.assignedCrewId != crewUserId) {
              IncomingRequestNotificationHelper.showAlreadyAssignedNotification(
                context = this@CrewDispatchService,
                bookingId = event.bookingId,
                assignedTo = event.assignedCrewName
              )
            } else {
              IncomingRequestNotificationHelper.showShootConfirmedNotification(
                context = this@CrewDispatchService,
                bookingId = event.bookingId,
                shootTitle = "Shoot Assignment",
                shootDate = "Upcoming"
              )
            }
          }

          is RequestEvent.RequestCancelled -> {
            IncomingRequestNotificationHelper.cancelIncomingNotification(
              this@CrewDispatchService,
              event.bookingId
            )
          }

          is RequestEvent.RequestDeclined -> {
            if (event.crewId == crewUserId) {
              IncomingRequestNotificationHelper.cancelIncomingNotification(
                this@CrewDispatchService,
                event.bookingId
              )
            }
          }
        }
      }
    }
  }

  private fun stopForegroundCompat() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      stopForeground(STOP_FOREGROUND_REMOVE)
    } else {
      @Suppress("DEPRECATION")
      stopForeground(true)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
  }
}
