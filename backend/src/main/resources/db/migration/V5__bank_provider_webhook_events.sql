create table bank_provider_webhook_events (
    id bigserial primary key,
    provider varchar(20) not null,
    provider_event_id varchar(120) not null,
    event_type varchar(120) not null,
    status varchar(30) not null,
    received_at timestamptz not null default now(),
    processed_at timestamptz,
    error_code varchar(120),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index ux_bank_provider_webhook_events_provider_event
    on bank_provider_webhook_events(provider, provider_event_id);

create index idx_bank_provider_webhook_events_provider_status
    on bank_provider_webhook_events(provider, status);

create index idx_bank_provider_webhook_events_event_type
    on bank_provider_webhook_events(event_type);
