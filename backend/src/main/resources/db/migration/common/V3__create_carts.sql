create table carts (
    id uuid primary key,
    user_id uuid not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_carts_user unique (user_id)
);

create table cart_items (
    id uuid primary key,
    cart_id uuid not null,
    product_id uuid not null,
    quantity integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_cart_items_cart
        foreign key (cart_id) references carts (id) on delete cascade,
    constraint fk_cart_items_product
        foreign key (product_id) references products (id),
    constraint uk_cart_items_cart_product unique (cart_id, product_id),
    constraint chk_cart_items_quantity check (quantity between 1 and 100)
);

create index idx_cart_items_cart_id on cart_items (cart_id);
