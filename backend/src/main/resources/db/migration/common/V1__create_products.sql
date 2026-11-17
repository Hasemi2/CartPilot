create table products (
    id uuid primary key,
    name varchar(200) not null,
    description varchar(2000) not null,
    price_amount numeric(19, 2) not null,
    currency varchar(3) not null,
    published boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint chk_products_price_non_negative check (price_amount >= 0),
    constraint chk_products_currency_length check (char_length(currency) = 3)
);

create index idx_products_published_price
    on products (published, price_amount);
