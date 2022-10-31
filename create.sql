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
    min_donation bigint,
    max_donation bigint,
    role VARCHAR(25)
);


insert into donations (role, min_donation, max_donation)
VALUES ('931846572851986492', 0, 19999999);

insert into donations (role, min_donation, max_donation)
VALUES ('931845256889126922', 20000000, 49999999);

insert into donations (role, min_donation, max_donation)
VALUES ('931269453063266375', 50000000, 99999999);

insert into donations (role, min_donation, max_donation)
VALUES ('931273576739405864', 100000000, 249999999);

insert into donations (role, min_donation, max_donation)
VALUES ('931276083326771220', 250000000, 499999999);

insert into donations (role, min_donation, max_donation)
VALUES ('931277539270344744', 500000000, 999999999);

insert into donations (role, min_donation, max_donation)
VALUES ('931281334570197022', 1000000000, 100000000000);

--$0 to $19999999 - @Styrofoam (931846572851986492)
--$20000000 to $49999999 - @Copper (931845256889126922)
--$50000000 to $99999999 - @Bronze (931269453063266375)
--$100000000 to $249999999 - @Silver (931273576739405864)
--$250000000 to $499999999 - @Gold (931276083326771220)
--$500000000 to $999999999 - @Diamond (931277539270344744)
--$1,000000000 to ∞ - @Platinum (931281334570197022)



CREATE TABLE IF NOT EXISTS system_reminder (
    id serial PRIMARY KEY,
    reminder_time timestamp,
    type  VARCHAR(25)
);


ALTER TABLE system_reminder add message_id VARCHAR(255);

ALTER TABLE system_reminder add name VARCHAR(255);

CREATE TABLE IF NOT EXISTS user_upgrades (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    location  VARCHAR(25),
    upgrade  VARCHAR(150),
    progress int
);


ALTER TABLE reminder add locked BOOLEAN;
ALTER TABLE sm_alerts add channel  VARCHAR(25);


alter table profile add depth int ;
update profile set depth = 1;

CREATE TABLE IF NOT EXISTS user_stats (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    work  int,
    tips  int,
    overtime int
);


ALTER TABLE user_stats alter column work set default 0;
ALTER TABLE user_stats alter column tips set default 0;
ALTER TABLE user_stats alter column overtime set default 0;
ALTER TABLE profile alter column depth set default 1;

CREATE TABLE IF NOT EXISTS team_stats (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    team  VARCHAR(50),
    owner BOOLEAN,
    joined BOOLEAN
);


alter table profile add upgrade int ;
update profile set upgrade = 30;
ALTER TABLE profile alter column upgrade set default 30;


alter table profile add sleep_start time ;
alter table profile add sleep_end time ;


CREATE TABLE IF NOT EXISTS member_donations (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    donation  bigint
);