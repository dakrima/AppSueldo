create table import_batches (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    bank_account_id bigint not null references bank_accounts(id) on delete restrict,
    original_filename varchar(255) not null,
    import_source varchar(40) not null default 'CSV',
    status varchar(20) not null default 'PROCESSING',
    total_rows integer not null default 0,
    created_count integer not null default 0,
    skipped_count integer not null default 0,
    invalid_count integer not null default 0,
    failure_reason text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

alter table transactions
    add column import_batch_id bigint references import_batches(id) on delete set null;

create index idx_import_batches_user_id_created_at on import_batches(user_id, created_at desc);
create index idx_import_batches_bank_account_id on import_batches(bank_account_id);
create index idx_transactions_import_batch_id on transactions(import_batch_id);

create unique index ux_transactions_csv_import_external_id
    on transactions(bank_account_id, source, external_id)
    where source = 'CSV_IMPORT' and external_id is not null;
