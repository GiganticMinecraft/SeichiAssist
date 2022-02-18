package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{validateItemDropRate, validateUrl}
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.DateTimeDuration

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object Valentine {
  val EVENT_YEAR: Int = 2022

  private val START_DATE = LocalDate.of(EVENT_YEAR, 2, 17)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 27)
  val EVENT_DURATION: DateTimeDuration = DateTimeDuration.fromLocalDate(START_DATE, END_DATE)

  /**
   * 2022バレンタインイベントにおいて
   *
   *  - 0時で区切ってイベント日時を設定していたため、0時を超えてログインし続けていたプレイヤー
   *    - 配布処理はログイン時に行われ、最終ログアウト日時を参照しているため、0時を超えた時点でそこで一度ログアウトしていなければ配布済みと判断されてしまう
   *  - lastquitがnullableなのにそれを考慮しなかったために配布されなかった初見プレイヤー
   *
   * に正常に配布されなかったので、そのプレイヤーたちのリスト
   */
  val cookieUnGivenPlayers: Set[UUID] = Set(
    "4328d5fb-e4ed-461f-8349-d7df34910547", "57370d2e-0c1a-4a70-9e23-8e539f5daee2", "36402334-e319-4dc5-806a-2d61fd376351",
    "59adc18e-e498-46fb-879f-058522cf1252", "54bcb3eb-a393-41c5-b87b-649c164f2bf7", "6632887b-fa8e-48c8-8510-1fc2b5e2a949",
    "1e335e7c-bc2d-4136-a817-8cf5b69ed70c", "dc638e00-943f-42b7-95b7-235c851b6e26", "3431c15c-268c-47d1-ba82-6a691fb76c97",
    "09f1067b-a393-43af-a9c8-289b42e77e34", "d19012e3-b6d4-4678-b339-be10a5f3e831", "f5167115-2136-46c2-8f6e-34b1b1cbc06b",
    "58fcfaf8-41b1-4a7d-b506-1e322abbe1a7", "122fd5a3-86ab-4e19-af87-80f85a2966b6", "4279e953-ffe3-4971-ac6c-3bd50265d78b",
    "91097db5-dc97-46ff-bc55-a92677bc8761", "252aaf75-fe66-42fe-8967-52d666ef2dbf", "41fb305b-b591-47a5-a467-1bb0ca333834",
    "85dd5867-db09-4a2f-bae7-8d38d5a9c547", "424d2b92-94a3-4b57-8168-db1b001b8465", "ea6157be-28e2-4765-87c8-22f5a43567c9",
    "3423c69b-a1cd-41cf-a896-992f9a6f5935", "6b5a0649-e89a-4087-aff2-f6e32a983582", "6d705d18-296a-47d9-b5cf-1b557c7a35e7",
    "3d3c592b-ef28-4052-92b5-b0dcfac2a20c", "d42436db-9c85-4ac1-bef0-ec00ad201bfd", "de34061f-0f25-47f2-ad57-43510ae4060e",
    "ce3b6eea-204f-4aaa-b8f7-cd5a72a38ad3", "3dc5d243-2c85-4abb-aada-237511b410b8", "d1d2b094-d0c2-41bd-be61-b6cf38600806",
    "57da9e7b-6390-45f5-92e3-2b93596facb2", "c36e3251-e498-4558-ad5c-6099c0cc9707", "7ce8c94e-0dcc-46c9-9913-142b84fb325e",
    "ab757137-3b3c-45ce-9dc2-49572833db89", "e55e5441-8d8b-4fb2-9573-d32c8a11cc46"
  ).map(UUID.fromString)
  // 2022バレンタインイベントにおいて、このUUIDのプレイヤーにエラーなくクッキーが配布されなかったらしい
  val cookieUnGivenPlayer: UUID = UUID.fromString("85dd5867-db09-4a2f-bae7-8d38d5a9c547")

  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/valentine$EVENT_YEAR")

  def isInEvent: Boolean = EVENT_DURATION.isInDuration(LocalDateTime.now())
}
