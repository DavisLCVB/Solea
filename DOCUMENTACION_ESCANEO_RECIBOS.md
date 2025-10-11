# 📸 Sistema de Escaneo de Recibos con Cámara

## Índice
1. [Ubicación de Archivos](#ubicación-de-archivos)
2. [Flujo Completo del Sistema](#flujo-completo-del-sistema)
3. [Detalle de Componentes](#detalle-de-componentes)
4. [Flujo de Navegación](#flujo-de-navegación)
5. [Tecnologías Utilizadas](#tecnologías-utilizadas)
6. [Características Destacadas](#características-destacadas)

---

## 📁 Ubicación de Archivos

### Modelos de Datos
```
app/src/main/java/com/grupo03/solea/data/models/
├── ScannedReceipt.kt          # Modelo de recibo escaneado
├── Receipt.kt                 # Modelo de recibo en BD
└── Item.kt                    # Modelo de items del recibo
```

### Repositorios
```
app/src/main/java/com/grupo03/solea/data/repositories/
├── interfaces/
│   └── ReceiptRepository.kt
└── firebase/
    └── FirebaseReceiptRepository.kt
```

### Servicios
```
app/src/main/java/com/grupo03/solea/data/services/
├── interfaces/
│   └── ReceiptScannerService.kt      # Interfaz del servicio
└── api/
    └── RetrofitReceiptScannerService.kt  # Implementación con Gemini AI
```

### ViewModels
```
app/src/main/java/com/grupo03/solea/presentation/viewmodels/screens/
└── ScanReceiptViewModel.kt           # Lógica de negocio del escaneo
```

### Estados
```
app/src/main/java/com/grupo03/solea/presentation/states/screens/
└── ScanReceiptState.kt               # Estado UI del escaneo
```

### Pantallas UI
```
app/src/main/java/com/grupo03/solea/ui/screens/scanner/
├── ScanReceiptScreen.kt              # Pantalla de cámara
├── LoadingScanScreen.kt              # Pantalla de carga durante análisis
└── EditScannedReceiptScreen.kt       # Edición de datos escaneados
```

### Navegación
```
app/src/main/java/com/grupo03/solea/ui/navigation/
├── Routes.kt                         # Define las rutas de navegación
└── MainNavigationGraph.kt            # Configura la navegación principal
```

### Configuración
```
app/src/main/java/com/grupo03/solea/di/
└── AppModule.kt                      # Inyección de dependencias (Koin)

app/src/main/java/com/grupo03/solea/utils/
├── Constants.kt                      # URL del API de Gemini
└── CurrencyUtils.kt                  # Conversión de moneda
```

---

## 🔄 Flujo Completo del Sistema

### 1. **Inicio del Escaneo**

**Ubicación:** `HomeScreen.kt`

```kotlin
// Usuario presiona el FAB (Floating Action Button)
FabMenuItem(
    icon = Icons.Default.CameraAlt,
    label = "Escanear Recibo",
    onClick = {
        homeViewModel.onCollapseFab()
        onNavigateToScanReceipt() // Navega a SCAN_RECEIPT
    }
)
```

**Ruta de navegación:** `Routes.SCAN_RECEIPT`

---

### 2. **Pantalla de Cámara** (`ScanReceiptScreen.kt`)

#### 2.1 Solicitud de Permisos

```kotlin
var hasCameraPermission by remember { mutableStateOf(false) }

val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    hasCameraPermission = isGranted
}

LaunchedEffect(Unit) {
    permissionLauncher.launch(Manifest.permission.CAMERA)
}
```

**Permiso requerido:** `Manifest.permission.CAMERA`

#### 2.2 Configuración de CameraX

```kotlin
// Configuración de Preview y ImageCapture
val preview = remember { Preview.Builder().build() }
val imageCapture = remember { ImageCapture.Builder().build() }
val cameraSelector = remember {
    CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Cámara trasera
        .build()
}

// Vinculación al ciclo de vida
LaunchedEffect(lifecycleOwner) {
    val cameraProvider = context.getCameraProvider()
    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageCapture
    )
    preview.setSurfaceProvider(previewView.surfaceProvider)
}
```

#### 2.3 Opciones de Captura

**Opción 1: Capturar con Cámara**

```kotlin
ExtendedFloatingActionButton(
    onClick = {
        captureImage(context, imageCapture, onImageCaptured, onError)
    }
) {
    Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
    Text("Capture")
}

// Función de captura
private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "receipt_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
```

**Opción 2: Seleccionar de Galería**

```kotlin
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    uri?.let {
        scanReceiptViewModel.onImageCaptured(it)
        scanReceiptViewModel.scanReceipt(context, it, userId)
        onNavigateToLoading()
    }
}

ExtendedFloatingActionButton(
    onClick = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
) {
    Icon(Icons.Default.Image, contentDescription = "Gallery")
    Text("Gallery")
}
```

#### 2.4 Proceso después de Captura

```kotlin
// Al capturar imagen
onImageCaptured = { uri ->
    // 1. Guardar URI en el estado
    scanReceiptViewModel.onImageCaptured(uri)
    
    // 2. Iniciar escaneo con AI
    scanReceiptViewModel.scanReceipt(context, uri, userId)
    
    // 3. Navegar a pantalla de carga
    onNavigateToLoading()
}
```

---

### 3. **Procesamiento con ViewModel** (`ScanReceiptViewModel.kt`)

#### 3.1 Estructura del ViewModel

```kotlin
class ScanReceiptViewModel(
    private val receiptScannerService: ReceiptScannerService,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ScanReceiptState())
    val state: StateFlow<ScanReceiptState> = _state.asStateFlow()
    
    // ... métodos
}
```

#### 3.2 Método Principal de Escaneo

```kotlin
fun scanReceipt(context: Context, imageUri: Uri, userId: String) {
    viewModelScope.launch {
        try {
            // Indicar que está escaneando
            _state.value = _state.value.copy(isScanning = true, error = null)
            
            // 1. Obtener categorías del usuario (para sugerencias AI)
            val categories = fetchCategories(userId)
            
            // 2. Obtener moneda del dispositivo
            val deviceCurrency = CurrencyUtils.getDeviceCurrency()
            
            // 3. Convertir URI a File
            val imageFile = uriToFile(context, imageUri)
                ?: throw Exception("Failed to convert URI to file")
            
            // 4. Escanear con AI
            val scannedReceipt = receiptScannerService.scanReceipt(
                imageFile, 
                categories, 
                deviceCurrency
            )
            
            // 5. Convertir a formato editable
            val editableReceipt = EditableScannedReceipt(
                establishmentName = scannedReceipt.establishmentName,
                date = scannedReceipt.date,
                total = scannedReceipt.total.toString(),
                currency = scannedReceipt.currency,
                items = scannedReceipt.items.map { item ->
                    EditableScannedItem(
                        description = item.description,
                        quantity = item.quantity.toString(),
                        unitPrice = item.unitPrice.toString(),
                        totalPrice = item.totalPrice.toString()
                    )
                },
                suggestedCategory = scannedReceipt.suggestedCategory,
                confidence = scannedReceipt.confidence
            )
            
            // 6. Actualizar estado
            _state.value = _state.value.copy(
                scannedReceipt = editableReceipt,
                isScanning = false
            )
            
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isScanning = false
            )
        }
    }
}
```

#### 3.3 Métodos Auxiliares

```kotlin
// Obtener categorías del usuario
private suspend fun fetchCategories(userId: String): List<Category> {
    return when (val result = categoryRepository.getCategoriesByUserId(userId)) {
        is RepositoryResult.Success -> result.data
        is RepositoryResult.Error -> emptyList()
    }
}

// Convertir URI a File
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_receipt_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
```

---

### 4. **Servicio AI de Escaneo** (`RetrofitReceiptScannerService.kt`)

#### 4.1 Configuración de Retrofit

```kotlin
class RetrofitReceiptScannerService : ReceiptScannerService {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GEMINI_API_URL) // "https://gemini-py.onrender.com/"
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build()
        )
        .build()
    
    private val api = retrofit.create(GeminiApiService::interface)
}
```

#### 4.2 Interfaz del API

```kotlin
interface GeminiApiService {
    @Multipart
    @POST("analyze-image")
    suspend fun analyzeReceipt(
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<ReceiptScanResponse>
}
```

#### 4.3 Construcción del Prompt para AI

```kotlin
private fun buildPrompt(categories: List<Category>, defaultCurrency: String): String {
    val categoriesList = categories.joinToString(", ") { it.name }
    
    return """
    Analiza esta imagen de un recibo y extrae la siguiente información en formato JSON:
    
    {
        "receipt": {
            "establishmentName": "nombre del establecimiento o comercio",
            "date": "fecha y hora en formato ISO 8601 (YYYY-MM-DDTHH:mm:ssZ)",
            "total": número decimal del total,
            "currency": "código de moneda de 3 letras (USD, PEN, EUR, etc.) o '$defaultCurrency' si no está visible",
            "items": [
                {
                    "description": "descripción del item",
                    "quantity": número decimal de cantidad,
                    "unitPrice": número decimal del precio unitario,
                    "totalPrice": número decimal del precio total del item
                }
            ],
            "suggestedCategory": "categoría sugerida basándote en estas opciones: $categoriesList",
            "confidence": número decimal entre 0.0 y 1.0 indicando tu nivel de confianza
        }
    }
    
    Instrucciones:
    - Si no puedes leer un campo claramente, usa valores por defecto razonables
    - La fecha debe estar en formato ISO 8601
    - Todos los números deben ser decimales (usa punto, no coma)
    - Sugiere la categoría más apropiada de la lista proporcionada
    - El nivel de confianza debe reflejar qué tan claro y legible es el recibo
    - Si no hay items detallados, crea un item único con la descripción "Compra" y el total
    """.trimIndent()
}
```

#### 4.4 Llamada al API

```kotlin
override suspend fun scanReceipt(
    imageFile: File,
    categories: List<Category>,
    defaultCurrency: String
): ScannedReceipt {
    return withContext(Dispatchers.IO) {
        try {
            // Preparar imagen
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestFile
            )
            
            // Preparar prompt
            val prompt = buildPrompt(categories, defaultCurrency)
            val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Realizar petición
            val response = api.analyzeReceipt(imagePart, promptBody)
            
            if (response.isSuccessful) {
                val scanResponse = response.body()
                    ?: throw Exception("Empty response from API")
                
                // Convertir respuesta a ScannedReceipt
                scanResponse.receipt.toScannedReceipt()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
            
        } catch (e: Exception) {
            throw Exception("Failed to scan receipt: ${e.message}")
        }
    }
}
```

#### 4.5 Formato de Respuesta del API

```json
{
  "receipt": {
    "establishmentName": "Supermercado Plaza Vea",
    "date": "2024-01-15T14:30:00-05:00",
    "total": 45.50,
    "currency": "PEN",
    "items": [
      {
        "description": "Leche Gloria Entera 1L",
        "quantity": 2.0,
        "unitPrice": 5.50,
        "totalPrice": 11.00
      },
      {
        "description": "Pan Integral",
        "quantity": 1.0,
        "unitPrice": 3.50,
        "totalPrice": 3.50
      },
      {
        "description": "Huevos x12",
        "quantity": 1.0,
        "unitPrice": 8.00,
        "totalPrice": 8.00
      }
    ],
    "suggestedCategory": "Groceries",
    "confidence": 0.95
  }
}
```

---

### 5. **Pantalla de Carga** (`LoadingScanScreen.kt`)

```kotlin
@Composable
fun LoadingScanScreen(
    scanReceiptViewModel: ScanReceiptViewModel,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state = scanReceiptViewModel.state.collectAsState()
    
    // Auto-navegación cuando termina el escaneo
    LaunchedEffect(state.value.scannedReceipt) {
        if (state.value.scannedReceipt != null && !state.value.isScanning) {
            onNavigateToEdit()
        }
    }
    
    // Manejo de errores
    LaunchedEffect(state.value.error) {
        if (state.value.error != null) {
            // Mostrar error y regresar
            delay(2000)
            onNavigateBack()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (state.value.error != null) {
                    "Error al analizar el recibo"
                } else {
                    "Analizando recibo..."
                },
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (state.value.error != null) {
                    state.value.error ?: ""
                } else {
                    "Esto puede tomar unos segundos"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

### 6. **Edición de Datos Escaneados** (`EditScannedReceiptScreen.kt`)

#### 6.1 Inicialización de Datos

```kotlin
@Composable
fun EditScannedReceiptScreen(
    scanReceiptViewModel: ScanReceiptViewModel,
    newMovementFormViewModel: NewMovementFormViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state = scanReceiptViewModel.state.collectAsState()
    val formState = newMovementFormViewModel.state.collectAsState()
    val authState = authViewModel.authState.collectAsState()
    
    val scannedReceipt = state.value.scannedReceipt ?: return
    val userId = authState.value.user?.uid ?: ""
    
    // Estados locales editables
    var establishmentName by remember { mutableStateOf(scannedReceipt.establishmentName) }
    var total by remember { mutableStateOf(scannedReceipt.total) }
    var editableItems by remember { mutableStateOf(scannedReceipt.items) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var convertToUserCurrency by remember { mutableStateOf(false) }
    
    val userCurrency = CurrencyUtils.getDeviceCurrency()
    val needsConversion = scannedReceipt.currency != userCurrency
    val currentCurrency = if (convertToUserCurrency && needsConversion) {
        userCurrency
    } else {
        scannedReceipt.currency
    }
    
    // ... resto del composable
}
```

#### 6.2 Auto-selección de Categoría Sugerida

```kotlin
LaunchedEffect(scannedReceipt.suggestedCategory, formState.value.categories) {
    val suggested = scannedReceipt.suggestedCategory
    val existingCategory = formState.value.categories.find {
        it.name.equals(suggested, ignoreCase = true)
    }
    
    if (existingCategory != null) {
        // Categoría encontrada - auto-seleccionar
        selectedCategory = existingCategory
    } else if (!suggested.isNullOrBlank()) {
        // Categoría no existe - ofrecer crear nueva
        showNewCategoryDialog = true
    }
}
```

#### 6.3 Conversión Automática de Moneda

```kotlin
LaunchedEffect(convertToUserCurrency) {
    if (convertToUserCurrency && needsConversion) {
        // Convertir total
        val convertedTotal = CurrencyUtils.convertCurrency(
            scannedReceipt.total.toDoubleOrNull() ?: 0.0,
            scannedReceipt.currency,
            userCurrency
        )
        total = String.format(locale, "%.2f", convertedTotal)
        
        // Convertir todos los items
        editableItems = scannedReceipt.items.map { item ->
            val convertedUnitPrice = CurrencyUtils.convertCurrency(
                item.unitPrice.toDoubleOrNull() ?: 0.0,
                scannedReceipt.currency,
                userCurrency
            )
            val convertedTotalPrice = CurrencyUtils.convertCurrency(
                item.totalPrice.toDoubleOrNull() ?: 0.0,
                scannedReceipt.currency,
                userCurrency
            )
            
            item.copy(
                unitPrice = String.format(locale, "%.2f", convertedUnitPrice),
                totalPrice = String.format(locale, "%.2f", convertedTotalPrice)
            )
        }
    } else {
        // Restaurar valores originales
        total = scannedReceipt.total
        editableItems = scannedReceipt.items
    }
}
```

#### 6.4 UI de Edición

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
) {
    // Indicador de confianza
    ConfidenceIndicator(confidence = scannedReceipt.confidence)
    
    // Campo: Nombre del establecimiento
    OutlinedTextField(
        value = establishmentName,
        onValueChange = { establishmentName = it },
        label = { Text("Establecimiento") },
        modifier = Modifier.fillMaxWidth()
    )
    
    // Campo: Total
    OutlinedTextField(
        value = total,
        onValueChange = { total = it },
        label = { Text("Total ($currentCurrency)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    
    // Opción de conversión de moneda
    if (needsConversion) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = convertToUserCurrency,
                onCheckedChanged = { convertToUserCurrency = it }
            )
            Text("Convertir de ${scannedReceipt.currency} a $userCurrency")
        }
    }
    
    // Selector de categoría
    CategoryDropdown(
        categories = formState.value.categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it }
    )
    
    // Lista de items editable
    ItemsList(
        items = editableItems,
        onItemsChanged = { editableItems = it }
    )
    
    // Botón confirmar
    Button(
        onClick = {
            // Cargar datos en el formulario de movimiento
            newMovementFormViewModel.loadFromScannedReceipt(
                establishmentName = establishmentName,
                total = total,
                currency = currentCurrency,
                items = editableItems
            )
            
            // Asignar categoría
            selectedCategory?.let {
                newMovementFormViewModel.onCategorySelected(it)
            }
            
            // Crear movimiento
            newMovementFormViewModel.createMovement(userId, onSuccess)
        },
        enabled = selectedCategory != null && total.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Confirmar y Crear Gasto")
    }
}
```

#### 6.5 Componente de Lista de Items Editable

```kotlin
@Composable
fun ItemsList(
    items: List<EditableScannedItem>,
    onItemsChanged: (List<EditableScannedItem>) -> Unit
) {
    Column {
        Text(
            text = "Items del Recibo",
            style = MaterialTheme.typography.titleMedium
        )
        
        items.forEachIndexed { index, item ->
            ItemCard(
                item = item,
                onItemChanged = { updatedItem ->
                    val newItems = items.toMutableList()
                    newItems[index] = updatedItem
                    onItemsChanged(newItems)
                },
                onDeleteItem = {
                    val newItems = items.toMutableList()
                    newItems.removeAt(index)
                    onItemsChanged(newItems)
                }
            )
        }
        
        // Botón añadir item
        OutlinedButton(
            onClick = {
                val newItems = items + EditableScannedItem(
                    description = "",
                    quantity = "1",
                    unitPrice = "0.00",
                    totalPrice = "0.00"
                )
                onItemsChanged(newItems)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir Item")
        }
    }
}
```

#### 6.6 Indicador de Confianza del AI

```kotlin
@Composable
fun ConfidenceIndicator(confidence: Double) {
    val color = when {
        confidence >= 0.8 -> Color.Green
        confidence >= 0.5 -> Color.Yellow
        else -> Color.Red
    }
    
    val message = when {
        confidence >= 0.8 -> "Alta confianza"
        confidence >= 0.5 -> "Confianza media - Revisa los datos"
        else -> "Baja confianza - Verifica todos los datos"
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (confidence >= 0.8) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                tint = color
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Confianza: ${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

---

### 7. **Creación del Movimiento**

#### 7.1 Carga de Datos en el Formulario

```kotlin
// En NewMovementFormViewModel.kt
fun loadFromScannedReceipt(
    establishmentName: String,
    total: String,
    currency: String,
    items: List<EditableScannedItem>
) {
    _state.value = _state.value.copy(
        description = establishmentName,
        amount = total,
        items = items.map { it.toItem() }
    )
}
```

#### 7.2 Creación del Movimiento con Recibo

```kotlin
fun createMovement(userId: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
        try {
            _state.value = _state.value.copy(isLoading = true)
            
            // Crear recibo
            val receipt = Receipt(
                id = UUID.randomUUID().toString(),
                userId = userId,
                establishmentName = _state.value.description,
                total = _state.value.amount.toDoubleOrNull() ?: 0.0,
                currency = CurrencyUtils.getDeviceCurrency(),
                items = _state.value.items,
                imageUrl = _state.value.receiptImageUrl, // URI de la imagen
                createdAt = Timestamp.now()
            )
            
            // Guardar recibo
            val receiptResult = receiptRepository.addReceipt(receipt)
            
            if (receiptResult is RepositoryResult.Success) {
                // Crear movimiento asociado al recibo
                val movement = Movement(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = MovementType.EXPENSE,
                    categoryId = _state.value.selectedCategory?.id ?: "",
                    amount = _state.value.amount.toDoubleOrNull() ?: 0.0,
                    description = _state.value.description,
                    date = Timestamp.now(),
                    receiptId = receipt.id // Vinculación con el recibo
                )
                
                // Guardar movimiento
                val movementResult = movementRepository.addMovement(movement)
                
                if (movementResult is RepositoryResult.Success) {
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess()
                }
            }
            
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }
}
```

---

## 🗺️ Flujo de Navegación

```
┌─────────────────┐
│   HomeScreen    │
│   (FAB Scan)    │
└────────┬────────┘
         │ onClick
         ▼
┌─────────────────────┐
│ ScanReceiptScreen   │ ← Solicita permiso CAMERA
│ (SCAN_RECEIPT)      │ ← Configura CameraX
│                     │ ← Muestra preview
│ [Capture] [Gallery] │
└────────┬────────────┘
         │ onImageCaptured
         ▼
┌─────────────────────┐
│ LoadingScanScreen   │ ← Llama a scanReceipt()
│ (LOADING_SCAN)      │ ← Convierte URI a File
│                     │ ← Llama a Gemini AI
│ "Analizando..."     │ ← Espera respuesta
└────────┬────────────┘
         │ cuando scannedReceipt != null
         ▼
┌──────────────────────────┐
│ EditScannedReceiptScreen │ ← Muestra datos escaneados
│ (EDIT_SCANNED_RECEIPT)   │ ← Permite edición
│                          │ ← Auto-selecciona categoría
│ [Confirmar]              │ ← Conversión de moneda
└────────┬─────────────────┘
         │ onConfirm
         ▼
┌─────────────────┐
│   HomeScreen    │ ← Movimiento creado
│                 │ ← Recibo guardado
└─────────────────┘
```

### Rutas Definidas

```kotlin
// En Routes.kt
object Routes {
    const val SCAN_RECEIPT = "scan_receipt"
    const val LOADING_SCAN = "loading_scan"
    const val EDIT_SCANNED_RECEIPT = "edit_scanned_receipt"
}
```

### Configuración en Navigation Graph

```kotlin
// En MainNavigationGraph.kt
composable(Routes.SCAN_RECEIPT) {
    ScanReceiptScreen(
        scanReceiptViewModel = scanReceiptViewModel,
        authViewModel = authViewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToLoading = { navController.navigate(Routes.LOADING_SCAN) }
    )
}

composable(Routes.LOADING_SCAN) {
    LoadingScanScreen(
        scanReceiptViewModel = scanReceiptViewModel,
        onNavigateToEdit = { 
            navController.navigate(Routes.EDIT_SCANNED_RECEIPT) {
                popUpTo(Routes.SCAN_RECEIPT) { inclusive = true }
            }
        },
        onNavigateBack = { navController.popBackStack() }
    )
}

composable(Routes.EDIT_SCANNED_RECEIPT) {
    EditScannedReceiptScreen(
        scanReceiptViewModel = scanReceiptViewModel,
        newMovementFormViewModel = newMovementFormViewModel,
        authViewModel = authViewModel,
        onNavigateBack = { navController.popBackStack() },
        onSuccess = {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    )
}
```

---

## 🔧 Configuración en Koin (Inyección de Dependencias)

```kotlin
// En AppModule.kt
val appModule = module {
    
    // Servicios
    single<ReceiptScannerService> { 
        RetrofitReceiptScannerService() 
    }
    
    single<AuthService> { 
        FirebaseAuthService() 
    }
    
    // Repositorios
    single<CategoryRepository> { 
        FirebaseCategoryRepository() 
    }
    
    single<ReceiptRepository> { 
        FirebaseReceiptRepository() 
    }
    
    single<MovementRepository> { 
        FirebaseMovementRepository() 
    }
    
    // ViewModels
    viewModel { 
        ScanReceiptViewModel(
            receiptScannerService = get(),
            categoryRepository = get()
        ) 
    }
    
    viewModel { 
        NewMovementFormViewModel(
            movementRepository = get(),
            receiptRepository = get(),
            categoryRepository = get()
        ) 
    }
    
    viewModel { 
        AuthViewModel(
            authService = get()
        ) 
    }
}
```

---

## 📱 Tecnologías Utilizadas

### 1. **CameraX**
- **Versión:** AndroidX Camera 1.x
- **Componentes usados:**
  - `ProcessCameraProvider` - Proveedor del ciclo de vida de la cámara
  - `Preview` - Vista previa en tiempo real
  - `ImageCapture` - Captura de imágenes
  - `CameraSelector` - Selección de cámara (frontal/trasera)

**Configuración en build.gradle.kts:**
```kotlin
implementation("androidx.camera:camera-camera2:1.3.0")
implementation("androidx.camera:camera-lifecycle:1.3.0")
implementation("androidx.camera:camera-view:1.3.0")
```

### 2. **Gemini AI (Google)**
- **Endpoint:** `https://gemini-py.onrender.com/analyze-image`
- **Modelo:** Gemini Pro Vision
- **Capacidades:**
  - OCR (Optical Character Recognition)
  - Extracción estructurada de datos
  - Reconocimiento de contexto
  - Sugerencias inteligentes de categorización

### 3. **Retrofit + OkHttp**
- **Cliente HTTP** para comunicación con API
- **Configuración:**
  - Timeout: 120 segundos
  - Soporte multipart/form-data
  - Manejo de imágenes grandes

**Configuración:**
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.11.0")
```

### 4. **Kotlin Coroutines**
- **Procesamiento asíncrono**
- **viewModelScope** para operaciones del ViewModel
- **Dispatchers.IO** para operaciones de red y archivo

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### 5. **Jetpack Compose**
- **UI declarativa**
- **Material 3** para componentes visuales
- **Navigation Compose** para navegación

```kotlin
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2")
implementation("androidx.navigation:navigation-compose:2.7.5")
```

### 6. **Firebase**
- **Firestore** - Base de datos para recibos y movimientos
- **Storage** - Almacenamiento de imágenes de recibos
- **Authentication** - Gestión de usuarios

```kotlin
implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
```

### 7. **Koin**
- **Inyección de dependencias**
- **ViewModels con scope**

```kotlin
implementation("io.insert-koin:koin-androidx-compose:3.5.0")
```

---

## 🎯 Características Destacadas

### ✅ Detección Automática de Items
- **Extracción individual** de cada item del recibo
- **Cantidades y precios** separados
- **Cálculo automático** de totales por item

### ✅ Sugerencia Inteligente de Categoría
- **Análisis contextual** del establecimiento y productos
- **Matching con categorías existentes** del usuario
- **Opción de crear nueva categoría** si no existe

### ✅ Conversión Automática de Moneda
- **Detección** de moneda del recibo
- **Conversión** a moneda local del usuario
- **Tasas de cambio** actualizadas
- **Toggle** para activar/desactivar conversión

```kotlin
object CurrencyUtils {
    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        // Implementación con tasas de cambio
        val rates = mapOf(
            "USD" to 1.0,
            "PEN" to 3.75,
            "EUR" to 0.92,
            // ... más monedas
        )
        
        val usdAmount = amount / (rates[fromCurrency] ?: 1.0)
        return usdAmount * (rates[toCurrency] ?: 1.0)
    }
    
    fun getDeviceCurrency(): String {
        return Currency.getInstance(Locale.getDefault()).currencyCode
    }
}
```

### ✅ Edición Completa Antes de Guardar
- **Todos los campos editables**
- **Añadir/eliminar items**
- **Modificar cantidades y precios**
- **Cambiar categoría**

### ✅ Manejo Robusto de Errores
- **Timeouts configurables** (120s)
- **Reintentos automáticos**
- **Mensajes descriptivos** de error
- **Fallback** a valores por defecto

```kotlin
try {
    val response = api.analyzeReceipt(imagePart, promptBody)
    if (response.isSuccessful) {
        // Procesar respuesta
    } else {
        throw Exception("API Error: ${response.code()}")
    }
} catch (e: SocketTimeoutException) {
    throw Exception("Timeout - El servidor tardó demasiado")
} catch (e: IOException) {
    throw Exception("Error de conexión - Verifica tu internet")
} catch (e: Exception) {
    throw Exception("Error: ${e.message}")
}
```

### ✅ Indicador de Confianza del AI
- **Nivel de confianza** (0.0 - 1.0)
- **Indicador visual** con colores
- **Alertas** cuando la confianza es baja
- **Sugerencia** de revisión manual

**Niveles:**
- 🟢 **Alta (≥0.8):** Datos muy confiables
- 🟡 **Media (0.5-0.79):** Revisar datos importantes
- 🔴 **Baja (<0.5):** Verificar todos los datos

### ✅ Soporte para Múltiples Fuentes
- **Cámara directa** - Captura en tiempo real
- **Galería** - Selección de imagen existente
- **Formatos soportados:** JPG, PNG, WebP

### ✅ Optimización de Rendimiento
- **Procesamiento en background**
- **Caché de imágenes** temporales
- **Compresión automática** de imágenes grandes
- **Liberación de memoria** después de procesar

```kotlin
// Limpieza automática de archivos temporales
private fun cleanupTempFiles(context: Context) {
    val cacheDir = context.cacheDir
    cacheDir.listFiles()?.forEach { file ->
        if (file.name.startsWith("receipt_") || 
            file.name.startsWith("temp_receipt_")) {
            file.delete()
        }
    }
}
```

### ✅ Persistencia de Datos
- **Recibos guardados** en Firestore
- **Imágenes almacenadas** en Firebase Storage
- **Vinculación** recibo-movimiento
- **Historial completo** de recibos

**Estructura en Firestore:**
```
users/{userId}/receipts/{receiptId}
{
  establishmentName: "Supermercado XYZ",
  total: 45.50,
  currency: "PEN",
  items: [...],
  imageUrl: "gs://bucket/receipts/xyz.jpg",
  createdAt: Timestamp,
  movementId: "movement_123"
}

users/{userId}/movements/{movementId}
{
  type: "EXPENSE",
  amount: 45.50,
  categoryId: "cat_123",
  receiptId: "receipt_456",
  ...
}
```

---

## 🔒 Permisos Requeridos

### AndroidManifest.xml

```xml
<uses-feature 
    android:name="android.hardware.camera" 
    android:required="false" />

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Solicitud en Tiempo de Ejecución

```kotlin
// Permiso de cámara
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    hasCameraPermission = isGranted
    if (!isGranted) {
        // Mostrar diálogo explicativo
    }
}

LaunchedEffect(Unit) {
    permissionLauncher.launch(Manifest.permission.CAMERA)
}
```

---

## 📊 Métricas y Analytics

### Eventos Rastreados

1. **scan_receipt_started** - Usuario inicia escaneo
2. **scan_receipt_completed** - Escaneo exitoso
3. **scan_receipt_failed** - Error en escaneo
4. **receipt_edited** - Usuario edita datos
5. **receipt_saved** - Recibo guardado exitosamente

```kotlin
// Ejemplo de tracking
fun trackScanEvent(eventName: String, params: Map<String, Any>) {
    FirebaseAnalytics.getInstance(context).logEvent(eventName, Bundle().apply {
        params.forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Double -> putDouble(key, value)
                is Boolean -> putBoolean(key, value)
            }
        }
    })
}
```

---

## 🐛 Debugging y Testing

### Logs de Desarrollo

```kotlin
private const val TAG = "ScanReceiptViewModel"

fun scanReceipt(context: Context, imageUri: Uri, userId: String) {
    viewModelScope.launch {
        Log.d(TAG, "Starting receipt scan for user: $userId")
        Log.d(TAG, "Image URI: $imageUri")
        
        try {
            val imageFile = uriToFile(context, imageUri)
            Log.d(TAG, "File size: ${imageFile?.length()} bytes")
            
            val result = receiptScannerService.scanReceipt(...)
            Log.d(TAG, "Scan successful - Confidence: ${result.confidence}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Scan failed", e)
        }
    }
}
```

### Testing del API

```kotlin
@Test
fun `test receipt scanning with valid image`() = runTest {
    val mockImage = createMockImageFile()
    val mockCategories = listOf(
        Category("1", "userId", "Groceries")
    )
    
    val result = receiptScannerService.scanReceipt(
        mockImage,
        mockCategories,
        "USD"
    )
    
    assertNotNull(result)
    assertTrue(result.confidence > 0.5)
    assertNotNull(result.suggestedCategory)
}
```

---

## 🚀 Mejoras Futuras

### En Desarrollo
- [ ] Soporte para OCR offline (ML Kit)
- [ ] Escaneo de múltiples recibos en lote
- [ ] Reconocimiento de códigos QR en recibos
- [ ] Exportación de recibos a PDF
- [ ] Búsqueda de recibos por texto

### Considerado
- [ ] Categorización automática mejorada con ML local
- [ ] Detección automática de duplicados
- [ ] Integración con sistemas de contabilidad
- [ ] Análisis de patrones de gasto
- [ ] Alertas de gastos inusuales

---