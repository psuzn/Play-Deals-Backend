ALTER TABLE "app_deal"
  ADD COLUMN "offer_expires_in" timestamp not null default current_timestamp;

ALTER TABLE "app_deal"
  DROP COLUMN "expired";

