-- ============================================================
-- Seed: Бронирования (15 броней разных статусов)
-- ============================================================

-- ============================================================
-- 1. Активные брони (Pending и Paid)
-- ============================================================

INSERT INTO bookings (
    id, client_id, slot_id, equipment, final_price,
    status, cancellation_reason, rated, deposit_link,
    created_at, updated_at
) VALUES

-- Клиент 1 (Silver): Активная бронь Pending (сегодня, утро)
('11111111-1111-1111-1111-111111111111',
 '11111111-1111-1111-1111-111111111111',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 'Rental', 1200.00, -- 1500 - 20% скидка
 'Pending', NULL, FALSE,
 'https://pay.sbp.ru/link/booking_11111111',
 NOW() - INTERVAL '10 minutes', NOW()),

-- Клиент 2 (Bronze): Активная бронь Paid (сегодня, день)
('22222222-2222-2222-2222-222222222222',
 '22222222-2222-2222-2222-222222222222',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'Own', 1800.00, -- 2000 - 10% скидка
 'Paid', NULL, FALSE,
 NULL, -- Paid-бронь не имеет deposit_link
 NOW() - INTERVAL '2 hours', NOW()),

-- Клиент 3 (None): Активная бронь Pending (сегодня, вечер)
('33333333-3333-3333-3333-333333333333',
 '33333333-3333-3333-3333-333333333333',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 'Rental', 1500.00, -- Без скидки
 'Pending', NULL, FALSE,
 'https://pay.sbp.ru/link/booking_33333333',
 NOW() - INTERVAL '5 minutes', NOW()),

-- Клиент 4 (Bronze): Активная бронь Paid (завтра)
('44444444-4444-4444-4444-444444444444',
 '44444444-4444-4444-4444-444444444444',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'Rental', 2250.00, -- 2500 - 10% скидка
 'Paid', NULL, FALSE,
 NULL,
 NOW() - INTERVAL '1 day', NOW()),

-- Клиент 5 (None): Активная бронь Pending (завтра)
('55555555-5555-5555-5555-555555555555',
 '55555555-5555-5555-5555-555555555555',
 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 'Own', 1100.00, -- Без скидки
 'Pending', NULL, FALSE,
 'https://pay.sbp.ru/link/booking_55555555',
 NOW() - INTERVAL '30 minutes', NOW()),

-- ============================================================
-- 2. Отмененные брони (CancelledByClient и CancelledByCenter)
-- ============================================================

-- Клиент 9: Отменена клиентом (была на завтра)
('66666666-6666-6666-6666-666666666666',
 '99999999-9999-9999-9999-999999999999',
 '55555555-5555-5555-5555-555555555555',
 'Rental', 1500.00,
 'CancelledByClient', 'Передумал кататься', FALSE,
 NULL,
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),

-- Клиент 2: Отменена центром (погода)
('77777777-7777-7777-7777-777777777777',
 '22222222-2222-2222-2222-222222222222',
 '66666666-6666-6666-6666-666666666666',
 'Own', 2000.00,
 'CancelledByCenter', 'Отмена по погодным условиям', FALSE,
 NULL,
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),

-- ============================================================
-- 3. Истекшие брони (Expired)
-- ============================================================

-- Клиент 3: Истекла по TTL (не оплатил в течение 30 минут)
('88888888-8888-8888-8888-888888888888',
 '33333333-3333-3333-3333-333333333333',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 'Rental', 1500.00,
 'Expired', NULL, FALSE,
 NULL,
 NOW() - INTERVAL '2 days' - INTERVAL '1 hour', NOW() - INTERVAL '2 days'),

-- ============================================================
-- 4. Завершенные брони (Completed) - для оценок
-- ============================================================

-- Клиент 7: Завершенный заезд (вчера) - уже оценен
('99999999-9999-9999-9999-999999999999',
 '77777777-7777-7777-7777-777777777777',
 '77777777-7777-7777-7777-777777777777',
 'Rental', 1350.00, -- 1500 - 10% скидка
 'Completed', NULL, TRUE,
 NULL,
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),

-- Клиент 8: Завершенный заезд (2 дня назад) - еще не оценен
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 '88888888-8888-8888-8888-888888888888',
 '88888888-8888-8888-8888-888888888888',
 'Own', 2000.00,
 'Completed', NULL, FALSE,
 NULL,
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),

-- Клиент 1: Завершенный заезд (3 дня назад) - уже оценен
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 '11111111-1111-1111-1111-111111111111',
 '99999999-9999-9999-9999-999999999999',
 'Rental', 1200.00,
 'Completed', NULL, TRUE,
 NULL,
 NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),

-- Клиент 4: Завершенный заезд (4 дня назад) - еще не оценен
('cccccccc-cccc-cccc-cccc-cccccccccccc',
 '44444444-4444-4444-4444-444444444444',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 'Own', 2250.00,
 'Completed', NULL, FALSE,
 NULL,
 NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),

-- Клиент 5: Завершенный заезд (5 дней назад) - уже оценен
('dddddddd-dddd-dddd-dddd-dddddddddddd',
 '55555555-5555-5555-5555-555555555555',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'Rental', 1500.00,
 'Completed', NULL, TRUE,
 NULL,
 NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),

-- ============================================================
-- 5. Специальные случаи (для тестирования пограничных условий)
-- ============================================================

-- Клиент 6: Бронь на слот с 0 местами (должна была создать ошибку, но в seed просто показываем)
-- Примечание: в реальном приложении такая бронь не создастся, но для демонстрации добавляем
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 '66666666-6666-6666-6666-666666666666',
 'ffffffff-ffff-ffff-ffff-ffffffffffff',
 'Rental', 2000.00,
 'Pending', NULL, FALSE,
 'https://pay.sbp.ru/link/booking_eeeeeeee',
 NOW() - INTERVAL '15 minutes', NOW());

COMMENT ON TABLE bookings IS 'Seed: 15 броней разных статусов';