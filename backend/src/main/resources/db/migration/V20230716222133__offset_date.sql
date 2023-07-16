ALTER TABLE app_deal
  ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

ALTER TABLE app_deal
  ALTER column updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE app_deal
  ALTER column offer_expires_in TYPE timestamptz USING offer_expires_in AT TIME ZONE 'UTC';
