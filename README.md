Отлично, давай сделаем **полноценный README**, чтобы любой (включая тебя через полгода 😄) понял, **как пользоваться приложением и что менять под свой JSON / сервер / Wi-Fi**.

Ниже — готовый `README.md`, можно **копировать целиком**.

---

# Auto Wi-Fi Postman 🤖📡

Android-приложение для автоматического и ручного подключения к Wi-Fi сетям и отправки HTTP-запросов на сервер после успешного подключения.

Поддерживает:

* 🔁 **Автоматический режим** (фоновые циклы раз в час)
* 🖐 **Ручной режим** (сканирование и выбор сети)
* 📥 **Импорт списка сетей из JSON**
* ⏱ Таймер ожидания между циклами
* 🔔 Статусы и уведомления

---

## 🧠 Общая архитектура

* **UI** — Jetpack Compose
* **State** — `UiState` + `StateFlow`
* **Логика** — `MainViewModel`
* **Фоновая работа** — `AgentForegroundService`
* **Оркестрация** — `Orchestrator`
* **Сети** — `KnownNetwork`
* **Импорт JSON** — `KnownNetworksJsonParser`

---

## 📦 Формат данных (JSON)

### Текущий формат сети

```kotlin
data class KnownNetwork(
    val id: String,
    val ssid: String,
    val password: String? = null,
    val baseUrl: String,
    val updateEndpoint: String,
    val retries: Int = 2,
    val timeoutMs: Long = 5_000
)
```

### Пример JSON (по умолчанию)

```json
[
  {
    "id": "home-start",
    "ssid": "Xiaomi_AX3000",
    "password": "12345678",
    "baseUrl": "http://192.168.1.10",
    "updateEndpoint": "/api/update",
    "retries": 2,
    "timeoutMs": 5000
  }
]
```

---

## 🔧 Как использовать СВОЙ формат JSON

### 1️⃣ Где менять парсинг JSON

📍 **Файл**:

```
ui/parser/KnownNetworksJsonParser.kt
```

Там находится логика:

```kotlin
fun parse(json: String): ImportResult
```

### 2️⃣ Если ваш JSON отличается

Например, ваш JSON выглядит так:

```json
[
  {
    "wifi_name": "MyWiFi",
    "wifi_pass": "qwerty",
    "server": "192.168.0.100",
    "endpoint": "/ping"
  }
]
```

Нужно:

* либо создать **промежуточную DTO**
* либо вручную маппить поля

#### Пример маппинга:

```kotlin
KnownNetwork(
    id = UUID.randomUUID().toString(),
    ssid = dto.wifi_name,
    password = dto.wifi_pass,
    baseUrl = "http://${dto.server}",
    updateEndpoint = dto.endpoint
)
```

📌 **Меняется ТОЛЬКО парсер**, остальное приложение трогать не нужно.

---

## 📥 Импорт JSON

* Доступен **всегда** в AUTO и MANUAL режимах
* Кнопка — ⚙️ (шестерёнка)
* Поддерживает повторный импорт (заменяет список сетей)

📍 Импорт обрабатывается в:

```
MainViewModel → handleImport()
```

---

## 📡 Разрешения Android (ОБЯЗАТЕЛЬНО)

### 1️⃣ В `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<!-- Android 10+ -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

⚠️ **Без LOCATION Wi-Fi сканирование НЕ работает**

---

### 2️⃣ Разрешения в настройках телефона

В настройках приложения **вручную включить**:

* 📍 Геолокация → **Разрешить**
* 📡 Wi-Fi
* 🔋 Фоновая работа → **Разрешить**
* 🔔 Уведомления (для Foreground Service)

---

## 🌐 HTTP (НЕ HTTPS)

### Разрешение обычного HTTP

📍 **Файл**:

```
res/xml/network_security_config.xml
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

📍 **AndroidManifest.xml**:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="true"
    ... >
```

---

## 🤖 Автоматический режим (AUTO)

* Запускается через `ForegroundService`
* Работает **даже если приложение свернуто**
* Цикл:

    1. Сканирование Wi-Fi
    2. Выбор лучшей сети из известных
    3. Подключение
    4. HTTP запрос
    5. Ожидание 60 минут

📍 Логика:

```
AgentForegroundService
Orchestrator
```

📍 Таймер:

```
MainViewModel → startAutoCountdown()
```

---

## 🖐 Ручной режим (MANUAL)

* Кнопка **"Сканировать сети"**
* Показываются только **известные сети**
* После выбора:

    * подключение
    * HTTP запрос
    * показ результата

Анимация:

```
⏳ Получение данных…
```

---

## ⚠️ Типовые проблемы

### ❌ Авто режим не стартует после импорта

Причина:

* Wi-Fi был выключен при первом запуске
* Или нет разрешения LOCATION

Решение:

* Включить Wi-Fi
* Дать разрешение геолокации
* Перезапустить AUTO режим

---

### ❌ Wi-Fi scan → empty

В логах:

```
Wi-Fi permission missing → empty scan
```

➡️ **100% нет LOCATION разрешения**

---

## 🧪 Логи (очень полезны)

Фильтры в Logcat:

```
SERVICE
ORCHESTRATOR
WIFI_SCAN
WIFI_CONNECT
HTTP
```

---

## 🏁 Итог

Чтобы приложение работало корректно, нужно:

✅ Импортировать JSON
✅ Дать LOCATION разрешение
✅ Разрешить HTTP (если не HTTPS)
✅ Включить Wi-Fi
✅ Не отключать фоновую работу

┌─────────────┐
│ MainScreen  │  Jetpack Compose UI
└──────┬──────┘
│ AppEvent
┌──────▼──────┐
│ MainViewModel│
│  StateFlow   │
└───┬────┬────┘
│    │
│    └──────────┐
│               ▼
│        ImportStateRepository
│
┌───▼────────────┐
│  Orchestrator   │
│ (business logic)│
└───┬─────┬───────┘
│     │
│     ├── Wi-Fi Scan / Connect
│     └── HTTP Client
│
┌───▼────────────┐
│ ForegroundSvc  │
│  (AUTO mode)   │
└────────────────┘

flowchart TD
UI[MainScreen<br/>Jetpack Compose]
VM[MainViewModel<br/>StateFlow]
ORCH[Orchestrator]
WIFI[Wi-Fi Manager]
HTTP[HTTP Client]
SVC[Foreground Service]
DATA[Repositories<br/>DataStore / Memory]

    UI -->|AppEvent| VM
    VM -->|State| UI

    VM --> ORCH
    VM --> DATA

    ORCH --> WIFI
    ORCH --> HTTP

    SVC --> ORCH

