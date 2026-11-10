# LAB_OOP

All my labs OOP — лабораторні роботи з об'єктно-орієнтованого програмування.

Репозиторій — багатомодульний Maven-проєкт: кожна лабораторна є окремим
модулем, а спільні налаштування (версія Java, JUnit 5, Mockito, JaCoCo)
описані в кореневому [`pom.xml`](pom.xml).

| Лабораторна       | Тема                     |
|-------------------|--------------------------|
| [`LAB1`](LAB1)    | Фургон кави              |
| [`LAB2`](LAB2)    | Туристичні путівки: XML, XSD, SAX/DOM/StAX, XSLT |
| [`LAB3`](LAB3)    | Нотатки з хмарною синхронізацією (Android, Kotlin) |

LAB3 — Android-застосунок, тому він збирається Gradle в Android Studio
окремо від Maven-проєкту (див. [`LAB3/README.md`](LAB3/README.md)).

Зібрати LAB1 і LAB2 та запустити тести (Maven встановлювати не потрібно —
його завантажить Maven Wrapper; потрібна лише JDK 17+ і змінна `JAVA_HOME`):

```bash
./mvnw verify
```

У Windows PowerShell: `.\mvnw verify`.
