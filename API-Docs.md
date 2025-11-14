# API Documentation - Gemini Multimodal Analyzer

Documentación completa de la API REST para el analizador multimodal con Gemini AI.

**Base URL**: `http://localhost:3000` (desarrollo) / `https://solea-backend.vercel.app` (producción)

---

## Tabla de Contenidos

- [Autenticación](#autenticación)
- [Endpoints](#endpoints)
  - [POST /api/analyze-image](#post-apianalyze-image)
  - [POST /api/analyze-audio](#post-apianalyze-audio)
  - [GET /api/health](#get-apihealth)
- [Códigos de Estado](#códigos-de-estado)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Límites y Restricciones](#límites-y-restricciones)
- [Manejo de Errores](#manejo-de-errores)

---

## Autenticación

Esta API **no requiere autenticación** en su versión actual. Todos los endpoints son públicos.

> **Nota de Seguridad**: Para producción, se recomienda implementar autenticación (API keys, JWT, etc.)

---

## Endpoints

### POST /api/analyze-image

Analiza una imagen usando Google Gemini AI.

#### Request

**Headers:**
```
Content-Type: multipart/form-data
```

**Body (FormData):**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `image` | File | ✅ Sí | Archivo de imagen (JPEG, PNG, WebP, GIF) |
| `prompt` | String | ❌ No | Prompt personalizado (máx 2000 caracteres) |

**Restricciones del archivo:**
- Tamaño máximo: **5MB**
- Formatos aceptados: `image/jpeg`, `image/png`, `image/webp`, `image/gif`

#### Response

**Success (200 OK):**
```json
{
  "success": true,
  "result": "Esta imagen muestra un paisaje montañoso con un lago cristalino en el centro. Se pueden observar árboles de pino en el primer plano y montañas nevadas al fondo. Los colores dominantes son el azul del agua, el verde de la vegetación y el blanco de la nieve."
}
```

**Error (400 Bad Request):**
```json
{
  "success": false,
  "error": "No image file provided"
}
```

**Error (413 Payload Too Large):**
```json
{
  "success": false,
  "error": "File size exceeds maximum allowed size (5MB)"
}
```

**Error (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to analyze image with Gemini API"
}
```

#### Ejemplo cURL

```bash
curl -X POST http://localhost:3000/api/analyze-image \
  -F "image=@/path/to/image.jpg" \
  -F "prompt=Describe esta imagen en español"
```

#### Ejemplo JavaScript (Fetch)

```javascript
const formData = new FormData();
formData.append('image', fileInput.files[0]);
formData.append('prompt', 'Describe los objetos principales en esta imagen');

const response = await fetch('http://localhost:3000/api/analyze-image', {
  method: 'POST',
  body: formData
});

const data = await response.json();
console.log(data.result);
```

#### Ejemplo Python (requests)

```python
import requests

url = 'http://localhost:3000/api/analyze-image'
files = {'image': open('image.jpg', 'rb')}
data = {'prompt': 'Analiza esta imagen'}

response = requests.post(url, files=files, data=data)
result = response.json()
print(result['result'])
```

---

### POST /api/analyze-audio

Analiza un archivo de audio usando Google Gemini AI (transcripción, análisis de contenido).

#### Request

**Headers:**
```
Content-Type: multipart/form-data
```

**Body (FormData):**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `audio` | File | ✅ Sí | Archivo de audio (MP3, WAV, OGG, WebM) |
| `prompt` | String | ❌ No | Prompt personalizado (máx 2000 caracteres) |

**Restricciones del archivo:**
- Tamaño máximo: **10MB**
- Formatos aceptados: `audio/mpeg`, `audio/mp3`, `audio/wav`, `audio/ogg`, `audio/webm`

#### Response

**Success (200 OK):**
```json
{
  "success": true,
  "result": "Transcripción: 'Hola, bienvenidos a nuestro podcast sobre inteligencia artificial. Hoy hablaremos sobre modelos de lenguaje grandes...'\n\nResumen: El audio es un podcast que discute sobre inteligencia artificial, enfocándose en modelos de lenguaje grandes y sus aplicaciones prácticas."
}
```

**Error (400 Bad Request):**
```json
{
  "success": false,
  "error": "No audio file provided"
}
```

**Error (413 Payload Too Large):**
```json
{
  "success": false,
  "error": "File size exceeds maximum allowed size (10MB)"
}
```

**Error (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to analyze audio with Gemini API"
}
```

#### Ejemplo cURL

```bash
curl -X POST http://localhost:3000/api/analyze-audio \
  -F "audio=@/path/to/audio.mp3" \
  -F "prompt=Transcribe este audio y resume los puntos principales"
```

#### Ejemplo JavaScript (Fetch)

```javascript
const formData = new FormData();
formData.append('audio', audioFileInput.files[0]);
formData.append('prompt', 'Transcribe este audio completamente');

const response = await fetch('http://localhost:3000/api/analyze-audio', {
  method: 'POST',
  body: formData
});

const data = await response.json();
console.log(data.result);
```

#### Ejemplo Python (requests)

```python
import requests

url = 'http://localhost:3000/api/analyze-audio'
files = {'audio': open('podcast.mp3', 'rb')}
data = {'prompt': 'Transcribe y resume este audio'}

response = requests.post(url, files=files, data=data)
result = response.json()
print(result['result'])
```

---

### GET /api/health

Verifica el estado de salud del servidor y la conexión con Gemini API.

#### Request

**Headers:** Ninguno requerido

**Query Parameters:** Ninguno

#### Response

**Success (200 OK):**
```json
{
  "success": true,
  "status": "healthy",
  "services": {
    "gemini": "connected"
  }
}
```

**Error (503 Service Unavailable):**
```json
{
  "success": false,
  "status": "unhealthy",
  "error": "Service unavailable"
}
```

#### Ejemplo cURL

```bash
curl http://localhost:3000/api/health
```

#### Ejemplo JavaScript (Fetch)

```javascript
const response = await fetch('http://localhost:3000/api/health');
const health = await response.json();

if (health.services.gemini === 'connected') {
  console.log('✅ Gemini API is ready');
} else {
  console.log('❌ Gemini API is not available');
}
```

---

## Códigos de Estado

| Código | Descripción | Cuándo ocurre |
|--------|-------------|---------------|
| `200` | OK | Solicitud exitosa |
| `400` | Bad Request | Falta archivo requerido, prompt muy largo, tipo de archivo inválido |
| `413` | Payload Too Large | Archivo excede el tamaño máximo |
| `500` | Internal Server Error | Error en Gemini API o error del servidor |
| `503` | Service Unavailable | Servicio no disponible (health check) |

---

## Ejemplos de Uso

### Ejemplo 1: Análisis de Imagen Completo

```javascript
// HTML
<input type="file" id="imageInput" accept="image/*">
<textarea id="promptInput"></textarea>
<button onclick="analyzeImage()">Analizar</button>
<div id="result"></div>

// JavaScript
async function analyzeImage() {
  const imageFile = document.getElementById('imageInput').files[0];
  const prompt = document.getElementById('promptInput').value;

  if (!imageFile) {
    alert('Por favor selecciona una imagen');
    return;
  }

  const formData = new FormData();
  formData.append('image', imageFile);
  if (prompt) formData.append('prompt', prompt);

  try {
    const response = await fetch('/api/analyze-image', {
      method: 'POST',
      body: formData
    });

    const data = await response.json();

    if (data.success) {
      document.getElementById('result').textContent = data.result;
    } else {
      alert('Error: ' + data.error);
    }
  } catch (error) {
    alert('Error de conexión: ' + error.message);
  }
}
```

### Ejemplo 2: Análisis de Audio con Node.js

```javascript
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

async function analyzeAudio(audioPath, customPrompt) {
  const form = new FormData();
  form.append('audio', fs.createReadStream(audioPath));

  if (customPrompt) {
    form.append('prompt', customPrompt);
  }

  try {
    const response = await axios.post(
      'http://localhost:3000/api/analyze-audio',
      form,
      {
        headers: form.getHeaders(),
        maxBodyLength: Infinity,
        maxContentLength: Infinity
      }
    );

    return response.data;
  } catch (error) {
    if (error.response) {
      throw new Error(error.response.data.error);
    }
    throw error;
  }
}

// Uso
analyzeAudio('./podcast.mp3', 'Resume los temas principales')
  .then(result => console.log(result.result))
  .catch(err => console.error(err.message));
```

### Ejemplo 3: Health Check con Retry

```javascript
async function checkHealthWithRetry(maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch('/api/health');
      const health = await response.json();

      if (health.success && health.services.gemini === 'connected') {
        console.log('✅ API is healthy');
        return true;
      }

      console.log(`⚠️ Retry ${i + 1}/${maxRetries}...`);
      await new Promise(resolve => setTimeout(resolve, 2000));
    } catch (error) {
      console.error(`❌ Attempt ${i + 1} failed:`, error.message);
    }
  }

  throw new Error('API health check failed after retries');
}
```

---

## Límites y Restricciones

### Tamaños de Archivo

| Tipo | Tamaño Máximo | Configurable en |
|------|---------------|-----------------|
| Imagen | 5 MB | `MAX_FILE_SIZE` (env) |
| Audio | 10 MB | `MAX_AUDIO_SIZE` (env) |

### Formatos Soportados

**Imágenes:**
- `image/jpeg` (.jpg, .jpeg)
- `image/png` (.png)
- `image/webp` (.webp)
- `image/gif` (.gif)

**Audio:**
- `audio/mpeg` (.mp3)
- `audio/mp3` (.mp3)
- `audio/wav` (.wav)
- `audio/ogg` (.ogg)
- `audio/webm` (.webm)

### Prompts

- **Longitud máxima**: 2000 caracteres
- **Encoding**: UTF-8
- **Prompt por defecto (imagen)**: "Describe esta imagen en detalle, identificando objetos, escenas, colores, y cualquier texto visible."
- **Prompt por defecto (audio)**: "Transcribe este audio y proporciona un resumen de su contenido."

### Rate Limits

**Nota**: Los rate limits dependen de tu plan de Gemini API:
- **Free tier**: ~15 requests/min (límites de Google)
- Para producción, implementar rate limiting en el servidor

---

## Manejo de Errores

### Estructura de Error

Todos los errores siguen esta estructura:

```json
{
  "success": false,
  "error": "Mensaje descriptivo del error"
}
```

### Tipos de Errores Comunes

#### 1. Archivo No Proporcionado

```json
{
  "success": false,
  "error": "No image file provided"
}
```

**Solución**: Asegúrate de enviar el archivo con el nombre de campo correcto (`image` o `audio`)

#### 2. Tipo de Archivo Inválido

```json
{
  "success": false,
  "error": "Invalid file type. Allowed types: image/jpeg, image/png, image/webp, image/gif"
}
```

**Solución**: Usa solo los formatos soportados

#### 3. Archivo Muy Grande

```json
{
  "success": false,
  "error": "File size exceeds maximum allowed size (5MB)"
}
```

**Solución**: Reduce el tamaño del archivo o comprime antes de enviar

#### 4. Prompt Muy Largo

```json
{
  "success": false,
  "error": "Prompt exceeds maximum length of 2000 characters"
}
```

**Solución**: Reduce la longitud del prompt

#### 5. Error de Gemini API

```json
{
  "success": false,
  "error": "Failed to analyze image with Gemini API"
}
```

**Posibles causas**:
- Cuota de API excedida (error 429)
- API key inválida
- Problemas de conectividad
- Modelo no disponible

**Solución**: Verifica tu cuota en [Google AI Studio](https://makersuite.google.com/app/apikey)

### Ejemplo de Manejo de Errores

```javascript
async function safeAnalyze(file, type = 'image') {
  const endpoint = type === 'image'
    ? '/api/analyze-image'
    : '/api/analyze-audio';

  const formData = new FormData();
  formData.append(type, file);

  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      body: formData
    });

    const data = await response.json();

    if (!data.success) {
      // Error de la API
      switch (response.status) {
        case 400:
          throw new Error(`Solicitud inválida: ${data.error}`);
        case 413:
          throw new Error('Archivo demasiado grande');
        case 500:
          throw new Error('Error del servidor. Intenta más tarde');
        default:
          throw new Error(data.error);
      }
    }

    return data.result;
  } catch (error) {
    if (error.message.includes('Failed to fetch')) {
      throw new Error('Error de red. Verifica tu conexión');
    }
    throw error;
  }
}
```

---

## Notas Adicionales

### CORS

La API está configurada para aceptar solicitudes desde cualquier origen (`*`).

Para producción, configura `CORS_ORIGIN` en `.env`:

```env
CORS_ORIGIN=https://tu-frontend.com
```

### Seguridad

**Recomendaciones para producción**:

1. **Implementar autenticación**: API keys, JWT, OAuth
2. **Rate limiting**: Limitar requests por IP/usuario
3. **Validación adicional**: Escaneo de malware en archivos
4. **HTTPS**: Solo en conexiones seguras
5. **CORS restrictivo**: Solo dominios autorizados
6. **Logging**: Registrar todas las solicitudes

### Optimización

**Para mejorar el rendimiento**:

1. **Compresión**: Usa gzip en respuestas
2. **Cache**: Cachea resultados comunes (opcional)
3. **CDN**: Para archivos estáticos
4. **Monitoreo**: Usa herramientas como New Relic o Datadog

---

## Soporte

**Documentación adicional**:
- [Gemini API Docs](https://ai.google.dev/docs)
- [Rate Limits](https://ai.google.dev/gemini-api/docs/rate-limits)
- [Modelos Disponibles](https://ai.google.dev/models/gemini)

**Errores comunes**: Ver sección [Troubleshooting](README.md#troubleshooting) en README.md

---

**Última actualización**: Noviembre 2025
**Versión de la API**: 1.0.0
**Modelo Gemini**: gemini-1.5-flash (configurable)
