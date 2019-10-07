use seichiassist;

create table mine_stack_item(
    object_name varchar(128) not null,
    base_item_stack blob not null,

    required_mine_stack_level int not null,

    display_priority int not null,
    category_id int not null,

    constraint primary key (object_name),
    constraint unique (category_id, display_priority)
);
