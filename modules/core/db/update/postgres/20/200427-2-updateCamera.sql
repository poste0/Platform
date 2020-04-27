alter table PLATFORM_CAMERA add column URL_ADDRESS varchar(255) ^
update PLATFORM_CAMERA set URL_ADDRESS = '' where URL_ADDRESS is null ;
alter table PLATFORM_CAMERA alter column URL_ADDRESS set not null ;
alter table PLATFORM_CAMERA add column PORT integer ;
