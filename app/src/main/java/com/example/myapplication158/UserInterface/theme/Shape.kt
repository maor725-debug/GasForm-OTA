package com.example.myapplication158.UserInterface.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// צורות עגלגלות ורכות בסגנון iOS (Squircle-like)
val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),    // מתאים לכפתורים רגילים, שדות טקסט
    medium = RoundedCornerShape(20.dp),   // מתאים לכרטיסיות (Cards), דיאלוגים קטנים
    large = RoundedCornerShape(28.dp)     // מתאים למסכים קופצים מלמטה (Bottom Sheets), דיאלוגים גדולים
)