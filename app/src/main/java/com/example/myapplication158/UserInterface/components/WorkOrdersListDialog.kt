package com.example.myapplication158.UserInterface.components

import android.content.Context
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.os.Looper
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication158.util.LocationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.data.PeriodicGasForm
import com.example.myapplication158.data.WorkOrder
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.util.NavigationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrdersListDialog(
    viewModel: GasFormViewModel,
    onDismiss: () -> Unit,
    onAddNewWorkOrder: () -> Unit,
    onEditWorkOrder: (WorkOrder) -> Unit,
    onGenerateNormativeForm: (GasForm) -> Unit,
    onGeneratePeriodicForm: (PeriodicGasForm) -> Unit
) {
    val context = LocalContext.current
    val workOrders by viewModel.allWorkOrders.collectAsState()
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE) }

    var selectedFilterTab by remember { mutableIntStateOf(0) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Int>() }

    var formSelectionForWorkOrder by remember { mutableStateOf<WorkOrder?>(null) }
    
    // הגדרות סידור מסלול
    var isSmartRoutingEnabled by remember { mutableStateOf(prefs.getBoolean("smart_routing", false)) }
    var endAddress by remember { mutableStateOf(prefs.getString("end_address", "") ?: "") }
    var isRoutingInProgress by remember { mutableStateOf(false) }

    // State לרשימת המשימות המסודרת זמנית
    var temporarySortedOrders by remember { mutableStateOf<List<WorkOrder>?>(null) }

    val todayDateString2 = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date()) }
    val todayDateString4 = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    val filteredOrders = remember(workOrders, selectedFilterTab, temporarySortedOrders) {
        val listToFilter = temporarySortedOrders ?: workOrders
        when (selectedFilterTab) {
            1 -> listToFilter.filter { it.status == WorkOrder.STATUS_PENDING }
            2 -> listToFilter.filter { it.status == WorkOrder.STATUS_COMPLETED }
            3 -> listToFilter.filter { it.status == WorkOrder.STATUS_CANCELED }
            else -> listToFilter
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.EventNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "יומן עבודות ותזכורות",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "סגור")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar: Add New + Multi-Select Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddNewWorkOrder,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("הוסף עבודה ליומן", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            isMultiSelectMode = !isMultiSelectMode
                            if (!isMultiSelectMode) selectedIds.clear()
                        },
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isMultiSelectMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isMultiSelectMode) Icons.Default.Checklist else Icons.Default.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isMultiSelectMode) "סיים בחירה" else "בחירה מרובה", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- מנגנון סידור מסלול חכם ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "הפעל סידור מסלול חכם (GPS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Switch(
                                checked = isSmartRoutingEnabled,
                                onCheckedChange = {
                                    isSmartRoutingEnabled = it
                                    prefs.edit().putBoolean("smart_routing", it).apply()
                                    if (!it) temporarySortedOrders = null // ביטול הסידור אם מכבים
                                }
                            )
                        }

                        if (isSmartRoutingEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = endAddress,
                                onValueChange = { 
                                    endAddress = it
                                    prefs.edit().putString("end_address", it).apply()
                                },
                                label = { Text("נקודת סיום מסלול (בית/משרד)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                                if (permissions.any { it.value }) {
                                    isRoutingInProgress = true
                                    fetchLocationForRouting(context) { location ->
                                        if (location != null) {
                                            val todayOrders = workOrders.filter { it.targetDate == todayDateString2 || it.targetDate == todayDateString4 }
                                            if (todayOrders.isEmpty()) {
                                                isRoutingInProgress = false
                                                Toast.makeText(context, "אין משימות שנקבעו להיום.", Toast.LENGTH_SHORT).show()
                                                return@fetchLocationForRouting
                                            }

                                            scope.launch {
                                                val result = LocationUtils.sortWorkOrdersForToday(context, todayOrders, location, endAddress)
                                                isRoutingInProgress = false
                                                
                                                if (result.success) {
                                                    // אנחנו שומרים את הרשימה המלאה אבל מציגים את המשימות של היום בראש 
                                                    // (או שנוכל פשוט להחליף את הרשימה כולה ל-sorted + others)
                                                    val otherOrders = workOrders.filter { it.targetDate != todayDateString2 && it.targetDate != todayDateString4 }
                                                    temporarySortedOrders = result.sortedOrders + otherOrders
                                                }
                                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            isRoutingInProgress = false
                                            Toast.makeText(context, "לא הצלחנו לדגום מיקום נוכחי. בדוק ש-GPS פועל.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "יש לאשר הרשאות מיקום לסידור המסלול", Toast.LENGTH_SHORT).show()
                                }
                            }

                            Button(
                                onClick = { 
                                    if (endAddress.isBlank()) {
                                        Toast.makeText(context, "אנא הזן כתובת לנקודת סיום", Toast.LENGTH_SHORT).show()
                                    } else {
                                        locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isRoutingInProgress
                            ) {
                                if (isRoutingInProgress) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("מסדר מסלול...")
                                } else {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("סדר משימות היום לפי מסלול", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Batch Delete Bar if items are selected
                if (isMultiSelectMode && selectedIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val itemsToDelete = workOrders.filter { selectedIds.contains(it.id) }
                            itemsToDelete.forEach { viewModel.deleteWorkOrder(it) }
                            Toast.makeText(context, "נמחקו ${selectedIds.size} משימות מהיומן", Toast.LENGTH_SHORT).show()
                            selectedIds.clear()
                            isMultiSelectMode = false
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("מחק ${selectedIds.size} משימות שנבחרו 🗑️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equal Weight Filter Tabs (No Overflow)
                TabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = { Text("הכל (${workOrders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = { Text("ממתין", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedFilterTab == 2,
                        onClick = { selectedFilterTab = 2 },
                        text = { Text("בוצע ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedFilterTab == 3,
                        onClick = { selectedFilterTab = 3 },
                        text = { Text("בוטל ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Work Orders List
                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "אין עבודות להצגה בקטגוריה זו",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredOrders, key = { it.id }) { item ->
                            val isSelected = selectedIds.contains(item.id)

                            WorkOrderItemCard(
                                item = item,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = isSelected,
                                onSelectToggle = {
                                    if (isSelected) selectedIds.remove(item.id)
                                    else selectedIds.add(item.id)
                                },
                                onStatusChange = { newStatus ->
                                    if (newStatus == WorkOrder.STATUS_RESCHEDULED) {
                                        onEditWorkOrder(item)
                                    } else if (newStatus == WorkOrder.STATUS_COMPLETED) {
                                        viewModel.deleteWorkOrder(item)
                                        Toast.makeText(context, "העבודה סומנה כבוצעה ונמחקה מהיומן ✅", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateWorkOrder(item.copy(status = newStatus))
                                    }
                                },
                                onToggleMute = {
                                    viewModel.toggleMuteWorkOrder(item)
                                    val msg = if (!item.isMuted) "התראות עבודה זו הופסקו" else "התראות עבודה זו הופעלו"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                onExportCalendar = {
                                    viewModel.addToNativeCalendar(item)
                                },
                                onDelete = {
                                    viewModel.deleteWorkOrder(item)
                                    Toast.makeText(context, "העבודה נמחקה מהיומן", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = {
                                    onEditWorkOrder(item)
                                },
                                onGenerateReportClick = {
                                    formSelectionForWorkOrder = item
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Form Selection Modal Dialog for Pre-Filling
    formSelectionForWorkOrder?.let { workOrder ->
        AlertDialog(
            onDismissRequest = { formSelectionForWorkOrder = null },
            icon = { Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = {
                Text(
                    "הפקת דוח חדש מהמשימה",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "פרטי הלקוח והכתובת יתווספו אוטומטית לדוח שתבחר:",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Normative Form 158
                    Button(
                        onClick = {
                            val nextPartnerNum = viewModel.getNextPartnerNumber()
                            val prefilled = createPrefilledGasForm(workOrder, nextPartnerNum)
                            formSelectionForWorkOrder = null
                            onDismiss()
                            onGenerateNormativeForm(prefilled)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("1. טופס נורמטיבי (158)", fontWeight = FontWeight.Bold)
                    }

                    // Option 2: Periodic Form D-1
                    Button(
                        onClick = {
                            val prefilled = createPrefilledPeriodicForm(workOrder)
                            formSelectionForWorkOrder = null
                            onDismiss()
                            onGeneratePeriodicForm(prefilled)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("2. דוח בדיקה תקופתית (ד-1)", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { formSelectionForWorkOrder = null }) {
                    Text("ביטול", color = Color.Gray)
                }
            }
        )
    }
}

private fun createPrefilledGasForm(workOrder: WorkOrder, nextPartnerNum: String): GasForm {
    val addressParts = workOrder.location.split(",", "-").map { it.trim() }
    val city = if (addressParts.isNotEmpty()) addressParts[0] else ""
    val street = if (addressParts.size > 1) addressParts[1] else ""

    return GasForm(
        partnerNumber = nextPartnerNum,
        clientPhone = workOrder.clientPhone,
        clientCity = city,
        clientStreet = street,
        clientName = if (workOrder.jobDescription.isNotBlank()) workOrder.jobDescription else "לקוח $city",
        executionRemarks = "הוזמן מיומן עבודה: ${workOrder.jobDescription} | מחיר: ${workOrder.quotedPrice} ₪"
    )
}

private fun createPrefilledPeriodicForm(workOrder: WorkOrder): PeriodicGasForm {
    val addressParts = workOrder.location.split(",", "-").map { it.trim() }
    val city = if (addressParts.isNotEmpty()) addressParts[0] else ""
    val street = if (addressParts.size > 1) addressParts[1] else ""

    return PeriodicGasForm(
        clientPhone = workOrder.clientPhone,
        city = city,
        street = street,
        clientName = if (workOrder.jobDescription.isNotBlank()) workOrder.jobDescription else "לקוח $city",
        executionRemarks = "הוזמן מיומן עבודה: ${workOrder.jobDescription} | מחיר: ${workOrder.quotedPrice} ₪"
    )
}

// פונקציית עזר למשיכת המיקום הנוכחי בשביל סידור המסלול ביומן
@SuppressLint("MissingPermission")
fun fetchLocationForRouting(context: Context, onResult: (Location?) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            onResult(null)
            return
        }

        var locationFound = false
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!locationFound) {
                    locationFound = true
                    onResult(location)
                    locationManager.removeUpdates(this)
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener, Looper.getMainLooper())
        }
        if (isGpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener, Looper.getMainLooper())
        }

        // טיימאאוט אם לא מצליח לדגום מהר
        Handler(Looper.getMainLooper()).postDelayed({
            if (!locationFound) {
                locationManager.removeUpdates(locationListener)
                val lastGps = if (isGpsEnabled) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                val lastNetwork = if (isNetworkEnabled) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null

                val bestLast = when {
                    lastGps != null && lastNetwork != null -> if (lastGps.accuracy < lastNetwork.accuracy) lastGps else lastNetwork
                    else -> lastGps ?: lastNetwork
                }
                
                locationFound = true
                onResult(bestLast)
            }
        }, 8000)

    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}

@Composable
private fun WorkOrderItemCard(
    item: WorkOrder,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onStatusChange: (String) -> Unit,
    onToggleMute: () -> Unit,
    onExportCalendar: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onGenerateReportClick: () -> Unit
) {
    val context = LocalContext.current
    val isCompleted = item.status == WorkOrder.STATUS_COMPLETED

    val accentColor = when (item.status) {
        WorkOrder.STATUS_COMPLETED -> Color(0xFF2E7D32)
        WorkOrder.STATUS_CANCELED -> Color(0xFFD32F2F)
        WorkOrder.STATUS_RESCHEDULED -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusText = when (item.status) {
        WorkOrder.STATUS_COMPLETED -> "✅ בוצע"
        WorkOrder.STATUS_CANCELED -> "❌ בוטל"
        WorkOrder.STATUS_RESCHEDULED -> "🔄 תואם מחדש"
        else -> "⏳ ממתין לביצוע"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isMultiSelectMode) onSelectToggle()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Accent Status Strip on the Right Edge (In RTL: Start)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                // Header Row 1: Date & Time + Status Tag + Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isMultiSelectMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelectToggle() },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "בוצע",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = "${item.targetDate} בשעה ${item.targetTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Action Controls: Status Chip & Edit Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        // Prominent Clear Edit Pencil Button
                        Button(
                            onClick = onEdit,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "ערוך משימה",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("ערוך ✏️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Job Description
                if (item.jobDescription.isNotBlank()) {
                    Text(
                        text = "🛠️ ${item.jobDescription}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Location & Waze Navigation Button
                if (item.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { NavigationUtils.navigateToAddress(context, item.location) }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "📍 ${item.location}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0288D1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "נווט",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("נווט ב-Waze", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Client Phone & Quoted Price Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.clientPhone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.clientPhone}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.clientPhone,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (item.quotedPrice.isNotBlank()) {
                        Text(
                            text = "💰 מחיר: ${item.quotedPrice} ₪",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Generate Report Button for Technician
                OutlinedButton(
                    onClick = onGenerateReportClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("הפק דוח ללקוח 📄 (עם פרטים אוטומטיים)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons Row: Status Updates & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Actions: "בוצע", "תואם מחדש", "מחק"
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { onStatusChange(WorkOrder.STATUS_COMPLETED) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("בוצע ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onStatusChange(WorkOrder.STATUS_RESCHEDULED) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("תואם מחדש 🔄", fontSize = 11.sp)
                        }

                        // Direct Delete Button (Deletes task immediately without opening edit)
                        Button(
                            onClick = onDelete,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("מחק 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Extra Utility Badges (Notification Mute & Native Calendar Sync)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            onClick = onToggleMute,
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                Icon(
                                    imageVector = if (item.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.Default.NotificationsActive,
                                    contentDescription = "התראות",
                                    tint = if (item.isMuted) Color.Gray else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = onExportCalendar,
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "סנכרן ליומן",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
