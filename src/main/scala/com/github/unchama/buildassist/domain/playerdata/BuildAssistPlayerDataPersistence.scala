package com.github.unchama.buildassist.domain.playerdata

import com.github.unchama.util.RefDict

import java.util.UUID

/**
 * プレーヤーデータの永続化を担うオブジェクトのtrait。
 */
trait BuildAssistPlayerDataPersistence[F[_]] extends RefDict[F, UUID, BuildAssistPlayerData]
