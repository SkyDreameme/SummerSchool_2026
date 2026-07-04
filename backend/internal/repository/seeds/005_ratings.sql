-- ============================================================
-- Seed: Оценки маршалов (7 оценок)
-- ============================================================

INSERT INTO ratings (
    id, booking_id, marshal_id, client_id, stars, created_at, updated_at
) VALUES

-- Оценка 5 звёзд (Клиент 7 → Маршал 3)
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 '99999999-9999-9999-9999-999999999999',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 '77777777-7777-7777-7777-777777777777',
 5, NOW() - INTERVAL '1 day' + INTERVAL '1 hour', NOW() - INTERVAL '1 day' + INTERVAL '1 hour'),

-- Оценка 4 звёзды (Клиент 1 → Маршал 1)
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 '11111111-1111-1111-1111-111111111111',
 4, NOW() - INTERVAL '3 days' + INTERVAL '1 hour', NOW() - INTERVAL '3 days' + INTERVAL '1 hour'),

-- Оценка 5 звёзд (Клиент 5 → Маршал 2)
('cccccccc-cccc-cccc-cccc-cccccccccccc',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 '55555555-5555-5555-5555-555555555555',
 5, NOW() - INTERVAL '5 days' + INTERVAL '1 hour', NOW() - INTERVAL '5 days' + INTERVAL '1 hour'),

-- Оценка 3 звёзды (Клиент 2 → Маршал 2)
('dddddddd-dddd-dddd-dddd-dddddddddddd',
 '22222222-2222-2222-2222-222222222222',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 '22222222-2222-2222-2222-222222222222',
 3, NOW() - INTERVAL '2 days' + INTERVAL '2 hours', NOW() - INTERVAL '2 days' + INTERVAL '2 hours'),

-- Оценка 5 звёзд (Клиент 4 → Маршал 4)
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 '44444444-4444-4444-4444-444444444444',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 '44444444-4444-4444-4444-444444444444',
 5, NOW() - INTERVAL '4 days' + INTERVAL '1 hour', NOW() - INTERVAL '4 days' + INTERVAL '1 hour'),

-- Оценка 2 звёзды (Клиент 3 → Маршал 5)
('ffffffff-ffff-ffff-ffff-ffffffffffff',
 '33333333-3333-3333-3333-333333333333',
 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 '33333333-3333-3333-3333-333333333333',
 2, NOW() - INTERVAL '2 days' + INTERVAL '3 hours', NOW() - INTERVAL '2 days' + INTERVAL '3 hours'),

-- Оценка 4 звёзды (Клиент 6 → Маршал 1)
('11111111-1111-1111-1111-111111111111',
 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 '66666666-6666-6666-6666-666666666666',
 4, NOW() - INTERVAL '1 day' + INTERVAL '2 hours', NOW() - INTERVAL '1 day' + INTERVAL '2 hours');

COMMENT ON TABLE ratings IS 'Seed: 7 оценок маршалов (2-5 звёзд)';