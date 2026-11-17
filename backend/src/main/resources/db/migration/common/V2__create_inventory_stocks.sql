create table inventory_stocks (
    product_id uuid primary key,
    on_hand_quantity integer not null,
    updated_at timestamp with time zone not null,
    constraint fk_inventory_stocks_product
        foreign key (product_id) references products (id),
    constraint chk_inventory_stocks_quantity_non_negative
        check (on_hand_quantity >= 0)
);
