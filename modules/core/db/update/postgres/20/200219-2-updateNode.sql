-- alter table PLATFORM_NODE add column USER_ID uuid ^
-- update PLATFORM_NODE set USER_ID = <default_value> ;
-- alter table PLATFORM_NODE alter column USER_ID set not null ;
alter table PLATFORM_NODE add column USER_ID uuid not null ;
