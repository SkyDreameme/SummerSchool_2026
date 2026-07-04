-- ============================================================
-- Миграция 000005: Флаг оценки брони
-- ============================================================

-- 1. Функция для проверки возможности оценки
CREATE OR REPLACE FUNCTION can_rate_booking(booking_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    booking_status VARCHAR;
    slot_ends_at TIMESTAMP WITH TIME ZONE;
    is_rated BOOLEAN;
BEGIN
    -- Проверяем статус брони
    SELECT status, rated INTO booking_status, is_rated
    FROM bookings WHERE id = booking_id;

    -- Нельзя оценить, если уже оценено
    IF is_rated THEN
        RETURN FALSE;
    END IF;

    -- Нельзя оценить, если бронь не состоялась
    IF booking_status IN ('CancelledByClient', 'CancelledByCenter', 'Expired', 'Pending') THEN
        RETURN FALSE;
    END IF;

    -- Проверяем, что заезд завершился
    SELECT ends_at INTO slot_ends_at
    FROM slots s
    JOIN bookings b ON s.id = b.slot_id
    WHERE b.id = booking_id;

    -- F-059: оценка доступна только после завершения заезда
    IF slot_ends_at > CURRENT_TIMESTAMP THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION can_rate_booking IS 'Проверка возможности оценки маршала (F-059)';

-- 2. Триггер для автоматической установки rated при создании оценки
CREATE OR REPLACE FUNCTION mark_booking_as_rated()
RETURNS TRIGGER AS $$
BEGIN
    -- Проверяем, можно ли оценить
    IF NOT can_rate_booking(NEW.booking_id) THEN
        RAISE EXCEPTION 'Rating not allowed for booking %', NEW.booking_id
        USING ERRCODE = 'integrity_constraint_violation';
    END IF;

    -- Обновляем флаг rated в брони
    UPDATE bookings SET rated = TRUE WHERE id = NEW.booking_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER mark_booking_rated_on_rating
    BEFORE INSERT ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION mark_booking_as_rated();