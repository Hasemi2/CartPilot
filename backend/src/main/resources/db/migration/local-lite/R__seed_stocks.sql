merge into inventory_stocks (
    product_id, on_hand_quantity, updated_at
) key (product_id) values
    ('10000000-0000-0000-0000-000000000001', 10, timestamp with time zone '2026-11-17 00:00:00+09:00'),
    ('10000000-0000-0000-0000-000000000002', 25, timestamp with time zone '2026-11-17 00:00:00+09:00'),
    ('10000000-0000-0000-0000-000000000003', 0, timestamp with time zone '2026-11-17 00:00:00+09:00'),
    ('10000000-0000-0000-0000-000000000004', 5, timestamp with time zone '2026-11-17 00:00:00+09:00');
