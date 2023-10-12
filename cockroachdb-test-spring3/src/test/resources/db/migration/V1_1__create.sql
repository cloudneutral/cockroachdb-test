create table payment
(
    id       int            not null default unique_rowid(),
    state    varchar(128)   not null,
    merchant varchar(256)   not null,
    amount   decimal(18, 2) not null,

    primary key (id)
);
