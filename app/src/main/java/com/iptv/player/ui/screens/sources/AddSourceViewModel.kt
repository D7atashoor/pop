package com.iptv.player.ui.screens.sources

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.*
import com.iptv.player.data.network.SourceValidationService
import com.iptv.player.data.repository.SourceRepository
import com.iptv.player.data.network.StalkerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class AddSourceViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val validationService: SourceValidationService,
    private val stalkerService: StalkerService
) : ViewModel() {
    
    companion object {
        private const val TAG = "AddSourceViewModel"
    }
    
    private val _uiState = MutableStateFlow(AddSourceUiState())
    val uiState: StateFlow<AddSourceUiState> = _uiState.asStateFlow()
    
    private val _validationMessage = MutableStateFlow<String?>(null)
    val validationMessage: StateFlow<String?> = _validationMessage.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            url = url,
            validationResult = null
        )
    }
    
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun updateMacAddress(macAddress: String) {
        _uiState.value = _uiState.value.copy(macAddress = macAddress)
    }
    
    fun updatePortalPath(portalPath: String) {
        _uiState.value = _uiState.value.copy(portalPath = portalPath)
    }
    
    fun updateUserAgent(userAgent: String) {
        _uiState.value = _uiState.value.copy(userAgent = userAgent)
    }
    
    fun updateReferer(referer: String) {
        _uiState.value = _uiState.value.copy(referer = referer)
    }
    
    fun updateSourceType(type: SourceType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            validationResult = null,
            username = "",
            password = "",
            macAddress = "",
            portalPath = getDefaultPortalPath(type)
        )
    }
    
    fun detectSourceType() {
        val currentState = _uiState.value
        
        if (currentState.url.isBlank()) {
            _uiState.value = currentState.copy(error = "يرجى إدخال رابط أولاً")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isDetecting = true, error = null)
            
            try {
                val detectedType = validationService.detectSourceType(currentState.url)
                
                if (detectedType != null) {
                    _uiState.value = currentState.copy(
                        isDetecting = false,
                        selectedType = detectedType,
                        portalPath = getDefaultPortalPath(detectedType),
                        message = "تم اكتشاف نوع المصدر: ${getSourceTypeDisplayName(detectedType)}"
                    )
                    
                    if (detectedType == SourceType.STALKER) {
                        discoverStalkerEndpoint()
                    }
                } else {
                    _uiState.value = currentState.copy(
                        isDetecting = false,
                        error = "لم يتم التمكن من اكتشاف نوع المصدر"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isDetecting = false,
                    error = "خطأ في اكتشاف نوع المصدر: ${e.message}"
                )
            }
        }
    }
    
    fun discoverStalkerEndpoint() {
        val currentState = _uiState.value
        
        if (currentState.url.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isDetecting = true)
            
            try {
                val discoveredPath = validationService.discoverStalkerEndpoint(currentState.url)
                
                if (discoveredPath != null) {
                    _uiState.value = currentState.copy(
                        isDetecting = false,
                        portalPath = discoveredPath,
                        message = "تم اكتشاف portal path: $discoveredPath"
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isDetecting = false,
                        error = "لم يتم العثور على portal endpoint صحيح"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isDetecting = false,
                    error = "خطأ في اكتشاف portal endpoint: ${e.message}"
                )
            }
        }
    }
    
    fun generateMAC() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "بدء توليد MAC address جديد")
                
                // استخدام المعرف الشائع للأجهزة المختلفة
                val availablePrefixes = StalkerService.MAC_PREFIXES
                val selectedPrefix = availablePrefixes.random()
                
                val newMac = stalkerService.generateMACAddress(selectedPrefix)
                
                // الحصول على معلومات إضافية عن الجهاز
                val deviceCredentials = stalkerService.generateDeviceCredentials(newMac)
                val deviceModel = deviceCredentials["device_model"] ?: "MAG254"
                val userAgent = deviceCredentials["user_agent"] ?: ""
                
                Log.d(TAG, "تم توليد MAC: $newMac للجهاز: $deviceModel")
                
                _uiState.value = _uiState.value.copy(
                    macAddress = newMac,
                    userAgent = userAgent,
                    selectedDeviceModel = deviceModel,
                    deviceCredentials = deviceCredentials
                )
                
                // إظهار معلومات عن الجهاز
                _validationMessage.value = "تم توليد MAC للجهاز: $deviceModel"
                
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في توليد MAC", e)
                _validationMessage.value = "خطأ في توليد MAC: ${e.localizedMessage}"
            }
        }
    }
    
    fun selectDeviceModel(deviceModel: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "اختيار نموذج الجهاز: $deviceModel")
                
                // البحث عن MAC prefix مناسب لهذا الجهاز
                val matchingPrefix = StalkerService.DEVICE_MODELS.entries
                    .firstOrNull { it.value == deviceModel }?.key
                    ?: StalkerService.MAC_PREFIXES.first()
                
                val newMac = stalkerService.generateMACAddress(matchingPrefix)
                val deviceCredentials = stalkerService.generateDeviceCredentials(newMac)
                
                _uiState.value = _uiState.value.copy(
                    macAddress = newMac,
                    userAgent = deviceCredentials["user_agent"] ?: "",
                    selectedDeviceModel = deviceModel,
                    deviceCredentials = deviceCredentials
                )
                
                // إظهار قدرات الجهاز
                val capabilities = stalkerService.getDeviceCapabilities(deviceModel)
                val capabilitiesText = capabilities?.let { caps ->
                    val features = mutableListOf<String>()
                    if (caps["supports_4k"] == true) features.add("4K")
                    if (caps["supports_hevc"] == true) features.add("HEVC")
                    if (caps["supports_hdr"] == true) features.add("HDR")
                    if (features.isNotEmpty()) "يدعم: ${features.joinToString(", ")}" else "دقة 1080p"
                } ?: "معلومات غير متوفرة"
                
                _validationMessage.value = "تم اختيار $deviceModel - $capabilitiesText"
                
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في اختيار نموذج الجهاز", e)
                _validationMessage.value = "خطأ في اختيار الجهاز: ${e.localizedMessage}"
            }
        }
    }
    
    fun validateSourceSmart() {
        if (_uiState.value.isValidating) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isValidating = true)
                _validationMessage.value = "جاري التحقق الذكي من المصدر..."
                
                val currentState = _uiState.value
                val url = currentState.url.trim()
                
                if (url.isEmpty()) {
                    _validationMessage.value = "يرجى إدخال رابط المصدر"
                    return@launch
                }
                
                Log.d(TAG, "بدء التحقق الذكي: $url")
                
                // اكتشاف نوع المصدر تلقائياً
                val detectedType = validationService.detectSourceType(url)
                Log.d(TAG, "نوع المصدر المكتشف: $detectedType")
                
                if (detectedType == null) {
                    _validationMessage.value = "لا يمكن تحديد نوع المصدر"
                    return@launch
                }
                
                // تحديث نوع المصدر المكتشف
                _uiState.value = _uiState.value.copy(
                    sourceType = detectedType,
                    detectedSourceType = detectedType
                )
                
                // التحقق حسب النوع المكتشف
                when (detectedType) {
                    SourceType.STALKER, SourceType.MAC_PORTAL -> {
                        if (currentState.macAddress.isEmpty()) {
                            // توليد MAC تلقائياً
                            generateMAC()
                            delay(500) // انتظار قصير لضمان التحديث
                        }
                        
                        // اكتشاف portal endpoint
                        _validationMessage.value = "اكتشاف portal endpoint..."
                        val detectedEndpoint = validationService.discoverStalkerEndpoint(url)
                        Log.d(TAG, "تم اكتشاف endpoint: $detectedEndpoint")
                        
                        _uiState.value = _uiState.value.copy(
                            portalPath = detectedEndpoint ?: "/stalker_portal/server/load.php"
                        )
                    }
                    
                    SourceType.XTREAM -> {
                        if (currentState.username.isEmpty() || currentState.password.isEmpty()) {
                            _validationMessage.value = "يرجى إدخال اسم المستخدم وكلمة المرور لـ Xtream Codes"
                            return@launch
                        }
                    }
                    
                    SourceType.M3U -> {
                        // M3U لا يتطلب معلومات إضافية
                    }
                }
                
                // التحقق الفعلي من المصدر
                _validationMessage.value = "جاري التحقق من المصدر..."
                
                val validationResult = validationService.validateSource(
                    sourceType = detectedType,
                    url = url,
                    username = currentState.username.takeIf { it.isNotEmpty() },
                    password = currentState.password.takeIf { it.isNotEmpty() },
                    macAddress = currentState.macAddress.takeIf { it.isNotEmpty() },
                    portalPath = currentState.portalPath.takeIf { it.isNotEmpty() }
                )
                
                _uiState.value = _uiState.value.copy(
                    validationResult = validationResult,
                    detectedPortalPath = validationResult.detectedPortalPath,
                    serverInfo = validationResult.serverInfo,
                    statistics = validationResult.statistics
                )
                
                if (validationResult.isValid) {
                    val serverInfoText = validationResult.serverInfo?.let { info ->
                        val location = listOfNotNull(info.city, info.country).joinToString(", ")
                        if (location.isNotEmpty()) " - $location" else ""
                    } ?: ""
                    
                    val statsText = validationResult.statistics?.let { stats ->
                        " (${stats.totalChannels} قناة)"
                    } ?: ""
                    
                    _validationMessage.value = "✅ المصدر صالح$serverInfoText$statsText"
                    
                    // تحديث المعلومات المكتشفة
                    validationResult.detectedPortalPath?.let { path ->
                        _uiState.value = _uiState.value.copy(portalPath = path)
                    }
                    
                } else {
                    val issues = validationResult.issues.joinToString("\n• ", "• ")
                    _validationMessage.value = "❌ فشل التحقق:\n$issues"
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في التحقق الذكي", e)
                _validationMessage.value = "خطأ في التحقق: ${e.localizedMessage ?: e.message}"
            } finally {
                _uiState.value = _uiState.value.copy(isValidating = false)
            }
        }
    }
    
    fun detectSourceTypeAuto() {
        if (_uiState.value.url.isEmpty()) return
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "اكتشاف تلقائي لنوع المصدر")
                
                val detectedType = validationService.detectSourceType(_uiState.value.url)
                
                if (detectedType != null) {
                    _uiState.value = _uiState.value.copy(
                        sourceType = detectedType,
                        detectedSourceType = detectedType
                    )
                    
                    _validationMessage.value = "تم اكتشاف نوع المصدر: ${getSourceTypeName(detectedType)}"
                    
                    // إجراءات إضافية حسب النوع
                    when (detectedType) {
                        SourceType.STALKER, SourceType.MAC_PORTAL -> {
                            if (_uiState.value.macAddress.isEmpty()) {
                                generateMAC()
                            }
                        }
                        else -> {}
                    }
                } else {
                    _validationMessage.value = "لا يمكن تحديد نوع المصدر تلقائياً"
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في اكتشاف نوع المصدر", e)
                _validationMessage.value = "خطأ في اكتشاف النوع: ${e.localizedMessage}"
            }
        }
    }
    
    fun getAvailableDevices(): List<String> {
        return StalkerService.DEVICE_MODELS.values.distinct().sorted()
    }
    
    fun getAvailableMACPrefixes(): List<Pair<String, String>> {
        return StalkerService.MAC_PREFIXES.map { prefix ->
            val deviceModel = StalkerService.DEVICE_MODELS[prefix] ?: "Generic Device"
            prefix to deviceModel
        }
    }
    
    fun updateAdvancedSettings(
        userAgent: String? = null,
        referer: String? = null,
        timezone: String? = null
    ) {
        _uiState.value = _uiState.value.copy(
            userAgent = userAgent ?: _uiState.value.userAgent,
            referer = referer ?: _uiState.value.referer,
            timezone = timezone ?: _uiState.value.timezone
        )
    }
    
    fun addSource() {
        val currentState = _uiState.value
        
        val validationErrors = validateRequiredFields(currentState)
        if (validationErrors.isNotEmpty()) {
            _uiState.value = currentState.copy(error = validationErrors.first())
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val validationResult = currentState.validationResult ?: run {
                    validationService.validateSource(
                        sourceType = currentState.selectedType,
                        url = currentState.url,
                        username = currentState.username.takeIf { it.isNotBlank() },
                        password = currentState.password.takeIf { it.isNotBlank() },
                        macAddress = currentState.macAddress.takeIf { it.isNotBlank() },
                        portalPath = currentState.portalPath.takeIf { it.isNotBlank() }
                    )
                }
                
                if (!validationResult.isValid) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "فشل في التحقق من المصدر: ${validationResult.issues.firstOrNull()}"
                    )
                    return@launch
                }
                
                val source = createSourceFromState(currentState, validationResult)
                
                sourceRepository.insertSource(source)
                
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSourceAdded = true,
                    message = "تم إضافة المصدر بنجاح"
                )
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "فشل في إضافة المصدر: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
    
    fun resetForm() {
        _uiState.value = AddSourceUiState()
    }
    
    private fun validateRequiredFields(state: AddSourceUiState): List<String> {
        val errors = mutableListOf<String>()
        
        if (state.name.isBlank()) {
            errors.add("يرجى إدخال اسم المصدر")
        }
        
        if (state.url.isBlank()) {
            errors.add("يرجى إدخال رابط المصدر")
        }
        
        when (state.selectedType) {
            SourceType.STALKER, SourceType.MAC_PORTAL -> {
                if (state.macAddress.isBlank()) {
                    errors.add("يرجى إدخال عنوان MAC")
                }
            }
            SourceType.XTREAM -> {
                if (state.username.isBlank()) {
                    errors.add("يرجى إدخال اسم المستخدم")
                }
                if (state.password.isBlank()) {
                    errors.add("يرجى إدخال كلمة المرور")
                }
            }
            SourceType.M3U -> {
                // لا توجد حقول إضافية مطلوبة
            }
        }
        
        return errors
    }
    
    private fun createSourceFromState(state: AddSourceUiState, validationResult: SourceValidationResult): Source {
        return Source(
            name = state.name.trim(),
            type = state.selectedType,
            url = state.url.trim(),
            username = state.username.takeIf { it.isNotBlank() },
            password = state.password.takeIf { it.isNotBlank() },
            macAddress = state.macAddress.takeIf { it.isNotBlank() },
            portalPath = state.portalPath.takeIf { it.isNotBlank() },
            userAgent = state.userAgent.takeIf { it.isNotBlank() },
            referer = state.referer.takeIf { it.isNotBlank() },
            isActive = true,
            lastChecked = System.currentTimeMillis(),
            accountStatus = validationResult.accountInfo?.status,
            expiryDate = validationResult.accountInfo?.expiryDate,
            maxConnections = validationResult.accountInfo?.maxConnections,
            activeConnections = validationResult.accountInfo?.activeConnections,
            isTrial = validationResult.accountInfo?.isTrial ?: false,
            countryCode = validationResult.serverInfo?.countryCode,
            serverInfo = validationResult.serverInfo?.let { 
                """
                {
                    "host": "${it.host}",
                    "country": "${it.country ?: ""}",
                    "isp": "${it.isp ?: ""}",
                    "timezone": "${it.timezone ?: ""}"
                }
                """.trimIndent()
            }
        )
    }
    
    private fun getDefaultPortalPath(type: SourceType): String {
        return when (type) {
            SourceType.STALKER -> "/stalker_portal/server/load.php"
            SourceType.MAC_PORTAL -> "/portal.php"
            else -> ""
        }
    }
    
    private fun getSourceTypeDisplayName(type: SourceType): String {
        return when (type) {
            SourceType.M3U -> "M3U Playlist"
            SourceType.STALKER -> "Stalker Portal"
            SourceType.XTREAM -> "Xtream Codes"
            SourceType.MAC_PORTAL -> "MAC Portal"
        }
    }
    
    private fun getSourceTypeName(type: SourceType): String {
        return when (type) {
            SourceType.M3U -> "M3U Playlist"
            SourceType.STALKER -> "Stalker Portal"
            SourceType.XTREAM -> "Xtream Codes"
            SourceType.MAC_PORTAL -> "MAC Portal"
        }
    }
}

data class AddSourceUiState(
    val name: String = "",
    val selectedType: SourceType = SourceType.M3U,
    val sourceType: SourceType = SourceType.M3U, // للاكتشاف التلقائي
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val macAddress: String = "",
    val portalPath: String = "",
    val userAgent: String = "",
    val referer: String = "",
    val timezone: String = "",
    val selectedDeviceModel: String = "MAG254",
    val deviceCredentials: Map<String, String> = emptyMap(),
    val detectedSourceType: SourceType? = null,
    val detectedPortalPath: String? = null,
    val serverInfo: ServerInfo? = null,
    val statistics: SourceStatistics? = null,
    val isLoading: Boolean = false,
    val isDetecting: Boolean = false,
    val isValidating: Boolean = false,
    val isSourceAdded: Boolean = false,
    val validationResult: SourceValidationResult? = null,
    val message: String? = null,
    val error: String? = null
)