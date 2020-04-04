alter table PLATFORM_VIDEO rename column isfinished to isfinished__u54789 ;
alter table PLATFORM_VIDEO alter column isfinished__u54789 drop not null ;
