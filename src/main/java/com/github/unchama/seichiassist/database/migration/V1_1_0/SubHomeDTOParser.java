package com.github.unchama.seichiassist.database.migration.V1_1_0;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* package-private */ class SubHomeDTOParser {
    private final @NotNull String uuid;
    private final @NotNull String serverId;

    /* package-pribate */ SubHomeDTOParser(@NotNull String uuid, @NotNull String serverId) {
        this.uuid = uuid;
        this.serverId = serverId;
    }

    private Optional<SubHomeDTO> parseIndividualRawData(int index,
                                                        @NotNull List<@NotNull String> homePointData,
                                                        @NotNull String subHomeName) {
        final @NotNull String xCoordinate = homePointData.get(0);
        final @NotNull String yCoordinate = homePointData.get(1);
        final @NotNull String zCoordinate = homePointData.get(2);
        final @NotNull String worldName = homePointData.get(3);

        // セットされていないかどうかはx座標データについて空文字テストをすれば十分である
        if (xCoordinate.equals("")) return Optional.empty();

        final SubHomeDTO dto =
                new SubHomeDTO(
                        String.valueOf(index),
                        uuid, serverId,
                        subHomeName.equals("") ? null : subHomeName,
                        xCoordinate, yCoordinate, zCoordinate, worldName
                );

        return Optional.of(dto);
    }

    /**
     * @param <T> リストの型
     * @return {@code list}から{@code chunkSize}個ずつ要素を取り出して作ったリストのリスト
     *
     * 余った要素は捨てられるので、戻り値の要素はすべて同じ長さ({@code chunkSize})を持つことになる。
     */
    private static <T> ArrayList<ArrayList<T>> chunk(@NotNull ArrayList<T> inputList, int chunkSize) {
        final int inputListSize = inputList.size();
        final int outputListSize = inputListSize / chunkSize;

        return IntStream
                .range(0, outputListSize)
                .mapToObj(outputIndex ->
                        new ArrayList<>(inputList.subList(outputIndex * chunkSize, (outputIndex + 1) * chunkSize))
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Optional<SubHomeDTO>> parseRawData(@NotNull String homePointRawData,
                                                    @Nullable String parsedSubHomeNameData) {
        final ArrayList<String> homePointSplitData = new ArrayList<>(Arrays.asList(homePointRawData.split(",")));
        // NOTE: https://github.com/GiganticMinecraft/SeichiAssist/pull/110#discussion_r281012395 (変えると不整合が生じる)
        final ArrayList<ArrayList<String>> rawHomePoints = chunk(homePointSplitData, 4);
        final int subHomeCount = rawHomePoints.size();

        // NOTE: https://github.com/GiganticMinecraft/SeichiAssist/pull/110#discussion_r281027497
        final ArrayList<@NotNull String> rawSubHomesNames = parsedSubHomeNameData == null
                ? new ArrayList<>(Collections.nCopies(subHomeCount, ""))
                : new ArrayList<>(Arrays.asList(parsedSubHomeNameData.split(",")));

        return IntStream
                .range(0, subHomeCount)
                .mapToObj(index ->
                        parseIndividualRawData(index, rawHomePoints.get(index), rawSubHomesNames.get(index)))
                .collect(Collectors.toList());
    }

    private @Nullable String parseSubHomeNameData(@Nullable String subHomeNameRawData) {
        if (subHomeNameRawData == null) return null;
        try {
            return new String(Hex.decodeHex(subHomeNameRawData.toCharArray()), StandardCharsets.UTF_8);
        } catch (DecoderException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* package-private */ List<SubHomeDTO> parseRawDataAndFilterUndefineds(@NotNull String homePointRawData,
                                                                           @Nullable String subHomeNameRawData) {
        return parseRawData(homePointRawData, parseSubHomeNameData(subHomeNameRawData)).stream()
                .map(optionalData -> optionalData.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
