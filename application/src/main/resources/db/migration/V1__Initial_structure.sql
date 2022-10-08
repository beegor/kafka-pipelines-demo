create sequence hibernate_sequence as integer;
alter sequence hibernate_sequence owner to admin;

create table books
(
    id     bigint       not null
        primary key,
    author varchar(255),
    isbn   varchar(255) not null
        constraint books_isbn_unique
            unique,
    title  varchar(255)
);

alter table books
    owner to admin;

