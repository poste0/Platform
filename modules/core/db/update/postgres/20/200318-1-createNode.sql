create table PLATFORM_NODE (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    ADDRESS varchar(255) not null,
    GPU varchar(255) not null,
    CPU varchar(255) not null,
    USER_ID uuid not null,
    --
    primary key (ID)
);