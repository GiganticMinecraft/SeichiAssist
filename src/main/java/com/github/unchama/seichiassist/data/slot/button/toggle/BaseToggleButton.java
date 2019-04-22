package com.github.unchama.seichiassist.data.slot.button.toggle;

/**
 * Toggle (クリックすると順に {@link Button} が変化する機能) 可能な {@link Button} です.
 *
 * @author karayuu
 */
/*
public class BaseToggleButton extends Button {
    private final CircularButtonList list = new CircularButtonList(Collections.emptyList());

    /**
     * Toggle可能な {@link Button} を生成します. <br>
     * {@link Inventory} において,
     * {@link SlotActionHandlers#READ_ONLY} が付与されるためReadOnlyなButtonとして働きます.
     *
     * @param position {@link Inventory} への設置位置
     * @param builder  {@link Inventory} へセットする {@link ItemStack} を構成する {@link ItemStackBuilder} <br>
     *                 Toggleの際に,これが最初の {@link Button} となります. ({@code null} は許容されません.)
     * @see Slot#getPosition()
     * @see BaseToggleButton
     */
    /*
    public BaseToggleButton(int position, @Nonnull ItemStackBuilder builder) {
        super(position, requireNonNull(builder));
    }
    */

    /**
     * {@link Button} を次に登録します.<br>
     * この関数が呼ばれた引数の {@link Button} の順に Toggle することに注意してください.
     *
     * @param button 次に登録する {@link Button}
     */
    /*
    public void setNext(@Nonnull Button button) {
        this.list.addLast(button);
    }
}
*/
