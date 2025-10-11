# 🧭 Sistema de Navegación - Solea

## Índice
1. [Arquitectura de Navegación](#arquitectura-de-navegación)
2. [Estructura de Rutas](#estructura-de-rutas)
3. [Grafos de Navegación](#grafos-de-navegación)
4. [Bottom Navigation Bar](#bottom-navigation-bar)
5. [Flujos de Navegación](#flujos-de-navegación)
6. [Gestión de Estado](#gestión-de-estado)
7. [Animaciones y Transiciones](#animaciones-y-transiciones)
8. [Ejemplos de Uso](#ejemplos-de-uso)

---

## 🏗️ Arquitectura de Navegación

La aplicación Solea utiliza **Jetpack Navigation Compose** con una arquitectura de navegación modular basada en dos grafos principales:

### Componentes Principales

```
MainActivity.kt
    ├── AppNavigation() [Root]
    │   ├── Auth Navigation Graph (No autenticado)
    │   └── Main Navigation Graph (Autenticado)
    │       └── Bottom Navigation Bar
    └── Theme & Configuration
```

### Archivos de Navegación

```
app/src/main/java/com/grupo03/solea/ui/navigation/
├── Routes.kt                    # Definición de todas las rutas
├── AuthNavigationGraph.kt       # Grafo de autenticación
└── MainNavigationGraph.kt       # Grafo principal de la app
```

### Archivo Principal

```
app/src/main/java/com/grupo03/solea/
└── MainActivity.kt              # Punto de entrada y configuración
```

---

## 🛣️ Estructura de Rutas

### Definición de Rutas (`Routes.kt`)

```kotlin
object AuthRoutes {
    const val PREFIX = "auth"
    const val LOGIN = "auth/login"
    const val SIGN_UP = "auth/sign_up"
    const val WELCOME = "auth/welcome"
}

object AppRoutes {
    const val PREFIX = "solea"
    const val HOME = "solea/home"
    const val HISTORY = "solea/history"
    const val SAVINGS = "solea/savings"
    const val SHOPPING_LIST = "solea/shopping_list"
    const val SETTINGS = "solea/settings"
    const val NEW_CATEGORY = "solea/new_category"
    const val NEW_MOVEMENT = "solea/new_movement"
    const val BUDGET_LIMITS = "solea/budget_limits"
    const val EDIT_BUDGET = "solea/edit_budget"
    const val SCAN_RECEIPT = "solea/scan_receipt"
    const val LOADING_SCAN = "solea/loading_scan"
    const val EDIT_SCANNED_RECEIPT = "solea/edit_scanned_receipt"
}
```

### Organización por Prefijos

#### 🔐 Rutas de Autenticación (`auth/`)
| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `auth/welcome` | WelcomeScreen | Pantalla de bienvenida inicial |
| `auth/login` | SignInScreen | Inicio de sesión |
| `auth/sign_up` | SignUpScreen | Registro de nuevo usuario |

#### 💰 Rutas Principales de la App (`solea/`)
| Ruta | Pantalla | Descripción | En Bottom Nav |
|------|----------|-------------|---------------|
| `solea/home` | HomeScreen | Pantalla principal | ✅ |
| `solea/history` | HistoryScreen | Historial de movimientos | ✅ |
| `solea/savings` | SavingsScreen | Ahorros y presupuestos | ✅ |
| `solea/shopping_list` | ShoppingListScreen | Lista de compras | ✅ |
| `solea/settings` | SettingsScreen | Configuración | ✅ |

#### 📝 Rutas de Formularios (`solea/`)
| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `solea/new_category` | NewCategoryFormScreen | Crear nueva categoría |
| `solea/new_movement` | NewMovementFormScreen | Crear nuevo movimiento |

#### 💵 Rutas de Presupuesto (`solea/`)
| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `solea/budget_limits` | BudgetLimitsScreen | Ver límites de presupuesto |
| `solea/edit_budget` | EditBudgetForm | Editar presupuesto de categoría |

#### 📸 Rutas de Escaneo (`solea/`)
| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `solea/scan_receipt` | ScanReceiptScreen | Cámara para escanear |
| `solea/loading_scan` | LoadingScanScreen | Análisis con AI |
| `solea/edit_scanned_receipt` | EditScannedReceiptScreen | Edición de datos |

---

## 📊 Grafos de Navegación

### 1. Grafo de Autenticación (`AuthNavigationGraph.kt`)

```kotlin
fun NavGraphBuilder.authNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = AuthRoutes.WELCOME,  // Destino inicial
        route = AuthRoutes.PREFIX               // Ruta del grafo
    ) {
        // Welcome Screen
        composable(route = AuthRoutes.WELCOME) {
            WelcomeScreen(
                navigateToSignIn = {
                    navController.navigate(AuthRoutes.LOGIN)
                },
                navigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                }
            )
        }
        
        // Login Screen (con animaciones)
        composable(
            route = AuthRoutes.LOGIN,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            }
        ) {
            SignInScreen(
                viewModel = authViewModel,
                navigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                }
            )
        }
        
        // Sign Up Screen (con animaciones)
        composable(
            route = AuthRoutes.SIGN_UP,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            }
        ) {
            SignUpScreen(
                viewModel = authViewModel,
                navigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
```

#### Características del Grafo Auth:
- ✅ **Destino inicial:** `auth/welcome`
- ✅ **Animaciones personalizadas** de deslizamiento
- ✅ **Navegación fluida** entre login y registro
- ✅ **Sin Bottom Navigation Bar**

---

### 2. Grafo Principal de la App (`MainNavigationGraph.kt`)

```kotlin
fun NavGraphBuilder.mainNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel,
    movementsViewModel: MovementsViewModel,
    scanReceiptViewModel: ScanReceiptViewModel,
    contentPadding: PaddingValues,
) {
    navigation(
        startDestination = AppRoutes.HOME,  // Destino inicial
        route = AppRoutes.PREFIX            // Ruta del grafo
    ) {
        // Pantalla Principal (Home)
        composable(AppRoutes.HOME) {
            HomeScreen(
                homeViewModel = koinViewModel(),
                movementsViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateToNewMovement = {
                    navController.navigate(AppRoutes.NEW_MOVEMENT)
                },
                onNavigateToNewCategory = {
                    navController.navigate(AppRoutes.NEW_CATEGORY)
                },
                onNavigateToScanReceipt = {
                    navController.navigate(AppRoutes.SCAN_RECEIPT)
                }
            )
        }
        
        // Historial de Movimientos
        composable(AppRoutes.HISTORY) {
            HistoryScreen(
                historyViewModel = koinViewModel(),
                authViewModel = authViewModel,
                movementsViewModel = movementsViewModel,
                modifier = Modifier.padding(contentPadding)
            )
        }
        
        // Ahorros y Presupuestos
        composable(AppRoutes.SAVINGS) {
            SavingsScreen(
                authViewModel = authViewModel,
                budgetViewModel = budgetViewModel,
                movementsViewModel = koinViewModel(),
                onNavigateToBudgetLimits = {
                    navController.navigate(AppRoutes.BUDGET_LIMITS)
                },
                onEditBudget = { categoryName ->
                    // Buscar categoría y seleccionarla
                    val budgetLimitsState = budgetViewModel.budgetLimitsScreenState.value
                    val category = budgetLimitsState.categoriesWithBudgets
                        .find { it.first.name == categoryName }?.first

                    if (category != null) {
                        budgetViewModel.onSelectCategory(category)
                        navController.navigate(AppRoutes.EDIT_BUDGET)
                    }
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        
        // Lista de Compras
        composable(AppRoutes.SHOPPING_LIST) {
            ShoppingListScreen(
                modifier = Modifier.padding(contentPadding)
            )
        }
        
        // Configuración
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                authViewModel = authViewModel,
                settingsViewModel = koinViewModel(),
                onNavigateToBudgetLimits = {
                    navController.navigate(AppRoutes.BUDGET_LIMITS)
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        
        // Formulario de Nueva Categoría
        composable(AppRoutes.NEW_CATEGORY) {
            NewCategoryFormScreen(
                newCategoryFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Formulario de Nuevo Movimiento
        composable(AppRoutes.NEW_MOVEMENT) {
            NewMovementFormScreen(
                newMovementFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNewCategory = {
                    navController.navigate(AppRoutes.NEW_CATEGORY)
                }
            )
        }
        
        // Límites de Presupuesto
        composable(AppRoutes.BUDGET_LIMITS) {
            val authState = authViewModel.authState.collectAsState()
            val userId = authState.value.user?.uid ?: ""

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    budgetViewModel.fetchBudgetsAndCategories(userId)
                }
            }

            BudgetLimitsScreen(
                budgetViewModel = budgetViewModel,
                authViewModel = authViewModel,
                onSelectCategory = { category ->
                    budgetViewModel.onSelectCategory(category)
                    navController.navigate(AppRoutes.EDIT_BUDGET)
                },
                onBack = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        
        // Editar Presupuesto
        composable(AppRoutes.EDIT_BUDGET) {
            val editBudgetFormState = budgetViewModel.editBudgetFormState.collectAsState()
            val authState = authViewModel.authState.collectAsState()
            val userId = authState.value.user?.uid ?: ""

            LaunchedEffect(Unit) {
                budgetViewModel.fetchStatuses()
            }

            EditBudgetForm(
                budgetFormState = editBudgetFormState.value,
                onAmountChange = budgetViewModel::onAmountChange,
                onSave = {
                    budgetViewModel.saveBudget(userId) {
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    budgetViewModel.clearForm()
                    navController.popBackStack()
                },
                onDelete = if (editBudgetFormState.value.existingBudget != null) {
                    {
                        budgetViewModel.deleteBudget(
                            userId,
                            editBudgetFormState.value.existingBudget!!.id
                        ) {
                            navController.popBackStack()
                        }
                    }
                } else null,
                modifier = Modifier.padding(contentPadding)
            )
        }
        
        // Escanear Recibo (Cámara)
        composable(AppRoutes.SCAN_RECEIPT) {
            ScanReceiptScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                },
                onNavigateToLoading = {
                    navController.navigate(AppRoutes.LOADING_SCAN)
                }
            )
        }
        
        // Procesando Escaneo
        composable(AppRoutes.LOADING_SCAN) {
            LoadingScanScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                onNavigateToEdit = {
                    navController.navigate(AppRoutes.EDIT_SCANNED_RECEIPT) {
                        popUpTo(AppRoutes.SCAN_RECEIPT) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                }
            )
        }
        
        // Editar Recibo Escaneado
        composable(AppRoutes.EDIT_SCANNED_RECEIPT) {
            EditScannedReceiptScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                newMovementFormViewModel = koinViewModel(),
                newCategoryFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                },
                onSuccess = {
                    scanReceiptViewModel.clearState()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}
```

#### Características del Grafo Principal:
- ✅ **Destino inicial:** `solea/home`
- ✅ **Integración con Koin** para ViewModels
- ✅ **Gestión de ContentPadding** para Bottom Bar
- ✅ **Limpieza de estado** en navegación
- ✅ **LaunchedEffect** para cargas iniciales

---

## 📱 Bottom Navigation Bar

### Componente (`BottomNavigationBar.kt`)

```kotlin
sealed class BottomNavItem(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    object Home : BottomNavItem(
        AppRoutes.HOME, 
        R.drawable.icons_home,
        R.string.nav_home_title
    )

    object History : BottomNavItem(
        AppRoutes.HISTORY, 
        R.drawable.icons_history,
        R.string.nav_history_title
    )

    object Savings : BottomNavItem(
        AppRoutes.SAVINGS, 
        R.drawable.icons_savings,
        R.string.nav_savings_title
    )

    object ShoppingList : BottomNavItem(
        AppRoutes.SHOPPING_LIST, 
        R.drawable.icons_shopping_list,
        R.string.nav_shopping_list_title
    )

    object Settings : BottomNavItem(
        AppRoutes.SETTINGS, 
        R.drawable.icons_settings,
        R.string.nav_settings_title
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val items = listOf(
        BottomNavItem.ShoppingList,
        BottomNavItem.History,
        BottomNavItem.Home,
        BottomNavItem.Savings,
        BottomNavItem.Settings,
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.title)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.title),
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Restaurar el grafo al destino inicial
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Evitar múltiples copias de la misma pantalla
                        launchSingleTop = true
                        // Restaurar estado previo
                        restoreState = true
                    }
                }
            )
        }
    }
}
```

### Orden de los Items

```
┌──────────────┬──────────┬────────┬──────────┬──────────┐
│ ShoppingList │ History  │  Home  │ Savings  │ Settings │
│      🛒      │    📜    │   🏠   │    💰    │    ⚙️    │
└──────────────┴──────────┴────────┴──────────┴──────────┘
```

### Características del Bottom Nav:
- ✅ **5 pantallas principales** accesibles
- ✅ **Iconos personalizados** desde recursos
- ✅ **Selección visual** de la ruta activa
- ✅ **Preservación de estado** al cambiar de tab
- ✅ **LaunchSingleTop** para evitar duplicados

---

## 🔄 Flujos de Navegación

### 1. Flujo de Autenticación

```
┌─────────────┐
│   Welcome   │
│   Screen    │
└──────┬──────┘
       │
       ├─────────────┐
       │             │
       ▼             ▼
┌──────────┐   ┌──────────┐
│  Login   │◄─►│ Sign Up  │
│  Screen  │   │  Screen  │
└──────┬───┘   └──────────┘
       │
       │ [Auth exitosa]
       ▼
┌──────────────┐
│     Main     │
│  App (Home)  │
└──────────────┘
```

**Código:**
```kotlin
// En AppNavigation()
if (authState.value.user == null) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.PREFIX
    ) {
        authNavigationGraph(navController, authViewModel)
    }
} else {
    MainAppContent(...)
}
```

---

### 2. Flujo de Creación de Movimiento

```
┌──────────┐
│   Home   │
│  Screen  │
└──────┬───┘
       │ [FAB: Nuevo Movimiento]
       ▼
┌─────────────────┐
│ NewMovementForm │
└────────┬────────┘
         │
         │ [Necesita nueva categoría]
         ▼
┌─────────────────┐
│ NewCategoryForm │
└────────┬────────┘
         │ [Categoría creada]
         ▼
┌─────────────────┐
│ NewMovementForm │
│ (Actualizado)   │
└────────┬────────┘
         │ [Guardar]
         ▼
┌──────────┐
│   Home   │
│  Screen  │
└──────────┘
```

**Código:**
```kotlin
// En HomeScreen
onNavigateToNewMovement = {
    navController.navigate(AppRoutes.NEW_MOVEMENT)
}

// En NewMovementFormScreen
onNavigateToNewCategory = {
    navController.navigate(AppRoutes.NEW_CATEGORY)
}

// Al guardar
onNavigateBack = {
    navController.popBackStack()
}
```

---

### 3. Flujo de Escaneo de Recibos

```
┌──────────┐
│   Home   │
│  Screen  │
└──────┬───┘
       │ [FAB: Escanear Recibo]
       ▼
┌─────────────────┐
│  ScanReceipt    │
│  (Cámara)       │
└────────┬────────┘
         │ [Foto capturada]
         ▼
┌─────────────────┐
│  LoadingScan    │
│  (Procesando)   │
└────────┬────────┘
         │ [Análisis completo]
         ▼
┌─────────────────┐
│ EditScanned     │
│   Receipt       │
└────────┬────────┘
         │ [Confirmar]
         ▼
┌──────────┐
│   Home   │
│  Screen  │
└──────────┘
```

**Código:**
```kotlin
// En ScanReceiptScreen
onNavigateToLoading = {
    navController.navigate(AppRoutes.LOADING_SCAN)
}

// En LoadingScanScreen
onNavigateToEdit = {
    navController.navigate(AppRoutes.EDIT_SCANNED_RECEIPT) {
        popUpTo(AppRoutes.SCAN_RECEIPT) { inclusive = false }
    }
}

// En EditScannedReceiptScreen
onSuccess = {
    scanReceiptViewModel.clearState()
    navController.navigate(AppRoutes.HOME) {
        popUpTo(AppRoutes.HOME) { inclusive = false }
    }
}
```

---

### 4. Flujo de Gestión de Presupuestos

```
┌──────────┐
│ Savings  │
│  Screen  │
└──────┬───┘
       │ [Ver presupuestos]
       ▼
┌─────────────────┐
│  BudgetLimits   │
│     Screen      │
└────────┬────────┘
         │ [Seleccionar categoría]
         ▼
┌─────────────────┐
│  EditBudget     │
│     Form        │
└────────┬────────┘
         │ [Guardar/Eliminar]
         ▼
┌─────────────────┐
│  BudgetLimits   │
│  (Actualizado)  │
└────────┬────────┘
         │ [Volver]
         ▼
┌──────────┐
│ Savings  │
│  Screen  │
└──────────┘
```

**Código:**
```kotlin
// En SavingsScreen
onNavigateToBudgetLimits = {
    navController.navigate(AppRoutes.BUDGET_LIMITS)
}

onEditBudget = { categoryName ->
    val category = budgetLimitsState.categoriesWithBudgets
        .find { it.first.name == categoryName }?.first
    
    if (category != null) {
        budgetViewModel.onSelectCategory(category)
        navController.navigate(AppRoutes.EDIT_BUDGET)
    }
}

// En EditBudgetForm
onSave = {
    budgetViewModel.saveBudget(userId) {
        navController.popBackStack()
    }
}
```

---

## 📦 Gestión de Estado en Navegación

### 1. Estado de Autenticación

```kotlin
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState = authViewModel.authState.collectAsState()
    
    // Cambio automático de grafo según autenticación
    if (authState.value.user == null) {
        // Mostrar Auth Navigation
    } else {
        // Mostrar Main Navigation
    }
}
```

**Ventajas:**
- ✅ Cambio automático de navegación al autenticar/desautenticar
- ✅ No requiere navegación manual
- ✅ Estado reactivo con Flow

---

### 2. Preservación de Estado en Bottom Nav

```kotlin
NavigationBarItem(
    onClick = {
        navController.navigate(item.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true  // Guarda el estado
            }
            launchSingleTop = true
            restoreState = true   // Restaura el estado
        }
    }
)
```

**Comportamiento:**
- ✅ Al cambiar de tab, se guarda el scroll y estado UI
- ✅ Al volver, se restaura exactamente donde estaba
- ✅ Evita recargas innecesarias

---

### 3. Limpieza de Estado al Navegar

```kotlin
// En ScanReceiptScreen
onNavigateBack = {
    scanReceiptViewModel.clearState()  // Limpia estado
    navController.popBackStack()
}

onSuccess = {
    scanReceiptViewModel.clearState()  // Limpia estado
    navController.navigate(AppRoutes.HOME) {
        popUpTo(AppRoutes.HOME) { inclusive = false }
    }
}
```

**Por qué es importante:**
- ✅ Evita datos obsoletos en siguiente uso
- ✅ Libera memoria
- ✅ Previene bugs de estado antiguo

---

### 4. Carga Inicial con LaunchedEffect

```kotlin
composable(AppRoutes.BUDGET_LIMITS) {
    val authState = authViewModel.authState.collectAsState()
    val userId = authState.value.user?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            budgetViewModel.fetchBudgetsAndCategories(userId)
        }
    }

    BudgetLimitsScreen(...)
}
```

**Ventajas:**
- ✅ Carga automática al entrar a la pantalla
- ✅ Se ejecuta solo cuando cambia el userId
- ✅ No bloquea la UI

---

## 🎨 Animaciones y Transiciones

### Transiciones en Auth Navigation

```kotlin
composable(
    route = AuthRoutes.LOGIN,
    enterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
    },
    exitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
    },
    popEnterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
    },
    popExitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
    }
)
```

### Tipos de Transiciones Disponibles

| Transición | Uso | Dirección |
|------------|-----|-----------|
| `enterTransition` | Al entrar a la pantalla | → |
| `exitTransition` | Al salir de la pantalla | ← |
| `popEnterTransition` | Al volver (back) | ← |
| `popExitTransition` | Al salir por back | → |

### Direcciones de Deslizamiento

```kotlin
// De derecha a izquierda
SlideDirection.Left

// De izquierda a derecha
SlideDirection.Right

// De arriba hacia abajo
SlideDirection.Down

// De abajo hacia arriba
SlideDirection.Up
```

---

## 🔧 Configuración en MainActivity

### Estructura Completa

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settingsState = settingsViewModel.uiState.collectAsState()

            SoleaTheme(darkTheme = settingsState.value.isDarkTheme) {
                // Configurar color de status bar
                val surfaceColor = MaterialTheme.colorScheme.surface
                val isDarkTheme = settingsState.value.isDarkTheme

                SideEffect {
                    window.statusBarColor = surfaceColor.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !isDarkTheme
                    }
                }
                
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}
```

### AppNavigation - Punto de Entrada

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState = authViewModel.authState.collectAsState()
    val movementsViewModel: MovementsViewModel = koinViewModel()
    val budgetViewModel: BudgetViewModel = koinViewModel()

    if (authState.value.user == null) {
        // Auth Navigation
        NavHost(
            navController = navController,
            startDestination = AuthRoutes.PREFIX
        ) {
            authNavigationGraph(navController, authViewModel)
        }
    } else {
        // Main App Navigation
        movementsViewModel.fetchMovements(userId = authState.value.user!!.uid)
        MainAppContent(
            navController = navController,
            authViewModel = authViewModel,
            budgetViewModel = budgetViewModel,
            movementsViewModel = movementsViewModel
        )
    }
}
```

### MainAppContent - App Principal

```kotlin
@Composable
fun MainAppContent(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel,
    movementsViewModel: MovementsViewModel
) {
    val scanReceiptViewModel: ScanReceiptViewModel = koinViewModel()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.PREFIX,
            modifier = Modifier.padding(paddingValues)
        ) {
            mainNavigationGraph(
                navController = navController,
                authViewModel = authViewModel,
                budgetViewModel = budgetViewModel,
                movementsViewModel = movementsViewModel,
                scanReceiptViewModel = scanReceiptViewModel,
                contentPadding = PaddingValues(0.dp)
            )
        }
    }
}
```

---

## 📝 Ejemplos de Uso

### Ejemplo 1: Navegación Simple

```kotlin
// Navegar a una pantalla
navController.navigate(AppRoutes.SETTINGS)

// Volver atrás
navController.popBackStack()
```

### Ejemplo 2: Navegación con Limpieza de Pila

```kotlin
// Navegar y limpiar todo hasta Home
navController.navigate(AppRoutes.HOME) {
    popUpTo(AppRoutes.HOME) { 
        inclusive = true  // Incluye el destino en la limpieza
    }
}
```

### Ejemplo 3: Navegación con Argumentos (Estado Compartido)

```kotlin
// En SavingsScreen - Preparar datos antes de navegar
onEditBudget = { categoryName ->
    val category = budgetLimitsState.categoriesWithBudgets
        .find { it.first.name == categoryName }?.first
    
    if (category != null) {
        // Guardar en ViewModel compartido
        budgetViewModel.onSelectCategory(category)
        
        // Navegar
        navController.navigate(AppRoutes.EDIT_BUDGET)
    }
}

// En EditBudgetForm - Leer del ViewModel
composable(AppRoutes.EDIT_BUDGET) {
    val editBudgetFormState = budgetViewModel.editBudgetFormState.collectAsState()
    
    EditBudgetForm(
        budgetFormState = editBudgetFormState.value,
        // ... resto de parámetros
    )
}
```

### Ejemplo 4: Navegación Condicional

```kotlin
// Solo navegar si hay datos válidos
Button(
    onClick = {
        if (selectedCategory != null && total.isNotBlank()) {
            newMovementFormViewModel.createMovement(userId) {
                navController.navigate(AppRoutes.HOME) {
                    popUpTo(AppRoutes.HOME) { inclusive = false }
                }
            }
        }
    },
    enabled = selectedCategory != null && total.isNotBlank()
) {
    Text("Guardar Movimiento")
}
```

### Ejemplo 5: Navegación desde ViewModel (No Recomendado)

```kotlin
// ❌ EVITAR: ViewModels no deben tener NavController
class MyViewModel(private val navController: NavHostController) : ViewModel() {
    fun doSomething() {
        navController.navigate(...)  // NO HACER ESTO
    }
}

// ✅ CORRECTO: Usar callbacks
class MyViewModel : ViewModel() {
    fun doSomething(onSuccess: () -> Unit) {
        // ... lógica
        onSuccess()  // Callback para navegar
    }
}

// En la UI
Button(
    onClick = {
        viewModel.doSomething {
            navController.navigate(AppRoutes.HOME)
        }
    }
)
```

---

## 🛡️ Mejores Prácticas

### 1. Organización de Rutas
- ✅ Usar objetos `Routes` para centralizar
- ✅ Prefijos para agrupar (`auth/`, `solea/`)
- ✅ Nombres descriptivos y consistentes

### 2. Gestión de Estado
- ✅ Usar ViewModels compartidos para datos entre pantallas
- ✅ Limpiar estado al salir de flujos
- ✅ `LaunchedEffect` para cargas iniciales

### 3. Navegación
- ✅ Siempre usar `popBackStack()` para volver
- ✅ `popUpTo` para limpiar stack
- ✅ `launchSingleTop` en Bottom Nav
- ✅ Callbacks en ViewModels, no NavController

### 4. Performance
- ✅ `saveState` y `restoreState` en Bottom Nav
- ✅ Lazy loading con Koin `koinViewModel()`
- ✅ No recargar datos innecesariamente

### 5. UX
- ✅ Animaciones suaves en transiciones importantes
- ✅ Feedback visual de ruta activa
- ✅ Manejo de backpress consistente

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: Bottom Nav Duplica Pantallas

**Síntoma:** Al hacer clic varias veces en el mismo tab, se apilan copias de la pantalla.

**Solución:**
```kotlin
onClick = {
    navController.navigate(item.route) {
        launchSingleTop = true  // Previene duplicados
    }
}
```

---

### Problema 2: Estado se Pierde al Cambiar Tabs

**Síntoma:** Al cambiar de tab, pierdo el scroll o el estado de la pantalla.

**Solución:**
```kotlin
onClick = {
    navController.navigate(item.route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true  // Guardar estado
        }
        restoreState = true  // Restaurar estado
    }
}
```

---

### Problema 3: ViewModel Retiene Datos Viejos

**Síntoma:** Al volver a una pantalla, muestra datos del uso anterior.

**Solución:**
```kotlin
onNavigateBack = {
    viewModel.clearState()  // Limpiar antes de salir
    navController.popBackStack()
}
```

---

### Problema 4: Navegación No Funciona Después de Auth

**Síntoma:** Después de login, no cambia a la app principal.

**Solución:** Verificar que el `authState` se actualice correctamente:
```kotlin
if (authState.value.user == null) {
    // Auth Navigation
} else {
    // Main Navigation
}
```

---

### Problema 5: Animaciones Entrecortadas

**Síntoma:** Las transiciones se ven lentas o entrecortadas.

**Solución:** Simplificar las animaciones o usar `rememberSaveable` para estados pesados:
```kotlin
var myState by rememberSaveable { mutableStateOf("") }
```

---

## 📊 Diagrama Completo de Navegación

```
                    ┌─────────────────────┐
                    │   MainActivity      │
                    │   AppNavigation()   │
                    └──────────┬──────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
                ▼                             ▼
      ┌──────────────────┐         ┌──────────────────┐
      │  Auth Navigation │         │  Main Navigation │
      │     (PREFIX)     │         │     (PREFIX)     │
      └────────┬─────────┘         └────────┬─────────┘
               │                             │
    ┌──────────┼──────────┐                 │
    │          │          │                 │
    ▼          ▼          ▼                 ▼
┌────────┐ ┌──────┐ ┌────────┐   ┌──────────────────┐
│Welcome │ │Login │ │Sign Up │   │  Scaffold with   │
└────────┘ └──────┘ └────────┘   │  Bottom Nav Bar  │
                                  └────────┬─────────┘
                                           │
                       ┌───────────────────┼───────────────────┐
                       │                   │                   │
                       ▼                   ▼                   ▼
                  ┌────────┐         ┌──────────┐       ┌──────────┐
                  │  Home  │         │ History  │       │ Savings  │
                  └───┬────┘         └──────────┘       └────┬─────┘
                      │                                       │
              ┌───────┼───────┐                       ┌──────┴──────┐
              │       │       │                       │             │
              ▼       ▼       ▼                       ▼             ▼
          ┌──────┐ ┌────┐ ┌──────┐              ┌────────┐   ┌──────────┐
          │ New  │ │New │ │Scan  │              │Budget  │   │  Edit    │
          │Move  │ │Cat │ │Receipt│             │Limits  │   │  Budget  │
          └──────┘ └────┘ └───┬──┘              └────────┘   └──────────┘
                               │
                        ┌──────┴──────┐
                        │             │
                        ▼             ▼
                   ┌────────┐   ┌──────────┐
                   │Loading │   │   Edit   │
                   │  Scan  │   │ Scanned  │
                   └────────┘   └──────────┘
```

---

## 🚀 Futuras Mejoras

### En Consideración
- [ ] Deep Links para notificaciones
- [ ] Navegación con parámetros tipados
- [ ] Transiciones personalizadas por pantalla
- [ ] Modo split-screen para tablets
- [ ] Navegación por gestos

### Mejoras de Performance
- [ ] Lazy loading de pantallas pesadas
- [ ] Precarga de datos en background
- [ ] Optimización de animaciones

---

## 📚 Referencias

### Documentación Oficial
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Jetpack Navigation](https://developer.android.com/guide/navigation)
- [Material 3 Navigation](https://m3.material.io/components/navigation-bar)

### Librerías Utilizadas
```kotlin
// build.gradle.kts (app)
implementation("androidx.navigation:navigation-compose:2.7.5")
implementation("androidx.compose.material3:material3:1.1.2")
implementation("io.insert-koin:koin-androidx-compose:3.5.0")
```

---

**Documentación generada el:** 10 de octubre de 2025  
**Versión de la app:** 1.0.0  
**Navigation Compose:** 2.7.5
