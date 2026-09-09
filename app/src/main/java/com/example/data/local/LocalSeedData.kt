package com.example.data.local

import com.example.domain.model.AppNotification
import com.example.domain.model.Booking
import com.example.domain.model.CrewProfile
import com.example.domain.model.CrewRole
import com.example.domain.model.Message
import com.example.domain.model.User
import com.example.domain.model.UserRole

/**
 * Initial verified studio accounts and profiles for FameBros Studio.
 * Designed for clean offline/local architecture prior to remote backend synchronization.
 * Contains NO fake demo countdowns, timers, or placeholder mock bookings.
 */
object LocalSeedData {

  val verifiedClient = User(
    id = "usr_client_01",
    name = "Alex Vance",
    email = "client@famebros.studio",
    phone = "+91 98201 54321",
    role = UserRole.CLIENT,
    companyName = "Apex Creative Agency",
    bio = "Creative Director producing digital campaigns, brand documentaries, and lookbooks."
  )

  val verifiedCrewMarcus = User(
    id = "usr_crew_01",
    name = "Marcus Chen",
    email = "marcus@famebros.studio",
    phone = "+91 98111 87654",
    role = UserRole.CREW,
    companyName = "FameBros Studio",
    bio = "Senior Cinematographer & Drone Specialist. 7+ years shooting music videos and commercials."
  )

  val verifiedCrewPriya = User(
    id = "usr_crew_02",
    name = "Priya Sharma",
    email = "priya@famebros.studio",
    phone = "+91 98333 12345",
    role = UserRole.CREW,
    companyName = "FameBros Studio",
    bio = "Editorial & Fashion Photographer. Published in leading fashion magazines."
  )

  val verifiedCrewDavid = User(
    id = "usr_crew_03",
    name = "David Miller",
    email = "david@famebros.studio",
    phone = "+91 98444 67890",
    role = UserRole.CREW,
    companyName = "FameBros Studio",
    bio = "Commercial Videographer & Colorist. Fast turnaround and on-set DIT/editing."
  )

  val verifiedAdmin = User(
    id = "usr_admin_01",
    name = "Kabir Sayed",
    email = "admin@famebros.studio",
    phone = "+91 99000 11223",
    role = UserRole.ADMIN,
    companyName = "FameBros Studio HQ",
    bio = "Head of Production Operations & Crew Logistics at FameBros."
  )

  val initialUsers: List<User> = listOf(
    verifiedClient,
    verifiedCrewMarcus,
    verifiedCrewPriya,
    verifiedCrewDavid,
    verifiedAdmin
  )

  val initialCrewProfiles: List<CrewProfile> = listOf(
    CrewProfile(
      id = "cp_marcus",
      userId = verifiedCrewMarcus.id,
      name = "Marcus Chen",
      primaryRole = CrewRole.CINEMATOGRAPHER,
      secondaryRoles = listOf(CrewRole.DRONE_OPERATOR, CrewRole.VIDEOGRAPHER),
      skills = listOf("Anamorphic 4K/8K", "DJI Ronin 4D", "FPV Drone Cinematography", "DaVinci Color"),
      experienceYears = 7,
      gearSummary = "RED V-Raptor 8K + ARRI Signature Primes + DJI Inspire 3 + Aputure 600c",
      rating = 4.96,
      totalCompletedShoots = 118,
      isAvailable = true,
      phone = "+91 98111 87654",
      email = "marcus@famebros.studio",
      instagramHandle = "@marcuschen.cine"
    ),
    CrewProfile(
      id = "cp_priya",
      userId = verifiedCrewPriya.id,
      name = "Priya Sharma",
      primaryRole = CrewRole.PHOTOGRAPHER,
      secondaryRoles = listOf(CrewRole.EDITOR),
      skills = listOf("High-Fashion Editorial", "Studio Strobe Lighting", "Lookbook Curation", "Capture One Pro"),
      experienceYears = 6,
      gearSummary = "Sony A7R V + Hasselblad 907X + Profoto Pro-11 Packs + Broncolor Paras",
      rating = 4.98,
      totalCompletedShoots = 142,
      isAvailable = true,
      phone = "+91 98333 12345",
      email = "priya@famebros.studio",
      instagramHandle = "@priya.sharma_frames"
    ),
    CrewProfile(
      id = "cp_david",
      userId = verifiedCrewDavid.id,
      name = "David Miller",
      primaryRole = CrewRole.VIDEOGRAPHER,
      secondaryRoles = listOf(CrewRole.EDITOR),
      skills = listOf("Commercial 4K Reels", "Gimbal Specialist", "Multi-Cam Concerts", "Premiere Pro"),
      experienceYears = 5,
      gearSummary = "Sony FX6 & FX3 + GM Lenses + Sennheiser Wireless Mics + Nanlite Forza",
      rating = 4.91,
      totalCompletedShoots = 89,
      isAvailable = true,
      phone = "+91 98444 67890",
      email = "david@famebros.studio",
      instagramHandle = "@davidmiller_films"
    )
  )

  val initialBookings: List<Booking> = emptyList()
  val initialMessages: List<Message> = emptyList()
  val initialNotifications: List<AppNotification> = emptyList()
}
