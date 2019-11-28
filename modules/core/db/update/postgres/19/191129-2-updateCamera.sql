alter table PLATFORM_CAMERA add column FRAMERATE integer ^
update PLATFORM_CAMERA set FRAMERATE = 0 where FRAMERATE is null ;
alter table PLATFORM_CAMERA alter column FRAMERATE set not null ;
alter table PLATFORM_CAMERA add column HEIGHT integer ^
update PLATFORM_CAMERA set HEIGHT = 0 where HEIGHT is null ;
alter table PLATFORM_CAMERA alter column HEIGHT set not null ;
alter table PLATFORM_CAMERA add column WEIGHT integer ^
update PLATFORM_CAMERA set WEIGHT = 0 where WEIGHT is null ;
alter table PLATFORM_CAMERA alter column WEIGHT set not null ;
