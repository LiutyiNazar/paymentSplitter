INSERT INTO user_group(id, group_name, currency, members)
VALUES (1, 'testGroup', 'UAH', '1;2');

INSERT INTO payment(id, payment_description, price, co_payers, creator_id, group_id, timestamp)
VALUES (1, 'description 1', 357.0, '1;2;3', 1, 1, '2012-01-01T00:00:00Z'),
       (2, 'second description', 400.0, '1;2;3', 1, 1, '2019-01-01T00:00:00Z');