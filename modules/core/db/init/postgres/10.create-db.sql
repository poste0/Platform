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
    URL_ADDRESS varchar(255) not null,
    PORT integer,
    NAME varchar(255) not null,
    USER_ID uuid not null,
    height integer not null,
    weight integer not null,
    frameRate integer not null,
    path varchar(255),
    --
    primary key (ID)
)^
-- end PLATFORM_CAMERA
-- begin PLATFORM_VIDEO
create table PLATFORM_VIDEO (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    name varchar(255) not null,
    fileDescriptorId uuid,
    cameraId uuid,
    parentName varchar(255),
    status varchar(255) not null,
    parentVideo uuid,
    --
    primary key (ID)
)^
-- end PLATFORM_VIDEO
-- begin PLATFORM_NODE
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
)^
-- end PLATFORM_NODE
