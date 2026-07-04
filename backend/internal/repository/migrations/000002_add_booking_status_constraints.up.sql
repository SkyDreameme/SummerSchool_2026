-- ============================================================
-- Миграция 000002: Добавление ограничений статусов брони
-- ============================================================

-- 1. Ограничение: Нельзя изменить бронь на Paid/Completed, если слот отменен
CREATE OR REPLACE FUNCTION check_booking_status_transition()
RETURNS TRIGGER AS $$
BEGIN
    -- Если пытаемся перевести в Paid или Completed, проверяем статус слота
    IF NEW.status IN ('Paid', 'Completed') AND OLD.status != NEW.status THEN
        IF EXISTS (
            SELECT 1 FROM slots WHERE id = NEW.slot_id AND status = 'CancelledByCenter'
        ) THEN
            RAISE EXCEPTION 'Cannot change booking to % for cancelled slot', NEW.status
            USING ERRCODE = 'integrity_constraint_violation';
        END IF;
    END IF;

    -- Если пытаемся отменить после завершения заезда
    IF NEW.status = 'CancelledByClient' AND OLD.status != NEW.status THEN
        IF EXISTS (
            SELECT 1 FROM slots WHERE id = NEW.slot_id AND ends_at <= CURRENT_TIMESTAMP
        ) THEN
            RAISE EXCEPTION 'Cannot cancel completed booking'
            USING ERRCODE = 'integrity_constraint_violation';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_booking_status_transition
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION check_booking_status_transition();

-- 2. Ограничение: Pending-бронь автоматически истекает через 30 минут (F-045)
-- Реализуется через триггер при создании
CREATE OR REPLACE FUNCTION set_booking_expiry()
RETURNS TRIGGER AS $$
BEGIN
    -- Автоматически переводим Pending в Expired через 30 минут
    -- (может быть выполнено через фоновый процесс, но для надежности добавим триггер)
    IF NEW.status = 'Pending' THEN
        -- Создаем запись в отдельной таблице для TTL-обработки (см. миграцию 000007)
        INSERT INTO pending_bookings_ttl (booking_id, expires_at)
        VALUES (NEW.id, CURRENT_TIMESTAMP + INTERVAL '30 minutes');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER schedule_booking_expiry
    AFTER INSERT ON bookings
    FOR EACH ROW
    WHEN (NEW.status = 'Pending')
    EXECUTE FUNCTION set_booking_expiry();

-- 3. Создаем таблицу для TTL-обработки
CREATE TABLE pending_bookings_ttl (
    booking_id UUID PRIMARY KEY REFERENCES bookings(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pending_bookings_ttl_expires_at_check CHECK (expires_at > created_at)
);

COMMENT ON TABLE pending_bookings_ttl IS 'TTL для Pending-броней (F-045)';