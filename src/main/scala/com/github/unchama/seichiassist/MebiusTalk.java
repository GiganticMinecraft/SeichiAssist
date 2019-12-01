package com.github.unchama.seichiassist;

public final class MebiusTalk {
    private final String mebiusMessage;
    private final String playerMessage;

    public MebiusTalk(String mebiusMessage, String playerMessage) {
        this.mebiusMessage = mebiusMessage;
        this.playerMessage = playerMessage;
    }

    private String getPlayerMessage() {
        return playerMessage;
    }

    private String getMebiusMessage() {
        return mebiusMessage;
    }

    /**
     * 従来のList&lt;String&gt;と互換性を保つためのメソッド。
     *
     * @param i インデックス
     * @return メッセージ
     */
    @Deprecated
    public String get(int i) {
        if (i == 0) {
            return getMebiusMessage();
        } else if (i == 1) {
            return getPlayerMessage();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MebiusTalk) {
            return ((MebiusTalk) obj).mebiusMessage.equals(this.mebiusMessage) && ((MebiusTalk) obj).playerMessage.equals(this.playerMessage);
        } else {
            return false;
        }
    }
}
