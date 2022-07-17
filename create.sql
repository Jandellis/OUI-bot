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

CREATE TABLE IF NOT EXISTS profile (
    id serial PRIMARY KEY,
    shack_name VARCHAR(30),
    name VARCHAR(25),
    status VARCHAR(25)
);

CREATE TABLE IF NOT EXISTS reminder (
    id serial PRIMARY KEY,
    reminder_time timestamp,
    name VARCHAR(25),
    type  VARCHAR(25),
    channel  VARCHAR(25)
);
