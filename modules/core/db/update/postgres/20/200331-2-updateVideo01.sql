alter table PLATFORM_VIDEO add constraint FK_PLATFORM_VIDEO_ON_PARENTVIDEO foreign key (PARENTVIDEO) references PLATFORM_VIDEO(ID);
create index IDX_PLATFORM_VIDEO_ON_PARENTVIDEO on PLATFORM_VIDEO (PARENTVIDEO);
