Автоматический Android-сервис, который:

* сканирует Wi-Fi сети
* находит **заранее известные SSID**
* последовательно работает с ними
* выполняет HTTP-запросы (локальные и без интернета)
* корректно обрабатывает ошибки и ретраи
* работает в фоне как ForegroundService

## 📐 Архитектура (коротко)

ForegroundService
        │
        ▼
   Orchestrator
        │
        ├─▶ WifiScanner     — скан известных SSID
        ├─▶ WifiConnector   — работа с активной Wi-Fi
        ├─▶ LocalHttpClient — HTTP-запросы
        └─▶ Repository      — known_networks.json
```

## ✅ Основная концепция 

### 🔹 Вариант A — используемый в проекте (рекомендуемый)

> ❗ **Все Wi-Fi сети должны быть заранее сохранены в системе Android**

* Сети добавляются вручную в настройках устройства
* Сеть может:

    * не иметь интернета
    * быть локальной (`192.168.x.x`)
* Приложение **НЕ управляет подключением**, а работает с **активной Wi-Fi**

📌 Это обеспечивает:

* стабильность
* отсутствие системных диалогов
* корректную работу без `NET_CAPABILITY_INTERNET`

---

## 📶 Wi-Fi логика

### Сканирование

Используется стандартный Wi-Fi scan:

* Проверяется `Location enabled`
* Получаются `ScanResult`
* Фильтруются только известные SSID

```text
Raw scan → Filtered known SSIDs

### Подключение

Приложение:

* использует текущий network stack
* не вызывает `WifiNetworkSpecifier`
* не дергает систему

## 🌐 HTTP-работа

### LocalHttpClient

* Используется `HttpURLConnection`
* Все запросы выполняются **НЕ на main thread**
* Возвращается HTTP-код

Пример:

```text
HTTP code = 200
HTTP code = 500

### Endpoint пример

```json
{
  "baseUrl": "http://000.000.00.000:3000",
  "updateEndpoint": "/api/update",
  "timeoutMs": 3000,
  "retries": 2
}
```

---

## 🔁 Orchestrator — state machine

```text
SCANNING
  ↓
FOUND_NETWORKS
  ↓
CONNECTING
  ↓
DISCONNECT
  ↓
SLEEP
```

### Поведение:

* для каждой сети:

    * N попыток
    * фиксированный timeout
* HTTP `200` → SUCCESS
* HTTP `>=400` → retry
* после всех → FAILED

---

## 📁 known_networks.json

📍 `app/src/main/assets/known_networks.json`

```json
{
  "networks": [
    {
      "id": "home-start",
      "ssid": "Xiaomi_AX3000",
      "baseUrl": "http://000.000.00.000:3000",
      "updateEndpoint": "/api/update",
      "timeoutMs": 3000,
      "retries": 2
    },
    {
      "id": "home-backup",
      "ssid": "Xiaomi_AX3000",
      "baseUrl": "http://000.000.00.000:3000",
      "updateEndpoint": "/api/update?mode=backup",
      "timeoutMs": 4000,
      "retries": 2
    },
    {
      "id": "home-test",
      "ssid": "Xiaomi_AX3000",
      "baseUrl": "http://000.000.00.000:3000",
      "updateEndpoint": "/api/update?mode=test",
      "timeoutMs": 2000,
      "retries": 2
    }
  ]
}
```

---

## 🔐 Network Security Config

📍 `res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">
            000.000.00.000
        </domain>
    </domain-config>
</network-security-config>
```

📌 Нужно для HTTP без HTTPS.

---

## 📦 AndroidManifest.xml

Ключевые моменты:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config">

<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

---

## 🧵 Потоки и корутины

* ❌ Нет сети на main thread
* ❌ Нет `NetworkOnMainThreadException`
* Все HTTP → `Dispatchers.IO`
* Orchestrator — suspend-логика

Успешный цикл выглядит так:

```text
STATE → SCANNING
STATE → FOUND_NETWORKS
→ PROCESS home-start
HTTP code = 200
SUCCESS
→ PROCESS home-backup
HTTP code = 200
SUCCESS
→ PROCESS home-test
HTTP code = 500
FAILED after retries
STATE → SLEEP
```

---

## ⚠️ Ограничения 
* ❌ Нельзя переключать Wi-Fi без участия пользователя (Android restriction)
* ❌ Нельзя подключаться к неизвестным сетям
* ❌ Нельзя управлять captive portal

✔️ Зато:

* стабильно
* предсказуемо
* production-ready


