-- Откат миграции 000005

DROP TRIGGER IF EXISTS mark_booking_rated_on_rating ON ratings;
DROP FUNCTION IF EXISTS mark_booking_as_rated CASCADE;
DROP FUNCTION IF EXISTS can_rate_booking CASCADE;