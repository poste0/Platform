create table PLATFORM_IMAGE_PROCESSING (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    nodeId uuid,
    DATE_ timestamp,
    --
    primary key (ID)
);