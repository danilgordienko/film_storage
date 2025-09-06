--liquibase formatted sql

--changeset danilgordienko:010-add-visibility-to-users-ratings
ALTER TABLE users
    ADD COLUMN rating_visibility VARCHAR(20) NOT NULL DEFAULT 'ALL';
