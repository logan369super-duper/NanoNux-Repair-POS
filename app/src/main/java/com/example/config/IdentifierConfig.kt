package com.example.config

/**
 * Global configuration for the Serial / VIN / Identifier label.
 * To change the label throughout the entire application, simply modify this constant.
 */
object IdentifierConfig {
    const val LABEL = "IMEI"
    
    // Derived helper strings
    val LABEL_OPTIONAL = "$LABEL (Optional)"
    val SCAN_INSTRUCTION = "Point camera at item $LABEL or barcode"
    val TYPE_MANUALLY_LABEL = "Or Type $LABEL Manually"
    val RECEIPT_PREFIX = "$LABEL: "
}
