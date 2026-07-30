package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.service.printer.EscPosPrinterService

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReceiptPreviewDialog(
    receiptText: String,
    title: String = "Thermal Receipt Preview",
    fontSizeSp: Float = 18f,
    onDismiss: () -> Unit,
    onPrint: () -> Unit = {}
) {
    var isPrinted by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(true) }

    val printerService = remember { EscPosPrinterService() }
    val bwBitmap = remember(receiptText, fontSizeSp) {
        val rawBitmap = printerService.generateReceiptBitmap(receiptText, widthDots = 384, fontSizeSp = fontSizeSp)
        printerService.convertToMonochrome(rawBitmap)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_dialog_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mode toggle & mode indicator
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = showImagePreview,
                        onClick = { showImagePreview = true },
                        label = { Text("B&W Image (ESC/POS)") },
                        leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("mode_bw_image_chip")
                    )
                    FilterChip(
                        selected = !showImagePreview,
                        onClick = { showImagePreview = false },
                        label = { Text("Raw Text Layout") },
                        leadingIcon = { Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("mode_raw_text_chip")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ Image Mode Active: Receipt converted to B&W Bitmap to prevent printer font & layout glitches.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Thermal Paper Roll Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .background(Color(0xFFFFFDF5), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (showImagePreview) {
                        Image(
                            bitmap = bwBitmap.asImageBitmap(),
                            contentDescription = "Monochrome Thermal Receipt Image",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = receiptText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Black,
                            lineHeight = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (isPrinted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ GS v 0 Raster Image byte stream generated & sent to Thermal Printer!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_receipt_button")
                    ) {
                        Text("Done")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isPrinted = true
                            onPrint()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("send_to_printer_button")
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print B&W Image")
                    }
                }
            }
        }
    }
}

