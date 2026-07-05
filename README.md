# SummerSchool_2026 — «Апекс» (картинг-центр)

Учебный проект: полный цикл разработки клиентского мобильного приложения для картинг-центра «Апекс» — от анализа предметной области и требований до Android-прототипа с мок-бэкендом и SQL-схемой реального бэкенда.

> Приложение решает проблему ручной записи на заезды (Telegram + маркерная доска): клиенты сами видят свободные слоты, бронируют место, оплачивают депозит по ссылке, управляют своими бронями и оценивают маршалов.

Этот README — навигационная карта по репозиторию: что где лежит и зачем.

---

## Структура репозитория

```
SummerSchool_2026/
├── 01-analysis/         # Аналитика: от брифа заказчика до OpenAPI-контракта
├── android/             # Android-приложение (Kotlin + Jetpack Compose, мок-бэкенд)
├── backend/             # SQL-миграции и сиды реального бэкенда (PostgreSQL)
├── bug-reports/         # Баг-репорты и тест-кейсы по Android-приложению
├── implemented features.md   # Сводка реализованных фич приложения
└── prompts.txt          # Лог промптов, использованных при разработке (для отчёта по курсу)
```

---

## 01-analysis/ — аналитика и дизайн

Последовательные этапы анализа, от интервью с заказчиком до готового API-контракта.

| Папка | Содержимое |
|---|---|
| `1-elicitation/` | Сбор требований: `brief-karting.md` (бриф заказчика), `domain-description.md` (описание предметной области), `questions-to-client.md` (вопросы заказчику) |
| `2-requirements/` | Формальные требования: бизнес- (`01-business-requirements.md`), функциональные (`02-functional-requirements.md`), нефункциональные (`03-non-functional-requirements.md`), use-case'ы (`04-use-cases.md`), пользовательские истории (`05-user-stories.md`). Есть свой `README.md` с описанием структуры |
| `3-design-brief/` | UX/UI-бриф: `00-foundations.md` (общие принципы UI, состояния, tone of voice), реестр экранов `SCR-001…009` и шторок (bottom sheets) `BS-001…006`, а также `design-brief.md` (карта навигации) и `design-review.md` (ревью и разрешение противоречий) |
| `4-design/` | Технический дизайн: `data-model.md` (модель данных), `api-sequence.md` (диаграммы последовательностей запросов) |
| `5-mobile-app-spec/` | Финальная спецификация клиентского приложения — то, по чему реально разрабатывался Android-код: экраны `SCR-*.md`, шторки `BS-*.md`, сквозные сценарии `FLOW-001…007` (папка `09_Логики/`), `feature-list.md` (89 фич с приоритетами MoSCoW), шаблоны `_SCREEN_TEMPLATE.md` / `_LOGIC_TEMPLATE.md`. Есть свой `README.md` — если нужно разобраться в приложении, **начинать читать отсюда** |
| `api/` | Модульный OpenAPI 3.0.3-контракт, разбитый по доменам: `auth/`, `slots/`, `bookings/`, `instructors/`, `profile/`, общие схемы в `common/models.yaml`, сборный файл `openapi.yaml`. Есть свой `README.md` |

**Если нужно быстро понять, что делает приложение** → `01-analysis/5-mobile-app-spec/README.md` (там пошаговый гайд для разработчика).

---

## android/ — Android-приложение

Прототип клиентского приложения на **Kotlin + Jetpack Compose** с мок-репозиторием вместо реального бэкенда (все данные в памяти, сеть имитируется).

- `IMPLEMENTATION_CHECKLIST.md` — чек-лист реализации по спринтам (статус: 92%, 155/168 задач)
- `app/src/main/java/com/apex/karting/`
  - `MainActivity.kt` — точка входа, `NavHost`
  - `data/` — `ApexRepository.kt` (мок-репозиторий с имитацией сети и ошибок API), `Models.kt` (модели данных), `MockData.kt` (тестовые данные)
  - `ui/screens/` — экраны по доменам: `auth/` (регистрация/вход), `slots/` (список и детали слотов), `booking/` (создание брони), `bookings/` (список и детали брони), `rating/` (оценка маршала), `profile/` (профиль), `sheets/` (bottom sheets)
  - `ui/components/` — общие UI-компоненты (`CommonComponents.kt`)
  - `ui/navigation/` — граф навигации (`NavGraph.kt`)
  - `ui/theme/` — тема и цвета
  - `util/Formatters.kt` — форматирование дат/цен
  - `src/test/` — юнит-тесты (`ModelsTest.kt`)

---

## backend/ — схема БД реального бэкенда

Только SQL-слой (PostgreSQL), код бэкенда в этой поставке не реализован — только миграции и сиды.

- `internal/repository/migrations/` — миграции `000001…000007` (парами `.up.sql` / `.down.sql`): начальная схема, статусы бронирований и слотов, программа лояльности, флаг оценки, причины отмены, индексы производительности
- `internal/repository/seeds/` — тестовые данные по таблицам (`clients`, `marshals`, `slots`, `bookings`, `ratings`, `sms_attempts`, `jwt_tokens`) и общий `seed_all.sql`

Основные сущности БД: `clients`, `marshals`, `slots`, `bookings`, `ratings` (см. `000001_init_schema.up.sql`).

---

## bug-reports/ — баги и тесты

- `01bag-report.md`, `02bag-report.md`, `03bag-report.md` — отдельные баг-репорты по найденным дефектам Android-приложения
- `test-case.md` — тест-кейс(ы) для проверки сценариев

---

## Файлы в корне

- `implemented features.md` — итоговая сводка реализованных функций Android-приложения (авторизация, слоты, бронирование, отмена, оценка маршала, профиль и т.д.) с разделом Out of Scope
- `prompts.txt` — журнал промптов, которые использовались при генерации аналитики и кода (для отчёта по курсу)

---

## Как быстро найти нужное

| Хочу понять... | Смотреть в |
|---|---|
| Зачем этот проект и какая проблема решается | `01-analysis/1-elicitation/domain-description.md` |
| Какие функции должно/не должно делать приложение | `01-analysis/2-requirements/` |
| Как выглядит и работает каждый экран | `01-analysis/5-mobile-app-spec/SCR-*.md` |
| Какие модальные окна (шторки) есть | `01-analysis/3-design-brief/` или `5-mobile-app-spec/BS-*.md` |
| Контракт API (эндпоинты, модели) | `01-analysis/api/` |
| Схему базы данных | `backend/internal/repository/migrations/000001_init_schema.up.sql` |
| Код Android-приложения | `android/app/src/main/java/com/apex/karting/` |
| Что уже реализовано, а что нет | `implemented features.md` |
| Найденные баги | `bug-reports/` |
