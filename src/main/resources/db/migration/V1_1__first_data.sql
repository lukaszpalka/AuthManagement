INSERT INTO users(username, password, email, is_active)
VALUES (
    'super_admin',
    '$2a$10$xfP66jjK3Tq2yogEeRFkYuxP95cTT/RLkyAZhAnt256Dc699H7uve',
    'super_admin@nomail',
    TRUE
);

INSERT INTO users(username, password, email, is_active)
VALUES (
    'admin',
    '$2a$10$5OeKZXUz3zgPmKv/8XEglewxI1Og/fJK2QYz.NWgVQB5s5ZJoRiNO',
    'admin@nomail',
    TRUE
);

INSERT INTO users(username, password, email, is_active)
VALUES (
    'user',
    '$2a$10$N2INBO2DSUktjUPACPWZzeVheJR4WgxIfHCv37mDsZI5EiUf7o8rO',
    'user@nomail',
    TRUE
);

INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'super_admin'), 'SUPER_ADMIN');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'super_admin'), 'ADMIN');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'super_admin'), 'USER');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'admin'), 'ADMIN');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'admin'), 'USER');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'user'), 'USER');