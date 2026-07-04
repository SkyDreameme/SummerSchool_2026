-- Откат миграции 000003

DROP VIEW IF EXISTS active_slots CASCADE;
DROP TRIGGER IF EXISTS enforce_slot_availability_on_booking ON bookings;
DROP TRIGGER IF EXISTS enforce_slot_availability ON slots;
DROP FUNCTION IF EXISTS check_slot_availability_for_booking CASCADE;
DROP FUNCTION IF EXISTS validate_slot_availability CASCADE;