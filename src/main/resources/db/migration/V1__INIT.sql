CREATE TABLE user_tokens
(
    id            SERIAL PRIMARY KEY,
    creation_time timestamp(6) NOT NULL,
    token         varchar(255) NOT NULL UNIQUE,
    user_id       varchar(255) NOT NULL
);