create type allowed_format as enum (
    'text',
    'token',
    'structured',
    'binary',
    'location',
    'generic'
    );

create table nlip_request
(
    uuid       uuid primary key,
    control    boolean        not null,
    format     allowed_format not null,
    subformat  text           not null,
    content    jsonb          not null,
    created_at timestamptz    not null default now()
);

create table nlip_submessage
(
    id           serial primary key,
    request_uuid uuid           not null references nlip_request (uuid) on delete cascade,
    format       allowed_format not null,
    subformat    text           not null,
    content      jsonb          not null
);

create index idx_nlip_request_format on nlip_request (format);
create index idx_nlip_submessage_request on nlip_submessage (request_uuid);