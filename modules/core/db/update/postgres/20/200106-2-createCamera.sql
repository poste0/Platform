alter table PLATFORM_CAMERA add constraint FK_PLATFORM_CAMERA_ON_USER foreign key (USER_ID) references SEC_USER(ID);
create index IDX_PLATFORM_CAMERA_ON_USER on PLATFORM_CAMERA (USER_ID);
