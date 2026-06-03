alter table users
    alter column google_id drop not null;

alter table users
    add column password_hash text,
    add column email_verified boolean not null default false;

alter table refresh_tokens
    rename column token to token_hash;
