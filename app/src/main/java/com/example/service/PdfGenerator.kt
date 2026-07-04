package com.example.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.AppSettings
import com.example.data.Customer
import com.example.data.ExtraService
import com.example.data.MovementOrder
import com.example.data.Payment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        settings: AppSettings,
        customer: Customer,
        order: MovementOrder,
        extraServices: List<ExtraService>,
        payment: Payment?,
        onComplete: (File?) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size: 595 x 842 pt
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }

            // Draw Decorative Header Background (Slate/Dark Theme Accent)
            val headerPaint = Paint().apply {
                color = Color.rgb(33, 43, 54) // Cosmic Slate Theme Background
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 120f, headerPaint)

            // Header Arabic Typography (App Title & Metadata)
            paint.apply {
                color = Color.WHITE
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("فاتورة حركة أسطول", 560f, 50f, paint)

            paint.apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText(settings.ownerName.ifEmpty { "شركة مسار اللوجستية" }, 560f, 75f, paint)
            canvas.drawText("هاتف: ${settings.phoneNumber.ifEmpty { "091-XXXXXXX" }}", 560f, 95f, paint)

            // Left Header Info (Invoice Number & Date)
            paint.apply {
                color = Color.WHITE
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("Invoice No: ${order.orderNumber}", 35f, 45f, paint)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val formattedDate = dateFormat.format(Date(order.date))
            canvas.drawText("Date: $formattedDate", 35f, 65f, paint)
            canvas.drawText("Status: ${order.status}", 35f, 85f, paint)

            // Divider Line
            val dividerPaint = Paint().apply {
                color = Color.rgb(224, 224, 224)
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(35f, 135f, 560f, 135f, dividerPaint)

            // Section: Customer Details (العميل)
            paint.apply {
                color = Color.BLACK
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("تفاصيل العميل / Customer Details:", 560f, 160f, paint)

            paint.apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("اسم العميل: ${customer.name}", 560f, 180f, paint)
            canvas.drawText("الهاتف: ${customer.phone}", 560f, 200f, paint)
            if (customer.email.isNotEmpty()) {
                canvas.drawText("البريد الإلكتروني: ${customer.email}", 560f, 220f, paint)
            }

            canvas.drawLine(35f, 235f, 560f, 235f, dividerPaint)

            // Section: Trip details (تفاصيل الرحلة)
            paint.apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("تفاصيل أمر الحركة / Movement Details:", 560f, 260f, paint)

            paint.apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("نوع الحركة: ${if (order.movementType == "INTERNAL") "داخلية" else "خارجية"}", 560f, 280f, paint)
            canvas.drawText("مسار الرحلة: من (${order.origin}) إلى (${order.destination})", 560f, 300f, paint)
            canvas.drawText("نوع الرحلة: ${if (order.tripType == "ONE_WAY") "ذهاب فقط" else "ذهاب وعودة"}", 560f, 320f, paint)
            if (order.tripType == "ROUND_TRIP" && order.returnDetails.isNotEmpty()) {
                canvas.drawText("تفاصيل العودة: ${order.returnDetails}", 560f, 340f, paint)
            }
            if (order.notes.isNotEmpty()) {
                canvas.drawText("ملاحظات: ${order.notes}", 560f, 360f, paint)
            }

            canvas.drawLine(35f, 380f, 560f, 380f, dividerPaint)

            // Section: Financial Breakdown (الأسعار والتفاصيل المالية)
            paint.apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("البيان المالي / Financial Summary:", 560f, 405f, paint)

            paint.apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            var currentY = 430f
            // Base Price
            canvas.drawText("السعر الأساسي للرحلة:", 560f, currentY, paint)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("د.ل ${String.format(Locale.US, "%.2f", order.basePrice)}", 35f, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT

            // Extra Services
            for (service in extraServices) {
                currentY += 20f
                canvas.drawText("+ خدمة إضافية (${service.name}):", 560f, currentY, paint)
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("د.ل ${String.format(Locale.US, "%.2f", service.price)}", 35f, currentY, paint)
                paint.textAlign = Paint.Align.RIGHT
            }

            // Discount
            val discountVal = payment?.discount ?: 0.0
            if (discountVal > 0.0) {
                currentY += 20f
                canvas.drawText("- الخصم الممنوح:", 560f, currentY, paint)
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("د.ل ${String.format(Locale.US, "%.2f", discountVal)}", 35f, currentY, paint)
                paint.textAlign = Paint.Align.RIGHT
            }

            canvas.drawLine(35f, currentY + 10f, 560f, currentY + 10f, dividerPaint)
            currentY += 30f

            // Total Amount
            paint.apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("الإجمالي الكلي / Final Total:", 560f, currentY, paint)
            paint.textAlign = Paint.Align.LEFT
            val servicesTotal = extraServices.sumOf { it.price }
            val finalTotal = order.basePrice + servicesTotal - discountVal
            canvas.drawText("د.ل ${String.format(Locale.US, "%.2f", finalTotal)}", 35f, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT

            // Payment Mode Info
            currentY += 25f
            paint.apply {
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            if (payment != null) {
                val methodDesc = when (payment.paymentMethod) {
                    "CASH" -> "نقداً (Cash)"
                    "BANK_TRANSFER" -> "تحويل مصرفي (Bank Transfer)"
                    "SPLIT" -> "دفع مشترك (Split): نقداً د.ل ${payment.cashAmount} + مصرفي د.ل ${payment.bankAmount}"
                    else -> "دفع مسبق"
                }
                canvas.drawText("طريقة الدفع / Payment Method: $methodDesc", 560f, currentY, paint)
            }

            canvas.drawLine(35f, currentY + 15f, 560f, currentY + 15f, dividerPaint)
            currentY += 45f

            // Stamp and Signature Section (Footer)
            // Draw Digital Stamp (Circle layout on bottom right)
            val stampCenterX = 450f
            val stampCenterY = currentY + 60f
            val stampPaint = Paint().apply {
                color = Color.rgb(0, 102, 204) // Safe Blue color for approved stamp
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawCircle(stampCenterX, stampCenterY, 45f, stampPaint)
            stampPaint.strokeWidth = 0.8f
            canvas.drawCircle(stampCenterX, stampCenterY, 41f, stampPaint)

            // Stamp Texts
            val textPaintStamp = Paint().apply {
                color = Color.rgb(0, 102, 204)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("مــسـار", stampCenterX, stampCenterY - 15f, textPaintStamp)
            canvas.drawText("MASAR LOGISTICS", stampCenterX, stampCenterY, textPaintStamp)
            canvas.drawText("مــعـتـمـد * APPROVED", stampCenterX, stampCenterY + 15f, textPaintStamp)

            // Drawing Signature area on the left
            paint.apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("توقيع المدير المعتمد / Authorized Sign", 35f, currentY + 15f, paint)
            canvas.drawLine(35f, currentY + 65f, 210f, currentY + 65f, dividerPaint)
            paint.apply {
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("Approved digital stamp automatically appended.", 35f, currentY + 80f, paint)

            pdfDocument.finishPage(page)

            // Write PDF file
            val storageDir = context.getExternalFilesDir(null) ?: context.filesDir
            val invoiceFile = File(storageDir, "invoice_${order.orderNumber}.pdf")
            val outputStream = FileOutputStream(invoiceFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            onComplete(invoiceFile)
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }
}
