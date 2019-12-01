# minuteがnullのときは無期限
create table if not exists flying(uuid chars(36) primary key, minute int default null)