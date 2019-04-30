package com.github.unchama.seichiassist.database.migration.V1_1_0;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * サブホームの情報を含むData Transfer Objectのクラス
 */
/* package-private */ class SubHomeDTO {
    private final @NotNull String id;
    private final @NotNull String playerUuid;
    private final @NotNull String serverId;
    public final @Nullable String name;
    private final @NotNull String xCoordinate;
    private final @NotNull String yCoordinate;
    private final @NotNull String zCoordinate;
    private final @NotNull String worldName;

    SubHomeDTO(@NotNull String id,
               @NotNull String playerUuid,
               @NotNull String serverId,
               @Nullable String name,
               @NotNull String xCoordinate,
               @NotNull String yCoordinate,
               @NotNull String zCoordinate,
               @NotNull String worldName) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.serverId = serverId;
        this.name = name;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.zCoordinate = zCoordinate;
        this.worldName = worldName;
    }

    /* package-private */ String generateTemplateForInsertionCommand() {
        return "insert into sub_home " +
                "(player_uuid, server_id, id, name, location_x, location_y, location_z, world_name) " +
                "values ('" + playerUuid + "', " + serverId + ", " + id + ", " +
                "?, " + xCoordinate + ", " + yCoordinate + ", " + zCoordinate + ", '" + worldName + "')";
    }

}
