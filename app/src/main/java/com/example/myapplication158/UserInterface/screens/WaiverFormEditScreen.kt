package com.example.myapplication158.UserInterface.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.ClientSignaturePad
import com.example.myapplication158.data.WaiverForm
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiverFormEditScreen(
    viewModel: GasFormViewModel,
    form: WaiverForm,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var currentFormId by remember { mutableIntStateOf(form.id) }
    var date by remember { mutableStateOf(form.date.ifEmpty { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }) }
    var clientName by remember { mutableStateOf(form.clientName) }
    var clientId by remember { mutableStateOf(form.clientId) }
    var address by remember { mutableStateOf(form.address) }
    var phone by remember { mutableStateOf(form.phone) }
    var workDescription by remember { mutableStateOf(form.workDescription) }

    var image1Uri by remember { mutableStateOf(form.image1Uri) }
    var image2Uri by remember { mutableStateOf(form.image2Uri) }
    var image3Uri by remember { mutableStateOf(form.image3Uri) }

    var clientSignatureUri by remember { mutableStateOf(form.clientSignatureUri) }

    val saveForm = {
        val updatedForm = WaiverForm(
            id = currentFormId, date = date, clientName = clientName, clientId = clientId,
            address = address, phone = phone, workDescription = workDescription,
            image1Uri = image1Uri, image2Uri = image2Uri, image3Uri = image3Uri,
            clientPhotoUri = null, clientSignatureUri = clientSignatureUri,
            technicianSignatureUri = null,
            savedPdfFilePath = form.savedPdfFilePath, isSavedToTarget = form.isSavedToTarget,
            savedTargetLocation = form.savedTargetLocation
        )
        viewModel.autoSaveWaiverForm(updatedForm) { newId -> currentFormId = newId }
    }

    BackHandler {
        saveForm()
        onNavigateBack()
    }

    val image1Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { image1Uri = it.toString(); saveForm() } }
    val image2Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { image2Uri = it.toString(); saveForm() } }
    val image3Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { image3Uri = it.toString(); saveForm() } }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("טופס הסרת אחריות", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { saveForm(); onNavigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "חזור")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("פרטי לקוח ועבודה", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color.LightGray)

                        OutlinedTextField(value = date, onValueChange = { date = it; saveForm() }, label = { Text("תאריך") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                        OutlinedTextField(value = clientName, onValueChange = { clientName = it; saveForm() }, label = { Text("שם הלקוח / חברה (חובה)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { address = it; saveForm() }, label = { Text("כתובת העבודה") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it; saveForm() }, label = { Text("טלפון") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        OutlinedTextField(value = workDescription, onValueChange = { workDescription = it; saveForm() }, label = { Text("תיאור מפורט של העבודה והסיכונים") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4, placeholder = { Text("לדוגמה: קידוח קיר בטון במטבח להעברת צנרת. הלקוח מודע שייתכן ועובר שם צינור מים...") })
                    }
                }

                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("תיעוד השטח לפני עבודה", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("העלה עד 3 תמונות של אזור העבודה לפני התחלת החציבה/קידוח כדי למנוע טענות לנזק קודם.", fontSize = 12.sp, color = Color.Gray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { image1Launcher.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (image1Uri == null) "תמונה 1" else "✓ צורף") }
                            Button(onClick = { image2Launcher.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (image2Uri == null) "תמונה 2" else "✓ צורף") }
                            Button(onClick = { image3Launcher.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (image3Uri == null) "תמונה 3" else "✓ צורף") }
                        }
                    }
                }

                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), border = BorderStroke(1.dp, Color(0xFFFF9800))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(Modifier.width(8.dp))
                            Text("הסרת אחריות לנזקי תשתית סמויה", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                        Text(
                            text = "1. מהות העבודה והסיכונים\n" +
                                    "אני החתום מטה מאשר בזאת כי הזמנתי מאת הטכנאי עבודות תשתית/גז הדורשות חציבה, קידוח, הלחמה או חדירה לקירות, רצפות או תקרות. מובן לי כי בעבודות מסוג זה קיים סיכון ממשי ובלתי נמנע לפגיעה בתשתיות סמויות שאינן גלויות לעין (כגון: צנרת מים, קווי חשמל, כבלי תקשורת, ביוב או תשתיות גז אחרות).\n\n" +
                                    "2. גילוי נאות ומגבלות השטח\n" +
                                    "אני מצהיר כי מסרתי לטכנאי את כל המידע שברשותי אודות מיקום תשתיות אלו. עם זאת, ידוע לי כי ללא תוכניות בנייה מדויקות, אין לטכנאי דרך טכנית לדעת בוודאות מוחלטת היכן עוברות התשתיות בתוך יצוקות המבנה.\n\n" +
                                    "3. פטור מלא מאחריות\n" +
                                    "לאור האמור לעיל, אני פוטר בזאת את הטכנאי המבצע, מנהליו או מי מטעמו, מכל אחריות משפטית או כספית בגין נזק ישיר או עקיף שייגרם לתשתיות הסמויות כתוצאה מביצוע העבודה שהזמנתי.\n\n" +
                                    "4. נשיאה בהוצאות\n" +
                                    "במידה וייגרם נזק לתשתית כלשהי במהלך העבודה, אני מתחייב לשאת באחריות המלאה ולכסות את כל העלויות הנדרשות לתיקון הנזק מול בעלי המקצוע הרלוונטיים (אינסטלטור, חשמלאי וכד'), ללא כל דרישה, תלונה או תביעה כלפי טכנאי הגז המבצע.",
                            fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Justify
                        )
                    }
                }

                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("אימות וחתימת לקוח", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text("בקש מהלקוח לחתום על המסך לאישור. חתימתך כטכנאי תצורף למסמך באופן אוטומטי בהתאם להגדרות המערכת.", fontSize = 12.sp, color = Color.Gray)

                        // קריאה לרכיב הציור במקום הרכיב הישן שפותח את הגלריה!
                        ClientSignaturePad(
                            initialSignatureUri = clientSignatureUri ?: "",
                            clientId = clientId,
                            onClientIdChanged = { clientId = it; saveForm() },
                            onSignatureSaved = { sigUri, _ ->
                                clientSignatureUri = sigUri
                                saveForm()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}