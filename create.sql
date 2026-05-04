CREATE TABLE IF NOT EXISTS sm_alerts (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    alert_type VARCHAR(25),
    sm_trigger VARCHAR(25),
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

alter table profile add enabled boolean ;
alter table profile add react VARCHAR(255) ;

#ALTER TABLE profile ALTER COLUMN react TYPE VARCHAR(255);

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

#$0 to $19999999 - @Styrofoam (931846572851986492)
#--$20000000 to $49999999 - @Copper (931845256889126922)
#--$50000000 to $99999999 - @Bronze (931269453063266375)
#--$100000000 to $249999999 - @Silver (931273576739405864)
#--$250000000 to $499999999 - @Gold (931276083326771220)
#--$500000000 to $999999999 - @Diamond (931277539270344744)
#--$1,000000000 to ∞ - @Platinum (931281334570197022)



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


alter table profile add dm_reminder BOOLEAN;
update profile set dm_reminder = false;
alter table profile alter column dm_reminder set default false;

alter table profile add username VARCHAR(255);

CREATE TABLE IF NOT EXISTS warning_data (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    immunity_until timestamp,
    last_warning timestamp
);

CREATE TABLE IF NOT EXISTS sm_history (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    price int,
    update_time timestamp
);

alter table sm_history add last_change int;
alter table sm_history add change_1_to_2 int;
alter table sm_history add change_1_to_3 int;
alter table sm_history add change_1_to_4 int;
alter table sm_history add change_1_to_5 int;

alter table sm_history add change_0_to_1 int;
alter table sm_history add change_0_to_2 int;
alter table sm_history add change_0_to_3 int;
alter table sm_history add change_0_to_4 int;

CREATE TABLE IF NOT EXISTS profile_stats (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    income bigint,
    balance bigint,
    location VARCHAR(25),
    import_time timestamp
);

ALTER TABLE member_data add  overtime int;
ALTER TABLE member_data add  votes int;


ALTER TABLE warning_data add giveaway_until timestamp;

CREATE TABLE IF NOT EXISTS giveaway_winner (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    wins int,
    last_win timestamp
);

CREATE TABLE IF NOT EXISTS franchise (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    members int
);

CREATE TABLE IF NOT EXISTS reminder_settings (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    tip BOOLEAN,
    work BOOLEAN,
    overtime BOOLEAN,
    vote BOOLEAN,
    daily BOOLEAN,
    clean BOOLEAN,
    boost BOOLEAN
);

CREATE TABLE IF NOT EXISTS giveaway_log (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    win_time timestamp
);


#ALTER TABLE member_data DROP COLUMN franchise ;
#ALTER TABLE Donations DROP COLUMN franchise ;

ALTER TABLE member_data add franchise VARCHAR(25);
ALTER TABLE donations add franchise VARCHAR(25);

CREATE TABLE IF NOT EXISTS franchise_config (
    id serial PRIMARY KEY,
    guild VARCHAR(25),
    name VARCHAR(25),
    warning VARCHAR(25),
    warning_2 VARCHAR(25),
    warning_3 VARCHAR(25),
    flex VARCHAR(25),
    recruiter VARCHAR(25),
    immunity VARCHAR(25),
    giveawayRole VARCHAR(25),
    court VARCHAR(25)
);


insert into franchise_config (guild,
    name,
    warning,
    warning_2 ,
    warning_3 ,
    flex ,
    recruiter ,
    immunity ,
    giveawayRole ,
    court )
VALUES ('840395541791768599',
'OUI',
'937728752887144549',
'937729131557322822',
'937730070431277066',
'932741586079588482',
'841425869628899369',
'1067324308323581952',
'875882014606262282',
'841173549871661056');




ALTER TABLE franchise add balance bigint;
ALTER TABLE franchise add sold bigint;
ALTER TABLE franchise add income int;



alter table profile add ignored_hidden BOOLEAN;
update profile set ignored_hidden = false;
alter table profile alter column ignored_hidden set default false;

alter table profile add dnd BOOLEAN;
update profile set dnd = false;
alter table profile alter column dnd set default false;

CREATE TABLE IF NOT EXISTS franchise_stats (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    income bigint,
    balance bigint,
    sold bigint,
    import_time timestamp
);

CREATE TABLE IF NOT EXISTS team_event (
    id serial PRIMARY KEY,
    reminder VARCHAR(25),
    id1 VARCHAR(25),
    id2 VARCHAR(25),
    id3 VARCHAR(25),
    id4 VARCHAR(25)
);


alter table reminder_settings add grind BOOLEAN;
alter table reminder_settings alter column grind set default false;
alter table franchise_config add rush_hour VARCHAR(25);

alter table profile add rush_hour_end timestamp;

alter table franchise add donations bigint;
alter table franchise add vote_estimate double;
alter table franchise add ot_estimate double;
alter table franchise add votes int;
alter table franchise add rush_hour int;
alter table franchise add rush_hour_active timestamp;


CREATE TABLE IF NOT EXISTS donation_log (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    amount int,
    donation_time timestamp
);

CREATE TABLE IF NOT EXISTS shopping_items_history (
    id serial PRIMARY KEY,
    item_name VARCHAR(255),
    store_item_name VARCHAR(255),
    store VARCHAR(30),
    price double,
    on_sale boolean,
    discount double,
    import_time timestamp
);
CREATE TABLE IF NOT EXISTS shopping_items (
    id serial PRIMARY KEY,
    name VARCHAR(255),
    store_name VARCHAR(255),
    store VARCHAR(30)
);
CREATE TABLE IF NOT EXISTS shopping_list (
    id serial PRIMARY KEY,
    user VARCHAR(255),
    item_name  VARCHAR(255),
    sale boolean
);


CREATE TABLE IF NOT EXISTS questions (
    id serial PRIMARY KEY,
    crossword_day date,
    questions VARCHAR(255),
    answer VARCHAR(255),
    direction VARCHAR(255),
    number VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS crossword (
    id serial PRIMARY KEY,
    crossword_day date,
    guess VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS guesses (
    id serial PRIMARY KEY,
    name VARCHAR(25),
    start_time timestamp,
    guess VARCHAR(255),
    crossword_day date,
    message_id VARCHAR(255)
);

ALTER TABLE donation_log MODIFY amount BIGINT;

alter table profile add rush_hour_ignore timestamp;

alter table reminder_settings add work_modifier double;
alter table reminder_settings alter column work_modifier set default 1;
alter table reminder_settings add tips_modifier double;
alter table reminder_settings alter column tips_modifier set default 1;
alter table reminder_settings add overtime_modifier double;
alter table reminder_settings alter column overtime_modifier set default 1;

alter table profile add work_income int;
alter table profile add overtime_income int;
alter table profile add tips_income int;


CREATE TABLE IF NOT EXISTS contract_msg (
    id serial PRIMARY KEY,
    taco_id VARCHAR(25),
    cylon_id VARCHAR(25)
    );

CREATE TABLE IF NOT EXISTS contract (
    id serial PRIMARY KEY,
    name VARCHAR(150),
    rewards VARCHAR(200),
    total int,
    rep int,
    objective VARCHAR(100),
    objective_type VARCHAR(100),
    work_cooldown double,
    work_buff double,
    tips_cooldown double,
    tips_buff double,
    ot_cooldown double,
    ot_buff double
    );


alter table contract add detailed boolean;