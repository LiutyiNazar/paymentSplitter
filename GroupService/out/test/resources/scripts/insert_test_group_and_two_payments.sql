INSERT INTO user_group(id, group_name, currency, members)
VALUES (1, 'testGroup', 'UAH', '1;2');

INSERT INTO payment(id, payment_description, price, co_payers, creator_id, group_id, timestamp)
VALUES (1, 'test payment description 1', 100.0, '1;2;3', 1, 1, '2008-01-01T00:00:00Z'),
       (2, 'test payment description 2', 100.0, '1;2;3', 1, 1, '2008-01-01T00:00:00Z');