package com.example.presentation.crew.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Booking
import com.example.domain.model.User
import com.example.domain.repository.BookingRepository
import com.example.domain.repository.CrewRepository
import com.example.presentation.components.CardEntrance
import com.example.presentation.components.PrimaryGoldButton
import com.example.presentation.components.SecondaryDarkButton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RecRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun CrewRequestsScreen(
  currentUser: User,
  bookingRepository: BookingRepository,
  crewRepository: CrewRepository,
  snackbarHostState: SnackbarHostState,
  onBookingClick: (String) -> Unit
) {
  val scope = rememberCoroutineScope()
  val incomingRequests by bookingRepository.getIncomingRequestsForCrew(currentUser.id)
    .collectAsState(initial = emptyList())

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
  ) {
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "SHOOT REQUESTS",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = PureWhite
        )
        Text(
          text = "Opportunities awaiting your acceptance",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    if (incomingRequests.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = DarkCard,
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DarkElevated),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No Pending Requests",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "When clients request crew for upcoming shoots in Mumbai, they will appear here immediately.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      itemsIndexed(incomingRequests, key = { _, req -> req.id }) { index, req ->
        CardEntrance(index = index) {
          CrewRequestCard(
          booking = req,
          onAccept = {
            scope.launch {
              val result = bookingRepository.acceptBooking(req.id, currentUser.id)
              if (result.isSuccess) {
                snackbarHostState.showSnackbar("Confirmed! You are assigned to '${req.title}'.")
              } else {
                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Acceptance failed.")
              }
            }
          },
          onDecline = {
            scope.launch {
              bookingRepository.declineBooking(req.id, currentUser.id)
              snackbarHostState.showSnackbar("Request declined.")
            }
          },
          onClick = { onBookingClick(req.id) }
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun CrewRequestCard(
  booking: Booking,
  onAccept: () -> Unit,
  onDecline: () -> Unit,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = DarkCard,
    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("crew_request_card_${booking.id}")
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Top Label
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = AmberGold.copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.3f))
        ) {
          Text(
            text = "NEW SHOOT REQUEST",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = AmberGold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Text(
          text = booking.shootType.title,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = booking.title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = PureWhite
      )

      if (booking.clientCompany != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Client: ${booking.clientName} • ${booking.clientCompany}",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = DarkBorder)
      Spacer(modifier = Modifier.height(14.dp))

      // Production specs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = booking.date,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = PureWhite
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${booking.startTime} (${booking.durationHours}h)",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = PureWhite
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = TextMuted,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${booking.location.name}, ${booking.location.address}",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      val requiredRole = booking.requirements.firstOrNull()?.role?.title ?: "Crew"
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CameraAlt,
          contentDescription = null,
          tint = TextMuted,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Required Role: $requiredRole",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
          color = AmberGold
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Action Buttons (DECLINE / ACCEPT) - NO TIMER, NO COUNTDOWN
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = onDecline,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = RecRed
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, RecRed.copy(alpha = 0.4f)),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("request_decline_button_${booking.id}")
        ) {
          Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("DECLINE", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onAccept,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AmberGold,
            contentColor = ObsidianBlack
          ),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("request_accept_button_${booking.id}")
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("ACCEPT", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
