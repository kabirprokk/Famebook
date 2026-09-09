package com.example.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Booking
import com.example.domain.model.BookingStatus
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.repository.BookingRepository
import com.example.presentation.components.EmptyState
import com.example.presentation.components.SectionHeader
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun MessagesOverviewScreen(
  currentUser: User,
  bookingRepository: BookingRepository,
  onOpenChat: (String) -> Unit
) {
  val allBookings by if (currentUser.role == UserRole.CREW) {
    bookingRepository.getCrewBookings(currentUser.id).collectAsState(initial = emptyList())
  } else {
    bookingRepository.getClientBookings(currentUser.id).collectAsState(initial = emptyList())
  }

  val activeChats = allBookings.filter {
    it.status == BookingStatus.CONFIRMED ||
        it.status == BookingStatus.IN_PROGRESS ||
        it.status == BookingStatus.COMPLETED
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
  ) {
    item {
      SectionHeader(title = "MESSAGES")
      Spacer(modifier = Modifier.height(6.dp))
    }

    if (activeChats.isEmpty()) {
      item {
        EmptyState(
          title = "NO ACTIVE CHATS",
          message = "Direct messaging unlocks as soon as a FameBros shoot is confirmed."
        )
      }
    } else {
      items(activeChats) { booking ->
        val otherPartyName = if (currentUser.role == UserRole.CREW) {
          booking.clientName
        } else {
          booking.assignedCrewName ?: "Marcus Chen"
        }

        val otherPartyRole = if (currentUser.role == UserRole.CREW) {
          booking.clientCompany ?: "Client"
        } else {
          booking.assignedCrewRole ?: "Lead Cinematographer"
        }

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = DarkCard,
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onOpenChat(booking.id) }
            .testTag("chat_row_${booking.id}")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(DarkElevated),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = otherPartyName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PureWhite
              )
              Text(
                text = "${booking.title} • $otherPartyRole",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }

            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
