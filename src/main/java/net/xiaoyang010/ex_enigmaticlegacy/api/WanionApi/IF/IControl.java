package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

public interface IControl<C extends IControl<C>> extends ISmartNBT, ICopyable<C>
{
    default boolean canOperate()
    {
        return true;
    }

    default void operate() {}
}