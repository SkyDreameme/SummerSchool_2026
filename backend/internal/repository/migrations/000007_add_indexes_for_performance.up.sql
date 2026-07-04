-- ============================================================
-- Миграция 000007: Индексы для производительности
-- ============================================================

-- ============================================================
-- 1. Индексы для таблицы slots
-- ============================================================

-- Для быстрого поиска слотов по дате (F-008, F-013)
CREATE INDEX idx_slots_starts_at ON slots(starts_at);
CREATE INDEX idx_slots_ends_at ON slots(ends_at);
CREATE INDEX idx_slots_starts_ends_at ON slots(starts_at, ends_at);

-- Для фильтрации по статусу и доступности
CREATE INDEX idx_slots_status_available ON slots(status, available_seats)
    WHERE status = 'Available' AND available_seats > 0;

-- Для поиска слотов маршала
CREATE INDEX idx_slots_marshal_id ON slots(marshal_id);

-- ============================================================
-- 2. Индексы для таблицы bookings
-- ============================================================

-- Для поиска броней клиента (SCR-005, SCR-006)
CREATE INDEX idx_bookings_client_id ON bookings(client_id);
CREATE INDEX idx_bookings_client_status ON bookings(client_id, status);

-- Для поиска броней по слоту
CREATE INDEX idx_bookings_slot_id ON bookings(slot_id);

-- Для фильтрации по статусу (активные, состоявшиеся, отмененные)
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_created_at ON bookings(created_at);

-- Для проверки активных броней (NFR-2)
CREATE UNIQUE INDEX idx_bookings_active_client
    ON bookings(client_id)
    WHERE status IN ('Pending', 'Paid');

-- ============================================================
-- 3. Индексы для таблицы ratings
-- ============================================================

-- Для поиска оценок маршала
CREATE INDEX idx_ratings_marshal_id ON ratings(marshal_id);

-- Для проверки уникальности оценки на бронь
CREATE UNIQUE INDEX idx_ratings_booking_id ON ratings(booking_id);

-- ============================================================
-- 4. Индексы для таблицы pending_bookings_ttl
-- ============================================================

-- Для фоновой обработки TTL
CREATE INDEX idx_pending_ttl_expires_at ON pending_bookings_ttl(expires_at)
    WHERE processed = FALSE;

-- ============================================================
-- 5. Индексы для таблицы sms_attempts
-- ============================================================

-- Для быстрого поиска по телефону
CREATE INDEX idx_sms_attempts_phone ON sms_attempts(phone);

-- ============================================================
-- 6. Индексы для таблицы jwt_tokens
-- ============================================================

-- Для быстрого поиска токена
CREATE INDEX idx_jwt_tokens_token_hash ON jwt_tokens(token_hash);

-- Для очистки истекших токенов
CREATE INDEX idx_jwt_tokens_expires_at ON jwt_tokens(expires_at);

-- ============================================================
-- 7. Полнотекстовый поиск (для будущих расширений)
-- ============================================================

-- Триггер для автоматического обновления tsvector (опционально)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX idx_slots_description_search ON slots USING GIN (track_description gin_trgm_ops);

-- ============================================================
-- 8. Материализованное представление для быстрого отображения
-- ============================================================

-- Создаем материализованное представление для активных слотов (обновляется периодически)
CREATE MATERIALIZED VIEW mv_active_slots AS
SELECT
    s.id,
    s.starts_at,
    s.ends_at,
    s.track_configuration,
    s.track_description,
    s.price_with_rental,
    s.price_own_gear,
    s.total_seats,
    s.available_seats,
    m.full_name AS marshal_name,
    EXTRACT(EPOCH FROM (s.ends_at - s.starts_at)) / 60 AS duration_minutes
FROM slots s
JOIN marshals m ON s.marshal_id = m.id
WHERE s.status = 'Available'
  AND s.available_seats > 0
  AND s.starts_at > CURRENT_TIMESTAMP
  AND s.starts_at < CURRENT_TIMESTAMP + INTERVAL '30 days';

COMMENT ON MATERIALIZED VIEW mv_active_slots IS 'Материализованное представление активных слотов для быстрого отображения';

-- Индекс для материализованного представления
CREATE UNIQUE INDEX idx_mv_active_slots_id ON mv_active_slots(id);
CREATE INDEX idx_mv_active_slots_starts_at ON mv_active_slots(starts_at);

-- ============================================================
-- 9. Функция для обновления материализованного представления
-- ============================================================

CREATE OR REPLACE FUNCTION refresh_active_slots()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_active_slots;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION refresh_active_slots IS 'Обновление материализованного представления активных слотов';