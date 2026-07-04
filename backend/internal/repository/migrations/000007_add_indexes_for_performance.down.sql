-- Откат миграции 000007

DROP MATERIALIZED VIEW IF EXISTS mv_active_slots CASCADE;
DROP FUNCTION IF EXISTS refresh_active_slots CASCADE;

DROP INDEX IF EXISTS idx_pending_ttl_expires_at;
DROP INDEX IF EXISTS idx_jwt_tokens_expires_at;
DROP INDEX IF EXISTS idx_jwt_tokens_token_hash;
DROP INDEX IF EXISTS idx_sms_attempts_phone;
DROP INDEX IF EXISTS idx_ratings_booking_id;
DROP INDEX IF EXISTS idx_ratings_marshal_id;
DROP INDEX IF EXISTS idx_bookings_active_client;
DROP INDEX IF EXISTS idx_bookings_created_at;
DROP INDEX IF EXISTS idx_bookings_status;
DROP INDEX IF EXISTS idx_bookings_slot_id;
DROP INDEX IF EXISTS idx_bookings_client_status;
DROP INDEX IF EXISTS idx_bookings_client_id;
DROP INDEX IF EXISTS idx_slots_marshal_id;
DROP INDEX IF EXISTS idx_slots_status_available;
DROP INDEX IF EXISTS idx_slots_starts_ends_at;
DROP INDEX IF EXISTS idx_slots_ends_at;
DROP INDEX IF EXISTS idx_slots_starts_at;