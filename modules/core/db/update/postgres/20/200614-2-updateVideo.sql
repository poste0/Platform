alter table PLATFORM_VIDEO rename column nodeprocessing to nodeprocessing__u84645 ;
alter table PLATFORM_VIDEO drop constraint FK_PLATFORM_VIDEO_ON_NODEPROCESSING ;
drop index IDX_PLATFORM_VIDEO_ON_NODEPROCESSING ;
