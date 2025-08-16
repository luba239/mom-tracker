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

### State
Чтобы сохранять стейт между Activity при переключении экранов пока нашла два варианта:
- viewModel с `MutableStateFlow`
- `mutableStateOf` вместе с `remember`:
    ```kotlin
    var isRunning by remember { mutableStateOf(false) }
    ```

## MVVM
Model-View-ViewModel - текущий стандарт архитектуры приложения
- `View`: Activity/Fragment/Compose, показывает данные.
- `ViewModel`: хранит состояние UI, взаимодействует с Model, не знает о View.
- `Model`: данные и обработка данных.
