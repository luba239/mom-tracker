# Про сборку проекта на gradle

### Общая структура
Это относительно новая для меня область, я до этого gradle практически не трогала. Пока поверим тому, что сгенерил Android Studio и объяснил GPT. 


#### Структура файлов

Основные файлы и папки:

```
project-root/
├── app/                   <-- модуль приложения
│   ├── build.gradle
│   └── ...
├── build.gradle           <-- главный build-скрипт
├── settings.gradle
└── gradle/
```

- `settings.gradle` — указывает, какие модули входят в проект (например, include ':app').
- `build.gradle` (в корне) — основной файл с настройками для всего проекта.
- `app/build.gradle` — настройки конкретного модуля приложения (чаще всего собираешь APK именно отсюда).
- `gradle/` — служебные скрипты для запуска gradle.

#### Структура файла build.gradle

**Основные секции:**

- `plugins` — подключение плагинов (например, android, kotlin).
- `android` — android-специфичные параметры.
- `dependencies` — сторонние библиотеки.

#### Как запускать сборку

**Стандартные команды через terminal:**

`./gradlew` — это gradle wrapper: проект запускается с конкретной, нужной версией Gradle, которая гарантированно подходит для этого проекта.

- Собрать APK для debug:
  ```
  ./gradlew assembleDebug
  ```
  Здесь assembleDebug - это не пользовательская команда, а задача, автоматически создаваемая Android Gradle Plugin. Она формируется на основе секции buildTypes (debug, release) внутри блока android в файле build.gradle. Т.е. если в android { buildTypes { ... } } есть debug, Gradle сам создает задачу assembleDebug. Описывать её отдельно не нужно.
- Собрать APK для release:
  ```
  ./gradlew assembleRelease
  ```
- Запустить приложение на устройстве:
  ```
  ./gradlew installDebug
  ```
- Очистить старые билды:
  ```
  ./gradlew clean
  ```
- Запустить тесты:
  ```
  ./gradlew test
  ```

### Version catalog
В `gradle/libs.versions.toml` указаны версии всех библиотек. Нужно быть внимательными с версией kotlin и compose - они должны сочетаться друг с другом