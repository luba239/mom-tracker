# Открытия непосредственно Android-разработки

## Jetpack Compose
Jetpack Compose — это современный декларативный фреймворк для создания пользовательских интерфейсов (UI) в Android-приложениях, официально поддерживаемый Google.

### ComponentActivity
Базовый Activity для compose-фреймворка

### MaterialTheme
Composable-функция и объект в Jetpack Compose, который задаёт визуальный стиль (цвета, типографику, формы) приложения по принципам Material Design от Google.

## Activity
### MainActivity
Как и раньше (просто я уже забыла) в `AndroidManifest.xml` нужно указывать main activity:
```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```
Но кроме того, ещё надо включить `android:exported="true"`, чтобы её можно было вызывать.