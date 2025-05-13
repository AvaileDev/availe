-- ---------- ENUM TYPES -----------------------------------------------------
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

-- ---------- MAIN REQUEST TABLE --------------------------------------------
CREATE TABLE nlip_request
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    conversation_id UUID           NOT NULL,
    ordinal         INTEGER        NOT NULL,
    messagetype     message_type,
    format          allowed_format NOT NULL,
    subformat       TEXT           NOT NULL,
    content         JSONB          NOT NULL
);

ALTER TABLE nlip_request
    ADD CONSTRAINT uniq_conv_ordinal UNIQUE (conversation_id, ordinal);

-- ---------- SUB-MESSAGE TABLE ---------------------------------------------
CREATE TABLE nlip_submessage
(
    id         SERIAL PRIMARY KEY,
    request_id UUID           NOT NULL REFERENCES nlip_request (id) ON DELETE CASCADE,
    format     allowed_format NOT NULL,
    subformat  TEXT           NOT NULL,
    content    JSONB          NOT NULL,
    label      TEXT
);

-- ---------- INDICES --------------------------------------------------------
CREATE INDEX idx_nlip_request_format ON nlip_request (format);
CREATE INDEX idx_nlip_req_conv_ord ON nlip_request (conversation_id, ordinal);
CREATE INDEX idx_submessage_request_id ON nlip_submessage (request_id);
