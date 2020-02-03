create table PLATFORM_CAMERA (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ADDRESS varchar(255) not null,
    NAME varchar(255) not null,
    USER_ID uuid not null,
    height integer not null,
    weight integer not null,
    frameRate integer not null,
    --
    primary key (ID)
);