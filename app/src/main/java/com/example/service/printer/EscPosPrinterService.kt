package com.example.service.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.local.entity.ServiceOrderEntity
import com.example.data.local.entity.ShopSettingsEntity
import com.example.data.local.entity.TransactionLogEntity
import com.example.config.IdentifierConfig
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EscPosPrinterService {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun generateClaimTicketReceipt(
        order: ServiceOrderEntity,
        shop: ShopSettingsEntity?
    ): String {
        val shopName = shop?.shopName ?: "REPAIR & SERVICE SHOP"
        val shopAddress = shop?.address ?: "Tech & Repair Service Center"
        val shopPhone = shop?.phone ?: "Ph: (555) 000-0000"
        val dateStr = dateFormat.format(Date(order.createdAt))
        val showLogo = shop?.showLogoInPrintedInvoice ?: true

        val builder = StringBuilder()
        builder.appendLine("=================================")
        if (showLogo) {
            builder.appendLine("        📷 [ SHOP LOGO ]         ")
            builder.appendLine("=================================")
        }
        builder.appendLine(centerText(shopName, 33))
        builder.appendLine(centerText(shopAddress, 33))
        builder.appendLine(centerText(shopPhone, 33))
        builder.appendLine("=================================")
        builder.appendLine("        REPAIR CLAIM TICKET       ")
        builder.appendLine("=================================")
        builder.appendLine("Ticket No : ${order.ticketNumber}")
        builder.appendLine("Date      : $dateStr")
        builder.appendLine("Customer  : ${order.customerName}")
        builder.appendLine("Phone     : ${order.customerPhone}")
        builder.appendLine("---------------------------------")
        builder.appendLine("Item      : ${order.deviceBrand} ${order.deviceModel}")
        builder.appendLine("${IdentifierConfig.LABEL.padEnd(10)}: ${order.serialImei}")
        builder.appendLine("Issue     : ${order.issueType}")
        builder.appendLine("Notes     : ${order.issueDescription.take(30)}")
        builder.appendLine("---------------------------------")
        builder.appendLine(formatLine("Est. Total:", "Ks ${String.format(Locale.US, "%.2f", order.estimatedCost)}"))
        builder.appendLine(formatLine("Deposit Paid:", "Ks ${String.format(Locale.US, "%.2f", order.depositPaid)}"))
        val balanceDue = (order.estimatedCost - order.depositPaid).coerceAtLeast(0.0)
        builder.appendLine(formatLine("Est. Balance Due:", "Ks ${String.format(Locale.US, "%.2f", balanceDue)}"))
        builder.appendLine("=================================")
        builder.appendLine("     TERMS & CONDITIONS          ")
        builder.appendLine("- Claim ticket required for pickup.")
        builder.appendLine("- Items left > 30 days subject to policy.")
        builder.appendLine("- Diagnostic fee non-refundable.")
        builder.appendLine("=================================")
        builder.appendLine("       Thank you for choosing     ")
        builder.appendLine(centerText(shopName, 33))
        builder.appendLine("=================================")
        return builder.toString()
    }

    fun generateFinalSalesReceipt(
        transaction: TransactionLogEntity,
        shop: ShopSettingsEntity?
    ): String {
        val shopName = shop?.shopName ?: "REPAIR & SERVICE POS"
        val shopAddress = shop?.address ?: "Tech Service Store"
        val shopPhone = shop?.phone ?: "Ph: (555) 000-0000"
        val dateStr = dateFormat.format(Date(transaction.timestamp))
        val showLogo = shop?.showLogoInPrintedInvoice ?: true
        val showTax = shop?.showTaxInPrintedInvoice ?: true

        val builder = StringBuilder()
        builder.appendLine("=================================")
        if (showLogo) {
            builder.appendLine("        📷 [ SHOP LOGO ]         ")
            builder.appendLine("=================================")
        }
        builder.appendLine(centerText(shopName, 33))
        builder.appendLine(centerText(shopAddress, 33))
        builder.appendLine(centerText(shopPhone, 33))
        builder.appendLine("=================================")
        builder.appendLine("          OFFICIAL RECEIPT       ")
        builder.appendLine("=================================")
        builder.appendLine("Tx No   : ${transaction.transactionNumber}")
        builder.appendLine("Date    : $dateStr")
        builder.appendLine("Cashier : ${transaction.cashierName}")
        if (!transaction.customerName.isNullOrBlank()) {
            builder.appendLine("Customer: ${transaction.customerName}")
        }
        builder.appendLine("---------------------------------")
        builder.appendLine("Items / Services:")
        builder.appendLine(transaction.itemsJson)
        builder.appendLine("---------------------------------")
        builder.appendLine(formatLine("Subtotal:", "Ks ${String.format(Locale.US, "%.2f", transaction.subtotal)}"))
        if (transaction.discount > 0) {
            builder.appendLine(formatLine("Discount:", "-Ks ${String.format(Locale.US, "%.2f", transaction.discount)}"))
        }
        if (showTax && transaction.tax > 0) {
            builder.appendLine(formatLine("Tax:", "Ks ${String.format(Locale.US, "%.2f", transaction.tax)}"))
        }
        builder.appendLine("=================================")
        val displayTotal = if (showTax) transaction.totalAmount else (transaction.subtotal - transaction.discount).coerceAtLeast(0.0)
        builder.appendLine(formatLine("TOTAL PAID:", "Ks ${String.format(Locale.US, "%.2f", displayTotal)}"))
        builder.appendLine(formatLine("Payment Method:", transaction.paymentMethod))
        builder.appendLine("=================================")
        builder.appendLine("       Thank you for your business! ")
        builder.appendLine("=================================")
        return builder.toString()
    }

    /**
     * Renders text receipt string into an Android Bitmap (384px width for 58mm ESC/POS printer).
     * Adjusts font size before layout conversion to Black & White image.
     */
    fun generateReceiptBitmap(text: String, widthDots: Int = 384, fontSizeSp: Float = 18f): Bitmap {
        val lines = text.split("\n")
        val sidePadding = 12f
        val topPadding = 24f
        val bottomPadding = 24f
        val lineSpacingExtra = 6f

        val paint = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textSize = fontSizeSp * 1.15f
        }

        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) + lineSpacingExtra
        val totalHeight = (topPadding + (lines.size * lineHeight) + bottomPadding).toInt().coerceAtLeast(100)

        val bitmap = Bitmap.createBitmap(widthDots, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var currentY = topPadding - fontMetrics.ascent
        for (line in lines) {
            canvas.drawText(line, sidePadding, currentY, paint)
            currentY += lineHeight
        }

        return bitmap
    }

    /**
     * Converts a bitmap into a monochrome black & white bitmap with thresholding.
     */
    fun convertToMonochrome(bitmap: Bitmap, threshold: Int = 128): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = if (luminance < threshold) Color.BLACK else Color.WHITE
        }

        bwBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bwBitmap
    }

    /**
     * Converts a monochrome Bitmap into ESC/POS GS v 0 raster bit image command byte stream.
     */
    fun bitmapToEscPosRasterBytes(bitmap: Bitmap, chunkSize: Int = 256): ByteArray {
        val bwBitmap = convertToMonochrome(bitmap)
        val width = bwBitmap.width
        val height = bwBitmap.height
        val widthBytes = (width + 7) / 8

        val outputStream = ByteArrayOutputStream()

        // Initialize printer: ESC @ (1B 40)
        outputStream.write(byteArrayOf(0x1B, 0x40))
        // Set line spacing to 0: ESC 3 0 (1B 33 00)
        outputStream.write(byteArrayOf(0x1B, 0x33, 0x00))

        val pixels = IntArray(width * height)
        bwBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var y = 0
        while (y < height) {
            val currentChunkHeight = minOf(chunkSize, height - y)

            // GS v 0 mode xL xH yL yH
            val xL = (widthBytes and 0xFF).toByte()
            val xH = ((widthBytes shr 8) and 0xFF).toByte()
            val yL = (currentChunkHeight and 0xFF).toByte()
            val yH = ((currentChunkHeight shr 8) and 0xFF).toByte()

            outputStream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH))

            for (row in 0 until currentChunkHeight) {
                val pixelRowY = y + row
                for (colByte in 0 until widthBytes) {
                    var byteVal = 0
                    for (bit in 0 until 8) {
                        val pixelX = colByte * 8 + bit
                        if (pixelX < width) {
                            val pixelColor = pixels[pixelRowY * width + pixelX]
                            val isBlack = (pixelColor and 0xFFFFFF) == 0 || (Color.red(pixelColor) < 128)
                            if (isBlack) {
                                byteVal = byteVal or (1 shl (7 - bit))
                            }
                        }
                    }
                    outputStream.write(byteVal)
                }
            }
            y += currentChunkHeight
        }

        // Reset line spacing: ESC 2 (1B 32)
        outputStream.write(byteArrayOf(0x1B, 0x32))
        // Feed lines: ESC d 4
        outputStream.write(byteArrayOf(0x1B, 0x64, 0x04))
        // Cut paper: GS V 66 0
        outputStream.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))

        return outputStream.toByteArray()
    }

    /**
     * Formats receipt text into ESC/POS binary bytes by first generating a Black & White image,
     * ensuring font-independent and pixel-perfect printing across all Bluetooth thermal printers.
     */
    fun formatToEscPosBytes(text: String, fontSizeSp: Float = 18f): ByteArray {
        val bitmap = generateReceiptBitmap(text, widthDots = 384, fontSizeSp = fontSizeSp)
        return bitmapToEscPosRasterBytes(bitmap)
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun formatLine(left: String, right: String, width: Int = 33): String {
        val totalSpace = width - left.length - right.length
        if (totalSpace <= 0) return "$left $right"
        return left + " ".repeat(totalSpace) + right
    }
}

