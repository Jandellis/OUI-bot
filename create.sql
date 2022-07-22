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

alter table profile add enabled BOOLEAN ;
alter table profile add react VARCHAR(25) ;

ALTER TABLE profile ALTER COLUMN react TYPE VARCHAR(255);

ALTER TABLE profile add  message VARCHAR(255);

CREATE TABLE IF NOT EXISTS member_data (
    id serial PRIMARY KEY,
    export_time timestamp,
    name VARCHAR(25),
    shack_name VARCHAR(30),
    income int,
    shifts int,
    weekly_shifts int,
    tips int,
    donations bigint,
    happy real
);

CREATE TABLE IF NOT EXISTS donations (
    id serial PRIMARY KEY,
    min_donation int,
    max_donation int,
    role VARCHAR(25)
);
