create table person
(
    id       bigserial primary key,
    username varchar(255),
    password varchar(255)
);

create table role
(
    id        bigserial primary key,
    authority varchar(255)
);

create table room
(
    id   bigserial primary key,
    name varchar(255)
);

create table message
(
    id          bigserial primary key,
    description varchar(255),
    person_id   bigint not null references person (id),
    room_id     bigint not null references room (id)
);

create table room_message
(
    room_id    bigint references room (id),
    message_id bigint references message (id),
    primary key (room_id, message_id)
);

create table room_person
(
    room_id   bigint references room (id),
    person_id bigint references person (id),
    primary key (room_id, person_id)
);

create table person_message
(
    person_id  bigint references person (id),
    message_id bigint references message (id),
    primary key (person_id, message_id)
);

create table person_role
(
    person_id bigint references person (id),
    role_id   bigint references role (id),
    primary key (person_id, role_id)
);

