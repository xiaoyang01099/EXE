package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

public interface IUpdatable
{
    default void update()
    {
        update(System.nanoTime() / (double) 1000000000);
    }

    void update(final double seconds);
}