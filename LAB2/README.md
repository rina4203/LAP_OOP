# Лабораторна робота 2 — Туристичні путівки (варіант 13)

XML-документ із туристичними путівками, XSD-схема для нього, розбір парсерами
SAX, DOM і StAX, валідація, сортування та XSL-перетворення з групуванням.

## Предметна область

Кожна путівка (`touristVoucher`) має:

| Поле | XML | Обмеження в XSD |
|---|---|---|
| Номер | атрибут `id` | `xs:ID`, шаблон `v` + 2–4 цифри, обов'язковий |
| Тип путівки | атрибут `type` | перелік: `WEEKEND`, `EXCURSION`, `REST`, `PILGRIMAGE`, `HEALTH` |
| Країна | `country` | рядок 2–56 символів |
| Дні / ночі | `duration` з атрибутами `days`, `nights` | 1–60 днів, 0–59 ночей |
| Транспорт | `transport` | `AIR`, `RAIL`, `AUTO`, `LINER` |
| Готель | `hotel` з атрибутом `stars` | зірки 1–5; харчування `NONE`/`BB`/`HB`/`FB`/`AI`, номер 1–3-місний, телевізор, кондиціонер |
| Вартість | `cost` зі списком `service` | що включено (`TRANSPORT`, `ACCOMMODATION`, `MEALS`, ...) і скільки, сума ≥ 0 з двома знаками після коми |

Кореневий елемент — `touristVouchers` (список путівок Tourist voucher).

Файли даних — у [`src/main/resources`](src/main/resources):
[`vouchers.xml`](src/main/resources/vouchers.xml),
[`vouchers.xsd`](src/main/resources/vouchers.xsd),
[`vouchers-by-type.xsl`](src/main/resources/vouchers-by-type.xsl).

## Структура

```
ua.lab.vouchers
├── TouristVoucherApplication   валідація → розбір трьома парсерами →
│                               сортування → XSL-перетворення
├── model                       TouristVoucher, Hotel, ServiceCost (records) і переліки
├── validation                  XmlValidator — перевірка XML за XSD, усі помилки з номерами рядків
├── parser
│   ├── VoucherParser           інтерфейс парсера
│   ├── AbstractVoucherParser   спільна частина: обробка помилок і журнал
│   ├── Sax/Dom/StaxVoucherParser
│   ├── VoucherAssembler        збирає путівки з подій «початок/кінець елемента»
│   └── ParserType              SAX, DOM, STAX — фабрика парсерів
├── sort                        VoucherOrder — порядки сортування (Comparator)
└── transform                   XslTransformer
```

**Без дублювання коду:** усі три парсери передають події свого API в один
`VoucherAssembler`. SAX і StAX і так працюють подіями, а DOM обходить дерево
й генерує ті самі події. Тому правила «який елемент у яке поле» записані
один раз.

**Сортування** — `VoucherOrder` реалізує `Comparator<TouristVoucher>`:
за вартістю, за тривалістю, за країною (за українською абеткою) та за зірками
готелю.

**XSL** групує путівки за типом (групування Мюнха, XSLT 1.0):

```xml
<touristVouchers>
    <EXCURSION>
        <touristVoucher id="v01" type="EXCURSION">...</touristVoucher>
        <touristVoucher id="v05" type="EXCURSION">...</touristVoucher>
    </EXCURSION>
    <HEALTH>...</HEALTH>
    ...
</touristVouchers>
```

**Логування** — Log4j2, налаштування у
[`log4j2.xml`](src/main/resources/log4j2.xml): у консоль — рівень INFO,
у файл `logs/tourist-vouchers.log` — DEBUG. У тестах журнал вимкнено
(`src/test/resources/log4j2-test.xml`).

## Запуск

В IDEA — зелена стрілка біля `main` у `TouristVoucherApplication`.

З терміналу (з кореня `LAB_OOP`):

```bash
./mvnw -pl LAB2 -am package -DskipTests
```

```bash
java -jar LAB2/target/tourist-vouchers-1.0.jar
```

Можна вказати свій XML і файл результату:
`java -jar LAB2/target/tourist-vouchers-1.0.jar мої-путівки.xml результат.xml`.
Без аргументів береться вбудований `vouchers.xml`, а результат записується
в `target/vouchers-by-type.xml`.

## Тести

`./mvnw -pl LAB2 -am verify` — JUnit 5, покриття рядків і гілок 100% (JaCoCo,
збірка падає нижче 95%). Парсери перевіряються однаковими тестами
(`@EnumSource(ParserType.class)`), зокрема що всі три дають однаковий результат
і не читають зовнішні сутності (захист від XXE).
