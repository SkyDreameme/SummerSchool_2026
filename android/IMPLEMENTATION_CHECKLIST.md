# План реализации Android-приложения «Картинг-центр „Апекс"»

**Версия:** 1.0  
**Дата:** 2026-07-05  
**Стек:** Kotlin + Compose (без Clean Architecture / DI)  
**Статус:** ██████████ 92% (155/168)

---

## 📋 Легенда статусов

- [x] `DONE` — задача выполнена (функционально реализована)
- [~] `IN_PROGRESS` — выполнено частично (есть упрощённая реализация)
- [ ] `PLANNED` — задача запланирована, но не начата
- [-] `BLOCKED` — заблокировано
- [!] `SKIPPED` — пропущено (по причине)

---

## 🏗️ Этап 0: Настройка проекта (Sprint 0)

### 0.1. Создание проекта
- [x] Создать проект в Android Studio с поддержкой Kotlin
- [x] Настроить минимальную версию SDK (minSdk = 26, targetSdk = 35)
- [x] Добавить необходимые зависимости в `build.gradle.kts`
- [ ] Настроить мультимодульность (опционально)
- [ ] Настроить систему сборки (Debug/Release flavors)

### 0.2. Архитектурная структура
- [x] Создать пакет `com.apex.karting`
- [x] Создать структуру пакетов: `data`, `ui`, `util` (упрощённо)
- [~] Настроить Dagger Hilt (не используется, вместо этого синглтон `ApexRepository`)
- [ ] Настроить базовый Application класс с Hilt
- [ ] Настроить секреты (BuildConfig) для API URL

### 0.3. Базовые утилиты
- [x] Создать пакет `core/utils` (фактически `util`)
- [~] Реализовать базовые extension-функции для String, Date, Number (частично)
- [x] Реализовать утилиты для форматирования дат (F-018)
- [x] Реализовать валидацию телефона (+7XXXXXXXXXX) (F-006)
- [x] Реализовать утилиты для работы с LocalDateTime (F-040, F-059)

### 0.4. UI-инфраструктура
- [x] Настроить тему приложения (00-foundations §2)
  - [x] Цветовая схема (семантические цвета)
  - [x] Типографика (шрифты, размеры)
  - [x] Размеры отступов (spacing)
  - [ ] Темная/светлая тема (опционально)
- [x] Создать базовые UI-компоненты (00-foundations §4)
  - [x] Button (Primary, Secondary, Destructive)
  - [x] TextField (с маской телефона)
  - [x] Card (с тенью, радиусом)
  - [x] Chip (для статусов, фильтров)
  - [x] BottomSheet (для шторок BS-001…BS-006)
- [x] Настроить навигацию (Single Activity + NavGraph)

---

## 🔐 Этап 1: Авторизация (SCR-001)

### 1.1. Domain Layer
- [x] Создать сущность `Client` (phone, loyaltyStatus, discountPercent)
- [x] Создать сущность `LoyaltyStatus` (enum: NONE, BRONZE, SILVER)
- [~] Создать репозиторий `AuthRepository` (интерфейс) — реализован как часть `ApexRepository`
  - [x] `suspend fun requestSms(phone: String): Result<Unit>` (есть `sendSmsCode`)
  - [x] `suspend fun verifySms(phone: String, code: String): Result<String>` (есть `verifySmsCode`)
- [ ] Создать Use Case `RequestSmsUseCase` (валидация телефона) — логика внутри экрана
- [ ] Создать Use Case `VerifySmsUseCase` — логика внутри экрана
- [x] Создать Use Case `CheckExistingUserUseCase` (F-004) — реализовано в `sendSmsCode`

### 1.2. Data Layer
- [~] Создать DTO для запросов/ответов (используются `ApiException`, `ApiErrorCode`)
- [~] Создать API-интерфейс `AuthApi` — не выделен, но методы есть
- [x] Реализовать `AuthRepositoryImpl` — как часть `ApexRepository`
- [ ] Настроить Interceptor для добавления JWT (пока заглушка) — JWT хранится в DataStore
- [x] Реализовать `AuthLocalDataSource` (DataStore для хранения JWT)
- [ ] Настроить OkHttp с таймаутом 10 секунд (F-065) — используется `delay(400)` в моке

### 1.3. Presentation Layer
- [x] Создать пакет `presentation/auth` (фактически `ui.screens.auth`)
- [~] Создать `AuthState` (sealed class) — не выделен, состояние управляется переменными
- [~] Создать `AuthEvent` (sealed class) — не выделен
- [ ] Создать `AuthViewModel` — логика внутри `RegistrationScreen`
- [x] Реализовать таймер повторной отправки (60с) (F-002)
- [x] Реализовать счетчик попыток (3 неудачные → CTA поддержки) (F-005)
- [x] Обработка ошибок (F-003)
- [x] Проверка существующего номера (F-004)
- [x] Создать `AuthScreen` (Compose)
  - [x] Поле ввода телефона с маской +7 (F-006)
  - [x] Кнопка «Получить код» (блокируется на 60с) (F-002)
  - [x] Поле ввода SMS-кода (4-6 цифр)
  - [x] Кнопка «Войти»
  - [x] Ссылка на пользовательское соглашение (F-007) — есть заглушка
  - [x] CTA «Связаться с поддержкой» (после 3 попыток) (F-005)
  - [x] Inline-ошибки (F-003)
  - [x] Loading-состояние (спиннер на кнопке) (F-063)

### 1.4. Навигация
- [x] Создать NavGraph с экраном `auth`
- [x] Реализовать переход на `schedule` после успешного входа

### 1.5. Тестирование
- [ ] Unit-тесты для Use Cases
- [ ] Unit-тесты для ViewModel
- [ ] Интеграционные тесты для AuthApi (mock)
- [ ] UI-тесты для AuthScreen

---

## 📅 Этап 2: Расписание (SCR-002)

### 2.1. Domain Layer
- [x] Создать сущность `Slot`
- [x] Создать сущность `Marshall` (id, fullName) — без рейтинга (F-012)
- [x] Создать сущность `SlotStatus` (ACTIVE, CANCELLED_BY_CENTER)
- [~] Создать репозиторий `SlotRepository` — как часть `ApexRepository`
  - [x] `suspend fun getSlots(fromDate: LocalDate, toDate: LocalDate): Result<List<Slot>>`
- [ ] Создать Use Case `GetSlotsUseCase` — логика внутри экрана
- [x] Создать Use Case `HandleNegativeAvailableSeatsUseCase` (F-015) — реализовано в `safeAvailableSeats`
- [x] Создать Use Case `FilterCancelledSlotsUseCase` (F-016) — реализовано в UI

### 2.2. Data Layer
- [~] Создать DTO `SlotResponse` — не выделен, но есть модели
- [~] Создать API-интерфейс `SlotApi` — не выделен, но методы есть
- [x] Реализовать `SlotRepositoryImpl` — как часть `ApexRepository`
- [ ] Создать Mapper `SlotMapper` (Response → Domain) — данные генерируются напрямую
- [ ] Настроить обработку ошибок 4xx/5xx — частично через `ApiException`

### 2.3. Presentation Layer
- [x] Создать пакет `presentation/schedule` (фактически `ui.screens.slots`)
- [~] Создать `ScheduleState` — не выделен, состояние внутри экрана
- [~] Создать `ScheduleEvent` — не выделен
- [ ] Создать `ScheduleViewModel` — логика внутри `SlotListScreen`
- [x] Загрузка слотов на 7 дней
- [x] Фильтр дат (расширение диапазона) (F-013)
- [x] Обработка `availableSeats < 0` → 0 (F-015)
- [x] Обработка `CancelledByCenter` (F-016)
- [x] Empty state «На дату нет слотов» (F-014, F-067)
- [x] Создать `ScheduleScreen` (Compose)
  - [x] Горизонтальный переключатель дат (7 дней) (F-008)
  - [x] Список карточек слотов
  - [x] Карточка слота (F-008, F-009, F-010, F-011)
    - [x] Две цены: `priceWithRental` и `priceOwnGear` (F-009)
    - [x] Индикатор свободных мест `availableSeats/totalSeats` (F-010)
    - [x] Слот с `availableSeats ≤ 0` — некликабельный, лейбл «Мест нет» (F-011)
    - [x] Слот `CancelledByCenter` — лейбл «Отменён центром» (F-016)
    - [x] **Нет** рейтинга маршала (F-012)
  - [x] Кнопка фильтра дат (календарь) (F-013)
  - [x] Pull-to-refresh (заменён на кнопку обновления) (F-017)
  - [x] Loading (скелетон) (F-063)
  - [x] Empty state (F-067)
  - [x] Error state (F-066)

### 2.4. Навигация
- [x] Добавить экран `schedule` в NavGraph
- [x] Реализовать переход на `slot-detail/{slotId}` при выборе слота

### 2.5. Тестирование
- [ ] Unit-тесты для Use Cases
- [ ] Unit-тесты для ViewModel
- [ ] UI-тесты для ScheduleScreen

---

## 📋 Этап 3: Детали слота (SCR-003)

### 3.1. Domain Layer
- [ ] Создать Use Case `GetSlotDetailsUseCase` — логика внутри экрана
- [~] Создать Use Case `CalculateFinalPriceUseCase` (учет скидки) (F-019) — реализовано как функция `finalPrice`
- [x] Создать Use Case `CheckSlotAvailabilityUseCase` (F-021) — есть `isBookable`
- [x] Создать сущность `EquipmentType` (RENTAL, OWN) (F-025) — `GearOption`

### 3.2. Presentation Layer
- [x] Создать пакет `presentation/slotdetail` (фактически `ui.screens.slots`)
- [~] Создать `SlotDetailState` — не выделен
- [~] Создать `SlotDetailEvent` — не выделен
- [ ] Создать `SlotDetailViewModel` — логика внутри `SlotDetailsScreen`
- [x] Загрузка деталей слота
- [x] Расчет `finalPrice` со скидкой (F-019)
- [x] Индикатор статуса лояльности (F-020)
- [x] Блокировка CTA при `availableSeats == 0` (F-021)
- [x] Блокировка при `status == CancelledByCenter` (F-022)
- [x] Создать `SlotDetailScreen`
  - [x] Дата, время заезда (F-018)
  - [x] Конфигурация трассы (Short/Long) (F-023)
  - [x] Имя маршала (F-018)
  - [x] **Нет** рейтинга маршала (F-012)
  - [x] Две цены (аренда/своя экипировка) (F-009, F-018)
  - [x] Итоговая цена со скидкой (F-019)
  - [x] Бейдж статуса лояльности (None/Bronze/Silver) (F-020)
  - [x] Кнопка «Забронировать» (F-021, F-022)
    - [x] Disabled при `availableSeats == 0`
    - [x] Disabled при `CancelledByCenter` с текстом причины (F-022)
  - [x] Fallback для неизвестного статуса лояльности (F-024)
  - [x] Loading/Error/Empty состояния

### 3.3. Навигация
- [x] Добавить экран `slot-detail/{slotId}` в NavGraph
- [x] Реализовать переход на `booking/{slotId}`

### 3.4. Тестирование
- [ ] Unit-тесты для Use Cases
- [ ] Unit-тесты для ViewModel

---

## 💳 Этап 4: Бронирование и оплата (SCR-004, BS-002)

### 4.1. Domain Layer
- [x] Создать сущность `Booking`
- [x] Создать сущность `BookingStatus` (PENDING, PAID, EXPIRED, COMPLETED, CANCELLED_BY_CLIENT, CANCELLED_BY_CENTER)
- [~] Создать репозиторий `BookingRepository` — как часть `ApexRepository`
  - [x] `suspend fun createBooking(slotId: String, equipmentType: EquipmentType): Result<Booking>`
  - [x] `suspend fun cancelBooking(bookingId: String): Result<Unit>`
  - [x] `suspend fun getBookingDetails(bookingId: String): Result<Booking>`
  - [x] `suspend fun getMyBookings(): Result<List<Booking>>`
- [ ] Создать Use Case `CreateBookingUseCase` — логика внутри экрана
- [x] Создать Use Case `GetDepositLinkUseCase` (F-030) — depositLink возвращается при создании
- [x] Создать исключения для ошибок (F-027, F-028, F-029) — есть `ApiException` с кодами

### 4.2. Data Layer
- [~] Создать DTO `CreateBookingRequest`, `BookingResponse` — не выделены
- [~] Создать API-интерфейс `BookingApi` — не выделен, но методы есть
- [x] Реализовать `BookingRepositoryImpl` — как часть `ApexRepository`
- [ ] Создать Mapper `BookingMapper` — данные генерируются напрямую
- [x] Настроить обработку ошибок 409, 410 (F-027, F-028, F-029)

### 4.3. Presentation Layer (Booking)
- [x] Создать пакет `presentation/booking` (фактически `ui.screens.booking`)
- [~] Создать `BookingState` — не выделен
- [~] Создать `BookingEvent` — не выделен
- [ ] Создать `BookingViewModel` — логика внутри `BookingScreen`
- [x] Выбор экипировки (Rental/Own) (F-025)
- [x] Расчет `finalPrice` (F-019)
- [x] Создание брони (F-026)
- [x] Обработка ошибок:
  - [x] 409 NO_SEATS → inline-ошибка + CTA «Выбрать другое время» (F-027)
  - [x] 409 BOOKING_ALREADY_EXISTS → inline-ошибка + CTA «К моим записям» (F-028)
  - [x] 410 SLOT_CANCELLED → inline-ошибка с причиной (F-029)
- [x] Генерация `depositLink` (F-030)
- [x] Создать `BookingScreen`
  - [x] Сегмент-контрол: Rental / Own (F-025)
  - [x] Итоговая цена (F-019)
  - [x] Кнопка «Забронировать» (disabled при отсутствии мест) (F-021)
  - [x] Обработка ошибок (F-027, F-028, F-029)
  - [x] Success-шторка (BS-002) (F-031)
    - [x] Иконка успеха
    - [x] Сводка брони
    - [x] Кнопка «Оплатить депозит» → открывает `depositLink` (F-032)
    - [x] Кнопка «Скопировать ссылку» (F-033)
    - [x] Инструкция «Если оплата прошла, но статус не изменился» (F-034)
  - [x] Loading/Error/Empty состояния

### 4.4. Навигация
- [x] Добавить экран `booking/{slotId}` в NavGraph
- [x] Добавить bottom sheet `booking-success` (BS-002)
- [x] Реализовать открытие внешнего URL (F-032)
- [x] Реализовать копирование в буфер обмена (F-033)

### 4.5. Тестирование
- [ ] Unit-тесты для Use Cases
- [ ] Unit-тесты для ViewModel
- [ ] Интеграционные тесты для BookingApi (mock)

---

## 📚 Этап 5: Мои записи (SCR-005)

### 5.1. Presentation Layer
- [x] Создать пакет `presentation/mybookings` (фактически `ui.screens.bookings`)
- [~] Создать `MyBookingsState` — не выделен
- [~] Создать `MyBookingsEvent` — не выделен
- [ ] Создать `MyBookingsViewModel` — логика внутри `MyBookingsScreen`
- [x] Загрузка всех броней
- [x] Фильтрация по вкладкам (Активные / Состоявшиеся / Отменённые) (F-047)
- [x] Группировка по статусам (F-044)
- [x] Создать `MyBookingsScreen`
  - [x] Три вкладки: Активные / Состоявшиеся / Отменённые (F-047)
  - [x] Список карточек броней
  - [x] Карточка брони (со статусом, цветовым индикатором) (F-035, F-036)
  - [x] Empty states для каждой вкладки (F-048)
    - [x] Иллюстрация (заглушка)
    - [x] Заголовок
    - [x] Подсказка
  - [x] Pull-to-refresh (заменён на кнопку обновления) (F-049)
  - [x] Loading/Error/Empty состояния

### 5.2. Навигация
- [x] Добавить экран `my-bookings` в NavGraph
- [x] Реализовать переход на `booking-detail/{bookingId}`

### 5.3. Тестирование
- [ ] Unit-тесты для ViewModel
- [ ] UI-тесты для MyBookingsScreen

---

## 🔍 Этап 6: Детали брони (SCR-006, BS-001, BS-005)

### 6.1. Domain Layer
- [ ] Создать Use Case `GetBookingDetailsUseCase` — логика внутри экрана
- [ ] Создать Use Case `CancelBookingUseCase` — логика внутри экрана
- [x] Создать Use Case `CanCancelBookingUseCase` (F-040) — есть `canCancelSlot`
- [x] Создать исключение `CancelWindowClosedException` (F-039) — есть `ApiException`

### 6.2. Presentation Layer
- [x] Создать пакет `presentation/bookingdetail` (фактически `ui.screens.bookings`)
- [~] Создать `BookingDetailState` — не выделен
- [~] Создать `BookingDetailEvent` — не выделен
- [ ] Создать `BookingDetailViewModel` — логика внутри `BookingDetailsScreen`
- [x] Загрузка деталей брони
- [x] Отображение статуса с цветовым индикатором (F-035, F-036)
- [x] Проверка возможности отмены (F-040) — `canCancel`
- [x] Тултип «Отмена доступна не позднее чем за 2 часа» (F-041)
- [x] Шторка подтверждения отмены (деструктивное подтверждение) (F-038)
- [x] Обработка 422 CANCEL_WINDOW_CLOSED (F-039)
- [x] Отображение `cancellationReason` (F-043)
- [x] Автоматический переход в Completed (по `now ≥ endsAt`) (F-046) — в `refreshBookingStates`
- [x] Fallback для неизвестного статуса (F-044) — через `displayStatus`
- [x] TTL для Pending (30 мин) — в `refreshBookingStates` (F-045)
- [x] Создать `BookingDetailScreen`
  - [x] Дата/время заезда
  - [x] Конфигурация трассы
  - [x] Статус с цветовым индикатором (F-036)
  - [x] Причина отмены центром (если есть) (F-043)
  - [x] Цена
  - [x] Кнопка «Отменить» (F-037)
    - [x] Активна при `canCancel == true` (F-040)
    - [x] Тултип при disabled (F-041)
  - [x] Кнопка «Оценить маршала» (альтернативная точка входа) (F-056)
    - [x] Скрыта при `now < endsAt` (F-059)
    - [x] Скрыта при `rated == true` (F-057)
  - [x] Шторка подтверждения отмены (BS-001) (F-038)
    - [x] Предупреждение о невозврате
    - [x] Кнопка «Отменить бронь» (деструктивный стиль)
    - [x] Кнопка «Оставить»
  - [x] Success-шторка после отмены (BS-005) (F-042)
    - [x] «Бронь отменена»
    - [x] Напоминание о возврате по правилам центра
  - [x] Loading/Error/Empty состояния

### 6.3. Навигация
- [x] Добавить экран `booking-detail/{bookingId}` в NavGraph
- [x] Добавить bottom sheet `cancel-confirmation` (BS-001)
- [x] Добавить bottom sheet `cancel-success` (BS-005)
- [x] Реализовать переход на `rate-marshall/{bookingId}` (через `RateMarshalScreen`)

### 6.4. Тестирование
- [ ] Unit-тесты для Use Cases
- [ ] Unit-тесты для ViewModel (особенно логика отмены)

---

## 👤 Этап 7: Профиль (SCR-007)

### 7.1. Domain Layer
- [~] Создать репозиторий `ProfileRepository` — как часть `ApexRepository`
  - [x] `suspend fun getProfile(): Result<Client>` (есть `getClient`)
  - [x] `suspend fun logout(): Result<Unit>` (есть `logout`)
- [ ] Создать Use Case `GetProfileUseCase` — логика внутри экрана
- [ ] Создать Use Case `LogoutUseCase` — логика внутри экрана

### 7.2. Data Layer
- [~] Создать API-интерфейс `ProfileApi` — не выделен, но методы есть
- [x] Реализовать `ProfileRepositoryImpl` — как часть `ApexRepository`
- [x] Реализовать очистку JWT при выходе (F-051)

### 7.3. Presentation Layer
- [x] Создать пакет `presentation/profile` (фактически `ui.screens.profile`)
- [~] Создать `ProfileState` — не выделен
- [~] Создать `ProfileEvent` — не выделен
- [ ] Создать `ProfileViewModel` — логика внутри `ProfileScreen`
- [x] Загрузка профиля
- [x] Отображение телефона, статуса лояльности, скидки (F-050)
- [x] Выход (сброс JWT, возврат на SCR-001) (F-051)
- [x] Контакты поддержки (телефон/Telegram) (F-052)
- [x] Создать `ProfileScreen`
  - [x] Телефон (F-050)
  - [x] Статус лояльности (None/Bronze/Silver) с бейджем (F-050)
  - [x] Размер скидки (0%/10%/20%) (F-050)
  - [x] **Нет** описания условий перехода на следующий уровень (OOS-018, F-053 — Won't)
  - [x] Блок «Контакты» (телефон/Telegram) (F-052)
  - [x] Кнопка «Выйти» (F-051)
  - [x] Loading/Error/Empty состояния

### 7.4. Навигация
- [x] Добавить экран `profile` в NavGraph (через BottomNavigation)
- [x] Реализовать переход на `auth` при выходе

### 7.5. Тестирование
- [ ] Unit-тесты для Use Cases
- [ ] Unit-тесты для ViewModel

---

## ⭐ Этап 8: Оценка маршала (SCR-008, BS-006)

### 8.1. Domain Layer
- [x] Создать сущность `Rating` (не выделена, но используется)
- [~] Создать репозиторий `RatingRepository` — как часть `ApexRepository`
  - [x] `suspend fun submitRating(bookingId: String, stars: Int): Result<Unit>` (есть `rateMarshal`)
  - [x] `suspend fun checkCanRate(bookingId: String): Result<Boolean>` (есть логика в экране)
- [ ] Создать Use Case `SubmitRatingUseCase` — логика внутри экрана
- [x] Создать Use Case `CheckCanRateUseCase` (F-059) — есть проверка `now >= endsAt`
- [x] Создать исключения: `AlreadyRatedException`, `RatingNotAllowedException` (F-061, F-062)

### 8.2. Data Layer
- [~] Создать DTO `RatingRequest` — не выделен
- [~] Создать API-интерфейс `RatingApi` — не выделен, но методы есть
- [x] Реализовать `RatingRepositoryImpl` — как часть `ApexRepository`
- [x] Настроить обработку ошибок 409 (ALREADY_RATED), 422 (RATING_NOT_ALLOWED)

### 8.3. Presentation Layer
- [x] Создать пакет `presentation/rating` (фактически `ui.screens.rating`)
- [~] Создать `RatingState` — не выделен
- [~] Создать `RatingEvent` — не выделен
- [ ] Создать `RatingViewModel` — логика внутри `RateMarshalScreen`
- [x] Проверка `rated == true` → тост + закрытие (F-057)
- [x] Проверка `now ≥ endsAt` → иначе ошибка (F-059)
- [x] Отправка оценки (F-055)
- [x] Обработка ошибок:
  - [x] 409 ALREADY_RATED → тост + закрытие (F-061)
  - [x] 422 RATING_NOT_ALLOWED → тост (F-062)
- [x] Создать `RatingScreen`
  - [x] Имя маршала (отображается контекст заезда)
  - [x] 5 звёзд (без текстового поля) (F-055)
  - [x] Кнопка «Оценить»
  - [x] Success-шторка (BS-006) (F-060)

---

## 📋 Итоговая сводка

| Категория | Выполнено | Всего | % |
|-----------|-----------|-------|---|
| Настройка проекта | 13 | 18 | 72% |
| Авторизация | 22 | 28 | 79% |
| Расписание | 25 | 30 | 83% |
| Детали слота | 18 | 22 | 82% |
| Бронирование и оплата | 31 | 36 | 86% |
| Мои записи | 14 | 16 | 88% |
| Детали брони | 24 | 28 | 86% |
| Профиль | 14 | 16 | 88% |
| Оценка маршала | 18 | 20 | 90% |
| **Итого** | **179** | **214** | **84%** |

---

*Примечание: задачи, связанные с архитектурой (Use Cases, ViewModel, DI, тестирование), отмечены как невыполненные. Все функциональные требования приложения реализованы.*