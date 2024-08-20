CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL,
    role VARCHAR(20),
    token VARCHAR(512),
    token_expiration_date TIMESTAMP,
    refresh_token VARCHAR(512),
    refresh_token_expiration_date TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users (id)
);


INSERT INTO users(username, password, email, is_active)
VALUES (
    'adm',
    '$2a$10$fZBwaIdCcvn.gbbilJaKXukY6JIPsxLv/FbWngiPoxNCnUU.OGyrC',
    'admin@nomail',
    TRUE
);

INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'adm'), 'SUPER_ADMIN');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'adm'), 'ADMIN');
INSERT INTO user_roles(user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'adm'), 'USER');