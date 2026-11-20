package com.ao3reader.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ao3reader.app.data.model.WorkRating

enum class TagType(val bgColor: Color, val textColor: Color) {
    RATING(Color(0xFFFEE2E2), Color(0xFF991B1B)),
    RELATIONSHIP(Color(0xFFFCE7F3), Color(0xFF9D174D)), // Pink for CP
    FANDOM(Color(0xFFE0E7FF), Color(0xFF3730A3)),       // Indigo for Fandom
    CHARACTER(Color(0xFFCCFBF1), Color(0xFF115E59)),    // Teal for Character
    WARNING(Color(0xFFFEF3C7), Color(0xFF92400E)),      // Amber for Warning
    FREEFORM(Color(0xFFF3F4F6), Color(0xFF374151))      // Slate for Freeform
}

@Composable
fun TagChip(
    text: String,
    type: TagType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(type.bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = type.textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun RatingBadge(
    rating: WorkRating,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(rating.colorHex).copy(alpha = 0.15f)
    val textColor = Color(rating.colorHex)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = rating.displayName,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
