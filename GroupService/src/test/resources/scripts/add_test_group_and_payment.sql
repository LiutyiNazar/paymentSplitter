INSERT INTO user_group(id, group_name, currency, members)
VALUES (1, 'testGroup', 'UAH', '1;2');

INSERT INTO payment(id, payment_description, price, co_payers, creator_id, group_id, timestamp)
VALUES (1, 'testing payment description', 250.0, '1;2;3', 1, 1, '2010-01-01T00:00:00Z');