package com.example.myapplication158.UserInterface.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.util.PdfGenerator

@Composable
fun FormListItemAiStyle(
    form: GasForm, onEdit: () -> Unit, onPreview: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, onPricingClick: () -> Unit,
    aiCardBg: Color, aiTextColor: Color, aiTextGray: Color, primaryColor: Color, aiBorderColor: Color
) {
    val isDraft = !form.isSavedToTarget || form.savedTargetLocation == "מכשיר" || form.savedTargetLocation.isNullOrEmpty()

    val (badgeColor, badgeText) = if (isDraft) {
        Pair(aiTextGray, "טיוטה")
    } else {
        when {
            form.isStatusConforming -> Pair(Color(0xFF4CAF50), "תקין ✓")
            form.isStatusNonConforming -> Pair(Color(0xFFFF5252), "לקוי ✗")
            else -> Pair(aiTextGray, "טיוטה")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = aiCardBg),
        border = BorderStroke(1.dp, aiBorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(text = form.clientName.ifEmpty { "לקוח ללא שם" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = aiTextColor, fontSize = 15.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isDraft) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = "גובה", tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(color = badgeColor.copy(alpha = 0.15f), contentColor = badgeColor, shape = RoundedCornerShape(8.dp), border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.5f))) {
                        Text(text = badgeText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(text = "יישוב: ${form.clientCity.ifEmpty { "לא צוין" }} | רחוב: ${form.clientStreet.ifEmpty { "לא צוין" }}", style = MaterialTheme.typography.bodySmall, color = aiTextGray, modifier = Modifier.weight(1f).padding(top = 2.dp), fontSize = 11.sp)
                if (!form.customerPrice.isNullOrBlank() || !form.technicianCost.isNullOrBlank()) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!form.customerPrice.isNullOrBlank()) {
                            Surface(color = Color.White, shape = RoundedCornerShape(6.dp)) {
                                Text(text = "לקוח: ₪${form.customerPrice}", color = Color(0xFF2E7D32), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        if (!form.technicianCost.isNullOrBlank()) {
                            Surface(color = Color.White, shape = RoundedCornerShape(6.dp)) {
                                Text(text = "טכנאי: ₪${form.technicianCost}", color = Color(0xFFE65100), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = aiBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = aiTextGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = form.date.ifEmpty { "ללא תאריך" }, style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr), color = aiTextGray, fontSize = 10.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "תמחור", tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp).clickable { onPricingClick() })
                    Icon(imageVector = Icons.Default.Share, contentDescription = "שתף PDF", tint = primaryColor, modifier = Modifier.size(18.dp).clickable { onShare() })
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "ערוך", tint = primaryColor, modifier = Modifier.size(18.dp).clickable { onEdit() })
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "צפייה", tint = primaryColor, modifier = Modifier.size(18.dp).clickable { onPreview() })
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "מחק", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp).clickable { onDelete() })
                }
            }
        }
    }
}

@Composable
fun FinancialReportDialog(
    forms: List<GasForm>, onDismiss: () -> Unit, onGenerate: (List<GasForm>) -> Unit,
    aiCardBg: Color, aiTextColor: Color, primaryColor: Color, aiBorderColor: Color, aiTextGray: Color
) {
    val context = LocalContext.current
    var selectedForms by remember { mutableStateOf(setOf<GasForm>()) }
    val allSelected = selectedForms.size == forms.size && forms.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = aiCardBg, titleContentColor = primaryColor, textContentColor = aiTextColor,
        title = { Text("הפקת דוח", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedForms = if (allSelected) emptySet() else forms.toSet() }.padding(vertical = 4.dp)) {
                    Checkbox(checked = allSelected, onCheckedChange = { checked -> selectedForms = if (checked) forms.toSet() else emptySet() }, colors = CheckboxDefaults.colors(checkedColor = primaryColor, checkmarkColor = Color.White))
                    Text("בחר הכל", color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                HorizontalDivider(color = aiBorderColor)
                LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 250.dp)) {
                    items(forms) { form ->
                        val isSelected = selectedForms.contains(form)
                        val hasFinance = !form.customerPrice.isNullOrBlank() || !form.technicianCost.isNullOrBlank()
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedForms = if (isSelected) selectedForms - form else selectedForms + form }.padding(vertical = 4.dp)) {
                            Checkbox(checked = isSelected, onCheckedChange = { c -> selectedForms = if (c) selectedForms + form else selectedForms - form }, colors = CheckboxDefaults.colors(checkedColor = primaryColor, checkmarkColor = Color.White))
                            Column {
                                Text(form.clientName.ifEmpty { "לקוח ללא שם" }, color = aiTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(form.date.ifEmpty { "ללא תאריך" }, color = aiTextGray, fontSize = 11.sp)
                                    if (!hasFinance) { Spacer(modifier = Modifier.width(6.dp)); Text("(ללא תמחור)", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pdfFile = PdfGenerator.generateFinancialReportPdf(context, selectedForms.toList())
                    if (pdfFile != null) {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "לא נמצאה אפליקציה להצגת PDF", Toast.LENGTH_SHORT).show()
                        }
                        onGenerate(selectedForms.toList())
                    } else {
                        Toast.makeText(context, "שגיאה בהפקת הדוח", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = selectedForms.isNotEmpty()
            ) { Text("הפק", fontWeight = FontWeight.Bold, color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ביטול", color = aiTextGray) } }
    )
}

@Composable
fun PricingDialog(
    form: GasForm, onDismiss: () -> Unit, onSave: (GasForm) -> Unit,
    surfaceColor: Color, primaryColor: Color, textColor: Color, borderColor: Color
) {
    var customerPrice by remember { mutableStateOf(form.customerPrice ?: "") }
    var techCost by remember { mutableStateOf(form.technicianCost ?: "") }
    var workDesc by remember { mutableStateOf(form.internalWorkDescription ?: "") }

    val cPrice = customerPrice.toDoubleOrNull() ?: 0.0
    val tCost = techCost.toDoubleOrNull() ?: 0.0
    val profit = cPrice - tCost
    val profitColor = if (profit >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = surfaceColor, titleContentColor = textColor,
        title = { Text("תמחור ועבודה", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customerPrice, onValueChange = { customerPrice = it }, label = { Text("מחיר ללקוח (₪)", textAlign = TextAlign.Right, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = borderColor, focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = primaryColor, unfocusedLabelColor = textColor)
                )
                OutlinedTextField(
                    value = workDesc, onValueChange = { workDesc = it }, label = { Text("מה העבודה הייתה?", textAlign = TextAlign.Right, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = borderColor, focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = primaryColor, unfocusedLabelColor = textColor)
                )
                OutlinedTextField(
                    value = techCost, onValueChange = { techCost = it }, label = { Text("עלות טכנאי (₪)", textAlign = TextAlign.Right, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = borderColor, focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = primaryColor, unfocusedLabelColor = textColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "₪${if (profit % 1 == 0.0) profit.toInt() else profit}", fontWeight = FontWeight.Bold, color = profitColor, fontSize = 18.sp, style = TextStyle(textDirection = TextDirection.Ltr))
                    Text("רווח נקי:", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(form.copy(customerPrice = customerPrice, technicianCost = techCost, internalWorkDescription = workDesc)) }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) { Text("שמור", fontWeight = FontWeight.Bold, color = Color.White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ביטול", color = textColor) } }
    )
}

@Composable
fun FormCard(title: String, cardBgColor: Color, borderColor: Color, titleColor: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CheckboxWithLabel(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, colors: CheckboxColors, textColor: Color, modifier: Modifier = Modifier, subLabel: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange, colors = colors)
        Column {
            Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = if(isChecked) FontWeight.Bold else FontWeight.Normal)
            if (subLabel != null) { Text(text = subLabel, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp) }
        }
    }
}