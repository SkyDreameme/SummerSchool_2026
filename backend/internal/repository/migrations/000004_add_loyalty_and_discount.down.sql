-- Откат миграции 000004

DROP TRIGGER IF EXISTS sync_loyalty_discount ON clients;
DROP TRIGGER IF EXISTS calculate_booking_final_price ON bookings;
DROP FUNCTION IF EXISTS update_client_discount CASCADE;
DROP FUNCTION IF EXISTS set_booking_final_price CASCADE;
DROP FUNCTION IF EXISTS calculate_final_price CASCADE;