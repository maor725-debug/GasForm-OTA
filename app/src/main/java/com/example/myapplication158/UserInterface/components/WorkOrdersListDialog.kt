package com.example.myapplication158.UserInterface.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication158.data.WorkOrder
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.util.NavigationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrdersListDialog(
    viewModel: GasFormViewModel,
    onDismiss: () -> Unit,
    onAddNewWorkOrder: () -> Unit,
    onEditWorkOrder: (WorkOrder) -> Unit
) {
    val context = LocalContext.current
    val workOrders by viewModel.allWorkOrders.collectAsState()

    var selectedFilterTab by remember { mutableIntStateOf(0) }

    val filteredOrders = remember(workOrders, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> workOrders.filter { it.status == WorkOrder.STATUS_PENDING }
            2 -> workOrders.filter { it.status == WorkOrder.STATUS_COMPLETED }
            3 -> workOrders.filter { it.status == WorkOrder.STATUS_CANCELED }
            else -> workOrders
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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

                // Action Bar: Add New Work Order Button
                Button(
                    onClick = onAddNewWorkOrder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("הוסף עבודה חדשה ליומן", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedFilterTab,
                    edgePadding = 0.dp,
                    divider = {},
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = { Text("הכל (${workOrders.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = { Text("ממתין לביצוע", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedFilterTab == 2,
                        onClick = { selectedFilterTab = 2 },
                        text = { Text("בוצע ✅", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedFilterTab == 3,
                        onClick = { selectedFilterTab = 3 },
                        text = { Text("בוטל ❌", fontSize = 12.sp) }
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
                            WorkOrderItemCard(
                                item = item,
                                onStatusChange = { newStatus ->
                                    if (newStatus == WorkOrder.STATUS_RESCHEDULED) {
                                        onEditWorkOrder(item)
                                    } else {
                                        viewModel.updateWorkOrder(item.copy(status = newStatus))
                                        Toast.makeText(context, "סטטוס העבודה עודכן", Toast.LENGTH_SHORT).show()
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkOrderItemCard(
    item: WorkOrder,
    onStatusChange: (String) -> Unit,
    onToggleMute: () -> Unit,
    onExportCalendar: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current

    val statusBg = when (item.status) {
        WorkOrder.STATUS_COMPLETED -> Color(0xFFE8F5E9)
        WorkOrder.STATUS_CANCELED -> Color(0xFFFFEBEE)
        WorkOrder.STATUS_RESCHEDULED -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    }

    val statusText = when (item.status) {
        WorkOrder.STATUS_COMPLETED -> "✅ בוצע"
        WorkOrder.STATUS_CANCELED -> "❌ בוטל"
        WorkOrder.STATUS_RESCHEDULED -> "🔄 תואם מחדש"
        else -> "⏳ ממתין לביצוע"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = statusBg),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Date, Time & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.targetDate} בשעה ${item.targetTime}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Job Description & Location
            if (item.jobDescription.isNotBlank()) {
                Text(
                    text = "🛠️ ${item.jobDescription}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Status Updates ("בוצע", "בוטל", "תואם ליום אחר") & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { onStatusChange(WorkOrder.STATUS_COMPLETED) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("בוצע", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onStatusChange(WorkOrder.STATUS_CANCELED) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("בוטל", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onStatusChange(WorkOrder.STATUS_RESCHEDULED) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("תואם מחדש 🔄", fontSize = 11.sp)
                    }
                }

                // Control icons (Mute, Export to Native Calendar, Edit, Delete)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.Default.NotificationsActive,
                            contentDescription = "התראות",
                            tint = if (item.isMuted) Color.Gray else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onExportCalendar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "סנכרן ליומן",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ערוך",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "מחק",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
