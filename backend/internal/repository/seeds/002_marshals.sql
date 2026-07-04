-- ============================================================
-- Seed: Маршалы (5 маршалов с разными именами)
-- ============================================================

INSERT INTO marshals (id, full_name, created_at, updated_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Алексей Смирнов', NOW() - INTERVAL '100 days', NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Мария Иванова', NOW() - INTERVAL '90 days', NOW()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Дмитрий Петров', NOW() - INTERVAL '80 days', NOW()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Елена Сидорова', NOW() - INTERVAL '70 days', NOW()),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Сергей Козлов', NOW() - INTERVAL '60 days', NOW());

COMMENT ON TABLE marshals IS 'Seed: 5 маршалов';