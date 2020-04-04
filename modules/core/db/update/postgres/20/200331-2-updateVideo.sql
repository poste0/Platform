alter table PLATFORM_VIDEO add column PARENTVIDEO uuid ;
alter table PLATFORM_VIDEO add column STATUS varchar(50) ^
update PLATFORM_VIDEO set STATUS = 'ready' where STATUS is null ;
alter table PLATFORM_VIDEO alter column STATUS set not null ;
