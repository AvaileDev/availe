CREATE TYPE allowed_format AS ENUM (
  'text',
  'token',
  'structured',
  'binary',
  'location',
  'generic'
);

CREATE TYPE message_type AS ENUM (
  'control'
);

CREATE TABLE nlip_request
(
    uuid        UUID PRIMARY KEY,
    messagetype MESSAGE_TYPE,
    format      allowed_format NOT NULL,
    subformat   TEXT           NOT NULL,
    content     JSONB          NOT NULL
);

CREATE TABLE nlip_submessage
(
    id           SERIAL PRIMARY KEY,
    request_uuid UUID REFERENCES nlip_request (uuid) ON DELETE CASCADE,
    format       allowed_format NOT NULL,
    subformat    TEXT           NOT NULL,
    content      JSONB          NOT NULL,
    label        TEXT
);

CREATE INDEX idx_nlip_request_format ON nlip_request (format);
CREATE INDEX idx_nlip_submessage_request ON nlip_submessage (request_uuid);
