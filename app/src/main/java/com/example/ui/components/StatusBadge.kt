package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.RepairTicketStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: RepairTicketStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        RepairTicketStatus.RECEIVED -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        RepairTicketStatus.DIAGNOSING -> Color(0xFF3E2723) to ElegantStatusDiagnosing
        RepairTicketStatus.IN_PROGRESS -> Color(0xFF1A237E) to ElegantStatusInProgress
        RepairTicketStatus.READY -> Color(0xFF1B5E20) to ElegantStatusReady
        RepairTicketStatus.COMPLETED -> Color(0xFF49454F) to Color(0xFFE6E1E5)
        RepairTicketStatus.CANCELLED -> Color(0xFF601410) to Color(0xFFF2B8B5)
    }

    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("status_badge_${status.name.lowercase()}")
    ) {
        Text(
            text = status.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

