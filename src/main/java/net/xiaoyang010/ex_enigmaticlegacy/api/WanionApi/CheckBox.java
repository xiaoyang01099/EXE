package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IField;

import javax.annotation.Nonnull;

public class CheckBox implements IField<CheckBox> {
    private final String fieldName;
    private final boolean defaultChecked;
    private boolean checked;

    public CheckBox(@Nonnull final String fieldName)
    {
        this(fieldName, false);
    }

    public CheckBox(@Nonnull final String fieldName, final boolean defaultChecked) {
        this.fieldName = fieldName;
        this.defaultChecked = (this.checked = defaultChecked);
    }

    public final CheckBox toggle() {
        checked ^= true;
        return this;
    }

    public boolean isChecked()
    {
        return checked;
    }

    @Nonnull
    @Override
    public CheckBox copy()
    {
        return new CheckBox(fieldName, checked);
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT() {
        final CompoundTag fieldNBT = new CompoundTag();
        fieldNBT.putString("fieldName", fieldName);
        fieldNBT.putBoolean("checked", checked);
        return fieldNBT;
    }

    @Override
    public void receiveNBT(@Nonnull final CompoundTag compoundTag)
    {
        readNBT(compoundTag);
    }

    @Override
    public String getHoveringText(@Nonnull final WInteraction wInteraction)
    {
        return I18n.get(fieldName);
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag smartNBT) {
        this.checked = smartNBT.contains("checked") ? smartNBT.getBoolean("checked") : defaultChecked;
    }

    @Nonnull
    @Override
    public String getFieldName()
    {
        return fieldName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CheckBox && fieldName.equals(((CheckBox) obj).fieldName) && checked == ((CheckBox) obj).checked;
    }
}