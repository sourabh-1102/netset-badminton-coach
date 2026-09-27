package com.example.ui.components

import com.example.resources.Res
import com.example.resources.netset_logo
import com.example.resources.nexonvate_logo
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NetSetLogo(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.netset_logo),
        contentDescription = "NetSet",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
    )
}

@Composable
fun NexonvateLogo(height: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.nexonvate_logo),
        contentDescription = "NEXONVATE",
        contentScale = ContentScale.Fit,
        modifier = modifier.height(height)
    )
}
