create table users (
    id bigserial primary key,
    google_id varchar(255) not null unique,
    email varchar(255) not null unique,
    name varchar(255) not null,
    picture_url text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table categories (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    name varchar(120) not null,
    type varchar(20) not null,
    color varchar(32),
    icon varchar(80),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (user_id, name)
);

create table transactions (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    category_id bigint references categories(id) on delete set null,
    amount numeric(14, 2) not null,
    description varchar(255) not null,
    transaction_date date not null,
    type varchar(20) not null,
    source varchar(20) not null,
    notes text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table refresh_tokens (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    token varchar(255) not null unique,
    expires_at timestamptz not null,
    revoked boolean not null default false,
    created_at timestamptz not null
);

create index idx_categories_user_id on categories(user_id);
create index idx_transactions_user_id_date on transactions(user_id, transaction_date);
create index idx_refresh_tokens_user_id on refresh_tokens(user_id);
