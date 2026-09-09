package com.example

import com.example.data.local.LocalSeedData
import com.example.data.repository.LocalBookingRepository
import com.example.data.repository.LocalCrewRepository
import com.example.data.repository.LocalMessageRepository
import com.example.data.repository.LocalNotificationRepository
import com.example.data.repository.LocalUserRepository
import com.example.domain.model.Booking
import com.example.domain.model.BookingStatus
import com.example.domain.model.CrewRequirement
import com.example.domain.model.CrewRole
import com.example.domain.model.LocationInfo
import com.example.domain.model.ShootType
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FameBookMarketplaceTest {

  private lateinit var userRepository: LocalUserRepository
  private lateinit var crewRepository: LocalCrewRepository
  private lateinit var notificationRepository: LocalNotificationRepository
  private lateinit var bookingRepository: LocalBookingRepository
  private lateinit var messageRepository: LocalMessageRepository

  @Before
  fun setup() {
    userRepository = LocalUserRepository()
    crewRepository = LocalCrewRepository()
    notificationRepository = LocalNotificationRepository()
    bookingRepository = LocalBookingRepository(crewRepository, notificationRepository)
    messageRepository = LocalMessageRepository()
  }

  @Test
  fun `client booking request creation transitions to searching crew`() = runBlocking {
    val client = LocalSeedData.initialUsers.first { it.role == UserRole.CLIENT }
    val booking = Booking(
      id = "TEST-01",
      clientId = client.id,
      clientName = client.name,
      clientPhone = client.phone,
      clientEmail = client.email,
      shootType = ShootType.VIDEOGRAPHY,
      title = "Cyberpunk Music Video",
      description = "Fast 4K footage with neon lighting",
      date = "25 Oct 2026",
      startTime = "08:00 PM",
      durationHours = 4,
      location = LocationInfo("Bandra Studio", "Bandra West, Mumbai"),
      requirements = listOf(CrewRequirement(CrewRole.CINEMATOGRAPHER, 1)),
      status = BookingStatus.SEARCHING_CREW
    )

    val createResult = bookingRepository.createBooking(booking)
    assertTrue(createResult.isSuccess)
    val created = createResult.getOrThrow()
    assertEquals(BookingStatus.SEARCHING_CREW, created.status)

    // Check incoming requests for available crew
    val crew = LocalSeedData.initialUsers.first { it.role == UserRole.CREW }
    val crewIncoming = bookingRepository.getIncomingRequestsForCrew(crew.id).first()
    assertTrue(crewIncoming.any { it.id == created.id })
  }

  @Test
  fun `crew accepts shoot request and triggers atomic confirmation`() = runBlocking {
    val client = LocalSeedData.initialUsers.first { it.role == UserRole.CLIENT }
    val crewMembers = LocalSeedData.initialUsers.filter { it.role == UserRole.CREW }
    val firstCrew = crewMembers[0]
    val secondCrew = crewMembers[1]

    val booking = Booking(
      id = "TEST-02",
      clientId = client.id,
      clientName = client.name,
      clientPhone = client.phone,
      clientEmail = client.email,
      shootType = ShootType.FASHION_SHOOT,
      title = "Summer Editorial",
      description = "Outdoor editorial",
      date = "28 Oct 2026",
      startTime = "10:00 AM",
      durationHours = 3,
      location = LocationInfo("Juhu Beach", "Juhu, Mumbai"),
      requirements = listOf(CrewRequirement(CrewRole.PHOTOGRAPHER, 1)),
      status = BookingStatus.SEARCHING_CREW
    )

    val created = bookingRepository.createBooking(booking).getOrThrow()

    // First crew accepts
    val acceptResult = bookingRepository.acceptBooking(created.id, firstCrew.id)
    assertTrue(acceptResult.isSuccess)
    val confirmed = acceptResult.getOrThrow()

    assertEquals(BookingStatus.CONFIRMED, confirmed.status)
    assertEquals(firstCrew.id, confirmed.assignedCrewId)
    assertEquals(firstCrew.name, confirmed.assignedCrewName)

    // Second crew attempt to accept must fail due to atomic mutex lock
    val secondAcceptResult = bookingRepository.acceptBooking(created.id, secondCrew.id)
    assertFalse(secondAcceptResult.isSuccess)
  }

  @Test
  fun `chat messaging sends and retrieves messages for booking`() = runBlocking {
    val client = LocalSeedData.initialUsers.first { it.role == UserRole.CLIENT }
    val bookingId = "BK-7892"
    val initialList = messageRepository.getMessages(bookingId).first()
    val initialCount = initialList.size

    val sendResult = messageRepository.sendMessage(
      bookingId = bookingId,
      senderId = client.id,
      senderName = client.name,
      senderRole = UserRole.CLIENT,
      text = "Looking forward to shoot tomorrow!"
    )
    assertTrue(sendResult.isSuccess)

    val updatedList = messageRepository.getMessages(bookingId).first()
    assertEquals(initialCount + 1, updatedList.size)
    assertEquals("Looking forward to shoot tomorrow!", updatedList.last().text)
  }

  @Test
  fun `user login and logout flow works correctly`() = runBlocking {
    val loginResult = userRepository.login("client@famebros.studio", "password123")
    assertTrue(loginResult.isSuccess)
    val user = loginResult.getOrThrow()
    assertEquals(UserRole.CLIENT, user.role)
    assertEquals(user.id, userRepository.currentUser.value?.id)

    userRepository.logout()
    assertEquals(null, userRepository.currentUser.value)
  }
}
