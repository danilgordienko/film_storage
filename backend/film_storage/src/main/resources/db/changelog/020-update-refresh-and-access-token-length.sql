--liquibase formatted sql

--changeset danilgordienko:020-update-refresh-and-access-token-length
ALTER TABLE refresh_tokens
ALTER COLUMN token TYPE TEXT;

ALTER TABLE access_tokens
ALTER COLUMN token TYPE TEXT;