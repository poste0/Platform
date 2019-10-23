-- begin PLATFORM_CAMERA
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
    USER_ID uuid not null,
    --
    primary key (ID)
)^
-- end PLATFORM_CAMERA
