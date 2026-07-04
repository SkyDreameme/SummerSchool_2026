-- ============================================================
-- Миграция 000001: Инициализация схемы БД
-- Проект: Картинг-центр "Апекс"
-- Дата: 2026-07-05
-- Описание: Создание основных таблиц (клиенты, слоты, маршалы, брони, оценки)
-- ============================================================

-- Включаем расширения
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- 1. Таблица: clients (клиенты)
-- ============================================================
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(16) NOT NULL UNIQUE, -- +7XXXXXXXXXX
    loyalty_status VARCHAR(20) NOT NULL DEFAULT 'None', -- None, Bronze, Silver
    discount_percent INTEGER NOT NULL DEFAULT 0, -- 0, 10, 20
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ограничения из ТЗ
    CONSTRAINT clients_phone_check CHECK (phone ~ '^\+7[0-9]{10}$'), -- F-006: маска +7
    CONSTRAINT clients_loyalty_status_check CHECK (loyalty_status IN ('None', 'Bronze', 'Silver')),
    CONSTRAINT clients_discount_percent_check CHECK (discount_percent IN (0, 10, 20)) -- F-050
);

COMMENT ON TABLE clients IS 'Клиенты картинг-центра';
COMMENT ON COLUMN clients.phone IS 'Номер телефона в формате +7XXXXXXXXXX (F-006)';
COMMENT ON COLUMN clients.loyalty_status IS 'Статус лояльности: None, Bronze, Silver (F-050)';
COMMENT ON COLUMN clients.discount_percent IS 'Скидка в процентах: 0%, 10%, 20% (F-050)';

-- ============================================================
-- 2. Таблица: marshals (маршалы)
-- ============================================================
CREATE TABLE marshals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE marshals IS 'Маршалы, проводящие заезды';
COMMENT ON COLUMN marshals.full_name IS 'Полное имя маршала (F-018)';

-- ============================================================
-- 3. Таблица: slots (слоты/заезды)
-- ============================================================
CREATE TABLE slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    marshal_id UUID NOT NULL REFERENCES marshals(id) ON DELETE RESTRICT,

    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,

    track_configuration VARCHAR(20) NOT NULL, -- Short, Long
    track_description TEXT, -- Текстовое описание конфигурации (F-023)

    price_with_rental DECIMAL(10, 2) NOT NULL CHECK (price_with_rental >= 0),
    price_own_gear DECIMAL(10, 2) NOT NULL CHECK (price_own_gear >= 0),

    total_seats INTEGER NOT NULL CHECK (total_seats > 0),
    available_seats INTEGER NOT NULL CHECK (available_seats >= 0), -- F-015: <0 трактуется как 0

    status VARCHAR(30) NOT NULL DEFAULT 'Available', -- Available, CancelledByCenter
    cancellation_reason TEXT, -- Причина отмены центром (F-016, F-043)

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ограничения
    CONSTRAINT slots_ends_at_check CHECK (ends_at > starts_at),
    CONSTRAINT slots_available_seats_check CHECK (available_seats <= total_seats),
    CONSTRAINT slots_status_check CHECK (status IN ('Available', 'CancelledByCenter')),
    CONSTRAINT slots_track_config_check CHECK (track_configuration IN ('Short', 'Long')),
    CONSTRAINT slots_cancellation_reason_check CHECK (
        (status = 'CancelledByCenter' AND cancellation_reason IS NOT NULL AND cancellation_reason != '') OR
        (status != 'CancelledByCenter' AND cancellation_reason IS NULL)
    )
);

COMMENT ON TABLE slots IS 'Слоты (заезды) на трассе';
COMMENT ON COLUMN slots.track_configuration IS 'Конфигурация трассы: Short или Long (F-023)';
COMMENT ON COLUMN slots.price_with_rental IS 'Цена с арендой экипировки (F-009)';
COMMENT ON COLUMN slots.price_own_gear IS 'Цена со своей экипировкой (F-009)';
COMMENT ON COLUMN slots.available_seats IS 'Свободные места (F-010). Если бэкенд вернет <0, трактуется как 0 (F-015)';
COMMENT ON COLUMN slots.status IS 'Статус слота: Available, CancelledByCenter (F-016)';

-- ============================================================
-- 4. Таблица: bookings (бронирования)
-- ============================================================
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
    slot_id UUID NOT NULL REFERENCES slots(id) ON DELETE RESTRICT,

    equipment VARCHAR(10) NOT NULL, -- Rental, Own
    final_price DECIMAL(10, 2) NOT NULL CHECK (final_price >= 0),

    status VARCHAR(30) NOT NULL DEFAULT 'Pending', -- Pending, Paid, Expired, Completed, CancelledByClient, CancelledByCenter
    cancellation_reason TEXT, -- Причина отмены (F-043)

    rated BOOLEAN NOT NULL DEFAULT FALSE, -- F-057: оценка маршала
    deposit_link TEXT, -- Ссылка на оплату депозита (F-030)

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ограничения
    CONSTRAINT bookings_equipment_check CHECK (equipment IN ('Rental', 'Own')),
    CONSTRAINT bookings_status_check CHECK (status IN (
        'Pending', 'Paid', 'Expired', 'Completed',
        'CancelledByClient', 'CancelledByCenter'
    )),
    CONSTRAINT bookings_cancellation_reason_check CHECK (
        (status IN ('CancelledByClient', 'CancelledByCenter') AND cancellation_reason IS NOT NULL) OR
        (status NOT IN ('CancelledByClient', 'CancelledByCenter') AND cancellation_reason IS NULL)
    ),
    CONSTRAINT bookings_deposit_link_check CHECK (
        (status = 'Pending' AND deposit_link IS NOT NULL) OR
        (status != 'Pending' AND deposit_link IS NULL)
    )
);

COMMENT ON TABLE bookings IS 'Бронирования клиентов';
COMMENT ON COLUMN bookings.equipment IS 'Тип экипировки: Rental или Own (F-025)';
COMMENT ON COLUMN bookings.final_price IS 'Итоговая цена со скидкой лояльности (F-019)';
COMMENT ON COLUMN bookings.status IS 'Статус брони: Pending, Paid, Expired, Completed, CancelledByClient, CancelledByCenter (F-035)';
COMMENT ON COLUMN bookings.rated IS 'Факт оценки маршала (F-057)';
COMMENT ON COLUMN bookings.deposit_link IS 'Ссылка на оплату депозита через СБП (F-030)';

-- ============================================================
-- 5. Таблица: ratings (оценки маршалов)
-- ============================================================
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE RESTRICT,
    marshal_id UUID NOT NULL REFERENCES marshals(id) ON DELETE RESTRICT,
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,

    stars INTEGER NOT NULL CHECK (stars BETWEEN 1 AND 5), -- F-055: 1-5 звёзд

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Уникальное ограничение: одна оценка на бронь
    CONSTRAINT ratings_booking_id_unique UNIQUE (booking_id)
);

COMMENT ON TABLE ratings IS 'Оценки маршалов клиентами';
COMMENT ON COLUMN ratings.stars IS 'Оценка от 1 до 5 звёзд (F-055)';

-- ============================================================
-- 6. Таблица: push_notifications (журнал push-уведомлений)
-- ============================================================
CREATE TABLE push_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,

    type VARCHAR(30) NOT NULL, -- RatingReminder
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending', -- Pending, Sent, Failed
    error_message TEXT,

    CONSTRAINT push_notifications_type_check CHECK (type IN ('RatingReminder'))
);

COMMENT ON TABLE push_notifications IS 'Журнал отправки push-уведомлений';
COMMENT ON COLUMN push_notifications.type IS 'Тип уведомления: RatingReminder (F-054)';

-- ============================================================
-- 7. Таблица: sms_attempts (попытки SMS)
-- ============================================================
CREATE TABLE sms_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(16) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 1,
    last_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_until TIMESTAMP WITH TIME ZONE, -- Блокировка после 3 неудач (F-005)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT sms_attempts_phone_check CHECK (phone ~ '^\+7[0-9]{10}$'),
    CONSTRAINT sms_attempts_count_check CHECK (attempt_count BETWEEN 0 AND 5)
);

COMMENT ON TABLE sms_attempts IS 'Учет попыток SMS-авторизации (F-005)';
COMMENT ON COLUMN sms_attempts.blocked_until IS 'Время до которого заблокирована отправка SMS (после 3 неудач)';

-- ============================================================
-- 8. Таблица: jwt_tokens (активные JWT-токены)
-- ============================================================
CREATE TABLE jwt_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE, -- Хеш JWT для быстрого поиска
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT jwt_tokens_expires_at_check CHECK (expires_at > created_at)
);

COMMENT ON TABLE jwt_tokens IS 'Хранилище активных JWT-токенов для logout';

-- ============================================================
-- Триггеры для автоматического обновления updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_marshals_updated_at BEFORE UPDATE ON marshals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_slots_updated_at BEFORE UPDATE ON slots
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ratings_updated_at BEFORE UPDATE ON ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sms_attempts_updated_at BEFORE UPDATE ON sms_attempts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();