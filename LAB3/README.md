# Лабораторна робота 3 — «Нотатки» з хмарною синхронізацією (варіант 13)

Android-застосунок на Kotlin (Jetpack Compose): нотатки зберігаються на
пристрої й синхронізуються з хмарою Firebase Firestore.

## Можливості

- створення, редагування, видалення й пошук нотаток;
- робота без інтернету — нотатки зберігаються в локальній базі Room;
- обліковий запис Firebase (email + пароль) і синхронізація: вручну кнопкою,
  після входу, під час запуску та у фоні кожні 30 хвилин (WorkManager);
- одні й ті самі нотатки на кількох пристроях під одним акаунтом;
- українська та англійська мови з перемикачем у застосунку.

## Як працює синхронізація

Кожна нотатка має час останньої зміни `updatedAt` і прапорець `synced`.
Під час синхронізації `SyncPlanner` порівнює локальні нотатки з хмарними:

- нотатка, змінена локально й новіша за хмарну, надсилається в хмару;
- нотатка, новіша в хмарі, завантажується на пристрій;
- видалена нотатка спершу лишається в базі з позначкою `deleted`, щоб
  видалення дійшло до хмари й інших пристроїв, а після синхронізації
  зникає з пристрою.

Дані в Firestore: `users/{uid}/notes/{noteId}`. Правила доступу
([`firestore.rules`](firestore.rules)) дозволяють кожному користувачу бачити
лише власні нотатки.

## Структура

```
ua.lab.notes
├── NotesApplication      налаштування журналу, фонова синхронізація
├── AppContainer          створює базу, Firebase-сервіси й репозиторій
├── data
│   ├── NotesRepository   нотатки + синхронізація
│   ├── local             Room: NoteEntity, NoteDao, NotesDatabase
│   ├── remote            Firestore
│   ├── auth              вхід через Firebase Authentication
│   └── sync              SyncPlanner (правила злиття), SyncResult
├── sync                  SyncWorker, SyncScheduler (WorkManager)
├── logging               ReleaseTree
└── ui                    екрани Compose та їхні ViewModel: list, edit, settings
```

## Логування

Використано [Timber](https://github.com/JakeWharton/timber). У
`NotesApplication` налаштовується, що й куди писати:

- debug-збірка — `Timber.DebugTree`: усі рівні, тег — ім'я класу;
- release-збірка — `ReleaseTree`: лише попередження й помилки.

Журнал видно у вкладці **Logcat** Android Studio (фільтр `package:ua.lab.notes`).

## Мови (i18n)

- Тексти — у `res/values/strings.xml` (англійська, мова за замовчуванням) і
  `res/values-uk/strings.xml` (українська). У коді немає жодного рядка
  інтерфейсу.
- Множина — через `plurals` з українськими формами `one`, `few`, `many`
  («1 нотатка», «3 нотатки», «5 нотаток»).
- Дати форматуються відповідно до мови (`DateTimeFormatter.ofLocalizedDateTime`).
- Мову можна вибрати в налаштуваннях застосунку (per-app language через
  `AppCompatDelegate` і `res/xml/locales_config.xml`). На Android 13+ її також
  видно в системних налаштуваннях застосунку.
- Щоб додати нову мову, достатньо створити `values-<код>/strings.xml` і
  дописати мову в `locales_config.xml` та `AppLanguage`.

## Налаштування Firebase

Без цього застосунок працює, але лише локально.

1. https://console.firebase.google.com → **Add project**.
2. Додати Android-застосунок з package name `ua.lab.notes`.
3. Завантажити `google-services.json` і покласти в `LAB3/app/`. Файл не
   потрапляє в git (див. `.gitignore`): кожен підключає власний проєкт Firebase.
4. **Authentication → Sign-in method → Email/Password → Enable.**
5. **Firestore Database → Create database** → вкладка **Rules** → вставити
   вміст [`firestore.rules`](firestore.rules) → **Publish**.

## Запуск

Потрібні Android SDK (шлях до нього — у `local.properties`, файл не
комітиться) і повноцінна JDK 17+ з утилітою `jlink` у `JAVA_HOME`. Вбудована
в IntelliJ IDEA JBR не підходить: у ній немає `jlink`. Компіляція йде під
Android 17 (compileSdk 37), мінімальна версія — Android 8.0.

**На телефоні через USB:**

1. На телефоні: *Налаштування → Про телефон* → 7 разів натиснути
   *Номер збірки*, потім *Для розробників → Налагодження USB* → увімкнути.
2. Під'єднати телефон кабелем і дозволити налагодження.
3. З теки `LAB3`:

```bash
./gradlew installDebug
```

**В Android Studio:** *File → Open* → тека `LAB3` → вибрати емулятор або
телефон → **Run ▶**.

## Тести та перевірки

```bash
./gradlew testDebugUnitTest lintDebug
```

37 модульних тестів (JUnit 4, kotlinx-coroutines-test) перевіряють правила
синхронізації, репозиторій і всі ViewModel на підробках (fakes) замість Room
і Firebase. Android Lint перевіряє, зокрема, повноту перекладів і форми множини.
