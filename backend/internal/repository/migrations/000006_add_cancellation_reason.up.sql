-- ============================================================
-- Миграция 000006: Причины отмены броней
-- ============================================================

-- 1. Функция проверки окна отмены (F-040: ровно за 2 часа и более)
CREATE OR REPLACE FUNCTION can_cancel_booking(booking_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    slot_start TIMESTAMP WITH TIME ZONE;
    booking_status VARCHAR;
BEGIN
    -- Получаем время начала заезда
    SELECT s.starts_at, b.status INTO slot_start, booking_status
    FROM bookings b
    JOIN slots s ON b.slot_id = s.id
    WHERE b.id = booking_id;

    -- Проверяем статус
    IF booking_status NOT IN ('Pending', 'Paid') THEN
        RETURN FALSE;
    END IF;

    -- F-040: Отмена доступна не позднее чем за 2 часа до старта (включительно)
    -- now + 2h <= startsAt
    RETURN (CURRENT_TIMESTAMP + INTERVAL '2 hours') <= slot_start;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION can_cancel_booking IS 'Проверка возможности отмены брони (F-040)';

-- 2. Триггер для валидации отмены
CREATE OR REPLACE FUNCTION validate_booking_cancellation()
RETURNS TRIGGER AS $$
BEGIN
    -- Если пытаемся отменить бронь
    IF NEW.status IN ('CancelledByClient', 'CancelledByCenter') AND OLD.status != NEW.status THEN
        -- Проверка для клиентской отмены
        IF NEW.status = 'CancelledByClient' AND NOT can_cancel_booking(NEW.id) THEN
            RAISE EXCEPTION 'Cancel window closed (must be at least 2 hours before start)'
            USING ERRCODE = 'integrity_constraint_violation';
        END IF;

        -- Если причина отмены не указана
        IF NEW.cancellation_reason IS NULL OR NEW.cancellation_reason = '' THEN
            RAISE EXCEPTION 'Cancellation reason is required'
            USING ERRCODE = 'integrity_constraint_violation';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_booking_cancellation
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    WHEN (NEW.status IN ('CancelledByClient', 'CancelledByCenter'))
    EXECUTE FUNCTION validate_booking_cancellation();

-- 3. Функция для автоматического перевода в Completed (NFR-12)
CREATE OR REPLACE FUNCTION auto_complete_booking()
RETURNS TRIGGER AS $$
BEGIN
    -- Если заезд завершился, переводим Paid в Completed
    IF NEW.status = 'Paid' AND OLD.status = 'Paid' THEN
        IF EXISTS (
            SELECT 1 FROM slots
            WHERE id = NEW.slot_id AND ends_at <= CURRENT_TIMESTAMP
        ) THEN
            NEW.status = 'Completed';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION auto_complete_booking IS 'Автоматический переход в Completed по факту времени (NFR-12)';