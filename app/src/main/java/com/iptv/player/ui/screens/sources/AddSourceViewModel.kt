package com.iptv.player.ui.screens.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.*
import com.iptv.player.data.network.SourceValidationService
import com.iptv.player.data.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSourceViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val validationService: SourceValidationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddSourceUiState())
    val uiState: StateFlow<AddSourceUiState> = _uiState.asStateFlow()
    
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
    
    fun generateMACAddress() {
        val generatedMAC = validationService.generateMACAddress()
        _uiState.value = _uiState.value.copy(
            macAddress = generatedMAC,
            message = "تم توليد عنوان MAC جديد"
        )
    }
    
    fun validateSource() {
        val currentState = _uiState.value
        
        if (currentState.url.isBlank()) {
            _uiState.value = currentState.copy(error = "يرجى إدخال رابط")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isValidating = true, error = null, validationResult = null)
            
            try {
                val result = validationService.validateSource(
                    sourceType = currentState.selectedType,
                    url = currentState.url,
                    username = currentState.username.takeIf { it.isNotBlank() },
                    password = currentState.password.takeIf { it.isNotBlank() },
                    macAddress = currentState.macAddress.takeIf { it.isNotBlank() },
                    portalPath = currentState.portalPath.takeIf { it.isNotBlank() }
                )
                
                _uiState.value = currentState.copy(
                    isValidating = false,
                    validationResult = result,
                    message = if (result.isValid) "تم التحقق بنجاح!" else null,
                    error = if (!result.isValid) result.issues.firstOrNull() else null
                )
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isValidating = false,
                    error = "خطأ في التحقق: ${e.message}"
                )
            }
        }
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
}

data class AddSourceUiState(
    val name: String = "",
    val selectedType: SourceType = SourceType.M3U,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val macAddress: String = "",
    val portalPath: String = "",
    val userAgent: String = "",
    val referer: String = "",
    val isLoading: Boolean = false,
    val isDetecting: Boolean = false,
    val isValidating: Boolean = false,
    val isSourceAdded: Boolean = false,
    val validationResult: SourceValidationResult? = null,
    val message: String? = null,
    val error: String? = null
)