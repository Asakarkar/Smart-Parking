INSERT INTO parking_slots (slot_number)
SELECT 'A-' || lpad(n::text, 2, '0') FROM generate_series(1, 10) AS n;
