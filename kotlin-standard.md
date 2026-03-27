# Estándares para App Android Vibecodeada

> Guía de referencia para asegurar calidad, seguridad y sostenibilidad en proyectos generados con IA.
> Fuentes: Android Developers (Google), OWASP Mobile Top 10 2024, OWASP MASVS v2, OWASP MASTG, Material Design 3, MITRE CWE.

---

## Tabla de contenidos

1. [Calidad de código](#1-calidad-de-código)
2. [Seguridad](#2-seguridad)
3. [Escalabilidad](#3-escalabilidad)
4. [Mantenibilidad](#4-mantenibilidad)
5. [ACID — Integridad de datos](#5-acid--integridad-de-datos)
6. [Agile](#6-agile)
7. [SOLID](#7-solid)
8. [Google Play Console](#8-google-play-console)
9. [Arquitectura — Niveles básico, intermedio y avanzado](#9-arquitectura--niveles-básico-intermedio-y-avanzado)
10. [Vulnerabilidades por nivel](#10-vulnerabilidades-por-nivel)
11. [Accesibilidad — WCAG + Material Design 3](#11-accesibilidad--wcag--material-design-3)
12. [Checklist pre-release](#12-checklist-pre-release)
13. [Notas sobre vibecodeo](#13-notas-sobre-vibecodeo)

---

## 1. Calidad de código

- **Kotlin idiomático** — sin Java legacy mezclado ni código de estilo Android antiguo
- **Cobertura de tests ≥ 70%** en lógica de negocio (JUnit para unit tests, Espresso para UI)
- **Android Lint** sin errores críticos antes de cada release
- **Ktlint** para formato de código y **Detekt** para detectar code smells — ambos en el pipeline de CI
- **No magic strings** — usar `strings.xml`, `constants` o sealed classes según el caso
- **Crashlytics / Firebase** integrado desde v1, no retroactivo
- **Sin código muerto** — la IA genera muchos métodos que nunca se llaman; revisar periódicamente
- **Version Catalogs** (`libs.versions.toml`) para centralizar versiones de dependencias — nunca hardcodear versiones en múltiples `build.gradle`
- **Conventional Commits** en todos los mensajes: `feat:`, `fix:`, `chore:`, `refactor:`

---

## 2. Seguridad

### OWASP Mobile Top 10 — 2024

| # | Riesgo | Mitigación mínima |
|---|--------|-------------------|
| M1 | Uso incorrecto de credenciales | Android Keystore + `EncryptedSharedPreferences`. Nunca hardcodear claves |
| M2 | Cadena de suministro insegura | Auditar dependencias con OWASP Dependency-Check. Fijar versiones críticas |
| M3 | Autenticación/autorización insegura | JWT de corta duración, refresh tokens, invalidar en logout |
| M4 | Validación insuficiente de entrada | Queries parametrizadas en Room. Nunca confiar en datos de Intents externos |
| M5 | Comunicación insegura | HTTPS en todo. Network Security Config con `cleartextTrafficPermitted="false"` |
| M6 | Controles de privacidad inadecuados | Permisos mínimos. No loggear datos sensibles. Data minimization |
| M7 | Protecciones binarias insuficientes | R8/ProGuard habilitado. Play Integrity API para apps financieras o de salud |
| M8 | Mala configuración de seguridad | `android:exported="false"` en componentes privados. `android:debuggable="false"` en release |
| M9 | Almacenamiento inseguro | `EncryptedSharedPreferences` con AES256-GCM. Android Keystore para datos críticos |
| M10 | Criptografía insuficiente | AES-256-GCM. Nunca MD5 ni SHA-1 para contraseñas. `SecureRandom` para generación de claves |

### Estándares prácticos

- **Sin claves en el código fuente** — usar `secrets-gradle-plugin`; las claves van en `local.properties` (no commiteado)
- **Certificate pinning** con OkHttp para APIs propias; incluir siempre al menos 2 pins (actual + backup para rotación)
- **No logging de datos sensibles** en producción — usar R8 para eliminar `Log.v/d/i` automáticamente en release
- **ProGuard / R8 habilitado** con `minifyEnabled = true` y `shrinkResources = true` en release
- **Permisos mínimos** — justificar cada `<uses-permission>` en el Manifest
- **Validación de inputs** en cliente Y servidor — nunca confiar solo en uno
- **WebView** con `setJavaScriptEnabled(false)` por defecto; `allowFileAccess = false`; validar host antes de cargar URLs
- **Rotación de API keys** cada 90-180 días con restricciones de uso por paquete/firma en la consola del proveedor

### OWASP MASVS v2 — Grupos de control

| Dominio | Control principal |
|---------|------------------|
| MASVS-STORAGE | No almacenar datos sensibles en logs, cachés de WebView, SharedPreferences sin cifrado ni almacenamiento externo |
| MASVS-CRYPTO | IVs/nonces aleatorios y únicos. Claves no embebidas en código. Algoritmos actuales (AES-256-GCM) |
| MASVS-AUTH | Tokens con entropía suficiente, expiración razonable e invalidación en logout. Reautenticación en funciones sensibles |
| MASVS-NETWORK | TLS 1.2 mínimo (preferir 1.3). Certificate pinning en endpoints críticos. Sin bypass de TLS en producción |
| MASVS-PLATFORM | Intents e IPC exponen lo mínimo. WebViews con JS restringido. Deep links validan y sanitizan parámetros |
| MASVS-CODE | App firmada, no en debug en producción. Dependencias auditadas. Sin backdoors ni funciones de prueba en producción |
| MASVS-RESILIENCE | Para apps financieras/salud: detección de root, hooking (Frida/Xposed) y emulador. Play Integrity API |
| MASVS-PRIVACY | Mínimo privilegio en permisos. Transparencia al usuario. Cumplimiento GDPR u otras regulaciones aplicables |

### Network Security Config (obligatorio en producción)

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
    <domain-config>
        <domain includeSubdomains="true">api.tuapp.com</domain>
        <pin-set>
            <pin digest="SHA-256">hashDelCertificadoActual==</pin>
            <pin digest="SHA-256">hashDeBackupParaRotacion==</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### Configuración mínima del Manifest para producción

```xml
<application
    android:debuggable="false"
    android:allowBackup="false"
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false">

    <activity android:name=".InternalActivity"
              android:exported="false"/>
</application>
```

---

## 3. Escalabilidad

- **MVVM o MVI** como patrón de presentación — ninguna lógica de negocio en Activities o Fragments
- **MVI preferido** en apps con lógica de UI compleja: flujo `Intent → ViewModel → State → UI` elimina estados inconsistentes
- **Modularización por feature o capa** — definida desde el inicio, no cuando ya duele
  - `:app` — módulo integrador, configuración mínima
  - `:feature:x` — módulos por feature, aislados entre sí
  - `:core:network`, `:core:database`, `:core:ui` — código compartido
- **Paging 3** para cualquier lista que pueda crecer (feeds, historial, catálogos)
- **Backend stateless** — la app no asume que la sesión estará siempre activa
- **Offline-first con Room + sync** si el contexto lo requiere: Room emite datos locales inmediatamente, la red actualiza en segundo plano
- **Coroutines + Flow** para operaciones asíncronas — reemplaza RxJava en proyectos nuevos; evitar callbacks anidados
- **WorkManager** para tareas diferibles garantizadas (sincronización, uploads) — reemplaza `Service`, `AlarmManager` y `JobScheduler`

---

## 4. Mantenibilidad

- **Clean Architecture** — capas separadas: `data` / `domain` / `ui`; sin dependencias cruzadas hacia arriba
- **Use Cases** en la capa de dominio para lógica reutilizada por múltiples ViewModels (`GetUserProfileUseCase`, `LoginUseCase`)
- **Dependency Injection con Hilt** — obligatorio, no opcional; verificación en tiempo de compilación
- **Naming conventions** según la guía oficial de Android y Kotlin (PascalCase para clases, camelCase para funciones, UPPER_SNAKE_CASE para constantes)
- **Versionado semántico** (`MAJOR.MINOR.PATCH`) desde v0.1.0
- **Changelog actualizado** en cada release, aunque sea interno
- **README con setup en menos de 5 minutos** — si un nuevo dev no puede correr el proyecto solo, está incompleto
- **Sin "GodViewModels"** — si un ViewModel supera las 300 líneas, hay que dividirlo
- **Convention Plugins** (Gradle plugins personalizados) para estandarizar configuración de build entre módulos en proyectos grandes
- **Build Variants** correctamente configurados para separar endpoints, feature flags y comportamientos debug/release

---

## 5. ACID — Integridad de datos

- **Transacciones Room** para operaciones que tocan múltiples tablas o entidades relacionadas
- **Sin escrituras concurrentes** sin manejo explícito de conflictos (`@Transaction`, mutex o canal)
- **Rollback explícito** en flujos críticos como pagos, actualizaciones de perfil o sincronización
- **Validación antes de persistir** — nunca guardar un estado inválido en la base de datos local
- **Migraciones de DB testeadas** con `MigrationTestHelper` ante cada cambio de schema; nunca usar `fallbackToDestructiveMigration` en producción
- **Separación de fuente de verdad** — una sola fuente por entidad (Room o API, no ambas sin política clara)
- **DAOs con Flow** para observación reactiva de la base de datos local

---

## 6. Agile

- **Tickets con Definition of Done** incluyendo criterios de aceptación concretos y testeables
- **PR reviews obligatorias** — nadie hace merge a `main` o `develop` sin al menos una aprobación
- **CI/CD pipeline** con build + tests automáticos antes de cualquier merge (GitHub Actions, GitLab CI, Bitrise, CircleCI)
- **Análisis estático en CI**: Ktlint (formato) + Detekt (code smells) + Lint (Android-specific)
- **Distribución automática** de builds de QA con Firebase App Distribution en cada merge a `develop`
- **Retrospectivas cada sprint** — no solo cuando algo falla en producción
- **Backlog priorizado** — evitar que todo sea urgente; usar MoSCoW o similar
- **Branches con naming convention** (`feature/`, `fix/`, `chore/`) y PRs descriptivos
- **Ramas protegidas** para `main` y `develop` — nunca commitear directamente

---

## 7. SOLID

| Principio | Aplicación en Android |
|-----------|----------------------|
| **S** — Single Responsibility | Un ViewModel, una responsabilidad. Sin lógica de red, DB y UI en la misma clase |
| **O** — Open/Closed | Extender comportamiento mediante interfaces o herencia, sin modificar clases base existentes |
| **L** — Liskov Substitution | Las subclases deben poder reemplazar a sus padres sin romper el comportamiento esperado |
| **I** — Interface Segregation | Interfaces pequeñas y específicas — evitar interfaces con 10+ métodos que las clases implementan a medias |
| **D** — Dependency Inversion | Depender de abstracciones (interfaces), no de implementaciones concretas. Hilt facilita esto |

---

## 8. Google Play Console

- **Android Vitals dentro de umbrales**: ANR rate < 0.47%, crash rate < 1.09% (Google puede limitar visibilidad si se superan)
- **Cold Start < 2 segundos** — evitar trabajo pesado en `Application.onCreate()`; usar App Startup Library para inicialización lazy
- **Target SDK actualizado** — máximo 1 año de diferencia con el SDK más reciente de Android
- **App Bundle (`.aab`)** — no subir APK directo para producción; el bundle reduce el tamaño de descarga
- **Política de datos declarada** y coherente con lo que el código realmente hace (permisos, tracking, etc.)
- **Staged rollout** — comenzar con 10-20% de usuarios en versiones nuevas, nunca 100% directo
- **Release tracks correctamente usados**: internal → alpha → beta → production
- **Screenshots y metadatos actualizados** en cada release que cambie la UI

---

## 9. Arquitectura — Niveles básico, intermedio y avanzado

### Básico — Todo proyecto desde el día 1

**Estructura de paquetes por feature** (no por capa técnica):

```
com.tuapp/
  auth/
  home/
  profile/
  core/
```

Separación mínima en dos capas (UI y datos). Flujo Unidireccional de Datos (UDF) y Fuente Única de Verdad (SSOT). ViewModel para sobrevivir rotaciones. Nunca mantener referencias a `Context` o `View` dentro de un ViewModel (genera memory leaks).

### Intermedio — Lo que todo senior developer exige

**Clean Architecture completa con tres capas:**

- Capa de UI: Composables o vistas, ViewModels, `StateFlow<UiState>` inmutable
- Capa de Dominio: Use Cases con responsabilidad única, completamente independientes del Android SDK, testeables con JUnit puro
- Capa de Datos: Repositorios, DataSources locales y remotos, DTOs y entidades Room

**Pirámide de pruebas:**

| Nivel | Porcentaje | Herramientas |
|-------|-----------|--------------|
| Unitarias | 70% | JUnit, MockK, Turbine (Flow), Truth |
| Integración | 20% | AndroidX Test, Room in-memory |
| UI | 10% | Espresso, Compose UI Test, UI Automator |

### Avanzado — Enterprise y equipos grandes

**Modularización completa:**

- `:app` integra todo y genera el APK — configuración mínima
- `:feature:auth`, `:feature:home`, etc. — aislados, no se conocen entre sí directamente
- `:core:network`, `:core:database`, `:core:ui`, `:core:domain` — código compartido vía interfaces

**Rendimiento:**

- Cold Start < 2 segundos con App Startup Library
- ANR threshold: nunca bloquear el hilo principal más de 5 segundos
- Renderizado a 60fps (16ms/frame) — usar Profiler para identificar cuellos de botella
- LeakCanary en debug para detectar memory leaks
- `shrinkResources = true` y `minifyEnabled = true` en release

---

## 10. Vulnerabilidades por nivel

### Básicas — Presentes en casi toda app sin revisión de seguridad

**VB-01 — Datos sensibles en logs** (MASVS-STORAGE | CWE-532)

Los logs en logcat son accesibles para apps con `READ_LOGS`. Eliminar logs de producción con R8:

```proguard
-assumenosideeffects class android.util.Log {
    public static int v(...); public static int d(...);
    public static int i(...); public static int w(...); public static int e(...);
}
```

**VB-02 — Almacenamiento inseguro** (MASVS-STORAGE | CWE-312)

```kotlin
// Correcto: EncryptedSharedPreferences con AES256-GCM
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()
val encryptedPrefs = EncryptedSharedPreferences.create(
    context, "secure_prefs", masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

**VB-03 — `android:debuggable="true"` en producción** (MASVS-CODE | CWE-489)

Permite conectar un depurador vía USB, inspeccionar memoria y modificar el flujo de ejecución. Verificar explícitamente `debuggable false` en el buildType release.

**VB-04 — Componentes exportados sin protección** (MASVS-PLATFORM | CWE-926)

Activities, servicios, receivers y content providers con `android:exported="true"` sin permisos de protección son accesibles por cualquier app instalada.

**VB-05 — HTTP en lugar de HTTPS** (MASVS-NETWORK | CWE-319)

Permite ataques Man-in-the-Middle. Resolver con Network Security Config y `cleartextTrafficPermitted="false"`.

**VB-06 — Secrets hardcodeados** (MASVS-CRYPTO | CWE-798)

Extraíbles con jadx o apktool en minutos. Usar `secrets-gradle-plugin` y `local.properties`.

**VB-07 — Backup automático de datos sensibles** (MASVS-STORAGE | CWE-530)

`android:allowBackup="true"` por defecto respalda base de datos y SharedPreferences en Google Drive. Deshabilitar o usar `android:dataExtractionRules`.

---

### Medias — Requieren conocimiento específico de la plataforma

**VM-01 — SQL Injection en Content Providers** (MASVS-CODE | CWE-89)

```kotlin
// Vulnerable: concatenación directa
db.rawQuery("SELECT * FROM users WHERE email = '$userInput'", null)

// Seguro: queries parametrizadas
db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(userInput))
// O mejor: usar Room que verifica en tiempo de compilación
```

**VM-02 — Intent Hijacking** (MASVS-PLATFORM | CWE-927)

Los Intents implícitos pueden ser interceptados por apps maliciosas. Usar siempre Intents explícitos para iniciar Services.

**VM-03 — Redirección de Intents** (MASVS-PLATFORM | CWE-940)

Un Intent anidado en extras usado sin validación permite acceder a componentes privados. Usar `IntentSanitizer` en Android 12+.

**VM-04 — Tapjacking / Clickjacking** (MASVS-PLATFORM | CWE-1021)

Una app maliciosa superpone una ventana transparente para interceptar toques. Mitigación:

```xml
<Button android:filterTouchesWhenObscured="true" ... />
```

**VM-05 — WebView con JS habilitado y URLs arbitrarias** (MASVS-PLATFORM | CWE-749)

Con `setJavaScriptEnabled(true)` y `addJavaScriptInterface()`, JavaScript puede llamar métodos nativos con los permisos de la app. Restringir al máximo y validar hosts.

**VM-06 — Criptografía débil o mal configurada** (MASVS-CRYPTO | CWE-327/330)

Nunca usar MD5 o SHA-1 para contraseñas, modo ECB en AES, IVs fijos, ni `Math.random()` para generar claves. Usar `AES/GCM/NoPadding` con IV aleatorio via `SecureRandom`.

**VM-07 — StrandHogg / Task Affinity Hijacking** (MASVS-PLATFORM)

Una app maliciosa se inserta en la pila de tareas de tu app y muestra una UI de phishing. Mitigación: `targetSdkVersion >= 28`.

**VM-08 — PendingIntents con configuración insegura** (MASVS-PLATFORM | CWE-927)

```kotlin
// Seguro: siempre FLAG_IMMUTABLE con Intent explícito
PendingIntent.getActivity(
    context, 0,
    Intent(context, TargetActivity::class.java),
    PendingIntent.FLAG_IMMUTABLE
)
```

---

### Avanzadas — Para apps de alto riesgo (banca, salud, fintech)

**VA-01 — Falta de protecciones de integridad del binario** (MASVS-RESILIENCE)

Sin verificación de integridad, el APK puede modificarse y redistribuirse con código malicioso. Usar Play Integrity API para verificar en el servidor que el APK y el dispositivo son legítimos.

**VA-02 — Falta de Certificate Pinning** (MASVS-NETWORK)

Un certificado raíz instalado por el atacante permite interceptar HTTPS completo. Implementar pinning con OkHttp, siempre con pin de backup para rotación sin dejar la app inoperativa.

**VA-03 — Reverse Engineering / Falta de ofuscación efectiva** (MASVS-RESILIENCE)

Sin ofuscación, jadx decompila el APK a código casi legible. R8 con reglas agresivas es el mínimo; DexGuard para apps de alto valor.

**VA-04 — Hook Frameworks / Frida y Xposed** (MASVS-RESILIENCE)

Permiten interceptar y modificar cualquier método en tiempo de ejecución, bypassear licencias y extraer claves de la memoria. Implementar detección multi-capa; la validación definitiva siempre debe ser server-side.

**VA-05 — Exposición de datos en backups de ADB** (MASVS-STORAGE)

En dispositivos con debugging USB habilitado, `adb backup` extrae toda la información de la app sin root. Mitigar con `android:allowBackup="false"`.

**VA-06 — Side-Channel Attacks / Timing attacks** (MASVS-CRYPTO)

Comparar tokens con `==` es vulnerable porque el tiempo varía según cuántos caracteres coinciden. Usar comparación en tiempo constante:

```kotlin
fun secureEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var result = 0
    for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
    return result == 0
}
// O usar MessageDigest.isEqual() que ya es tiempo constante
```

**VA-07 — Deep Link Injection / URL Scheme Hijacking** (MASVS-PLATFORM | CWE-601)

Los deep links con parámetros no validados son explotables. Usar App Links (HTTPS con `assetlinks.json`) en lugar de esquemas personalizados (`myapp://`), y validar todos los parámetros server-side.

**VA-08 — Root / Emulator Detection Bypass** (MASVS-RESILIENCE)

En dispositivos rooteados el modelo de seguridad de Android está comprometido. Implementar detección multi-capa (binarios `su`, build tags, apps peligrosas instaladas). Ninguna técnica es 100% efectiva en solitario; combinar con validación server-side.

---

## 11. Accesibilidad — WCAG + Material Design 3

El 15% de la población mundial tiene algún tipo de discapacidad. En muchos países es obligatoria legalmente para apps gubernamentales y corporativas.

### Los 4 principios WCAG 2.1 (POUR)

**1. Perceptible — Toda información visual debe tener equivalente textual**

```kotlin
// Correcto: content description descriptiva
Image(
    painter = painterResource(R.drawable.user_avatar),
    contentDescription = stringResource(R.string.user_avatar_description)
    // Ej: "Foto de perfil de María García"
)

// Para elementos decorativos que TalkBack debe ignorar:
Image(
    painter = painterResource(R.drawable.decorative_divider),
    contentDescription = null,
    modifier = Modifier.semantics { this.invisibleToUser() }
)
```

**Contraste de color mínimo (WCAG AA):**

| Tipo de elemento | Ratio mínimo AA | Ratio AAA (recomendado) |
|-----------------|----------------|------------------------|
| Texto normal (< 18pt) | 4.5:1 | 7:1 |
| Texto grande (>= 18pt o >= 14pt negrita) | 3:1 | 4.5:1 |
| Elementos UI no-texto (iconos, bordes) | 3:1 | — |

**2. Operable — Los usuarios deben poder navegar e interactuar**

Tamaño mínimo de objetivo táctil: **48dp × 48dp** según Android Developers y Material Design 3.

```kotlin
// Correcto: área táctil mínima de 48dp
IconButton(
    onClick = { },
    modifier = Modifier
        .size(48.dp)
        .semantics { role = Role.Button }
) {
    Icon(
        Icons.Default.Add,
        contentDescription = stringResource(R.string.add_item),
        modifier = Modifier.size(24.dp)
    )
}
```

Todos los flujos deben ser navegables con teclado físico y con TalkBack. Ninguna acción debe depender exclusivamente de un gesto complejo sin alternativa accesible.

**3. Comprensible — Las etiquetas deben describir el propósito, no la apariencia**

No incluir el tipo de elemento en la descripción (TalkBack ya lo anuncia). No usar "Botón azul de envío" sino "Enviar formulario de contacto". Los mensajes de error deben ser descriptivos y accionables:

```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("Correo electrónico") },
    isError = !isValidEmail,
    supportingText = {
        if (!isValidEmail) {
            Text(
                "Formato inválido. Ej: nombre@dominio.com",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
)
```

**4. Robusto — El contenido debe ser interpretable por tecnologías asistivas**

Usar componentes nativos de Material Design 3 siempre que sea posible. Para componentes personalizados, declarar el rol semántico explícitamente:

```kotlin
Box(
    modifier = Modifier
        .clickable { onToggle() }
        .semantics {
            role = Role.Switch
            stateDescription = if (isChecked) "Activado" else "Desactivado"
            contentDescription = "Recibir notificaciones de ofertas"
        }
) { /* UI del switch personalizado */ }
```

### Herramientas de testing de accesibilidad

- **Accessibility Scanner (Google)**: Analiza screenshots y reporta problemas de contraste, tamaño de objetivos y etiquetas faltantes. Integrar en QA.
- **Compose Accessibility Testing**: Verificar descripciones con `composeTestRule.onNodeWithContentDescription()` en pruebas instrumentadas.
- **TalkBack**: Probar todos los flujos críticos con el lector de pantalla activado antes de cada release.

---

## 12. Checklist pre-release

### Arquitectura y código

- [ ] Separación clara de capas (UI / Dominio / Datos)
- [ ] ViewModels sin referencias a Contextos o Vistas
- [ ] Toda la lógica de negocio en Use Cases o Repositorios
- [ ] Cobertura de pruebas unitarias > 70% en lógica de negocio
- [ ] Sin memory leaks detectados por LeakCanary
- [ ] Ktlint y Detekt pasando en CI sin errores

### Seguridad

- [ ] `android:debuggable="false"` en release
- [ ] `allowBackup="false"` si hay datos sensibles
- [ ] Todos los componentes privados con `exported="false"`
- [ ] Network Security Config configurado (sin HTTP en producción)
- [ ] Datos sensibles cifrados con EncryptedSharedPreferences o Keystore
- [ ] API Keys fuera del código fuente y del repositorio
- [ ] Certificate Pinning para endpoints críticos (con pin de backup)
- [ ] ProGuard/R8 habilitado con reglas de ofuscación ajustadas
- [ ] Permisos mínimos necesarios en el Manifest
- [ ] Validación de todos los inputs del usuario
- [ ] Sin secretos en el historial de git

### Calidad y build

- [ ] Lint sin errores críticos
- [ ] App Bundle (`.aab`) en lugar de APK monolítico para Play Store
- [ ] `shrinkResources = true` y `minifyEnabled = true` en release
- [ ] Versiones de dependencias actualizadas y sin CVEs conocidos
- [ ] Tests ejecutándose automáticamente en cada Pull Request
- [ ] Android Vitals dentro de umbrales (ANR < 0.47%, crashes < 1.09%)

### Accesibilidad

- [ ] Todas las imágenes no decorativas tienen `contentDescription`
- [ ] Contraste de color cumple WCAG AA (4.5:1 para texto normal)
- [ ] Objetivos táctiles de mínimo 48dp x 48dp
- [ ] Flujos críticos navegables con TalkBack
- [ ] Mensajes de error descriptivos y accionables

---

## 13. Notas sobre vibecodeo

El código generado por IA tiende a tener estos antipatrones recurrentes que deben revisarse manualmente:

1. Lógica de negocio directamente en Activities o Fragments
2. URLs, API keys o configuraciones hardcodeadas
3. Manejo de errores con `try/catch` vacíos o que solo hacen log
4. Clases "Dios" con responsabilidades mezcladas
5. Imports no usados y código muerto que parece plausible
6. Tests ausentes o tests que solo verifican que el código compila
7. `Log.d()` con datos sensibles olvidados del desarrollo
8. `android:exported="true"` en componentes que no deberían serlo
9. SharedPreferences sin cifrar para tokens o datos de sesión
10. Migraciones de base de datos ausentes ante cambios de schema

> La primera revisión humana post-generación debería enfocarse en arquitectura y seguridad, no en funcionalidad. La IA produce código que *parece* funcionar; el trabajo humano es asegurar que sea correcto, seguro y mantenible.
