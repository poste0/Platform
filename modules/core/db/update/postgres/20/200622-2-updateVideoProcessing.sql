alter table PLATFORM_VIDEO_PROCESSING rename column date_ to date___u91713 ;
alter table PLATFORM_VIDEO_PROCESSING add column DATE timestamp ^
update PLATFORM_VIDEO_PROCESSING set DATE = current_date where DATE is null ;
alter table PLATFORM_VIDEO_PROCESSING alter column DATE set not null ;
