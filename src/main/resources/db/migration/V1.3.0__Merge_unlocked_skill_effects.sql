use seichiassist;

insert
    into unlocked_active_skill_effect(player_uuid, effect_name)
    select player_uuid, effect_name from unlocked_active_skill_premium_effect;

drop table unlocked_active_skill_premium_effect;
