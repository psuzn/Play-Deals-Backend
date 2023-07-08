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

CREATE TABLE potential_deal
(
  id                 text      not null primary key unique default gen_random_uuid(),
  scrap_attempts      int       not null                    default 0,
  last_scrap_attempt_at timestamp null                        default null,
  source             text      not null,

  created_at          timestamp not null                    default now(),
  updated_at          timestamp not null                    default current_timestamp
)
