alter table PLATFORM_VIDEO add column ISFINISHED boolean ^
update PLATFORM_VIDEO set ISFINISHED = false where ISFINISHED is null ;
alter table PLATFORM_VIDEO alter column ISFINISHED set not null ;
alter table PLATFORM_VIDEO add column PARENTNAME varchar(255) ;
