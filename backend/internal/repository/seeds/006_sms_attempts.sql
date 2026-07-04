-- ============================================================
-- Seed: Попытки SMS-авторизации
-- ============================================================

INSERT INTO sms_attempts (
    id, phone, attempt_count, last_attempt_at, blocked_until, created_at, updated_at
) VALUES

-- Клиент 1: успешные попытки
('11111111-1111-1111-1111-111111111111',
 '+79001234567', 1, NOW() - INTERVAL '1 hour', NULL,
 NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),

-- Клиент 2: 2 неудачные попытки
('22222222-2222-2222-2222-222222222222',
 '+79007654321', 2, NOW() - INTERVAL '30 minutes', NULL,
 NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),

-- Клиент 3: заблокирован после 3 неудачных попыток (F-005)
('33333333-3333-3333-3333-333333333333',
 '+79009876543', 3, NOW() - INTERVAL '10 minutes', NOW() + INTERVAL '50 minutes',
 NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes');

COMMENT ON TABLE sms_attempts IS 'Seed: Попытки SMS-авторизации (включая заблокированного)';