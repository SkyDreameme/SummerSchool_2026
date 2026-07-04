-- ============================================================
-- Миграция 000003: Статусы слотов и причины отмены
-- ============================================================

-- 1. Триггер для автоматической проверки available_seats при обновлении
CREATE OR REPLACE FUNCTION validate_slot_availability()
RETURNS TRIGGER AS $$
BEGIN
    -- Если available_seats становится 0, не блокируем слот автоматически
    -- (оставляем возможность для ручного управления)

    -- Проверка: available_seats не может быть отрицательным
    IF NEW.available_seats < 0 THEN
        NEW.available_seats = 0; -- F-015: <0 трактуется как 0
    END IF;

    -- Проверка: если слот отменен центром, available_seats должен быть 0
    IF NEW.status = 'CancelledByCenter' AND NEW.available_seats > 0 THEN
        NEW.available_seats = 0;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_slot_availability
    BEFORE INSERT OR UPDATE ON slots
    FOR EACH ROW
    EXECUTE FUNCTION validate_slot_availability();

-- 2. Ограничение: Нельзя создать бронь на слот с available_seats = 0
CREATE OR REPLACE FUNCTION check_slot_availability_for_booking()
RETURNS TRIGGER AS $$
DECLARE
    slot_available INTEGER;
BEGIN
    SELECT available_seats INTO slot_available
    FROM slots WHERE id = NEW.slot_id;

    IF slot_available <= 0 THEN
        RAISE EXCEPTION 'No seats available for slot %', NEW.slot_id
        USING ERRCODE = 'integrity_constraint_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_slot_availability_on_booking
    BEFORE INSERT ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION check_slot_availability_for_booking();

-- 3. Создаем представление для слотов с актуальным статусом
CREATE VIEW active_slots AS
SELECT
    s.*,
    EXTRACT(EPOCH FROM (s.ends_at - s.starts_at)) / 60 AS duration_minutes,
    m.full_name AS marshal_name
FROM slots s
JOIN marshals m ON s.marshal_id = m.id
WHERE s.status = 'Available'
  AND s.available_seats > 0
  AND s.starts_at > CURRENT_TIMESTAMP;

COMMENT ON VIEW active_slots IS 'Активные слоты для отображения в расписании (SCR-002)';