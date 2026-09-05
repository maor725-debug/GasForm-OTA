package com.example.myapplication158.UserInterface.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import java.io.File
import android.widget.Toast
import java.io.FileOutputStream

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    title: String = "חתימת טכנאי/ת:",
    initialSignatureUri: String? = null,
    onSignatureSaved: (String) -> Unit
) {
    val context = LocalContext.current
    var currentUri by remember(initialSignatureUri) { mutableStateOf(initialSignatureUri) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val file = File(context.filesDir, "tech_sig_${System.currentTimeMillis()}.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                val path = Uri.fromFile(file).toString()
                currentUri = path
                onSignatureSaved(path)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "שגיאה בטעינת תמונה", Toast.LENGTH_SHORT).show()
            }
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "חתימה קבועה (מהגדרות):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!currentUri.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(4.dp))
                    .border(0.5.dp, androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentUri,
                    contentDescription = "חתימה נבחרת",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        currentUri = null
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
                        contentDescription = "מחק",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "מחק תמונה", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "החלף תמונה", style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "בחר תמונת חתימה",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "לחץ לבחירה מהגלריה",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun ClientSignaturePad(
    modifier: Modifier = Modifier,
    initialSignatureUri: String? = null,
    initialPhotoUri: String? = null,
    clientId: String = "",
    onClientIdChanged: (String) -> Unit = {},
    onSignatureSaved: (String, String) -> Unit // Returns (Signature URI, Face Photo URI)
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
                text = "חתימת לקוח (צייר כאן):",
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
                            saveClientSignature(points, canvasWidth, canvasHeight, paths, strokeWidthPx, context) { sigUri ->
                                currentUri = sigUri
                                onSignatureSaved(sigUri, "")
                            }
                        },
                        onDragEnd = {
                            paths.add(currentPath)
                            currentPath = Path()
                            saveClientSignature(points, canvasWidth, canvasHeight, paths, strokeWidthPx, context) { sigUri ->
                                currentUri = sigUri
                                onSignatureSaved(sigUri, "")
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "חתימה נשמרה",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
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
                    onSignatureSaved("", "")
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

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = clientId,
            onValueChange = onClientIdChanged,
            label = { Text("ת.ז / ח.פ של הלקוח") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

private fun saveClientSignature(
    points: List<Offset>,
    canvasWidth: Int,
    canvasHeight: Int,
    paths: List<Path>,
    strokeWidthPx: Float,
    context: android.content.Context,
    onComplete: (String) -> Unit
) {
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
            val file = File(context.filesDir, "client_sig_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            savedSigUri = Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (savedSigUri.isNotEmpty()) {
            onComplete(savedSigUri)
        }
    }
}