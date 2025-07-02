package com.iptv.player.data.model

/**
 * نموذج بيانات للاحتفاظ بنتائج التحقق المتقدمة من Xtream Codes.
 */
data class XtreamValidationDetails(
    val isValid: Boolean,
    val endpoint: String? = null,
    val realUrl: String? = null,
    val status: String? = null,
    val expiryDate: String? = null,
    val maxConnections: Int? = null,
    val activeConnections: Int? = null,
    val timezone: String? = null,
    val generatedM3uUrl: String? = null,
    val rawResponse: String? = null
)