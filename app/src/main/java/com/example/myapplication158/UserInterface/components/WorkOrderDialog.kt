package com.example.myapplication158.UserInterface.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication158.data.WorkOrder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDialog(
    initialWorkOrder: WorkOrder? = null,
    onDismiss: () -> Unit,
    onSave: (WorkOrder) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val defaultDate = remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(calendar.time)
    }

    var targetDate by remember { mutableStateOf(initialWorkOrder?.targetDate?.ifEmpty { defaultDate } ?: defaultDate) }
    var targetTime by remember { mutableStateOf(initialWorkOrder?.targetTime?.ifEmpty { "10:00" } ?: "10:00") }
    var location by remember { mutableStateOf(initialWorkOrder?.location ?: "") }
    var clientPhone by remember { mutableStateOf(initialWorkOrder?.clientPhone ?: "") }
    var jobDescription by remember { mutableStateOf(initialWorkOrder?.jobDescription ?: "") }
    var quotedPrice by remember { mutableStateOf(initialWorkOrder?.quotedPrice ?: "") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            targetDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            targetTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        targetTime.substringBefore(":").toIntOrNull() ?: 10,
        targetTime.substringAfter(":").toIntOrNull() ?: 0,
        true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Header
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
                            text = if (initialWorkOrder == null) "הזמנת עבודה חדשה" else "עריכת עבודה ביומן",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "סגור")
                    }
                }

                HorizontalDivider()

                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = { Text("תאריך לביצוע") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "בחר תאריך")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    OutlinedTextField(
                        value = targetTime,
                        onValueChange = { targetTime = it },
                        label = { Text("שעת הגעה") },
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(Icons.Default.Schedule, contentDescription = "בחר שעה")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }

                // Location Field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("מיקום / כתובת העבודה") },
                    placeholder = { Text("עיר, רחוב, מספר בית...") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Client Phone Number
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("מספר נייד של הלקוח") },
                    placeholder = { Text("050-0000000") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Job Description
                OutlinedTextField(
                    value = jobDescription,
                    onValueChange = { jobDescription = it },
                    label = { Text("מהות העבודה (מה צריך לעשות)") },
                    placeholder = { Text("התקנת כיריים, בדיקת דליפה, נקודת גז...") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Quoted Price
                OutlinedTextField(
                    value = quotedPrice,
                    onValueChange = { quotedPrice = it },
                    label = { Text("הצעת מחיר שניתנה ללקוח (₪)") },
                    placeholder = { Text("350") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ביטול")
                    }

                    Button(
                        onClick = {
                            if (jobDescription.isBlank() && location.isBlank()) {
                                Toast.makeText(context, "אנא הזן פירוט עבודה או כתובת", Toast.LENGTH_SHORT).show()
                            } else {
                                val order = (initialWorkOrder ?: WorkOrder()).copy(
                                    targetDate = targetDate,
                                    targetTime = targetTime,
                                    location = location,
                                    clientPhone = clientPhone,
                                    jobDescription = jobDescription,
                                    quotedPrice = quotedPrice,
                                    status = if (initialWorkOrder != null) initialWorkOrder.status else WorkOrder.STATUS_PENDING
                                )
                                onSave(order)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("שמור ביומן", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
