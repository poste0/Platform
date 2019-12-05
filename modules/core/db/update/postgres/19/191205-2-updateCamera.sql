alter table PLATFORM_CAMERA add column NAME varchar(255) ^
update PLATFORM_CAMERA set NAME = '' where NAME is null ;
alter table PLATFORM_CAMERA alter column NAME set not null ;
