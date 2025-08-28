CREATE TABLE logs
(
    id        SERIAL PRIMARY KEY,
    timestamp TIMESTAMP    NOT NULL,
    level     VARCHAR(10)  NOT NULL,
    logger    VARCHAR(255) NOT NULL,
    message   TEXT         NOT NULL,
    exception TEXT,
    thread    VARCHAR(255) NOT NULL,
    context   VARCHAR(255)
);