CREATE TABLE app_deal
(
  id           text      not null primary key unique,
  name         text      not null,
  icon         text      not null,
  images       text[]             default array []::text[],
  normal_price  float     not null,
  current_price float     not null,
  currency     text      not null,
  store_url     text      not null,
  expired      boolean   not null default false,
  category     text,
  downloads    text      not null default '0',
  rating       text      not null default 'unknown',

  created_at    timestamp not null default now(),
  updated_at    timestamp not null default current_timestamp
);

