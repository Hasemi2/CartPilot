merge into products (
    id, name, description, price_amount, currency, published, created_at, updated_at
) key (id) values
    ('10000000-0000-0000-0000-000000000001', '저소음 무선 키보드', '조용한 타건감과 멀티 페어링을 지원하는 무선 키보드', 79000.00, 'KRW', true, timestamp with time zone '2026-11-17 00:00:00+09:00', timestamp with time zone '2026-11-17 00:00:00+09:00'),
    ('10000000-0000-0000-0000-000000000002', '휴대용 무선 마우스', '블루투스 연결과 저소음 클릭을 지원하는 휴대용 마우스', 39000.00, 'KRW', true, timestamp with time zone '2026-11-17 00:00:00+09:00', timestamp with time zone '2026-11-17 00:00:00+09:00'),
    ('10000000-0000-0000-0000-000000000003', '개발용 노트북', '메모리 32GB와 고해상도 화면을 갖춘 개발용 노트북', 1290000.00, 'KRW', true, timestamp with time zone '2026-11-17 00:00:00+09:00', timestamp with time zone '2026-11-17 00:00:00+09:00'),
    ('10000000-0000-0000-0000-000000000004', '출시 예정 키보드', '아직 고객에게 노출하면 안 되는 미게시 상품', 49000.00, 'KRW', false, timestamp with time zone '2026-11-17 00:00:00+09:00', timestamp with time zone '2026-11-17 00:00:00+09:00');
