# 過去ガチャ景品 /give コマンドの 1.12.2 から 1.18.2 への変換設計

## 前提と完了条件

過去ガチャの景品を配布する /give コマンドは 1.12.2 の書式のままであり、1.18.2 のサーバーではそのまま実行できない。
本書は、各コマンドを 1.18.2 で当時と同じ見た目と効果になるよう書き換えるための変換規則と、開催前の検証手順を定める。
ガチャへの登録や開催の運用手順は扱わない。
全景品のコマンドが検証チェックリストを通れば、過去ガチャは開催できる。

## 作業の進め方

変換器を使って全件を機械変換する前に、まず代表の景品 1 つで規則を一巡させる。

1. 景品コマンドを 1 行 1 コマンドのテキストにまとめる。
2. 装飾の多い景品（名前の色、Lore、エンチャント、属性が揃ったもの）を 1 つ選び、変換器で変換する。
3. 変換結果をデバッグ環境で検証チェックリストにかける。
4. 代表の景品が通ったら、同じ変換器で残りの景品を変換する。

## 変換器の実行

変換器は、1.12.2 形式のコマンドを構文解析してから、後述する規則を適用する。
単純な文字列置換ではないため、NBT 内のカンマ、引用符、入れ子を保持できる。

入力ファイルには、1 行につき 1 個の `/give` コマンドを書く。
空行と `#` で始まるコメント行はそのまま出力される。

次のコマンドは変換結果を第 2 引数のファイルへ出す。
入力ファイルと既存の出力ファイルは上書きしない。

```bash
./sbt --client --error \
  "runMain com.github.unchama.seichiassist.tools.pastgacha.ConvertPastGachaGiveCommands \
  commands-1.12.2.txt commands-1.18.2.txt"
```

変換器は、意味を確定できない入力を推測で変換しない。
未対応の数値エンチャント ID、種類を判定できない非ゼロのデータ値、不完全な UUID、壊れた SNBT を検出すると、行番号と理由を標準エラー出力へ出して終了する。
この場合は出力を採用せず、該当するアイテムの 1.12.2 での意味を調べて変換規則を追加する。

自動テストは、次の変換を検証する。

- Name と Lore の JSON 化、および色コードによる書式リセット
- エンチャント ID の名前化（ID 10 と ID 70 の区別を含む）
- 属性名と 64bit UUID の変換
- プレイヤーヘッドのアイテム ID と `SkullOwner.Id` の変換
- 耐久値の `Damage` への移動
- 本書に記載した種類 ID のフラット化
- 未対応入力の拒否

自動テストは、1.18.2 クライアントでの見た目とプラグイン固有効果までは検証しない。
変換後の全景品には、末尾の検証チェックリストによるデバッグ環境での確認が必要である。

## 実装時の検証結果

2026 年 8 月 9 日に、代表の PLUTO 景品を含む変換テスト 11 件を実行し、すべて通過した。

同日、変換後のコマンドを Paper 1.18.2 build 388 のサーバーコンソールへ投入した。
対象は PLUTO、プレイヤーヘッド、耐久値付きツール、エンチャントされた金のリンゴ、レコードの 5 件である。
5 件とも構文エラーを出さず、対象プレイヤーを解決する段階の `No player was found` まで到達した。

このサーバー検証が保証する範囲は、1.18.2 のコマンドディスパッチャーがアイテム ID と NBT を受理することまでである。
名前と Lore の描画、属性の実効値、プラグインが付与するウィザー効果は、プレイヤーが接続したデバッグ環境で別途確認する。

## コマンド構文の変換

引数の並びが変わる。

```
1.12.2: /give <対象> <アイテムID> [個数] [データ値] [NBT]
1.18.2: /give <対象> <アイテムID>[NBT] [個数]
```

NBT はアイテム ID の直後に空白なしで続ける。
個数は末尾に移り、データ値の引数は廃止された。
データ値が担っていた情報は、次の規則で NBT かアイテム ID に移す。

## データ値の移し方

データ値は用途によって行き先が異なる。

- **0 の場合**：単に削除する。
- **耐久値の場合**（剣、ツール、防具、弓、釣竿など壊れるアイテムでデータ値が 1 以上）：NBT に `Damage:<データ値>` を追加する。
- **種類の区別の場合**：1.13 のフラット化でアイテム ID そのものが分かれたので、ID を置き換える。

代表的なフラット化の対応を示す。

| 1.12.2 | 1.18.2 |
|---|---|
| `minecraft:skull`（データ値 0〜5） | `skeleton_skull` / `wither_skeleton_skull` / `zombie_head` / `player_head` / `creeper_head` / `dragon_head` |
| `minecraft:golden_apple`（データ値 1） | `minecraft:enchanted_golden_apple` |
| `minecraft:record_13` など | `minecraft:music_disc_13` など |

表にないアイテムでデータ値が種類を区別している場合は、Minecraft Wiki のフラット化（The Flattening）対応表で個別に引く。

## Name と Lore の JSON 化

§ の書式コードは使えなくなり、JSON テキストで書く。
変換は次の手順で行う。

1. 行を色コード（§0〜§f）の位置で区切る。
2. 区切った各部分を `{"text":"...","color":"..."}` のコンポーネントにする。その時点で有効な §l などがあれば `"bold":true` のように付ける。
3. 色を指定したコンポーネントには、§o が付いていた場合を除き `"italic":false` を付ける。
4. コンポーネントが複数になる行は配列にし、先頭に `""` を置く。
5. 色コードを含まない行は `{"text":"そのまま"}` とし、空行は `{"text":""}` とする。
6. できあがった JSON をシングルクォートで囲んで NBT に入れる。

手順 3 が今回の変換で最も漏れやすい。
1.14 以降、Name と Lore は既定で斜体で描画され、`color` を指定しても斜体は解除されない。
1.12.2 では色コードが書式をリセットして非斜体になっていたので、当時の見た目を再現するには明示的な打ち消しが要る。

手順 4 で先頭に `""` を置くのは、JSON テキストの配列では 2 要素目以降が先頭要素の書式を継承するためである。
先頭を空文字にしておけば、継承で書式が混ざる事故が起きない。

色コードの対応を示す。

| コード | color | コード | color |
|---|---|---|---|
| §0 | `black` | §8 | `dark_gray` |
| §1 | `dark_blue` | §9 | `blue` |
| §2 | `dark_green` | §a | `green` |
| §3 | `dark_aqua` | §b | `aqua` |
| §4 | `dark_red` | §c | `red` |
| §5 | `dark_purple` | §d | `light_purple` |
| §6 | `gold` | §e | `yellow` |
| §7 | `gray` | §f | `white` |

| コード | JSON での指定 |
|---|---|
| §l | `"bold":true` |
| §m | `"strikethrough":true` |
| §n | `"underlined":true` |
| §o | `"italic":true` |
| §k | `"obfuscated":true` |

変換例を示す。

```
1.12.2: "§a最大体力§f(小)§a増加"
1.18.2: '["",{"text":"最大体力","color":"green","italic":false},
            {"text":"(小)","color":"white","italic":false},
            {"text":"増加","color":"green","italic":false}]'
```

## エンチャント ID の名前化

キー名が `ench` から `Enchantments` に変わり、数値 ID は名前になる。
レベルには short 型を示す `s` を付ける（慣例であり、なくても解釈される）。

```
1.12.2: ench:[{id:16,lvl:10}]
1.18.2: Enchantments:[{id:"minecraft:sharpness",lvl:10s}]
```

1.12.2 に存在する全 ID の対応を示す。名前には `minecraft:` を前置する。

| ID | 名前 | ID | 名前 |
|---|---|---|---|
| 0 | `protection` | 20 | `fire_aspect` |
| 1 | `fire_protection` | 21 | `looting` |
| 2 | `feather_falling` | 22 | `sweeping` |
| 3 | `blast_protection` | 32 | `efficiency` |
| 4 | `projectile_protection` | 33 | `silk_touch` |
| 5 | `respiration` | 34 | `unbreaking` |
| 6 | `aqua_affinity` | 35 | `fortune` |
| 7 | `thorns` | 48 | `power` |
| 8 | `depth_strider` | 49 | `punch` |
| 9 | `frost_walker` | 50 | `flame` |
| 10 | `binding_curse` | 51 | `infinity` |
| 16 | `sharpness` | 61 | `luck_of_the_sea` |
| 17 | `smite` | 62 | `lure` |
| 18 | `bane_of_arthropods` | 70 | `mending` |
| 19 | `knockback` | 71 | `vanishing_curse` |

ID 70 は mending（修繕）である。
10 の binding_curse（束縛の呪い）と取り違えると、修繕可のはずの景品が呪われたアイテムになる。
検討段階の変換例に実際にこの取り違えがあったので、手作業で変換済みの分があればこの 2 つは重点的に見直す。

エンチャント本の `StoredEnchantments` はキー名が変わらず、中身の変換規則は上と同じである。

## 属性名と UUID の書き換え

AttributeModifiers は属性名と UUID の 2 点が変わる。

属性名は camelCase から snake_case になる。
`AttributeName` と `Name` の両方を書き換える（`Name` は表示されない識別用ラベルだが、揃えておくと読みやすい）。

| 1.12.2 | 1.18.2 |
|---|---|
| `generic.maxHealth` | `generic.max_health` |
| `generic.followRange` | `generic.follow_range` |
| `generic.knockbackResistance` | `generic.knockback_resistance` |
| `generic.movementSpeed` | `generic.movement_speed` |
| `generic.attackDamage` | `generic.attack_damage` |
| `generic.attackSpeed` | `generic.attack_speed` |
| `generic.armor` | 変更なし |
| `generic.armorToughness` | `generic.armor_toughness` |
| `generic.luck` | 変更なし |

UUID は `UUIDMost` と `UUIDLeast` の 2 つの 64bit 整数から、32bit 整数 4 つの配列 `UUID:[I;A,B,C,D]` になる。

- **A**：UUIDMost の上位 32bit
- **B**：UUIDMost の下位 32bit
- **C**：UUIDLeast の上位 32bit
- **D**：UUIDLeast の下位 32bit

過去ガチャの景品は Most も Least も 32bit に収まる小さい値なので、機械的に `[I;0,<UUIDMost>,0,<UUIDLeast>]` と書けばよい。

```
1.12.2: UUIDLeast:149252,UUIDMost:96488
1.18.2: UUID:[I;0,96488,0,149252]
```

この UUID は同一アイテム内でモディファイアを区別するためだけの値なので、一意でありさえすれば旧値の引き継ぎで問題ない。
Slot 未指定のモディファイアが全スロットで効く挙動は両バージョンで同じであり、変換で気にする必要はない。

## プレイヤーヘッドの扱い

テクスチャ付きヘッドは 2 箇所の変換が要る。

第一に、アイテム ID が `minecraft:skull`（データ値 3）から `minecraft:player_head` になる（前述のフラット化）。

第二に、`SkullOwner.Id` の書式が変わる。
1.12.2 ではハイフン区切りの UUID 文字列だったが、1.16 以降は文字列を認識せず、int 配列で書かないとテクスチャが表示されない。
UUID 文字列のハイフンを除いて 16 進数 8 桁ずつの 4 つに分け、それぞれを符号付き 32bit 整数に読み替える（値が `0x80000000` 以上なら 4294967296 を引いて負数にする）。

```
1.12.2: Id:"069a79f4-44e9-4726-a5be-fca90e38aaf5"
        （16進で区切ると 069a79f4 / 44e94726 / a5befca9 / 0e38aaf5）
1.18.2: Id:[I;110787060,1156138790,-1514210135,238594805]
```

この読み替えは手計算しづらいので、ヘッドが多い場合はオンラインの UUID 変換ツールを使うとよい。
`SkullOwner.Properties.textures` の中身と、`SkullOwner` がプレイヤー名の文字列だけの形は、変換不要でそのまま動く。

## そのまま使えるタグ

次のタグは 1.18.2 でも書式が変わらない。

`HideFlags`、`RepairCost`、`Unbreakable`、`display.color`（革防具の染色）、`Potion`（ポーションの種類）、`CustomPotionEffects`、`Fireworks`。

数値の型サフィックス（`0.08d` の `d` など）は、付けても付けなくても同じに解釈される。
ただし `CanDestroy` と `CanPlaceOn` を使う景品があれば、中のブロック ID にもフラット化が必要である。

## 検証チェックリスト

変換した各コマンドを、開催前にデバッグ環境（または手元の 1.18.2 サーバー）で実際に実行して確認する。
見た目の比較には、当時のスクリーンショットか 1.12.2 環境での表示を突き合わせる。

- [ ] コマンドがエラーなく実行でき、アイテムを受け取れる
- [ ] アイテム名の色、太字、斜体が 1.12.2 当時の見た目と一致する
- [ ] Lore の各行の色が一致し、意図しない斜体になっていない（斜体になっていたら `italic:false` の漏れ）
- [ ] エンチャントの種類とレベルが一致する（特に修繕と呪い）
- [ ] ツールチップの属性表示（最大体力増加など）が一致する
- [ ] ヘッドはテクスチャが表示される
- [ ] 記名など当時の仕様に関わる特殊な Lore 行があれば個別に確認する
