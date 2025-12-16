package com.grupo03.solea.presentation.viewmodels.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.data.repositories.interfaces.UserPreferencesRepository
import com.grupo03.solea.presentation.states.screens.SettingsState
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the settings screen.
 *
 * Manages user preferences including notifications, theme (dark/light), and language settings.
 * Automatically loads current preferences from DataStore on initialization and provides
 * methods to update individual preferences.
 *
 * @property userPreferencesRepository Repository for persisting user preferences
 * @property movementRepository Repository for accessing movement data
 */
class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val movementRepository: MovementRepository
) : ViewModel() {

    /** Settings state including notifications, theme, and language preferences */
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Loads user preferences from DataStore.
     *
     * Combines all preference flows (notifications, theme, language) and updates
     * the UI state reactively whenever any preference changes.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.getNotificationsEnabled(),
                userPreferencesRepository.getDarkTheme(),
                userPreferencesRepository.getLanguage(),
                userPreferencesRepository.getQuickStartEnabled(),
                userPreferencesRepository.getQuickStartTarget()
            ) { notifications, darkTheme, language, quickEnabled, quickTarget ->
                SettingsState(
                    notificationsEnabled = notifications,
                    isDarkTheme = darkTheme,
                    selectedLanguage = language,
                    quickStartEnabled = quickEnabled,
                    quickStartTarget = quickTarget
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Toggles notification preferences.
     *
     * @param enabled Whether notifications should be enabled
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveNotificationsEnabled(enabled)
                _uiState.value = _uiState.value.copy(
                    notificationsEnabled = enabled,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar preferencia de notificaciones"
                )
            }
        }
    }

    /**
     * Toggles theme preference between dark and light mode.
     *
     * @param isDark Whether dark theme should be enabled
     */
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveDarkTheme(isDark)
                _uiState.value = _uiState.value.copy(
                    isDarkTheme = isDark,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar preferencia de tema"
                )
            }
        }
    }

    fun toggleQuickStart(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveQuickStartEnabled(enabled)
                _uiState.value = _uiState.value.copy(quickStartEnabled = enabled, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al guardar quick start")
            }
        }
    }

    fun saveQuickStartTarget(target: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveQuickStartTarget(target)
                _uiState.value = _uiState.value.copy(quickStartTarget = target, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al guardar quick start target")
            }
        }
    }

    /**
     * Changes the application language preference.
     *
     * @param languageCode Language code (e.g., "en", "es")
     */
    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveLanguage(languageCode)
                _uiState.value = _uiState.value.copy(
                    selectedLanguage = languageCode,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar preferencia de idioma"
                )
            }
        }
    }

    /**
     * Clears any error message from the settings state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Exports movements to Excel format.
     *
     * @param context Android context for file operations
     * @param userUid User's unique identifier
     * @return Uri of the created Excel file or null if failed
     */
    fun exportMovementsToExcel(context: Context, userUid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportSuccess = null, error = null)
            try {
                val result = movementRepository.getMovementsByUserId(userUid)

                when (result) {
                    is RepositoryResult.Success -> {
                        val movements = result.data

                        // Create workbook and sheet
                        val workbook = XSSFWorkbook()
                        val sheet = workbook.createSheet("Movements")

                        // Create header style
                        val headerStyle = workbook.createCellStyle()
                        val headerFont = workbook.createFont()
                        headerFont.bold = true
                        headerStyle.setFont(headerFont)
                        headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

                        // Create header row
                        val headerRow = sheet.createRow(0)
                        val headers = listOf("ID", "Type", "Name", "Description", "Date", "Amount", "Currency", "Category")
                        headers.forEachIndexed { index, header ->
                            val cell = headerRow.createCell(index)
                            cell.setCellValue(header)
                            cell.cellStyle = headerStyle
                        }

                        // Add data rows
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        movements.forEachIndexed { index, movement ->
                            val row = sheet.createRow(index + 1)
                            row.createCell(0).setCellValue(movement.id)
                            row.createCell(1).setCellValue(movement.type.name)
                            row.createCell(2).setCellValue(movement.name)
                            row.createCell(3).setCellValue(movement.description)
                            row.createCell(4).setCellValue(movement.datetime.format(dateFormatter))
                            row.createCell(5).setCellValue(movement.total)
                            row.createCell(6).setCellValue("PEN")
                            row.createCell(7).setCellValue(movement.category ?: "")
                        }

                        // Set column widths manually (in units of 1/256th of a character width)
                        sheet.setColumnWidth(0, 8000)  // ID
                        sheet.setColumnWidth(1, 3000)  // Type
                        sheet.setColumnWidth(2, 6000)  // Name
                        sheet.setColumnWidth(3, 8000)  // Description
                        sheet.setColumnWidth(4, 5000)  // Date
                        sheet.setColumnWidth(5, 3000)  // Amount
                        sheet.setColumnWidth(6, 3000)  // Currency
                        sheet.setColumnWidth(7, 6000)  // Category

                        // Save to Downloads folder
                        val fileName = "Solea_Movements_${System.currentTimeMillis()}.xlsx"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Use MediaStore for Android 10+
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }

                            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                            uri?.let { fileUri ->
                                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                                    workbook.write(outputStream)
                                }
                                workbook.close()

                                _uiState.value = _uiState.value.copy(
                                    isExporting = false,
                                    exportSuccess = "Excel exported to Downloads/$fileName"
                                )
                            } ?: run {
                                workbook.close()
                                _uiState.value = _uiState.value.copy(
                                    isExporting = false,
                                    error = "Failed to create file in Downloads"
                                )
                            }
                        } else {
                            // Use legacy method for Android 9 and below
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = java.io.File(downloadsDir, fileName)

                            FileOutputStream(file).use { fos ->
                                workbook.write(fos)
                            }
                            workbook.close()

                            _uiState.value = _uiState.value.copy(
                                isExporting = false,
                                exportSuccess = "Excel exported to Downloads/$fileName"
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            error = "Failed to fetch movements: ${result.error.code}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Exports movements to PDF format.
     *
     * @param context Android context for file operations
     * @param userUid User's unique identifier
     * @return Uri of the created PDF file or null if failed
     */
    fun exportMovementsToPdf(context: Context, userUid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportSuccess = null, error = null)
            try {
                val result = movementRepository.getMovementsByUserId(userUid)

                when (result) {
                    is RepositoryResult.Success -> {
                        val movements = result.data

                        val fileName = "Solea_Movements_${System.currentTimeMillis()}.pdf"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Use MediaStore for Android 10+
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }

                            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                            uri?.let { fileUri ->
                                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                                    val writer = PdfWriter(outputStream)
                                    val pdfDoc = PdfDocument(writer)
                                    val document = Document(pdfDoc)

                                    // Add title
                                    val title = Paragraph("Movements Report")
                                        .setFontSize(20f)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                    document.add(title)

                                    document.add(Paragraph("\n"))

                                    // Create table
                                    val table = Table(floatArrayOf(1f, 1f, 2f, 2f, 2f, 1f, 1f, 2f))
                                    table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100f))

                                    // Add headers
                                    val headers = listOf("ID", "Type", "Name", "Description", "Date", "Amount", "Currency", "Category")
                                    headers.forEach { header ->
                                        table.addHeaderCell(
                                            com.itextpdf.layout.element.Cell()
                                                .add(Paragraph(header).setBold())
                                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                                        )
                                    }

                                    // Add data rows
                                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                    movements.forEach { movement ->
                                        table.addCell(movement.id.take(8))
                                        table.addCell(movement.type.name)
                                        table.addCell(movement.name)
                                        table.addCell(movement.description)
                                        table.addCell(movement.datetime.format(dateFormatter))
                                        table.addCell(movement.total.toString())
                                        table.addCell(movement.currency)
                                        table.addCell(movement.category ?: "")
                                    }

                                    document.add(table)
                                    document.close()
                                }

                                _uiState.value = _uiState.value.copy(
                                    isExporting = false,
                                    exportSuccess = "PDF exported to Downloads/$fileName"
                                )
                            } ?: run {
                                _uiState.value = _uiState.value.copy(
                                    isExporting = false,
                                    error = "Failed to create file in Downloads"
                                )
                            }
                        } else {
                            // Use legacy method for Android 9 and below
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = java.io.File(downloadsDir, fileName)

                            val writer = PdfWriter(file)
                            val pdfDoc = PdfDocument(writer)
                            val document = Document(pdfDoc)

                            // Add title
                            val title = Paragraph("Movements Report")
                                .setFontSize(20f)
                                .setBold()
                                .setTextAlignment(TextAlignment.CENTER)
                            document.add(title)

                            document.add(Paragraph("\n"))

                            // Create table
                            val table = Table(floatArrayOf(1f, 1f, 2f, 2f, 2f, 1f, 1f, 2f))
                            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100f))

                            // Add headers
                            val headers = listOf("ID", "Type", "Name", "Description", "Date", "Amount", "Currency", "Category")
                            headers.forEach { header ->
                                table.addHeaderCell(
                                    com.itextpdf.layout.element.Cell()
                                        .add(Paragraph(header).setBold())
                                        .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                                )
                            }

                            // Add data rows
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            movements.forEach { movement ->
                                table.addCell(movement.id.take(8))
                                table.addCell(movement.type.name)
                                table.addCell(movement.name)
                                table.addCell(movement.description)
                                table.addCell(movement.datetime.format(dateFormatter))
                                table.addCell(movement.total.toString())
                                table.addCell("PEN")
                                table.addCell(movement.category ?: "")
                            }

                            document.add(table)
                            document.close()

                            _uiState.value = _uiState.value.copy(
                                isExporting = false,
                                exportSuccess = "PDF exported to Downloads/$fileName"
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            error = "Failed to fetch movements: ${result.error.code}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears the export success message.
     */
    fun clearExportSuccess() {
        _uiState.value = _uiState.value.copy(exportSuccess = null)
    }
}
