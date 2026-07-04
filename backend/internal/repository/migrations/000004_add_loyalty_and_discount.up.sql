-- ============================================================
-- Миграция 000004: Система лояльности и скидок
-- ============================================================

-- 1. Функция для расчета финальной цены со скидкой
CREATE OR REPLACE FUNCTION calculate_final_price(
    slot_id UUID,
    equipment_type VARCHAR,
    client_loyalty VARCHAR
)
RETURNS DECIMAL(10, 2) AS $$
DECLARE
    base_price DECIMAL(10, 2);
    discount_rate INTEGER;
    final_price DECIMAL(10, 2);
BEGIN
    -- Получаем базовую цену в зависимости от типа экипировки
    SELECT
        CASE
            WHEN equipment_type = 'Rental' THEN price_with_rental
            WHEN equipment_type = 'Own' THEN price_own_gear
        END
    INTO base_price
    FROM slots
    WHERE id = slot_id;

    -- Определяем скидку по статусу лояльности
    discount_rate := CASE client_loyalty
        WHEN 'Silver' THEN 20
        WHEN 'Bronze' THEN 10
        ELSE 0
    END;

    -- Рассчитываем финальную цену
    final_price := base_price * (1 - discount_rate / 100.0);

    -- Округляем до 2 знаков
    RETURN ROUND(final_price, 2);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calculate_final_price IS 'Расчет финальной цены со скидкой лояльности (F-019)';

-- 2. Триггер для автоматического расчета final_price при создании брони
CREATE OR REPLACE FUNCTION set_booking_final_price()
RETURNS TRIGGER AS $$
DECLARE
    client_loyalty VARCHAR;
BEGIN
    -- Получаем статус лояльности клиента
    SELECT loyalty_status INTO client_loyalty
    FROM clients WHERE id = NEW.client_id;

    -- Рассчитываем финальную цену
    NEW.final_price := calculate_final_price(
        NEW.slot_id,
        NEW.equipment,
        client_loyalty
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calculate_booking_final_price
    BEFORE INSERT ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION set_booking_final_price();

-- 3. Функция для обновления скидки при изменении статуса лояльности
CREATE OR REPLACE FUNCTION update_client_discount()
RETURNS TRIGGER AS $$
BEGIN
    -- Обновляем discount_percent в соответствии со статусом
    NEW.discount_percent := CASE NEW.loyalty_status
        WHEN 'Silver' THEN 20
        WHEN 'Bronze' THEN 10
        ELSE 0
    END;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER sync_loyalty_discount
    BEFORE INSERT OR UPDATE OF loyalty_status ON clients
    FOR EACH ROW
    EXECUTE FUNCTION update_client_discount();