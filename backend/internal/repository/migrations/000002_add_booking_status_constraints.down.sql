-- Откат миграции 000002

DROP TABLE IF EXISTS pending_bookings_ttl CASCADE;
DROP TRIGGER IF EXISTS schedule_booking_expiry ON bookings;
DROP TRIGGER IF EXISTS enforce_booking_status_transition ON bookings;
DROP FUNCTION IF EXISTS set_booking_expiry CASCADE;
DROP FUNCTION IF EXISTS check_booking_status_transition CASCADE;