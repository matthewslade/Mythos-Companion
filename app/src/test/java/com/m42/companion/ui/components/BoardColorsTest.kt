package com.m42.companion.ui.components

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardColorsTest {
    private val dark = Color(0xFF1F1A12)

    @Test
    fun `light card backs get dark text and dark ones white`() {
        assertEquals(dark, readableOn(Color(0xFFA8A8A3))) // Downtown, light grey
        assertEquals(dark, readableOn(Color(0xFFD9AA32))) // Miskatonic U., yellow
        assertEquals(Color.White, readableOn(Color(0xFF555751))) // Easttown, dark grey
        assertEquals(Color.White, readableOn(Color(0xFF76528E))) // Rivertown, purple
        assertEquals(Color.White, readableOn(Color(0xFF00741E))) // Eldritch Americas
    }
}
