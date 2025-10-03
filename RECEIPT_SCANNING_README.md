# Solea - Gestión Financiera con Análisis de Boletas

## Nueva Funcionalidad: Escaneo de Boletas

La aplicación Solea ahora incluye la capacidad de escanear boletas y tickets de venta usando la cámara del dispositivo para extraer automáticamente la información financiera.

### Características Agregadas

1. **Captura de Imágenes**: La app puede acceder a la cámara del dispositivo para tomar fotos de boletas
2. **Análisis Automático**: Las imágenes se envían a un backend que utiliza Gemini AI para extraer información estructurada
3. **Parseo de Datos**: El sistema extrae información como:
   - Datos del comercio (nombre, RUC, dirección)
   - Detalles de la transacción (fecha, hora, tipo de documento)
   - Lista de productos con precios y descuentos
   - Totales y verificaciones aritméticas

### Cómo Usar la Nueva Funcionalidad

1. **Abrir la App**: Inicia la aplicación Solea
2. **Acceder al Menú**: Toca el botón flotante "+" en la pantalla principal
3. **Seleccionar Escanear**: Elige "Escanear boleta" del menú desplegable
4. **Permitir Cámara**: Concede permisos de cámara cuando se solicite
5. **Capturar Imagen**: 
   - Coloca la boleta dentro del marco guía
   - Asegúrate de que esté bien iluminada y legible
   - Toca el botón de captura (ícono de cámara)
6. **Esperar Análisis**: La app enviará la imagen al servidor para análisis
7. **Revisar Resultado**: Se mostrará la información extraída en formato JSON

### Archivos Modificados/Agregados

#### Nuevos Archivos:
- `ReceiptAnalysisService.kt` - Interfaz para el servicio de análisis
- `ReceiptAnalysisRepository.kt` - Repositorio para manejar el análisis de boletas
- `ReceiptCameraScreen.kt` - Pantalla de captura de cámara
- `ReceiptResultScreen.kt` - Pantalla de resultados (opcional)
- `ic_camera.xml` - Ícono de cámara

#### Archivos Modificados:
- `AndroidManifest.xml` - Permisos de cámara agregados
- `build.gradle.kts` - Dependencias de cámara y HTTP agregadas
- `CoreState.kt` - Estado para manejo de cámara
- `CoreViewModel.kt` - Lógica para análisis de imágenes
- `ModalBottomSheet.kt` - Opción de escaneo agregada
- `HomeScreen.kt` - Navegación a pantalla de cámara
- `MainActivity.kt` - Inyección del repositorio de análisis

### Configuración del Backend

El backend debe estar ejecutándose en: `https://gemini-py.onrender.com/analyze-image`

El endpoint espera:
- **Método**: POST
- **Content-Type**: multipart/form-data
- **Parámetros**:
  - `file`: Archivo de imagen
  - `prompt`: Texto del prompt (definido en el código)

### Dependencias Agregadas

```gradle
// Camera dependencies
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// HTTP client for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

### Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

### Próximos Pasos

1. **Parseo Automático**: Implementar el parseo del JSON retornado para crear movimientos automáticamente
2. **Validación de Datos**: Permitir al usuario revisar y editar la información antes de crear el movimiento
3. **Almacenamiento Local**: Cache de resultados para funcionamiento offline
4. **Mejoras de UI**: Feedback visual mejorado durante el proceso de análisis

### Notas Técnicas

- La funcionalidad requiere conexión a Internet para el análisis
- Las imágenes se procesan en un servidor remoto usando Gemini AI
- El prompt incluye instrucciones específicas para extraer datos de boletas peruanas
- Los archivos temporales se limpian automáticamente después del análisis