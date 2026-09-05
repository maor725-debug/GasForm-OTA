package com.example.myapplication158.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.media.ExifInterface
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.data.PeriodicGasForm
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private fun applyWatermark(bitmap: Bitmap, text: String): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.parseColor("#FFCC00")
            textSize = bitmap.width * 0.04f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
            isAntiAlias = true
        }
        val textWidth = paint.measureText(text)
        canvas.drawText(text, bitmap.width - textWidth - 20f, bitmap.height - 30f, paint)
        return result
    }

    private fun decodeBitmapWithExifRotation(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream -> BitmapFactory.decodeStream(stream, null, options) }
            var scale = 1
            if (options.outWidth > maxDimension || options.outHeight > maxDimension) {
                val largest = Math.max(options.outWidth, options.outHeight)
                scale = Math.round(largest.toFloat() / maxDimension.toFloat())
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
            var decodedBitmap: Bitmap? = null
            context.contentResolver.openInputStream(uri)?.use { stream -> decodedBitmap = BitmapFactory.decodeStream(stream, null, decodeOptions) }
            val bitmap = decodedBitmap ?: return null
            val matrix = Matrix()
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
                        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
                    }
                }
            } catch (exifEx: Throwable) { exifEx.printStackTrace() }

            if (!matrix.isIdentity) {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) bitmap.recycle()
                rotated
            } else { bitmap }
        } catch (e: Throwable) { e.printStackTrace(); null }
    }

    fun generateFinancialReportPdf(context: Context, forms: List<GasForm>): File? {
        val settingsManager = SettingsManager(context)
        val headerTitle = if (settingsManager.contractorHeader.isNullOrBlank()) "מאור מנחם - קבלן עבודות גז" else settingsManager.contractorHeader
        val pdfDocument = PdfDocument()
        try {
            var currentPageNum = 1
            var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
            var canvas = page.canvas

            val headerPaint = Paint().apply { color = Color.WHITE; textSize = 18f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val headerBgPaint = Paint().apply { color = Color.parseColor("#381504"); style = Paint.Style.FILL }
            val headerBorderPaint = Paint().apply { color = Color.parseColor("#FF8800"); strokeWidth = 5f; style = Paint.Style.STROKE }
            val textPaint = Paint().apply { color = Color.BLACK; textSize = 11f; textAlign = Paint.Align.RIGHT }
            val boldPaint = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.RIGHT }
            val tableHeaderPaint = Paint().apply { color = Color.WHITE; textSize = 12f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val tableHeaderBgPaint = Paint().apply { color = Color.parseColor("#FF8800"); style = Paint.Style.FILL }
            val borderPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
            val profitPaint = Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 11f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val rowBgAlt = Paint().apply { color = Color.parseColor("#F9F9F9"); style = Paint.Style.FILL }

            fun drawPageHeader() {
                canvas.drawRect(0f, 0f, 595f, 90f, headerBgPaint)
                canvas.drawLine(0f, 90f, 595f, 90f, headerBorderPaint)
                canvas.drawText("דוח ריכוז תמחור ועבודות", 297f, 40f, headerPaint)
                canvas.drawText(headerTitle, 297f, 65f, Paint(headerPaint).apply { color = Color.parseColor("#FF8800"); textSize = 14f })
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                canvas.drawText("תאריך הפקה: $dateStr", 560f, 120f, boldPaint)
            }
            drawPageHeader()

            val cols = floatArrayOf(560f, 480f, 360f, 260f, 180f, 100f, 20f)
            val colCenters = floatArrayOf(520f, 420f, 310f, 220f, 140f, 60f)
            val headers = arrayOf("תאריך", "לקוח", "מהות העבודה", "ללקוח", "לטכנאי", "רווח")

            var y = 140f
            canvas.drawRect(cols[6], y, cols[0], y + 25f, tableHeaderBgPaint)
            for (i in headers.indices) {
                canvas.drawText(headers[i], colCenters[i], y + 17f, tableHeaderPaint)
                if (i > 0) canvas.drawLine(cols[i], y, cols[i], y + 25f, borderPaint)
            }
            canvas.drawRect(cols[6], y, cols[0], y + 25f, borderPaint)
            y += 25f

            var totalCustomer = 0.0; var totalTech = 0.0; var totalProfit = 0.0
            forms.forEachIndexed { index, form ->
                if (y > 780f) {
                    pdfDocument.finishPage(page)
                    currentPageNum++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                    canvas = page.canvas
                    drawPageHeader()
                    y = 140f
                    canvas.drawRect(cols[6], y, cols[0], y + 25f, tableHeaderBgPaint)
                    for (i in headers.indices) {
                        canvas.drawText(headers[i], colCenters[i], y + 17f, tableHeaderPaint)
                        if (i > 0) canvas.drawLine(cols[i], y, cols[i], y + 25f, borderPaint)
                    }
                    canvas.drawRect(cols[6], y, cols[0], y + 25f, borderPaint)
                    y += 25f
                }
                if (index % 2 != 0) canvas.drawRect(cols[6], y, cols[0], y + 25f, rowBgAlt)

                val cPrice = form.customerPrice?.toDoubleOrNull() ?: 0.0
                val tCost = form.technicianCost?.toDoubleOrNull() ?: 0.0
                val profit = cPrice - tCost
                totalCustomer += cPrice; totalTech += tCost; totalProfit += profit

                val textCenterPaint = Paint(textPaint).apply { textAlign = Paint.Align.CENTER }
                val safeDate = form.date.takeIf { it.isNotBlank() } ?: "-"
                val safeName = if (form.clientName.length > 12) form.clientName.substring(0, 10) + ".." else form.clientName.ifBlank { "-" }
                val safeDesc = if ((form.internalWorkDescription ?: "").length > 15) form.internalWorkDescription!!.substring(0, 13) + ".." else (form.internalWorkDescription ?: "-").ifBlank { "-" }

                canvas.drawText(safeDate, colCenters[0], y + 17f, textCenterPaint)
                canvas.drawText(safeName, colCenters[1], y + 17f, textCenterPaint)
                canvas.drawText(safeDesc, colCenters[2], y + 17f, textCenterPaint)
                canvas.drawText("₪${cPrice.toInt()}", colCenters[3], y + 17f, textCenterPaint)
                canvas.drawText("₪${tCost.toInt()}", colCenters[4], y + 17f, textCenterPaint)
                val pPaint = if (profit >= 0) profitPaint else Paint(profitPaint).apply { color = Color.RED }
                canvas.drawText("₪${profit.toInt()}", colCenters[5], y + 17f, pPaint)

                for (i in 1..5) canvas.drawLine(cols[i], y, cols[i], y + 25f, borderPaint)
                canvas.drawRect(cols[6], y, cols[0], y + 25f, borderPaint)
                y += 25f
            }

            canvas.drawRect(cols[6], y, cols[0], y + 30f, Paint().apply { color = Color.parseColor("#E0E0E0") })
            val totalsBoldCenter = Paint(boldPaint).apply { textAlign = Paint.Align.CENTER; textSize = 12f }
            canvas.drawText("סה\"כ:", 300f, y + 20f, boldPaint)
            canvas.drawText("₪${totalCustomer.toInt()}", colCenters[3], y + 20f, totalsBoldCenter)
            canvas.drawText("₪${totalTech.toInt()}", colCenters[4], y + 20f, totalsBoldCenter)
            canvas.drawText("₪${totalProfit.toInt()}", colCenters[5], y + 20f, Paint(profitPaint).apply { textSize = 13f })
            for (i in 3..5) canvas.drawLine(cols[i], y, cols[i], y + 30f, borderPaint)
            canvas.drawRect(cols[6], y, cols[0], y + 30f, borderPaint)
            pdfDocument.finishPage(page)

            val outputDir = context.cacheDir
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val outputFile = File(outputDir, "דוח_פיננסי_$timestamp.pdf")
            FileOutputStream(outputFile).use { out -> pdfDocument.writeTo(out) }
            return outputFile
        } catch (e: Exception) { e.printStackTrace(); return null } finally { pdfDocument.close() }
    }

    fun generateFormPdf(context: Context, form: GasForm): File? {
        val settingsManager = SettingsManager(context)
        return if (settingsManager.pdfTemplateStyle == SettingsManager.TEMPLATE_MODERN) {
            generateModernFormPdf(context, form, settingsManager)
        } else {
            generateClassicFormPdf(context, form, settingsManager)
        }
    }

    private fun generateClassicFormPdf(context: Context, form: GasForm, settingsManager: SettingsManager): File? {
        // [הקוד נשאר בדיוק כפי שהיה עבור הטופס הנורמטיבי]
        val headerTitle = if (settingsManager.contractorHeader.isNullOrBlank()) "מאור מנחם - קבלן עבודות גז" else settingsManager.contractorHeader
        val headerPhone = "טלפון: " + if (settingsManager.contractorPhone.isNullOrBlank()) "054-6096487" else settingsManager.contractorPhone

        val pdfDocument = PdfDocument()
        try {
            val blueFooterPaint = Paint().apply { color = Color.BLUE; textSize = 9f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.LEFT }
            val creationDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(form.createdAt))
            val locationStr = if (form.isUnaddressedSite) "אתר בבנייה (GPS מוגדר)" else form.clientCity.takeIf { it.isNotBlank() } ?: "לא צוין"
            val footerText = "טופס הופק ב: $creationDateStr | מיקום: $locationStr"

            fun drawFooter(c: Canvas) {
                c.drawText(footerText, 30f, 830f, blueFooterPaint)
                val creditPaint = Paint(blueFooterPaint).apply { color = Color.GRAY; textSize = 8f }
                c.drawText("מערכת נורמטיבי - פותח ע\"י מאור מנחם ©", 30f, 818f, creditPaint)
            }

            var currentPageNum = 1
            var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            var canvas = page.canvas

            val textPaint = Paint().apply { color = Color.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.RIGHT }
            val boldTextPaint = Paint().apply { color = Color.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.RIGHT }
            val valuePaint = Paint().apply { color = Color.BLUE; textSize = 10f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.RIGHT }
            val headerPaint = Paint().apply { color = Color.BLACK; textSize = 16f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val subHeaderPaint = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.CENTER }
            val sectionTitlePaint = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.RIGHT; isUnderlineText = true }
            val linePaint = Paint().apply { color = Color.DKGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
            val dashedLinePaint = Paint().apply { color = Color.GRAY; strokeWidth = 0.5f; style = Paint.Style.STROKE; pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f) }
            val borderPaint = Paint().apply { color = Color.BLACK; strokeWidth = 2f; style = Paint.Style.STROKE }
            val bitmapPaint = Paint().apply { isAntiAlias = true; isFilterBitmap = true; isDither = true }

            canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
            canvas.drawText(headerTitle, 297f, 45f, headerPaint)
            canvas.drawText(headerPhone, 297f, 60f, subHeaderPaint)
            canvas.drawText("(מס' טופס: ${form.sequentialNumber})", 30f, 45f, Paint().apply { color = Color.RED; textSize = 14f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.LEFT })

            textPaint.textAlign = Paint.Align.LEFT
            val oldValueAlign = valuePaint.textAlign
            valuePaint.textAlign = Paint.Align.LEFT
            canvas.drawText("\u200Eתאריך: ", 115f, 80f, textPaint)
            canvas.drawText("\u200E" + form.date, 40f, 80f, valuePaint)
            valuePaint.textAlign = oldValueAlign
            canvas.drawLine(40f, 82f, 110f, 82f, dashedLinePaint)

            canvas.drawText("טופס הצהרה על התאמה של מתקן גפ\"מ חדש או חלק ממנו לדרישות התקן 158 חלק 4", 297f, 105f, subHeaderPaint)

            var y = 125f

            fun drawSectionTitle(title: String) { canvas.drawText(title, 550f, y, sectionTitlePaint); y += 15f }

            fun drawCheckbox(checked: Boolean, text: String, rx: Float, ry: Float, customColor: Int? = null, isBold: Boolean = false) {
                val originalLineColor = linePaint.color
                val originalTextColor = textPaint.color
                if (customColor != null && checked) { linePaint.color = customColor; textPaint.color = customColor }
                val size = 9f
                linePaint.strokeWidth = 1f
                canvas.drawRect(rx - size, ry - size, rx, ry, linePaint)
                if (checked) {
                    val checkPaint = Paint().apply { color = customColor ?: Color.BLACK; strokeWidth = 1.5f; style = Paint.Style.STROKE }
                    canvas.drawLine(rx - size + 2, ry - size/2, rx - size/2, ry - 2, checkPaint)
                    canvas.drawLine(rx - size/2, ry - 2, rx - 2, ry - size + 2, checkPaint)
                }
                val paintToUse = if (isBold) boldTextPaint else textPaint
                paintToUse.textAlign = Paint.Align.RIGHT
                canvas.drawText(text, rx - size - 4, ry - 1, paintToUse)
                if (customColor != null) { linePaint.color = originalLineColor; textPaint.color = originalTextColor }
            }

            fun drawBoldLabelAndValue(label: String, value: String, x: Float, yVal: Float) {
                canvas.drawText(label, x, yVal, boldTextPaint)
                val labelWidth = boldTextPaint.measureText(label)
                val valueStartX = x - labelWidth - 5f
                canvas.drawText(value, valueStartX, yVal, valuePaint)
                val valueWidth = Math.max(valuePaint.measureText(value), 50f)
                canvas.drawLine(valueStartX - valueWidth, yVal + 2f, valueStartX, yVal + 2f, dashedLinePaint)
            }

            drawSectionTitle("1. פרטי המתקן ומיקומו - סמן ב-X במקום הנדרש")
            drawCheckbox(form.isSharedBuilding, "בית משותף", 540f, y)
            drawCheckbox(form.isCommercial, "מסחרי", 440f, y)
            drawCheckbox(form.isCylinders12x2, "מכלים 12*2", 360f, y)
            drawCheckbox(form.isCylinders48x2, "מכלים 48*2", 270f, y)
            drawCheckbox(form.isOtherType, "אחר:", 170f, y)
            if (form.isOtherType) {
                canvas.drawText(form.otherTypeText, 100f, y - 1, valuePaint)
                canvas.drawLine(40f, y + 1, 100f, y + 1, dashedLinePaint)
            }
            y += 20f

            drawSectionTitle("2. פרטי לקוח")
            canvas.drawText("שם לקוח:", 550f, y, textPaint)
            canvas.drawText(form.clientName, 490f, y, valuePaint)
            canvas.drawLine(330f, y + 2, 490f, y + 2, dashedLinePaint)
            canvas.drawText("טלפון:", 310f, y, textPaint)
            canvas.drawText(form.clientPhone, 265f, y, valuePaint)
            canvas.drawLine(40f, y + 2, 265f, y + 2, dashedLinePaint)
            y += 16f

            if (form.isUnaddressedSite) {
                canvas.drawText("מיקום חלופי (GPS):", 550f, y, textPaint)
                canvas.drawText(form.gpsCoordinates.ifEmpty { "לא נדגמו קואורדינטות" }, 430f, y, valuePaint)
                canvas.drawLine(150f, y + 2, 430f, y + 2, dashedLinePaint)
                canvas.drawText("[השטח הוגדר כאתר ללא כתובת מוסדרת]", 140f, y, Paint(textPaint).apply { color = Color.GRAY })
            } else {
                canvas.drawText("ישוב:", 550f, y, textPaint)
                canvas.drawText(form.clientCity, 510f, y, valuePaint)
                canvas.drawLine(370f, y + 2, 510f, y + 2, dashedLinePaint)
                canvas.drawText("רחוב:", 350f, y, textPaint)
                canvas.drawText(form.clientStreet, 310f, y, valuePaint)
                canvas.drawLine(180f, y + 2, 310f, y + 2, dashedLinePaint)
                canvas.drawText("בניין/דירה:", 160f, y, textPaint)
                canvas.drawText(form.clientBuilding, 80f, y, valuePaint)
                canvas.drawLine(40f, y + 2, 110f, y + 2, dashedLinePaint)
            }
            y += 20f

            drawSectionTitle("3. תיאור העבודה - סמן ב-X")
            drawCheckbox(form.isWorkNew, "מתקן חדש", 540f, y)
            drawCheckbox(form.isWorkAddition, "תוספת למתקן קיים", 420f, y)
            drawCheckbox(form.isWorkRepair, "תיקון נזק / שינוי", 280f, y)
            drawCheckbox(form.isWorkConnection, "חיבור מכשיר:", 160f, y, isBold = true)
            y += 14f

            if (form.isWorkConnection) {
                val dev1X = 550f
                val dev2X = 270f
                if (form.hasAdditionalDevice) {
                    canvas.drawText("פרטי מכשיר 1:", dev1X, y, boldTextPaint)
                    canvas.drawText("פרטי מכשיר 2 (נוסף):", dev2X, y, boldTextPaint)
                    y += 15f
                    drawBoldLabelAndValue("סוג:", form.connectionDeviceType, dev1X, y)
                    drawBoldLabelAndValue("סוג:", form.additionalDeviceType, dev2X, y)
                    y += 15f
                    drawBoldLabelAndValue("מותג:", form.connectionDeviceBrand, dev1X, y)
                    drawBoldLabelAndValue("מותג:", form.additionalDeviceBrand, dev2X, y)
                    y += 15f
                    drawBoldLabelAndValue("דגם:", form.connectionDeviceModel, dev1X, y)
                    drawBoldLabelAndValue("דגם:", form.additionalDeviceModel, dev2X, y)
                    y += 15f
                    drawBoldLabelAndValue("מס' סריאלי:", form.connectionDeviceSerial, dev1X, y)
                    drawBoldLabelAndValue("מס' סריאלי:", form.additionalDeviceSerial, dev2X, y)
                    y += 15f
                } else {
                    if (form.connectionDeviceType.isNotEmpty()) { drawBoldLabelAndValue("סוג המכשיר:", form.connectionDeviceType, dev1X, y); y += 15f }
                    if (form.connectionDeviceBrand.isNotEmpty()) { drawBoldLabelAndValue("מותג המכשיר:", form.connectionDeviceBrand, dev1X, y); y += 15f }
                    if (form.connectionDeviceModel.isNotEmpty()) { drawBoldLabelAndValue("דגם המכשיר:", form.connectionDeviceModel, dev1X, y); y += 15f }
                    if (form.connectionDeviceSerial.isNotEmpty()) { drawBoldLabelAndValue("מס' סריאלי:", form.connectionDeviceSerial, dev1X, y); y += 15f }
                }
                canvas.drawText("המכשירים חוברו עם צינור גומי תקני עד 3 מטר למכשיר. סכ' חבקים:", 550f, y, textPaint)
                canvas.drawText(form.clampsCount, 240f, y, valuePaint)
                canvas.drawLine(220f, y + 2, 240f, y + 2, dashedLinePaint)
                canvas.drawText("שנ' ייצור:", 210f, y, textPaint)
                canvas.drawText(form.hoseProductionYear, 155f, y, valuePaint)
                canvas.drawLine(125f, y + 2, 155f, y + 2, dashedLinePaint)
                canvas.drawText("סוג:", 115f, y, textPaint)
                canvas.drawText(form.hoseType, 85f, y, valuePaint)
                canvas.drawLine(40f, y + 2, 85f, y + 2, dashedLinePaint)
                y += 15f
            }
            y += 6f

            drawSectionTitle("4. בדיקה חזותית")
            canvas.drawText("אני הח\"מ מצהיר בזאת כי בדקתי את מע' הגפ\"מ המתואר לעיל חזותית ומצאתי כי הוא עומד", 550f, y, textPaint)
            y += 13f
            canvas.drawText("בכל דרישות התקן הישראלי 158 חלקים 2 ו-3, לפי העניין.", 550f, y, textPaint)
            y += 20f

            drawSectionTitle("5. בדיקת לחץ / אטימות לפני הפעלה")
            valuePaint.textAlign = Paint.Align.LEFT
            drawCheckbox(form.isLeakTestChecked, "בדיקת אטימות ללחץ שימוש נערכה בתאריך:", 540f, y)
            if (form.isLeakTestChecked) { canvas.drawText(form.leakTestDate, 245f, y, valuePaint) }
            canvas.drawLine(240f, y + 2, 330f, y + 2, dashedLinePaint)
            canvas.drawText("לחץ הבדיקה:", 225f, y, textPaint)
            if (form.isLeakTestChecked) { canvas.drawText(form.leakTestPressure, 160f, y, valuePaint) }
            canvas.drawLine(155f, y + 2, 185f, y + 2, dashedLinePaint)
            canvas.drawText("mbar, במשך 15 דק'", 145f, y, textPaint)
            y += 16f
            drawCheckbox(form.isPressureTestChecked, "בדיקת לחץ נערכה בתאריך:", 540f, y)
            if (form.isPressureTestChecked) { canvas.drawText(form.pressureTestDate, 255f, y, valuePaint) }
            canvas.drawLine(250f, y + 2, 330f, y + 2, dashedLinePaint)
            y += 16f
            canvas.drawText("לחץ הבדיקה 0.250 BAR או לחץ אחר:", 550f, y, textPaint)
            if (form.isPressureTestChecked) { canvas.drawText(form.pressureTestValue, 255f, y, valuePaint) }
            canvas.drawLine(150f, y + 2, 330f, y + 2, dashedLinePaint)
            y += 16f
            drawCheckbox(form.isRegulatorTestChecked, "בדיקת לחץ ווסת:", 540f, y)
            if (form.isRegulatorTestChecked) {
                val rawPressure = form.regulatorPressure
                val pressureNum = rawPressure.toDoubleOrNull()
                if (pressureNum != null && (pressureNum < 25.0 || pressureNum > 39.0)) {
                    val errorPaint = Paint(valuePaint).apply { color = Color.RED; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
                    canvas.drawText(rawPressure, 415f, y, errorPaint)
                    canvas.drawText("(לחץ ווסת לא תקין)", 470f, y + 12f, Paint(errorPaint).apply { textAlign = Paint.Align.RIGHT; textSize = 9f })
                } else {
                    canvas.drawText(rawPressure, 415f, y, valuePaint)
                }
            }
            canvas.drawLine(410f, y + 2, 470f, y + 2, dashedLinePaint)
            canvas.drawText("mbar", 375f, y, textPaint)
            y += 20f
            valuePaint.textAlign = oldValueAlign

            drawSectionTitle("6. בדיקת תוואי וחומרים")
            canvas.drawText("התוואי נעשה בהתאם לתוכניות ובהתאם לתקן ישראלי ת\"י 158 חלק 2", 550f, y, textPaint); y += 13f
            canvas.drawText("חומרי המבנה מתאימים לדרישות התקן ישראלי ת\"י 158 חלק 2", 550f, y, textPaint); y += 15f
            drawCheckbox(form.isRouteNotDoneByChecked, "התוואי לא נעשה ע\"י", 540f, y); y += 15f
            drawCheckbox(form.isRouteDoneByChecked, "התוואי נעשה ע\"י:", 540f, y)
            if (form.isRouteDoneByChecked) {
                canvas.drawText(form.routeDoneBy, 420f, y, valuePaint)
                canvas.drawLine(150f, y + 2, 420f, y + 2, dashedLinePaint)
            }
            y += 20f

            drawSectionTitle("7. הערות ביצוע")
            val remarksPaint = Paint(valuePaint).apply { textAlign = Paint.Align.RIGHT }
            val maxRemarksWidth = 510f
            var remarksY = y
            val paragraphs = form.executionRemarks.split("\n")
            var activePage = page
            if (paragraphs.isEmpty() || paragraphs.all { it.isBlank() }) {
                for (i in 0..3) { canvas.drawLine(40f, remarksY + 2, 550f, remarksY + 2, dashedLinePaint); remarksY += 13f }
            } else {
                for (paragraph in paragraphs) {
                    val words = paragraph.split(" ")
                    var currentLine = ""
                    for (word in words) {
                        if (word.isEmpty()) continue
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (remarksPaint.measureText(testLine) > maxRemarksWidth) {
                            if (remarksY > 780f) {
                                drawFooter(canvas)
                                pdfDocument.finishPage(activePage)
                                currentPageNum++
                                activePage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                                page = activePage
                                canvas = activePage.canvas
                                canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
                                canvas.drawText(headerTitle, 297f, 45f, headerPaint)
                                canvas.drawText(headerPhone, 297f, 60f, subHeaderPaint)
                                remarksY = 110f
                            }
                            canvas.drawText(currentLine.trim(), 550f, remarksY, remarksPaint)
                            canvas.drawLine(40f, remarksY + 2, 550f, remarksY + 2, dashedLinePaint)
                            remarksY += 13f
                            currentLine = word
                        } else { currentLine = testLine }
                    }
                    if (currentLine.isNotEmpty()) {
                        if (remarksY > 780f) {
                            drawFooter(canvas)
                            pdfDocument.finishPage(activePage)
                            currentPageNum++
                            activePage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                            page = activePage
                            canvas = activePage.canvas
                            canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
                            canvas.drawText(headerTitle, 297f, 45f, headerPaint)
                            canvas.drawText(headerPhone, 297f, 60f, subHeaderPaint)
                            remarksY = 110f
                        }
                        canvas.drawText(currentLine.trim(), 550f, remarksY, remarksPaint)
                        canvas.drawLine(40f, remarksY + 2, 550f, remarksY + 2, dashedLinePaint)
                        remarksY += 13f
                    }
                }
            }

            var section8Y = remarksY + 12f
            if (section8Y > 630f) {
                drawFooter(canvas)
                pdfDocument.finishPage(activePage)
                currentPageNum++
                activePage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                page = activePage
                canvas = activePage.canvas
                canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
                canvas.drawText(headerTitle, 297f, 45f, headerPaint)
                canvas.drawText(headerPhone, 297f, 60f, subHeaderPaint)
                section8Y = 110f
            }
            y = section8Y

            drawSectionTitle("8. הצהרת התאמה וחתימות")
            if (form.isStatusConforming) {
                canvas.drawText("✔ המערכת מתאימה לדרישות התקן", 540f, y, Paint(valuePaint).apply { color = Color.parseColor("#065f46"); typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                y += 20f
            } else {
                canvas.drawText("✗ המערכת אינה מתאימה לדרישות התקן", 540f, y, Paint(valuePaint).apply { color = Color.parseColor("#dc2626"); typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                y += 20f
                canvas.drawText("סיבת למה המערכת לא תקינה:", 550f, y, boldTextPaint)
                canvas.drawText(form.nonCompliantReason, 400f, y, valuePaint)
                canvas.drawLine(40f, y + 2f, 400f, y + 2f, dashedLinePaint)
                y += 20f
            }

            canvas.drawText("שם הטכנאי:", 550f, y, textPaint)
            val techNamePaint = Paint(valuePaint).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textSize = 12f }
            canvas.drawText(form.technicianStamp, 490f, y, techNamePaint)
            canvas.drawLine(390f, y + 2, 490f, y + 2, dashedLinePaint)

            canvas.drawText("חתימה:", 360f, y, textPaint)
            val sigUriStr = settingsManager.savedSignatureUri
            if (!sigUriStr.isNullOrEmpty()) {
                try {
                    decodeBitmapWithExifRotation(context, Uri.parse(sigUriStr))?.let { bitmap ->
                        val boxWidth = 170f; val boxHeight = 70f
                        val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat(); val boxRatio = boxWidth / boxHeight
                        var drawWidth = boxWidth; var drawHeight = boxHeight
                        if (imgRatio > boxRatio) { drawWidth = boxWidth; drawHeight = boxWidth / imgRatio } else { drawHeight = boxHeight; drawWidth = boxHeight * imgRatio }
                        val left = 290f - drawWidth; val top = (y - 30f) + (boxHeight - drawHeight) / 2f
                        canvas.drawBitmap(bitmap, null, RectF(left, top, left + drawWidth, top + drawHeight), bitmapPaint)
                        bitmap.recycle()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            canvas.drawLine(100f, y + 2, 300f, y + 2, dashedLinePaint)
            y += 45f

            canvas.drawRect(40f, y - 5f, 550f, y + 95f, Paint().apply { color = Color.rgb(245, 247, 250); style = Paint.Style.FILL })
            canvas.drawRect(40f, y - 5f, 550f, y + 95f, Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f; style = Paint.Style.STROKE })

            canvas.drawText("תעודת זהות הלקוח לאישור:", 530f, y + 15f, textPaint)
            val oldAlignId = valuePaint.textAlign
            valuePaint.textAlign = Paint.Align.LEFT
            canvas.drawText(form.clientIdConfirm, 255f, y + 15f, valuePaint)
            valuePaint.textAlign = oldAlignId
            canvas.drawLine(250f, y + 17f, 380f, y + 17f, dashedLinePaint)

            canvas.drawText("חתימת הלקוח לאישור:", 530f, y + 55f, textPaint)
            if (form.clientSignatureUri.isNotEmpty()) {
                try {
                    decodeBitmapWithExifRotation(context, Uri.parse(form.clientSignatureUri))?.let { bitmap ->
                        canvas.drawBitmap(bitmap, null, RectF(250f, y + 25f, 400f, y + 85f), bitmapPaint)
                        bitmap.recycle()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            canvas.drawLine(250f, y + 72f, 390f, y + 72f, dashedLinePaint)

            drawFooter(canvas)
            pdfDocument.finishPage(activePage)

            val watermarkText = "נורמטיבי 158/4 | תאריך: ${form.date}"
            val extraImages = mutableListOf<Pair<String, String>>()
            if (form.isUnaddressedSite && form.sitePhotoUri.isNotEmpty()) { extraImages.add(Pair(form.sitePhotoUri, "צילום מפה / שטח (נ.צ: ${form.gpsCoordinates})")) }
            form.extraRouteImageUris.split(",").filter { it.isNotEmpty() }.forEachIndexed { index, uri -> extraImages.add(Pair(uri, "צילום תוואי ${index + 1}")) }
            form.remarksImageUris.split(",").filter { it.isNotEmpty() }.forEachIndexed { index, uri -> extraImages.add(Pair(uri, "הערת ביצוע ${index + 1}")) }

            if (extraImages.isNotEmpty()) {
                val chunkedImages = extraImages.chunked(2)
                for (chunkIndex in chunkedImages.indices) {
                    currentPageNum++
                    val pageImages = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                    val canvasImages = pageImages.canvas
                    canvasImages.drawRect(20f, 20f, 575f, 822f, borderPaint)
                    val suffix = if (chunkedImages.size > 1) " (חלק ${chunkIndex + 1})" else ""
                    canvasImages.drawText("נספח מצורף - צילומים ותמונות שטח$suffix", 297f, 50f, headerPaint)
                    val locationHeader = if(form.isUnaddressedSite) form.gpsCoordinates else form.clientCity
                    canvasImages.drawText("לקוח: ${form.clientName} | ישוב: $locationHeader | \u200Eתאריך: \u200E${form.date}", 297f, 75f, subHeaderPaint)

                    fun drawAttachedImage(uriStr: String, label: String, left: Float, top: Float, width: Float, height: Float) {
                        try {
                            decodeBitmapWithExifRotation(context, Uri.parse(uriStr))?.let { rawBitmap ->
                                val bitmap = applyWatermark(rawBitmap, watermarkText)
                                if (rawBitmap != bitmap) rawBitmap.recycle()
                                canvasImages.drawBitmap(bitmap, null, RectF(left, top, left + width, top + height), bitmapPaint)
                                canvasImages.drawText(label, left + width / 2, top + height + 15f, subHeaderPaint)
                                bitmap.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    val currentPair = chunkedImages[chunkIndex]
                    if (currentPair.size == 2) {
                        drawAttachedImage(currentPair[0].first, currentPair[0].second, 45f, 150f, 230f, 300f)
                        drawAttachedImage(currentPair[1].first, currentPair[1].second, 320f, 150f, 230f, 300f)
                    } else {
                        drawAttachedImage(currentPair[0].first, currentPair[0].second, 172.5f, 150f, 250f, 350f)
                    }
                    drawFooter(canvasImages)
                    pdfDocument.finishPage(pageImages)
                }
            }

            val outputDir = if (settingsManager.isAutoSavePdfEnabled) settingsManager.getAutoSaveDir(context) else context.cacheDir
            val rawClientName = form.clientName
            val sanitizedClientName = if (rawClientName.isBlank()) "ללא_שם" else rawClientName.replace(" ", "_")
            val outputFile = File(outputDir, "טופס_158_${sanitizedClientName}_${form.createdAt}.pdf")
            FileOutputStream(outputFile).use { out -> pdfDocument.writeTo(out) }

            val treeUriStr = settingsManager.customStorageTreeUri
            if (settingsManager.isAutoSavePdfEnabled && !treeUriStr.isNullOrEmpty()) {
                try {
                    val treeUri = Uri.parse(treeUriStr)
                    context.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val docFolder = DocumentFile.fromTreeUri(context, treeUri)
                    if (docFolder != null && docFolder.canWrite()) {
                        val fileName = "טופס_158_${sanitizedClientName}_${form.createdAt}.pdf"
                        docFolder.findFile(fileName)?.delete()
                        val createdFile = docFolder.createFile("application/pdf", fileName)
                        if (createdFile != null) {
                            context.contentResolver.openOutputStream(createdFile.uri)?.use { outStream ->
                                FileInputStream(outputFile).use { inStream -> inStream.copyTo(outStream) }
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            return outputFile
        } catch (e: Exception) { e.printStackTrace(); return null } finally { pdfDocument.close() }
    }

    private fun generateModernFormPdf(context: Context, form: GasForm, settingsManager: SettingsManager): File? {
        // [הקוד נשאר כפי שהיה עבור התבנית המודרנית]
        val headerTitle = if (settingsManager.contractorHeader.isNullOrBlank()) "מ.מ מערכות גז" else settingsManager.contractorHeader
        val headerPhone = if (settingsManager.contractorPhone.isNullOrBlank()) "054-6096487" else settingsManager.contractorPhone

        val pdfDocument = PdfDocument()
        try {
            var currentPageNum = 1
            var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            var canvas = page.canvas

            val bgPageColor = Color.parseColor("#f9fafb")
            val textDark = Color.parseColor("#111827")
            val textGray = Color.parseColor("#6b7280")
            val bluePrimary = Color.parseColor("#2563eb")
            val borderGray = Color.parseColor("#e5e7eb")
            val cardBgColor = Color.parseColor("#ffffff")
            val fieldBgColor = Color.parseColor("#f9fafb")
            val fieldBorderColor = Color.parseColor("#d1d5db")
            val chipBgSelected = Color.parseColor("#dbeafe")
            val chipTextSelected = Color.parseColor("#1e40af")
            val chipBorderSelected = Color.parseColor("#bfdbfe")

            val pageBgPaint = Paint().apply { color = bgPageColor; style = Paint.Style.FILL }
            val h1Paint = Paint().apply { color = Color.parseColor("#1f2937"); textSize = 24f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val h2Paint = Paint().apply { color = bluePrimary; textSize = 16f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val metaPaint = Paint().apply { color = textGray; textSize = 12f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.CENTER }
            val cardBgPaint = Paint().apply { color = cardBgColor; style = Paint.Style.FILL }
            val cardBorderPaint = Paint().apply { color = borderGray; style = Paint.Style.STROKE; strokeWidth = 1f }
            val sectionTitlePaint = Paint().apply { color = Color.parseColor("#374151"); textSize = 14f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.RIGHT }
            val circlePaint = Paint().apply { color = bluePrimary; style = Paint.Style.FILL; isAntiAlias = true }
            val circleTextPaint = Paint().apply { color = Color.WHITE; textSize = 12f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER; isAntiAlias = true }
            val labelPaint = Paint().apply { color = textGray; textSize = 10f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.RIGHT; isAntiAlias = true }
            val valuePaint = Paint().apply { color = textDark; textSize = 12f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.RIGHT; isAntiAlias = true }
            val fieldBoxPaint = Paint().apply { color = fieldBgColor; style = Paint.Style.FILL; isAntiAlias = true }
            val fieldBorderLinePaint = Paint().apply { color = fieldBorderColor; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }

            val creditPaint = Paint().apply { color = textGray; textSize = 9f; textAlign = Paint.Align.LEFT }
            val creationDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(form.createdAt))
            val locationStr = if (form.isUnaddressedSite) "אתר בבנייה (GPS מוגדר)" else form.clientCity.takeIf { it.isNotBlank() } ?: "לא צוין"
            val footerText = "טופס הופק ב: $creationDateStr | מיקום: $locationStr"

            fun drawFooter(c: Canvas) {
                c.drawText(footerText, 30f, 830f, creditPaint)
                c.drawText("מערכת נורמטיבי - פותח ע\"י מאור מנחם ©", 30f, 818f, creditPaint)
            }

            fun drawPageHeader() {
                canvas.drawRect(0f, 0f, 595f, 842f, pageBgPaint)
                canvas.drawText(headerTitle, 297f, 45f, h1Paint)
                canvas.drawText(headerPhone, 297f, 65f, h2Paint)
                canvas.drawText("(מס' טופס: ${form.sequentialNumber})", 50f, 45f, Paint().apply { color = Color.RED; textSize = 14f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.LEFT })
                canvas.drawText("טופס הצהרה על התאמה של מתקן גפ\"מ", 297f, 100f, Paint(h1Paint).apply { textSize = 18f })
                canvas.drawText("דרישות התקן 158 חלק 4 | תאריך: ${form.date}", 297f, 120f, metaPaint)
                canvas.drawLine(40f, 130f, 555f, 130f, cardBorderPaint)
            }

            var currentY = 140f

            fun checkNewPage(neededHeight: Float) {
                if (currentY + neededHeight > 790f) {
                    drawFooter(canvas)
                    pdfDocument.finishPage(page)
                    currentPageNum++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                    canvas = page.canvas
                    drawPageHeader()
                    currentY = 140f
                }
            }

            fun drawSectionHeader(number: String, title: String, startY: Float) {
                canvas.drawCircle(538f, startY + 5f, 12f, circlePaint)
                canvas.drawText(number, 538f, startY + 9f, circleTextPaint)
                canvas.drawText(title, 515f, startY + 10f, sectionTitlePaint)
                canvas.drawLine(40f, startY + 20f, 555f, startY + 20f, Paint().apply { color = Color.parseColor("#f3f4f6"); strokeWidth=1f })
            }

            fun drawField(label: String, value: String, rightX: Float, yPos: Float, width: Float) {
                canvas.drawText(label, rightX, yPos, labelPaint)
                val boxTop = yPos + 5f
                val boxBottom = boxTop + 24f
                val leftX = rightX - width
                canvas.drawRoundRect(RectF(leftX, boxTop, rightX, boxBottom), 4f, 4f, fieldBoxPaint)
                canvas.drawRoundRect(RectF(leftX, boxTop, rightX, boxBottom), 4f, 4f, fieldBorderLinePaint)
                canvas.drawText(value, rightX - 5f, boxBottom - 6f, valuePaint)
            }

            fun drawWrappedText(text: String, startX: Float, startY: Float, maxWidth: Float, paint: Paint, lineSpacing: Float = 16f): Float {
                var cY = startY
                val lines = text.split("\n")
                for (paragraph in lines) {
                    val words = paragraph.split(" ")
                    var currentLine = ""
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) > maxWidth) {
                            canvas.drawText(currentLine, startX, cY, paint)
                            cY += lineSpacing
                            currentLine = word
                        } else { currentLine = testLine }
                    }
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine, startX, cY, paint)
                        cY += lineSpacing
                    }
                }
                return cY - startY
            }

            fun drawChip(text: String, isSelected: Boolean, rightX: Float, centerY: Float): Float {
                val chipPaintText = Paint(labelPaint).apply {
                    textSize = 11f; textAlign = Paint.Align.CENTER
                    color = if (isSelected) chipTextSelected else Color.parseColor("#4b5563")
                    if (isSelected) typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val textWidth = chipPaintText.measureText(text)
                val chipWidth = textWidth + 30f
                val leftX = rightX - chipWidth
                val bgPaint = Paint().apply { color = if (isSelected) chipBgSelected else cardBgColor; style = Paint.Style.FILL; isAntiAlias = true }
                val borderPnt = Paint().apply { color = if (isSelected) chipBorderSelected else fieldBorderColor; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }

                canvas.drawRoundRect(RectF(leftX, centerY - 12f, rightX, centerY + 12f), 12f, 12f, bgPaint)
                canvas.drawRoundRect(RectF(leftX, centerY - 12f, rightX, centerY + 12f), 12f, 12f, borderPnt)
                canvas.drawText(text, leftX + (chipWidth / 2f), centerY + 4f, chipPaintText)
                return chipWidth
            }

            drawPageHeader()

            var sectionHeight = 70f
            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("1", "פרטי המתקן ומיקומו", currentY + 15f)

            var chipX = 540f
            val chipY = currentY + 50f
            chipX -= drawChip(if(form.isSharedBuilding) "✔ בית משותף" else "בית משותף", form.isSharedBuilding, chipX, chipY) + 10f
            chipX -= drawChip(if(form.isCommercial) "✔ מסחרי" else "מסחרי", form.isCommercial, chipX, chipY) + 10f
            chipX -= drawChip(if(form.isCylinders12x2) "✔ מכלים 12*2" else "מכלים 12*2", form.isCylinders12x2, chipX, chipY) + 10f
            chipX -= drawChip(if(form.isCylinders48x2) "✔ מכלים 48*2" else "מכלים 48*2", form.isCylinders48x2, chipX, chipY) + 10f
            if (form.isOtherType) drawChip("✔ אחר: ${form.otherTypeText}", true, chipX, chipY) else drawChip("אחר", false, chipX, chipY)
            currentY += sectionHeight + 15f

            sectionHeight = 110f
            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("2", "פרטי לקוח", currentY + 15f)

            drawField("שם לקוח", form.clientName, 540f, currentY + 45f, 240f)
            drawField("טלפון", form.clientPhone, 280f, currentY + 45f, 240f)

            if (form.isUnaddressedSite) {
                drawField("מיקום חלופי (נ.צ GPS)", form.gpsCoordinates.ifEmpty { "לא נדגמו קואורדינטות" }, 540f, currentY + 85f, 500f)
            } else {
                drawField("ישוב", form.clientCity, 540f, currentY + 85f, 160f)
                drawField("רחוב", form.clientStreet, 360f, currentY + 85f, 180f)
                drawField("בניין/דירה", form.clientBuilding, 160f, currentY + 85f, 120f)
            }
            currentY += sectionHeight + 15f

            sectionHeight = if (form.isWorkConnection) 180f else 70f
            if (form.hasAdditionalDevice) sectionHeight += 50f
            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("3", "תיאור העבודה", currentY + 15f)

            chipX = 540f
            var cY = currentY + 50f
            chipX -= drawChip(if(form.isWorkNew) "✔ מתקן חדש" else "מתקן חדש", form.isWorkNew, chipX, cY) + 10f
            chipX -= drawChip(if(form.isWorkAddition) "✔ תוספת למתקן" else "תוספת למתקן", form.isWorkAddition, chipX, cY) + 10f
            chipX -= drawChip(if(form.isWorkRepair) "✔ תיקון נזק/שינוי" else "תיקון נזק/שינוי", form.isWorkRepair, chipX, cY) + 10f
            drawChip(if(form.isWorkConnection) "✔ חיבור מכשיר" else "חיבור מכשיר", form.isWorkConnection, chipX, cY)

            if (form.isWorkConnection) {
                cY += 35f
                drawField("סוג המכשיר", form.connectionDeviceType, 540f, cY, 120f)
                drawField("מותג", form.connectionDeviceBrand, 410f, cY, 120f)
                drawField("דגם", form.connectionDeviceModel, 280f, cY, 120f)
                drawField("סריאלי", form.connectionDeviceSerial, 150f, cY, 110f)

                if (form.hasAdditionalDevice) {
                    cY += 45f
                    drawField("סוג מכשיר 2", form.additionalDeviceType, 540f, cY, 120f)
                    drawField("מותג מכשיר 2", form.additionalDeviceBrand, 410f, cY, 120f)
                    drawField("דגם", form.additionalDeviceModel, 280f, cY, 120f)
                    drawField("סריאלי", form.additionalDeviceSerial, 150f, cY, 110f)
                }

                cY += 45f
                canvas.drawText("צינור גומי תקני עד 3 מ'. חבקים:", 540f, cY + 15f, labelPaint)
                drawField("", form.clampsCount, 390f, cY, 50f)
                canvas.drawText("שנ' ייצור:", 330f, cY + 15f, labelPaint)
                drawField("", form.hoseProductionYear, 280f, cY, 60f)
                canvas.drawText("סוג צינור:", 210f, cY + 15f, labelPaint)
                drawField("", form.hoseType, 160f, cY, 120f)
            }
            currentY += sectionHeight + 15f

            val text4 = "אני הח\"מ מצהיר בזאת כי בדקתי את מע' הגפ\"מ המתואר לעיל חזותית ומצאתי כי הוא עומד בכל דרישות התקן הישראלי 158 חלקים 2 ו-3, לפי העניין."
            val visualHeight = drawWrappedText(text4, 525f, currentY + 50f, 480f, Paint(labelPaint).apply{ color = Color.parseColor("#374151") }) + 60f
            checkNewPage(visualHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + visualHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + visualHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("4", "בדיקה חזותית", currentY + 15f)

            val legalBg = Paint().apply { color = Color.parseColor("#f3f4f6"); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawRoundRect(RectF(40f, currentY + 35f, 540f, currentY + visualHeight - 10f), 4f, 4f, legalBg)
            canvas.drawRect(536f, currentY + 35f, 540f, currentY + visualHeight - 10f, Paint().apply { color = bluePrimary })
            drawWrappedText(text4, 525f, currentY + 52f, 470f, Paint(labelPaint).apply{ color = Color.parseColor("#374151") })
            currentY += visualHeight + 15f

            sectionHeight = 140f
            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("5", "בדיקת לחץ / אטימות לפני הפעלה", currentY + 15f)

            drawField("אטימות ללחץ שימוש בתאריך:", if (form.isLeakTestChecked) form.leakTestDate else "-", 540f, currentY + 45f, 180f)
            drawField("לחץ הבדיקה (mbar ל-15 דק'):", if (form.isLeakTestChecked) form.leakTestPressure else "-", 340f, currentY + 45f, 180f)

            drawField("בדיקת לחץ נערכה בתאריך:", if (form.isPressureTestChecked) form.pressureTestDate else "-", 540f, currentY + 95f, 180f)
            drawField("לחץ הבדיקה (0.250 BAR או אחר):", if (form.isPressureTestChecked) form.pressureTestValue else "-", 340f, currentY + 95f, 180f)
            drawField("לחץ ווסת (mbar):", if (form.isRegulatorTestChecked) form.regulatorPressure else "-", 140f, currentY + 95f, 100f)
            currentY += sectionHeight + 15f

            sectionHeight = 90f
            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("6", "בדיקת תוואי וחומרים", currentY + 15f)

            canvas.drawText("✔ התוואי נעשה בהתאם לתוכניות ולתקן 158 חלק 2", 540f, currentY + 50f, valuePaint)
            canvas.drawText("✔ חומרי המבנה מתאימים לדרישות התקן", 540f, currentY + 70f, valuePaint)

            val routeText = if (form.isRouteDoneByChecked) "נעשה ע\"י: ${form.routeDoneBy}" else if (form.isRouteNotDoneByChecked) "לא נעשה ע\"י הח\"מ" else "לא סומן"
            drawField("מי ביצע את התוואי:", routeText, 250f, currentY + 45f, 210f)
            currentY += sectionHeight + 15f

            val remarksText = if(form.executionRemarks.isBlank()) "אין הערות מיוחדות." else form.executionRemarks
            var testY = currentY + 45f
            val remarksLines = remarksText.split("\n")
            for (paragraph in remarksLines) {
                val words = paragraph.split(" ")
                var currentLine = ""
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (valuePaint.measureText(testLine) > 500f) { testY += 16f; currentLine = word } else { currentLine = testLine }
                }
                if (currentLine.isNotEmpty()) testY += 16f
            }
            sectionHeight = Math.max(70f, (testY - currentY) + 20f)

            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("7", "הערות ביצוע", currentY + 15f)
            drawWrappedText(remarksText, 540f, currentY + 50f, 500f, valuePaint)
            currentY += sectionHeight + 15f

            val reasonHeight = if (!form.isStatusConforming && form.nonCompliantReason.isNotBlank()) 60f else 0f
            sectionHeight = 150f + reasonHeight
            checkNewPage(sectionHeight)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + sectionHeight), 8f, 8f, cardBorderPaint)
            drawSectionHeader("8", "הצהרת התאמה וחתימות", currentY + 15f)

            var sigY = currentY + 45f
            if (form.isStatusConforming) {
                canvas.drawText("✔ המערכת מתאימה לדרישות התקן", 540f, sigY, Paint(valuePaint).apply { color = Color.parseColor("#065f46"); typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
            } else {
                canvas.drawText("✗ המערכת אינה מתאימה לדרישות התקן", 540f, sigY, Paint(valuePaint).apply { color = Color.parseColor("#dc2626"); typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                sigY += 25f
                canvas.drawText("סיבת למה המערכת לא תקינה:", 540f, sigY, labelPaint)
                canvas.drawRoundRect(RectF(40f, sigY + 5f, 540f, sigY + 45f), 4f, 4f, Paint().apply { color = Color.parseColor("#fef2f2"); style = Paint.Style.FILL })
                canvas.drawRoundRect(RectF(40f, sigY + 5f, 540f, sigY + 45f), 4f, 4f, Paint().apply { color = Color.parseColor("#fca5a5"); style = Paint.Style.STROKE; strokeWidth=1f })
                drawWrappedText(form.nonCompliantReason, 530f, sigY + 20f, 480f, valuePaint)
                sigY += 35f
            }

            sigY += 25f
            val boxPnt = Paint().apply { color = fieldBgColor; style = Paint.Style.FILL; isAntiAlias = true }
            val borderPnt = Paint().apply { color = fieldBorderColor; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }
            val bitmapPaint = Paint().apply { isFilterBitmap = true; isAntiAlias = true }

            canvas.drawRoundRect(RectF(310f, sigY, 540f, sigY + 70f), 6f, 6f, boxPnt)
            canvas.drawRoundRect(RectF(310f, sigY, 540f, sigY + 70f), 6f, 6f, borderPnt)
            canvas.drawText("שם הטכנאי:", 530f, sigY + 15f, labelPaint)
            canvas.drawText(form.technicianStamp, 470f, sigY + 15f, Paint(valuePaint).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
            canvas.drawText("חתימה:", 530f, sigY + 35f, labelPaint)
            val sigUriStr = settingsManager.savedSignatureUri
            if (!sigUriStr.isNullOrEmpty()) {
                try {
                    decodeBitmapWithExifRotation(context, Uri.parse(sigUriStr))?.let { bitmap ->
                        canvas.drawBitmap(bitmap, null, RectF(330f, sigY + 25f, 450f, sigY + 65f), bitmapPaint)
                        bitmap.recycle()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            canvas.drawRoundRect(RectF(50f, sigY, 290f, sigY + 70f), 6f, 6f, boxPnt)
            canvas.drawRoundRect(RectF(50f, sigY, 290f, sigY + 70f), 6f, 6f, borderPnt)
            canvas.drawText("ת.ז לקוח:", 280f, sigY + 15f, labelPaint)
            canvas.drawText(form.clientIdConfirm, 230f, sigY + 15f, valuePaint)
            canvas.drawText("חתימה:", 280f, sigY + 35f, labelPaint)
            if (form.clientSignatureUri.isNotEmpty()) {
                try {
                    decodeBitmapWithExifRotation(context, Uri.parse(form.clientSignatureUri))?.let { bitmap ->
                        canvas.drawBitmap(bitmap, null, RectF(70f, sigY + 25f, 190f, sigY + 65f), bitmapPaint)
                        bitmap.recycle()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            drawFooter(canvas)
            pdfDocument.finishPage(page)

            val watermarkText = "נורמטיבי 158/4 | תאריך: ${form.date}"
            val extraImages = mutableListOf<Pair<String, String>>()
            if (form.isUnaddressedSite && form.sitePhotoUri.isNotEmpty()) { extraImages.add(Pair(form.sitePhotoUri, "צילום מפה / שטח (נ.צ: ${form.gpsCoordinates})")) }
            form.extraRouteImageUris.split(",").filter { it.isNotEmpty() }.forEachIndexed { index, uri -> extraImages.add(Pair(uri, "צילום תוואי ${index + 1}")) }
            form.remarksImageUris.split(",").filter { it.isNotEmpty() }.forEachIndexed { index, uri -> extraImages.add(Pair(uri, "הערת ביצוע ${index + 1}")) }

            if (extraImages.isNotEmpty()) {
                val chunkedImages = extraImages.chunked(2)
                for (chunkIndex in chunkedImages.indices) {
                    currentPageNum++
                    val pageImages = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                    val canvasImages = pageImages.canvas
                    canvasImages.drawRect(0f, 0f, 595f, 842f, pageBgPaint)
                    canvasImages.drawRoundRect(RectF(30f, 30f, 565f, 800f), 8f, 8f, cardBgPaint)
                    canvasImages.drawRoundRect(RectF(30f, 30f, 565f, 800f), 8f, 8f, cardBorderPaint)

                    val suffix = if (chunkedImages.size > 1) " (חלק ${chunkIndex + 1})" else ""
                    canvasImages.drawText("נספח מצורף - צילומים ותמונות שטח$suffix", 297f, 60f, h2Paint)
                    val locationHeader = if(form.isUnaddressedSite) form.gpsCoordinates else form.clientCity
                    canvasImages.drawText("לקוח: ${form.clientName} | ישוב: $locationHeader | \u200Eתאריך: \u200E${form.date}", 297f, 80f, metaPaint)
                    canvasImages.drawLine(50f, 95f, 545f, 95f, cardBorderPaint)

                    fun drawAttachedImage(uriStr: String, label: String, left: Float, top: Float, width: Float, height: Float) {
                        try {
                            decodeBitmapWithExifRotation(context, Uri.parse(uriStr))?.let { rawBitmap ->
                                val bitmap = applyWatermark(rawBitmap, watermarkText)
                                if (rawBitmap != bitmap) rawBitmap.recycle()
                                canvasImages.drawBitmap(bitmap, null, RectF(left, top, left + width, top + height), bitmapPaint)
                                canvasImages.drawText(label, left + width / 2, top + height + 20f, Paint(h2Paint).apply{ textSize = 12f; color = textDark })
                                bitmap.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    val currentPair = chunkedImages[chunkIndex]
                    if (currentPair.size == 2) {
                        drawAttachedImage(currentPair[0].first, currentPair[0].second, 50f, 130f, 220f, 320f)
                        drawAttachedImage(currentPair[1].first, currentPair[1].second, 325f, 130f, 220f, 320f)
                    } else {
                        drawAttachedImage(currentPair[0].first, currentPair[0].second, 147.5f, 130f, 300f, 400f)
                    }
                    drawFooter(canvasImages)
                    pdfDocument.finishPage(pageImages)
                }
            }

            val outputDir = if (settingsManager.isAutoSavePdfEnabled) settingsManager.getAutoSaveDir(context) else context.cacheDir
            val rawClientName = form.clientName
            val sanitizedClientName = if (rawClientName.isBlank()) "ללא_שם" else rawClientName.replace(" ", "_")
            val outputFile = File(outputDir, "טופס_158_${sanitizedClientName}_${form.createdAt}.pdf")
            FileOutputStream(outputFile).use { out -> pdfDocument.writeTo(out) }

            val treeUriStr = settingsManager.customStorageTreeUri
            if (settingsManager.isAutoSavePdfEnabled && !treeUriStr.isNullOrEmpty()) {
                try {
                    val treeUri = Uri.parse(treeUriStr)
                    context.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val docFolder = DocumentFile.fromTreeUri(context, treeUri)
                    if (docFolder != null && docFolder.canWrite()) {
                        val fileName = "טופס_158_${sanitizedClientName}_${form.createdAt}.pdf"
                        docFolder.findFile(fileName)?.delete()
                        val createdFile = docFolder.createFile("application/pdf", fileName)
                        if (createdFile != null) {
                            context.contentResolver.openOutputStream(createdFile.uri)?.use { outStream ->
                                FileInputStream(outputFile).use { inStream -> inStream.copyTo(outStream) }
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            return outputFile
        } catch (e: Exception) { e.printStackTrace(); return null } finally { pdfDocument.close() }
    }

    // ==========================================
    // 3. מחולל טופס ד-1: בדיקה תקופתית (מאגר גפ"מ)
    // ==========================================
    fun generatePeriodicFormPdf(context: Context, form: PeriodicGasForm): File? {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        var currentPageNum = 1
        var pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 11f; color = Color.BLACK; textAlign = Paint.Align.RIGHT }
        val boldPaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 11f; color = Color.BLACK; textAlign = Paint.Align.RIGHT }
        val boldPaintCenter = Paint(boldPaint).apply { textAlign = Paint.Align.CENTER }
        val titlePaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 15f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
        val linePaint = Paint().apply { color = Color.parseColor("#BDBDBD"); strokeWidth = 1f; style = Paint.Style.STROKE }
        val borderPaint = Paint().apply { color = Color.BLACK; strokeWidth = 2f; style = Paint.Style.STROKE }

        var yPosition = 120f
        var tableRowIndex = 0

        // הגדרת קווי האורך של הטבלה
        val xLines = floatArrayOf(550f, 220f, 150f, 80f, 10f)

        fun drawPageHeader() {
            canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)

            val headerBgPaint = Paint().apply { color = Color.parseColor("#1565C0"); style = Paint.Style.FILL }
            canvas.drawRect(0f, 0f, 595f, 90f, headerBgPaint)

            val settingsManager = SettingsManager(context)
            val contractorHeader = settingsManager.contractorHeader.takeIf { !it.isNullOrBlank() } ?: "מאור מנחם - קבלן עבודות גז"
            val defaultPhone = settingsManager.contractorPhone.takeIf { !it.isNullOrBlank() } ?: "054-6096487"
            val headerTextPaint = Paint().apply { color = Color.WHITE; textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
            val headerPhonePaint = Paint().apply { color = Color.WHITE; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textAlign = Paint.Align.CENTER }

            canvas.drawText(contractorHeader, 297f, 45f, headerTextPaint)
            canvas.drawText(defaultPhone, 297f, 70f, headerPhonePaint)

            // מספר שוטף
            canvas.drawText("(טופס מס': ${form.sequentialNumber})", 30f, 45f, Paint().apply { color = Color.parseColor("#FFCC00"); textSize = 14f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textAlign = Paint.Align.LEFT })

            canvas.drawText("אישור על בדיקת תקינות למערכת הגז והתאמתה לתקן 158 (חלק 4)", 297f, 115f, titlePaint)
            canvas.drawText("טופס ד-1: דוח בדיקה תקופתית של מאגר גפ\"מ קיים במכלים", 297f, 135f, titlePaint)
        }

        drawPageHeader()
        yPosition = 160f

        val rightMargin = 550f
        val rowHeight = 22f

        canvas.drawText("תאריך בדיקה: ${form.date}", rightMargin, yPosition, boldPaint)
        yPosition += 25f

        canvas.drawRect(10f, yPosition - 15f, 550f, yPosition + 70f, Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL })
        canvas.drawRect(10f, yPosition - 15f, 550f, yPosition + 70f, linePaint)

        canvas.drawText("פרטי בית העסק והמאגר:", rightMargin - 5f, yPosition, boldPaint)
        yPosition += rowHeight
        val addressText = if (form.isUnaddressedSite) "אתר בבנייה/ללא כתובת. נ.צ: ${form.gpsCoordinates}" else "${form.street} ${form.building}, ${form.city}"
        canvas.drawText("שם העסק: ${form.businessName}  |  ח.פ/ת.ז: ${form.businessId}  |  כתובת: $addressText", rightMargin - 5f, yPosition, paint)
        yPosition += rowHeight
        canvas.drawText("איש קשר: ${form.clientName}  |  טלפון: ${form.clientPhone}  |  ספק הגז: ${form.gasProvider}", rightMargin - 5f, yPosition, paint)
        yPosition += rowHeight
        canvas.drawText("צרכנים: ${form.consumersCount} | מכלים: ${form.cylindersCount} | מרכזייה: ${form.manifoldNumber}", rightMargin - 5f, yPosition, paint)

        yPosition += 30f

        fun drawTableHeader() {
            canvas.drawRect(10f, yPosition, 550f, yPosition + 25f, Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.FILL })
            canvas.drawRect(10f, yPosition, 550f, yPosition + 25f, borderPaint)
            for (x in xLines) canvas.drawLine(x, yPosition, x, yPosition + 25f, borderPaint)

            val tableHeaderPaint = Paint(boldPaintCenter).apply { textSize = 12f }
            canvas.drawText("סעיף בדיקה", 385f, yPosition + 17f, tableHeaderPaint)
            canvas.drawText("מתאים", 185f, yPosition + 17f, tableHeaderPaint)
            canvas.drawText("לא מתאים", 115f, yPosition + 17f, tableHeaderPaint)
            canvas.drawText("לא ישים", 45f, yPosition + 17f, tableHeaderPaint)
            yPosition += 25f
        }

        fun drawCheckRow(text: String, state: String) {
            val h = 28f

            if (yPosition + h > 780f) {
                pdfDocument.finishPage(page)
                currentPageNum++
                pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawPageHeader()
                yPosition = 140f
                drawTableHeader()
            }

            if (state == "HEADER") {
                canvas.drawRect(10f, yPosition, 550f, yPosition + h, Paint().apply { color = Color.parseColor("#EEEEEE"); style = Paint.Style.FILL })
                canvas.drawRect(10f, yPosition, 550f, yPosition + h, borderPaint)
                canvas.drawText(text, 540f, yPosition + 19f, Paint(boldPaint).apply { textSize = 12f })
                yPosition += h
                tableRowIndex = 0
                return
            }

            // צביעת שורות (זברה או אדום ללקויים)
            if (state == "FAIL") {
                canvas.drawRect(10f, yPosition, 550f, yPosition + h, Paint().apply { color = Color.parseColor("#FFEEEE"); style = Paint.Style.FILL })
            } else {
                val rowColor = if (tableRowIndex % 2 == 0) Color.parseColor("#F9F9F9") else Color.WHITE
                canvas.drawRect(10f, yPosition, 550f, yPosition + h, Paint().apply { color = rowColor; style = Paint.Style.FILL })
            }
            tableRowIndex++

            canvas.drawRect(10f, yPosition, 550f, yPosition + h, linePaint)
            for (x in xLines) canvas.drawLine(x, yPosition, x, yPosition + h, linePaint)

            var safeText = text
            if (paint.measureText(text) > 320f) safeText = text.substring(0, 45) + "..."
            canvas.drawText(safeText, 540f, yPosition + 19f, paint)

            val centerPass = 185f
            val centerFail = 115f
            val centerNA = 45f

            when (state) {
                "PASS" -> canvas.drawText("V", centerPass, yPosition + 19f, Paint(boldPaintCenter).apply { color = Color.parseColor("#007A00"); textSize = 14f })
                "FAIL" -> canvas.drawText("X", centerFail, yPosition + 19f, Paint(boldPaintCenter).apply { color = Color.RED; textSize = 14f })
                "NA" -> canvas.drawText("-", centerNA, yPosition + 19f, Paint(boldPaintCenter).apply { color = Color.GRAY; textSize = 14f })
            }
            yPosition += h
        }

        drawTableHeader()
        drawCheckRow("1. בחינה חזותית של המאגר", "HEADER")
        drawCheckRow("1.1.1 במקום פתוח ומאוורר. לא במפלס נמוך/מגורים", form.checkLocationOpen)
        drawCheckRow("1.1.2.1 מרחק 0.7 מ' ממקור חום/ניצוצות", form.checkSafetyDistances07Heat)
        drawCheckRow("1.1.2.2 מרחק 1.7 מ' מאש גלויה", form.checkSafetyDistances17Fire)
        drawCheckRow("1.1.2.3 0.5מ' מבורות/תאים סגורים (אלא אם מוגבהים)", form.checkSafetyDistances05Pits)
        drawCheckRow("1.1.2.4 3מ' מבורות ופתחי ניקוז פתוחים", form.checkSafetyDistances3Drainage)
        drawCheckRow("1.1.2.5 1.2מ' מפתח בניין (אלא אם סף גבוה ב-0.5מ')", form.checkSafetyDistances12Building)
        drawCheckRow("1.1.2.6 3מ' מפתחי מפלס נמוך", form.checkSafetyDistances3LowLevel)
        drawCheckRow("1.2 הווסת והסעפת מקובעים כראוי", form.checkRegulatorSecured)
        drawCheckRow("1.3 יש שילוט אזהרה (מתלקח, סמל, ספק)", form.checkWarningSigns)
        drawCheckRow("1.4 במתקן מים, מובטחת התזה על כל המכלים", form.checkWaterSprinklers)
        drawCheckRow("1.5.1 אם בחדר: בחדר יש עד 20 מכלים", form.checkGasRoomMax20)
        drawCheckRow("1.5.2 אם בחדר: תאורה בתקרה, מפסק בחוץ", form.checkGasRoomLighting)
        drawCheckRow("1.5.3 אם בחדר: ללא חומרים דליקים", form.checkGasRoomNoFlammables)
        drawCheckRow("1.6.1 אם במכלאה: במכלאה יש עד 20 מכלים", form.checkCageMax20)
        drawCheckRow("1.6.2 אם במכלאה: המכלאה מגודרת ומאווררת", form.checkCageVentilated)
        drawCheckRow("1.7 המאספים (רמפות) יציבים ולכל אחד ברז ניתוק", form.checkRampsSecured)

        drawCheckRow("2. מערכת הצינורות המשותפת", "HEADER")
        drawCheckRow("2.1 שסתום לרעידת אדמה בקו לחץ ביניים", form.checkEarthquakeValve)
        drawCheckRow("2.2 השסתום מפולס והתקנתו תקינה", form.checkEarthquakeValveSecured)
        drawCheckRow("2.3 ברז ניתוק ראשי נגיש ומשולט בכניסה לבניין", form.checkMainValveAccessible)
        drawCheckRow("2.4 שסתומי פריקה מחוברים לאוויר חוץ כחוק", form.checkDischargeValves)
        drawCheckRow("2.5 לחץ הגז בפנים המבנה אינו גדול מ-1.4 בר", form.checkPressureUpTo1_4)
        drawCheckRow("2.6 הצנרת ומרכיביה מקובעים", form.checkPipingSecured)
        drawCheckRow("2.7 מוצאים ללא שימוש קבוע סגורים בפקק/ברז", form.checkUnusedOutletsPlugged)

        // סגירת טבלה תחתונה
        canvas.drawLine(10f, yPosition, 550f, yPosition, borderPaint)
        yPosition += 25f

        // בדיקות אטימות
        canvas.drawText("3. בדיקות אטימות ולחץ", rightMargin, yPosition, Paint(boldPaint).apply { textSize = 13f; isUnderlineText = true })
        yPosition += 20f

        val leakText = if (form.isLeakFoundPrimary) "נמצאה דליפה! מיקום: ${form.leakLocationDetails}" else "תקין. לא נמצאה דליפה בבדיקת נוזל ראשונית."
        val leakPaint = if (form.isLeakFoundPrimary) Paint(boldPaint).apply { color = Color.RED } else paint
        canvas.drawText("אטימות לחץ ראשוני: $leakText", rightMargin, yPosition, leakPaint)
        yPosition += rowHeight

        val pressureKeepText = if (form.isIntermediatePressureKept) "הלחץ נשמר ✓" else "הלחץ לא נשמר ✗"
        canvas.drawText("לחץ ביניים (15 דק'): ${form.intermediatePressureValue} mbar | $pressureKeepText", rightMargin, yPosition, paint)

        // הערות ליקויים - בעיצוב החדש והבולט
        if (form.failedReasonsJson.isNotBlank() && form.failedReasonsJson != "{}") {
            yPosition += 25f
            if (yPosition > 700f) {
                pdfDocument.finishPage(page)
                currentPageNum++
                pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawPageHeader()
                yPosition = 140f
            }

            canvas.drawText("הערות ליקויים (פירוט סעיפים לא תקינים):", rightMargin, yPosition, Paint(boldPaint).apply { textSize = 14f; color = Color.RED })
            yPosition += 20f

            try {
                val json = org.json.JSONObject(form.failedReasonsJson)
                json.keys().forEach { key ->
                    val reason = json.getString(key)

                    val boxTop = yPosition
                    var boxBottom = yPosition + 50f
                    val boxRight = 550f
                    val boxLeft = 10f

                    val innerLines = mutableListOf<String>()
                    val words = reason.split(" ")
                    var currentLine = ""
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) > (boxRight - boxLeft - 20f)) {
                            innerLines.add(currentLine)
                            currentLine = word
                        } else {
                            currentLine = testLine
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        innerLines.add(currentLine)
                    }

                    val textHeightNeeded = innerLines.size * 18f + 35f
                    if (textHeightNeeded > 50f) {
                        boxBottom = yPosition + textHeightNeeded
                    }

                    if (boxBottom > 780f) {
                        pdfDocument.finishPage(page); currentPageNum++; pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create(); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; drawPageHeader();
                        yPosition = 140f
                        boxBottom = yPosition + textHeightNeeded
                    }

                    // ציור מסגרת הליקוי האדומה
                    val rect = RectF(boxLeft, boxTop, boxRight, boxBottom)
                    canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL })
                    canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 1.5f })

                    // כותרת פנימית
                    val labelPaint = Paint(paint).apply { color = Color.RED; textSize = 10f }
                    canvas.drawText("סעיף $key:", boxRight - 10f, boxTop + 18f, labelPaint)

                    // התוכן עצמו
                    var textY = boxTop + 35f
                    for (line in innerLines) {
                        canvas.drawText(line, boxRight - 10f, textY, paint)
                        textY += 18f
                    }

                    yPosition = boxBottom + 15f
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        yPosition += 20f
        if (yPosition > 700f) {
            pdfDocument.finishPage(page)
            currentPageNum++
            pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawPageHeader()
            yPosition = 140f
        }

        // סיכום סטטוס והערות ביצוע
        canvas.drawText("4. סיכום מבצע הבדיקה", rightMargin, yPosition, Paint(boldPaint).apply { textSize = 13f; isUnderlineText = true })
        yPosition += 20f

        val statusMessage = when (form.finalStatus) {
            "OK" -> "המתקן נמצא תקין בהתאם לדרישות התקן."
            "DEFECTS" -> "נמצאו ליקויים. יש לתקן עד תאריך: ${form.defectsFixByDate}"
            "DISCONNECTED" -> "אזהרה: הספקת הגז נותקה עקב ליקויים חמורים!"
            else -> "טרם הוגדר סטטוס."
        }
        val finalStatusPaint = if (form.finalStatus == "OK") Paint(boldPaint).apply { color = Color.parseColor("#007A00"); textSize=14f } else Paint(boldPaint).apply { color = Color.RED; textSize=14f }
        canvas.drawText(statusMessage, rightMargin, yPosition, finalStatusPaint)

        if (form.executionRemarks.isNotBlank()) {
            yPosition += 25f
            canvas.drawText("הערות מסכמות: ${form.executionRemarks}", rightMargin, yPosition, paint)
        }

        // חתימות
        yPosition += 40f
        if (yPosition > 700f) { pdfDocument.finishPage(page); currentPageNum++; pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create(); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; drawPageHeader(); yPosition = 140f }

        canvas.drawLine(10f, yPosition, 550f, yPosition, borderPaint)
        yPosition += 20f
        canvas.drawText("חתימת מבצע הבדיקה", rightMargin, yPosition, boldPaint)
        canvas.drawText("חתימת הלקוח / אחראי המאגר", 200f, yPosition, boldPaint)

        yPosition += rowHeight
        canvas.drawText("שם: ${form.technicianName} | רישיון: ${form.technicianLicense}", rightMargin, yPosition, paint)
        canvas.drawText("שם החותם: ${form.clientNameConfirm}", 200f, yPosition, paint)

        if (form.clientSignatureUri.isNotBlank()) {
            try {
                decodeBitmapWithExifRotation(context, Uri.parse(form.clientSignatureUri))?.let { bitmap ->
                    canvas.drawBitmap(bitmap, null, RectF(50f, yPosition + 10f, 200f, yPosition + 60f), Paint().apply { isFilterBitmap = true })
                    bitmap.recycle()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        pdfDocument.finishPage(page)

        // הוספת עמודי נספחים
        if (form.extraImagesUris.isNotBlank()) {
            val extraUris = form.extraImagesUris.split(",").filter { it.isNotBlank() }
            if (extraUris.isNotEmpty()) {
                currentPageNum++
                var appendixPage = pdfDocument.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                var appendixCanvas = appendixPage.canvas
                drawPageHeader()
                var imgYPosition = 140f
                appendixCanvas.drawText("נספחים ותמונות - טופס ${form.sequentialNumber}", 297f, imgYPosition, titlePaint)
                imgYPosition += 40f

                for (uriStr in extraUris) {
                    if (imgYPosition > 600f) {
                        pdfDocument.finishPage(appendixPage)
                        currentPageNum++
                        appendixPage = pdfDocument.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create())
                        appendixCanvas = appendixPage.canvas
                        drawPageHeader()
                        imgYPosition = 140f
                    }
                    try {
                        decodeBitmapWithExifRotation(context, Uri.parse(uriStr))?.let { bitmap ->
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val targetWidth = 400
                            val targetHeight = (targetWidth / aspectRatio).toInt()
                            val xPos = (595f - targetWidth) / 2f
                            appendixCanvas.drawBitmap(bitmap, null, RectF(xPos, imgYPosition, xPos + targetWidth, imgYPosition + targetHeight), Paint().apply { isFilterBitmap = true })
                            bitmap.recycle()
                            imgYPosition += targetHeight + 20f
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                pdfDocument.finishPage(appendixPage)
            }
        }

        return try {
            val dir = File(context.filesDir, "pdfs").apply { if (!exists()) mkdirs() }
            val file = File(dir, "PeriodicForm_${form.sequentialNumber}_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}