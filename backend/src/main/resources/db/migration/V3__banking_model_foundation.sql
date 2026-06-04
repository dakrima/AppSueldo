create table bank_connections (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    provider varchar(20) not null default 'MANUAL',
    provider_connection_id varchar(255),
    institution_name varchar(120),
    status varchar(20) not null default 'ACTIVE',
    access_token_ref text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

comment on column bank_connections.access_token_ref is
    'Sensitive future field: store only an encrypted provider token or vault reference. Never store bank credentials.';

create table bank_accounts (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    bank_connection_id bigint not null references bank_connections(id) on delete cascade,
    name varchar(120) not null,
    account_type varchar(40),
    currency varchar(3) not null default 'CLP',
    external_id varchar(255),
    balance numeric(14, 2),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

alter table transactions
    add column bank_account_id bigint,
    add column currency varchar(3) not null default 'CLP',
    add column external_id varchar(255),
    add column transfer_group_id uuid;

alter table transactions
    alter column source set default 'MANUAL';

update transactions
set source = 'MANUAL'
where source not in ('MANUAL', 'FINTOC');

insert into bank_connections (
    user_id,
    provider,
    institution_name,
    status,
    created_at,
    updated_at
)
select
    users.id,
    'MANUAL',
    'Manual',
    'ACTIVE',
    now(),
    now()
from users
where not exists (
    select 1
    from bank_connections
    where bank_connections.user_id = users.id
      and bank_connections.provider = 'MANUAL'
);

insert into bank_accounts (
    user_id,
    bank_connection_id,
    name,
    account_type,
    currency,
    created_at,
    updated_at
)
select
    users.id,
    bank_connections.id,
    'Cuenta manual',
    'MANUAL',
    'CLP',
    now(),
    now()
from users
join bank_connections
  on bank_connections.user_id = users.id
 and bank_connections.provider = 'MANUAL'
where not exists (
    select 1
    from bank_accounts
    where bank_accounts.user_id = users.id
      and bank_accounts.bank_connection_id = bank_connections.id
      and bank_accounts.name = 'Cuenta manual'
);

update transactions
set bank_account_id = manual_accounts.id
from (
    select distinct on (bank_accounts.user_id)
        bank_accounts.user_id,
        bank_accounts.id
    from bank_accounts
    join bank_connections
      on bank_connections.id = bank_accounts.bank_connection_id
    where bank_connections.provider = 'MANUAL'
    order by bank_accounts.user_id, bank_accounts.id
) manual_accounts
where transactions.user_id = manual_accounts.user_id
  and transactions.bank_account_id is null;

alter table transactions
    add constraint fk_transactions_bank_account
    foreign key (bank_account_id) references bank_accounts(id) on delete restrict;

alter table transactions
    alter column bank_account_id set not null;

create table transaction_classifications (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    transaction_id bigint not null unique references transactions(id) on delete cascade,
    category_id bigint references categories(id) on delete set null,
    method varchar(30) not null default 'UNCLASSIFIED',
    confidence numeric(5, 4),
    reason text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_bank_connections_user_id on bank_connections(user_id);
create index idx_bank_connections_provider_external_id on bank_connections(provider, provider_connection_id);
create index idx_bank_accounts_user_id on bank_accounts(user_id);
create index idx_bank_accounts_bank_connection_id on bank_accounts(bank_connection_id);
create unique index ux_bank_accounts_connection_external_id
    on bank_accounts(bank_connection_id, external_id)
    where external_id is not null;

create index idx_transactions_bank_account_id on transactions(bank_account_id);
create index idx_transactions_transaction_date on transactions(transaction_date);
create index idx_transactions_category_id on transactions(category_id);
create unique index ux_transactions_fintoc_external_id
    on transactions(bank_account_id, source, external_id)
    where source = 'FINTOC' and external_id is not null;

create index idx_transaction_classifications_user_id on transaction_classifications(user_id);
create index idx_transaction_classifications_category_id on transaction_classifications(category_id);
