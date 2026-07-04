-- Откат миграции 000006

DROP TRIGGER IF EXISTS validate_booking_cancellation ON bookings;
DROP FUNCTION IF EXISTS auto_complete_booking CASCADE;
DROP FUNCTION IF EXISTS validate_booking_cancellation CASCADE;
DROP FUNCTION IF EXISTS can_cancel_booking CASCADE;