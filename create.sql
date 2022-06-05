CREATE TABLE IF NOT EXISTS sm_alerts (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    alert_type VARCHAR(25),
    trigger VARCHAR(25),
    price int
);


CREATE TABLE IF NOT EXISTS sm (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    price int,
    age int
);


CREATE TABLE IF NOT EXISTS sm_triggers (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    alert_type VARCHAR(25),
    price int
);

CREATE TABLE IF NOT EXISTS sm_watch (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    sauce VARCHAR(25)
);
