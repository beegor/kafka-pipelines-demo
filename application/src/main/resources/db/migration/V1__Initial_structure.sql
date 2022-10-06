create table players
(
    username varchar(255) not null
        primary key,
    active   boolean      not null,
    email    varchar(255),
    password varchar(255)
);

alter table players
    owner to admin;
