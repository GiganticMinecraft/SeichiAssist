package com.github.unchama.seichiassist.skill;

import javax.annotation.Nonnull;

/**
 * スキル全般を表すinterfaceです.
 *
 * @author karayuu
 */
public interface Skill {
    /**
     * スキルの名前を取得します.
     *
     * @return スキルの名前
     */
    @Nonnull
    String getName();
}
