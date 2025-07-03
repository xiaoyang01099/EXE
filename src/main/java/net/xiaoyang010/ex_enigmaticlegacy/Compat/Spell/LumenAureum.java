package net.xiaoyang010.ex_enigmaticlegacy.Compat.Spell;

import io.redspace.ironsspellbooks.item.spell_books.SimpleAttributeSpellBook;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.spells.SpellRarity;


public class LumenAureum extends SimpleAttributeSpellBook{
    public LumenAureum() {
        super(15, SpellRarity.LEGENDARY, AttributeRegistry.SPELL_POWER, 3);
    }
}
