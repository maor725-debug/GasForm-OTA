package com.example.myapplication158.UserInterface.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

@Composable
fun TechnicianSignatureTouchPad(
    modifier: Modifier = Modifier,
    initialSignatureUri: String? = null,
    onSignatureSaved: (String) -> Unit
) {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Path>() }
    var points = remember { mutableStateListOf<Offset>() }

    var canvasWidth by remember { mutableStateOf(200) }
    var canvasHeight by remember { mutableStateOf(100) }

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 4.dp.toPx() }
    var currentUri by remember(initialSignatureUri) { mutableStateOf(initialSignatureUri) }
    var isClearedByUser by remember { mutableStateOf(false) }

    LaunchedEffect(initialSignatureUri) {
        if (initialSignatureUri.isNullOrEmpty()) {
            paths.clear()
            points.clear()
            currentPath = Path()
            isClearedByUser = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "חתימה קבועה (צייר כאן):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(4.dp))
                .border(0.5.dp, androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isClearedByUser = false
                            currentUri = null
                            val maxX = size.width.toFloat()
                            val maxY = size.height.toFloat()
                            val x = offset.x.coerceIn(0f, maxX)
                            val y = offset.y.coerceIn(0f, maxY)
                            val newPath = Path()
                            newPath.moveTo(x, y)
                            currentPath = newPath
                            points.add(Offset(x, y))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val currentOffset = change.position
                            val maxX = size.width.toFloat()
                            val maxY = size.height.toFloat()
                            val x = currentOffset.x.coerceIn(0f, maxX)
                            val y = currentOffset.y.coerceIn(0f, maxY)
                            currentPath.lineTo(x, y)
                            points.add(Offset(x, y))
                        },
                        onDragCancel = {
                            paths.add(currentPath)
                            currentPath = Path()
                            if (points.isNotEmpty()) {
                                val targetW = 1200
                                val targetH = (if (canvasWidth > 0) (1200f * canvasHeight / canvasWidth).toInt() else 600).coerceAtLeast(100)
                                val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                canvas.drawColor(Color.WHITE)
                                val scaleFactor = if (canvasWidth > 0) targetW.toFloat() / canvasWidth.toFloat() else 4.0f
                                val paint = Paint().apply {
                                    color = Color.BLACK
                                    strokeWidth = strokeWidthPx * scaleFactor
                                    style = Paint.Style.STROKE
                                    strokeCap = Paint.Cap.ROUND
                                    strokeJoin = Paint.Join.ROUND
                                    isAntiAlias = true
                                }
                                canvas.save()
                                canvas.scale(scaleFactor, scaleFactor)
                                for (p in paths) {
                                    canvas.drawPath(p.asAndroidPath(), paint)
                                }
                                canvas.restore()
                                var savedSigUri = ""
                                try {
                                    val file = File(context.filesDir, "tech_touch_sig_${System.currentTimeMillis()}.png")
                                    FileOutputStream(file).use { out ->
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    savedSigUri = Uri.fromFile(file).toString()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                if (savedSigUri.isNotEmpty()) {
                                    currentUri = savedSigUri
                                    onSignatureSaved(savedSigUri)
                                }
                            }
                        },
                        onDragEnd = {
                            paths.add(currentPath)
                            currentPath = Path()
                            if (points.isNotEmpty()) {
                                val targetW = 1200
                                val targetH = (if (canvasWidth > 0) (1200f * canvasHeight / canvasWidth).toInt() else 600).coerceAtLeast(100)
                                val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                canvas.drawColor(Color.WHITE)
                                val scaleFactor = if (canvasWidth > 0) targetW.toFloat() / canvasWidth.toFloat() else 4.0f
                                val paint = Paint().apply {
                                    color = Color.BLACK
                                    strokeWidth = strokeWidthPx * scaleFactor
                                    style = Paint.Style.STROKE
                                    strokeCap = Paint.Cap.ROUND
                                    strokeJoin = Paint.Join.ROUND
                                    isAntiAlias = true
                                }
                                canvas.save()
                                canvas.scale(scaleFactor, scaleFactor)
                                for (p in paths) {
                                    canvas.drawPath(p.asAndroidPath(), paint)
                                }
                                canvas.restore()
                                var savedSigUri = ""
                                try {
                                    val file = File(context.filesDir, "tech_touch_sig_${System.currentTimeMillis()}.png")
                                    FileOutputStream(file).use { out ->
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    savedSigUri = Uri.fromFile(file).toString()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                if (savedSigUri.isNotEmpty()) {
                                    currentUri = savedSigUri
                                    onSignatureSaved(savedSigUri)
                                }
                            }
                        }
                    )
                }
        ) {
            ComposeCanvas(
                modifier = Modifier.fillMaxSize()
            ) {
                canvasWidth = size.width.toInt()
                canvasHeight = size.height.toInt()

                for (p in paths) {
                    drawPath(
                        path = p,
                        color = androidx.compose.ui.graphics.Color.Black,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                drawPath(
                    path = currentPath,
                    color = androidx.compose.ui.graphics.Color.Black,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            if (!currentUri.isNullOrEmpty() && paths.isEmpty() && !isClearedByUser) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentUri,
                        contentDescription = "חתימה שמורה",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    isClearedByUser = true
                    currentUri = null
                    paths.clear()
                    points.clear()
                    currentPath = Path()
                    onSignatureSaved("")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "נקה",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "נקה חתימה", style = MaterialTheme.typography.labelSmall)
            }

            if (paths.isNotEmpty() || !currentUri.isNullOrEmpty()) {
                Text(
                    text = "נשמר אוטומטית",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}