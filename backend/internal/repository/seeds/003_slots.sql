-- ============================================================
-- Seed: Слоты (20 слотов на разные даты и статусы)
-- ============================================================

-- ============================================================
-- 1. Активные слоты (сегодня + ближайшие дни)
-- ============================================================

INSERT INTO slots (
    id, marshal_id, starts_at, ends_at,
    track_configuration, track_description,
    price_with_rental, price_own_gear,
    total_seats, available_seats, status, cancellation_reason,
    created_at, updated_at
) VALUES

-- Сегодня: утро (активный слот с 5 местами)
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 NOW() + INTERVAL '2 hours', NOW() + INTERVAL '3 hours',
 'Short', 'Короткая конфигурация трассы (800 м) - идеально для новичков',
 1500.00, 1100.00,
 10, 5, 'Available', NULL,
 NOW(), NOW()),

-- Сегодня: день (активный слот с 0 местами - занят)
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 NOW() + INTERVAL '5 hours', NOW() + INTERVAL '6 hours',
 'Long', 'Длинная конфигурация трассы (1400 м) - для опытных пилотов',
 2500.00, 2000.00,
 8, 0, 'Available', NULL,
 NOW(), NOW()),

-- Сегодня: вечер (активный слот с 2 местами)
('cccccccc-cccc-cccc-cccc-cccccccccccc',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 NOW() + INTERVAL '8 hours', NOW() + INTERVAL '9 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 10, 2, 'Available', NULL,
 NOW(), NOW()),

-- Завтра: утро (активный слот с 8 местами)
('dddddddd-dddd-dddd-dddd-dddddddddddd',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 NOW() + INTERVAL '1 day' + INTERVAL '10 hours',
 NOW() + INTERVAL '1 day' + INTERVAL '11 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 10, 8, 'Available', NULL,
 NOW(), NOW()),

-- Завтра: день (активный слот с 1 местом)
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 NOW() + INTERVAL '1 day' + INTERVAL '14 hours',
 NOW() + INTERVAL '1 day' + INTERVAL '15 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 6, 1, 'Available', NULL,
 NOW(), NOW()),

-- Послезавтра: утро (активный слот с полными местами)
('ffffffff-ffff-ffff-ffff-ffffffffffff',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 NOW() + INTERVAL '2 days' + INTERVAL '9 hours',
 NOW() + INTERVAL '2 days' + INTERVAL '10 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 10, 10, 'Available', NULL,
 NOW(), NOW()),

-- Послезавтра: вечер (активный слот с 6 местами)
('11111111-1111-1111-1111-111111111111',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 NOW() + INTERVAL '2 days' + INTERVAL '18 hours',
 NOW() + INTERVAL '2 days' + INTERVAL '19 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 8, 6, 'Available', NULL,
 NOW(), NOW()),

-- Через 3 дня: утро (активный слот с 4 местами)
('22222222-2222-2222-2222-222222222222',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 NOW() + INTERVAL '3 days' + INTERVAL '11 hours',
 NOW() + INTERVAL '3 days' + INTERVAL '12 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 10, 4, 'Available', NULL,
 NOW(), NOW()),

-- Через 4 дня: утро (активный слот с 7 местами)
('33333333-3333-3333-3333-333333333333',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 NOW() + INTERVAL '4 days' + INTERVAL '10 hours',
 NOW() + INTERVAL '4 days' + INTERVAL '11 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 10, 7, 'Available', NULL,
 NOW(), NOW()),

-- Через 5 дней: день (активный слот с 3 местами)
('44444444-4444-4444-4444-444444444444',
 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 NOW() + INTERVAL '5 days' + INTERVAL '13 hours',
 NOW() + INTERVAL '5 days' + INTERVAL '14 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 8, 3, 'Available', NULL,
 NOW(), NOW()),

-- ============================================================
-- 2. Отмененные слоты (CancelledByCenter)
-- ============================================================

('55555555-5555-5555-5555-555555555555',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 NOW() + INTERVAL '1 day' + INTERVAL '16 hours',
 NOW() + INTERVAL '1 day' + INTERVAL '17 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 10, 10, 'CancelledByCenter', 'Технические проблемы на трассе',
 NOW(), NOW()),

('66666666-6666-6666-6666-666666666666',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 NOW() + INTERVAL '3 days' + INTERVAL '15 hours',
 NOW() + INTERVAL '3 days' + INTERVAL '16 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 8, 8, 'CancelledByCenter', 'Погодные условия (дождь)',
 NOW(), NOW()),

-- ============================================================
-- 3. Прошедшие слоты (для тестирования оценок и истории)
-- ============================================================

-- Завершенный заезд (вчера)
('77777777-7777-7777-7777-777777777777',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 NOW() - INTERVAL '1 day' - INTERVAL '2 hours',
 NOW() - INTERVAL '1 day' - INTERVAL '1 hour',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 10, 2, 'Available', NULL,
 NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'),

-- Завершенный заезд (2 дня назад)
('88888888-8888-8888-8888-888888888888',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 NOW() - INTERVAL '2 days' - INTERVAL '3 hours',
 NOW() - INTERVAL '2 days' - INTERVAL '2 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 8, 1, 'Available', NULL,
 NOW() - INTERVAL '6 days', NOW() - INTERVAL '2 days'),

-- Завершенный заезд (3 дня назад)
('99999999-9999-9999-9999-999999999999',
 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 NOW() - INTERVAL '3 days' - INTERVAL '4 hours',
 NOW() - INTERVAL '3 days' - INTERVAL '3 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 6, 0, 'Available', NULL,
 NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days'),

-- Завершенный заезд (4 дня назад)
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 NOW() - INTERVAL '4 days' - INTERVAL '5 hours',
 NOW() - INTERVAL '4 days' - INTERVAL '4 hours',
 'Long', 'Длинная конфигурация трассы (1400 м)',
 2500.00, 2000.00,
 10, 3, 'Available', NULL,
 NOW() - INTERVAL '8 days', NOW() - INTERVAL '4 days'),

-- Завершенный заезд (5 дней назад)
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 NOW() - INTERVAL '5 days' - INTERVAL '6 hours',
 NOW() - INTERVAL '5 days' - INTERVAL '5 hours',
 'Short', 'Короткая конфигурация трассы (800 м)',
 1500.00, 1100.00,
 8, 5, 'Available', NULL,
 NOW() - INTERVAL '9 days', NOW() - INTERVAL '5 days'),

-- ============================================================
-- 4. Слоты с разными ценами для тестирования
-- ============================================================

-- VIP-слот с высокой ценой
('cccccccc-cccc-cccc-cccc-cccccccccccc',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 NOW() + INTERVAL '6 days' + INTERVAL '12 hours',
 NOW() + INTERVAL '6 days' + INTERVAL '13 hours',
 'Long', 'Длинная конфигурация трассы (1400 м) - VIP заезд',
 3500.00, 2900.00,
 6, 6, 'Available', NULL,
 NOW(), NOW()),

-- Дешевый слот для новичков
('dddddddd-dddd-dddd-dddd-dddddddddddd',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 NOW() + INTERVAL '7 days' + INTERVAL '9 hours',
 NOW() + INTERVAL '7 days' + INTERVAL '10 hours',
 'Short', 'Короткая конфигурация трассы (800 м) - утренний тариф',
 1000.00, 700.00,
 10, 9, 'Available', NULL,
 NOW(), NOW());

COMMENT ON TABLE slots IS 'Seed: 20 слотов (активные, отмененные, прошедшие)';