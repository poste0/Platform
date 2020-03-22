alter table PLATFORM_NODE add constraint FK_PLATFORM_NODE_ON_USER foreign key (USER_ID) references SEC_USER(ID);
create index IDX_PLATFORM_NODE_ON_USER on PLATFORM_NODE (USER_ID);
