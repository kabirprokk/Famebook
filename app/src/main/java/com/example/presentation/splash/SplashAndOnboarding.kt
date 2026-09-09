package com.example.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.PrimaryGoldButton
import com.example.presentation.components.SecondaryDarkButton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
  onFinish: () -> Unit
) {
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    visible = true
    delay(1500)
    onFinish()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack),
    contentAlignment = Alignment.Center
  ) {
    AnimatedVisibility(
      visible = visible,
      enter = fadeIn(animationSpec = tween(700)) + scaleIn(initialScale = 0.92f, animationSpec = tween(700))
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Monogram Logo Node
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AmberGoldContainer)
            .border(1.5.dp, AmberGold, RoundedCornerShape(22.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "FameBook Logo",
            tint = AmberGold,
            modifier = Modifier.size(42.dp)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "FameBook",
          style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
          ),
          color = PureWhite
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "by",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "FameBros Studio",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = AmberGold
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "PROFESSIONAL SHOOT-BOOKING PLATFORM",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
          ),
          color = TextSecondary
        )
      }
    }
  }
}

data class OnboardingSlide(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val tag: String
)

@Composable
fun OnboardingScreen(
  onComplete: () -> Unit
) {
  val slides = listOf(
    OnboardingSlide(
      title = "Book Professional Production Crew",
      subtitle = "Instant access to verified FameBros cinematographers, photographers, sound technicians & editors.",
      icon = Icons.Default.Camera,
      tag = "ON-DEMAND TALENT"
    ),
    OnboardingSlide(
      title = "Lightning-Fast Dispatch",
      subtitle = "Submit your shoot specs. Available crew members receive immediate alerts and claim your shoot in seconds.",
      icon = Icons.Default.FlashOn,
      tag = "REAL-TIME MARKETPLACE"
    ),
    OnboardingSlide(
      title = "Direct Prep & Collaboration",
      subtitle = "Connect directly with your confirmed crew via live chat, call, or WhatsApp with clear call sheets.",
      icon = Icons.Default.Videocam,
      tag = "CREATIVE CONFIRMATION"
    )
  )

  val pagerState = rememberPagerState { slides.size }
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Skip Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      TextButton(
        onClick = onComplete,
        modifier = Modifier.testTag("onboarding_skip_button")
      ) {
        Text(
          text = "SKIP",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary
        )
      }
    }

    // Pager Content
    HorizontalPager(
      state = pagerState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) { page ->
      val slide = slides[page]
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(DarkCard)
            .border(1.5.dp, DarkBorder, RoundedCornerShape(32.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = slide.icon,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(54.dp)
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AmberGoldContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = slide.tag,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            ),
            color = AmberGold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = slide.title,
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = PureWhite,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = slide.subtitle,
          style = MaterialTheme.typography.bodyLarge,
          color = TextSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 22.sp
        )
      }
    }

    // Indicator & Action Buttons
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Dots
      Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 28.dp)
      ) {
        repeat(slides.size) { index ->
          val isSelected = pagerState.currentPage == index
          Box(
            modifier = Modifier
              .padding(horizontal = 4.dp)
              .height(6.dp)
              .width(if (isSelected) 24.dp else 6.dp)
              .clip(CircleShape)
              .background(if (isSelected) AmberGold else DarkBorder)
          )
        }
      }

      if (pagerState.currentPage < slides.size - 1) {
        PrimaryGoldButton(
          text = "NEXT",
          onClick = {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
          },
          modifier = Modifier.testTag("onboarding_next_button")
        )
      } else {
        PrimaryGoldButton(
          text = "GET STARTED",
          onClick = onComplete,
          modifier = Modifier.testTag("onboarding_get_started_button")
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
