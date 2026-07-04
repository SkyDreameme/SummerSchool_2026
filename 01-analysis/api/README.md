# OpenAPI-спецификация клиентского API «Апекс»

Модульная OpenAPI 3.0.3-спецификация REST API клиентского мобильного приложения картинг-центра «Апекс».

## Структура

Спецификация разделена на модули по доменным сущностям. Каждый домен — отдельная папка с двумя файлами:

# OpenAPI-спецификация клиентского API «Апекс»

Модульная OpenAPI 3.0.3-спецификация REST API клиентского мобильного приложения картинг-центра «Апекс».

## Структура

Спецификация разделена на модули по доменным сущностям. Каждый домен — отдельная папка с двумя файлами:

    api/
    ├── auth/                    # Авторизация по SMS (SCR-001, UC-1)
    │   ├── api.yaml             # Эндпоинты: /auth/sms/send, /auth/sms/verify
    │   └── models.yaml          # Схемы: SendSmsRequest, AuthResponse и др.
    │
    ├── slots/                   # Расписание слотов (SCR-002, SCR-003, UC-2)
    │   ├── api.yaml             # Эндпоинты: GET /slots, GET /slots/{slotId}
    │   └── models.yaml          # Схемы: Slot, Marshal, SlotListResponse
    │
    ├── bookings/                # Брони клиента (SCR-004, SCR-005, SCR-006, UC-3, UC-5)
    │   ├── api.yaml             # Эндпоинты: GET/POST /bookings, /bookings/{id}, /bookings/{id}/cancel
    │   └── models.yaml          # Схемы: Booking, BookingDetails, CreateBookingRequest
    │
    ├── instructors/             # Оценка маршала (SCR-008, UC-7)
    │   ├── api.yaml             # Эндпоинт: POST /slots/{slotId}/ratings
    │   └── models.yaml          # Схемы: CreateRatingRequest, MarshalRating
    │
    ├── profile/                 # Профиль клиента (SCR-007, UC-8)
    │   ├── api.yaml             # Эндпоинт: GET /clients/me
    │   └── models.yaml          # Схемы: Client
    │
    └── common/                  # Общие переиспользуемые схемы
        └── models.yaml          # Error, enums (TrackConfiguration, BookingStatus и др.)

### Формат файлов

- **`api.yaml`** — описание эндпоинтов домена (`paths`, `operations`, `parameters`, `responses`).
- **`models.yaml`** — схемы данных домена (`components/schemas`, `components/parameters`).
- **`common/models.yaml`** — сквозные схемы, переиспользуемые во всех доменах:
    - `Error` — универсальный формат ошибки (`code`, `message`, `retryAfterSeconds`).
    - Enums — `TrackConfiguration`, `GearOption`, `SlotStatus`, `BookingStatus`, `BookingPaymentStatus`, `LoyaltyStatus`.
    - Responses — `BadRequest`, `NotFound`.

### Ссылки между модулями

Модули связаны через `$ref` с относительными путями. Примеры:

```yaml
# В bookings/api.yaml — ссылка на схему из bookings/models.yaml
$ref: './models.yaml#/components/schemas/Booking'

# В bookings/api.yaml — ссылка на общую схему из common/models.yaml
$ref: '../common/models.yaml#/components/schemas/Error'

# В bookings/models.yaml — ссылка на схему из slots/models.yaml
$ref: '../slots/models.yaml#/components/schemas/Slot'
```


## Где искать сквозные схемы

Все переиспользуемые определения находятся в **`common/models.yaml`**:

| Схема | Назначение | Где используется |
|---|---|---|
| `Error` | Универсальный формат ошибки | Все эндпоинты при 4xx/5xx |
| `TrackConfiguration` | `Short` / `Long` | `slots`, `bookings` |
| `GearOption` | `Rental` / `Own` | `bookings` |
| `SlotStatus` | `Open` / `CancelledByCenter` | `slots` |
| `BookingStatus` | `Created` / `Paid` / `Completed` / `CancelledByClient` / `CancelledByCenter` | `bookings` |
| `BookingPaymentStatus` | `Pending` / `Paid` / `Expired` | `bookings` |
| `LoyaltyStatus` | `None` / `Bronze` / `Silver` | `profile` |
| `BadRequest` | Response 400 | Все эндпоинты |
| `NotFound` | Response 404 | Все эндпоинты с path-параметрами |

