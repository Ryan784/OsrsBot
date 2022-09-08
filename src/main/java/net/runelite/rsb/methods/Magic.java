package net.runelite.rsb.methods;

import net.runelite.rsb.internal.globval.VarpIndices;
import net.runelite.rsb.internal.globval.GlobalWidgetInfo;
import net.runelite.rsb.internal.globval.VarpValues;
import net.runelite.rsb.internal.globval.WidgetIndices;
import net.runelite.rsb.internal.globval.enums.InterfaceTab;
import net.runelite.rsb.wrappers.RSCharacter;
import net.runelite.rsb.wrappers.RSGroundItem;
import net.runelite.rsb.wrappers.RSObject;
import net.runelite.rsb.wrappers.RSWidget;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Magic tab and spell related operations.
 *
 * @author Gigiaj
 *
 */
public class Magic extends MethodProvider {

    Magic(final MethodContext ctx) {
        super(ctx);
    }

    /**
     * Checks whether or not a spell is selected.
     *
     * @return <code>true</code> if a spell is selected; otherwise <code>false</code>.
     */
    public boolean isSpellSelected() {
        RSWidget widget = methods.interfaces.getComponent(GlobalWidgetInfo.MAGIC_SPELL_LIST);
        for (RSWidget child : widget.getComponents()) {
            if (child.isVisible() || child.isSelfVisible()) {
                //Check api.widget to see what border is what or just validate that when one is selected
                // what border type it has at the time
                if (child.getBorderThickness() == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether a spell is currently set to autocast.
     *
     * @return <code>true</code> if autocasting; otherwise <code>false</code>.
     */
    public boolean isAutoCasting() {
        return methods.combat.getFightMode() == VarpValues.COMBAT_STYLE_AUTOCAST.getValue();
    }

    public int getSpell(String name) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getName().contains(name.toUpperCase())) {
                try {
                    return (int) field.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    /**
     * Clicks a specified spell, opens magic tab if not open and uses interface
     * of the spell to click it, so it works if the spells are layout in any
     * sway.
     *
     * @param spell The spell to cast.
     * @return <code>true</code> if the spell was clicked; otherwise <code>false</code>.
     */
    public boolean castSpell(final int spell) {
        if (methods.game.getCurrentTab() != InterfaceTab.MAGIC) {
            methods.game.openTab(InterfaceTab.MAGIC);
            for (int i = 0; i < 100; i++) {
                sleep(20);
                if (methods.game.getCurrentTab() == InterfaceTab.MAGIC) {
                    break;
                }
            }
            sleep(random(150, 250));
        }
        if (methods.game.getCurrentTab() == InterfaceTab.MAGIC) {
            RSWidget inter = getInterface();
            if (inter != null) {
                RSWidget comp = inter.getComponent(spell);
                return comp != null && comp.doAction("Cast");
            }
        }
        return false;
    }

    /**
     * Hovers a specified spell, opens magic tab if not open and uses interface
     * of the spell to hover it, so it works if the spells are layout in any
     * sway.
     *
     * @param spell The spell to hover.
     * @return <code>true</code> if the spell was clicked; otherwise <code>false</code>.
     */
    public boolean hoverSpell(final int spell) {
        if (methods.game.getCurrentTab() != InterfaceTab.MAGIC) {
            methods.game.openTab(InterfaceTab.MAGIC);
            for (int i = 0; i < 100; i++) {
                sleep(20);
                if (methods.game.getCurrentTab() == InterfaceTab.MAGIC) {
                    break;
                }
            }
            sleep(random(150, 250));
        }
        if (methods.game.getCurrentTab() == InterfaceTab.MAGIC) {
            RSWidget inter = getInterface();
            if (inter != null) {
                RSWidget comp = inter.getComponent(spell);
                return comp != null && comp.doHover();
            }
        }
        return false;
    }

    /**
     * Auto-casts a spell via the magic tab.
     *
     * @param spell The spell to auto-cast.
     * @return <code>true</code> if the "Auto-cast" interface option was clicked;
     * otherwise <code>false</code>.
     */
    public boolean autoCastSpell(final int spell) {
        if (methods.clientLocalStorage.getVarpValueAt(VarpIndices.COMBAT_STYLE)
                != VarpValues.COMBAT_STYLE_AUTOCAST.getValue()) {
            if (methods.game.getCurrentTab() != InterfaceTab.COMBAT) {
                methods.game.openTab(InterfaceTab.COMBAT);
                sleep(random(150, 250));
            }
            if (methods.interfaces.getComponent(GlobalWidgetInfo.COMBAT_AUTO_CAST_SPELL).doClick()) {
                sleep(random(500, 700));
                RSWidget widget = methods.interfaces.getComponent(GlobalWidgetInfo.MAGIC_SPELL_LIST);
                //The children are the spells
                for (RSWidget child : widget.getComponents()) {
                    //To speed up the search we'll filter the undesirables
                    if (child.isVisible() || child.isSelfVisible()) {
                        //This is the autocast book spell list
                        for (RSWidget autoCastSpell : methods.interfaces.getComponent(GlobalWidgetInfo.MAGIC_AUTOCAST_SPELL_LIST).getComponents()) {
                            //We need to compare sprites to determine if we've found the right value
                            //This alleviates the need to devise a convoluted method to find spells in this book
                            //All the spells start from at 4 so the index needs to be adjusted for that
                            if (child.getIndex() + 3 == spell) {
                                if (autoCastSpell.getSpriteId() == child.getSpriteId()) {
                                    return autoCastSpell.doClick();
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets the open magic book interface.
     *
     * @return The current magic RSWidget.
     */
    public RSWidget getInterface() {
        RSWidget widget = methods.interfaces.getComponent(GlobalWidgetInfo.MAGIC_SPELL_LIST);
        if (widget.isVisible()) {
            return widget;
        }
        return null;
    }

    /**
     * Gets the current spell book.
     *
     * @return The Book enum of your current spell book.
     */
    public MagicBook getCurrentSpellBook() {
        RSWidget widget;
        for (int x = 0; x < MagicBook.values().length; x++) {
            if (MagicBook.values()[x] != MagicBook.NULL) {
                widget = methods.interfaces.getComponent(WidgetIndices.SpellbookTab.GROUP_INDEX, MagicBook.values()[x].getIndex());
                if (widget.isValid() && widget.isSelfVisible()) {
                    return MagicBook.values()[x];
                }
            }
        }
        return MagicBook.NULL;
    }

    /**
     * Casts a spell on a Player/NPC/Object/Ground Item.
     *
     * @param entity A Character or Animable.
     * @param spell  The spell to cast.
     * @return <code>true</code> if casted; otherwise <code>false</code>.
     */
    public boolean castSpellOn(final Object entity, final int spell) {
        if (isSpellSelected() || entity == null) {
            return false;
        }
        if (castSpell(spell)) {
            if (entity instanceof RSCharacter) {
                return ((RSCharacter) entity).doAction("Cast");
            } else if (entity instanceof RSObject) {
                return ((RSObject) entity).doAction("Cast");
            } else if (entity instanceof RSGroundItem) {
                return ((RSGroundItem) entity).doAction("Cast");
            }
        }
        return false;
    }

    /**
     * Converts the spell list to variables to be used
     * *Variable updating usage*
     *
     * @return string containing the spells in variable form
     */
    public String convertSpellBookToVariables() {
        RSWidget widget = methods.interfaces.getComponent(GlobalWidgetInfo.MAGIC_SPELL_LIST);
        StringBuilder spells = new StringBuilder();
        for (RSWidget child : widget.getComponents()) {
            Pattern pattern = Pattern.compile(">(.*)<");
            Matcher matcher = pattern.matcher(child.getName());
            while (matcher.find()) {
                String spellToAdd = "public static final int SPELL_" + matcher.group(1).replaceAll(" ", "_")
                        .replaceAll("-", "_").replaceAll("'", "").toUpperCase() + " = ";
                for (int i = 0; i < MagicBook.values().length; i++) {
                    int reverseI = MagicBook.values().length - i - 2;
                    if (reverseI > 0) {
                        if (child.getIndex() + 3 == MagicBook.values()[reverseI].getIndex() - 1) {
                            spells.append("// ").append(MagicBook.values()[reverseI].name()).append("\n");
                        }
                        if (child.getIndex() + 3 >= MagicBook.values()[reverseI].getIndex() - 1) {
                            if (spells.toString().contains(spellToAdd)) {
                                spellToAdd = "public static final int SPELL_" + matcher.group(1).replaceAll(" ", "_")
                                        .replaceAll("-", "_").replaceAll("'", "").toUpperCase()
                                        + "_" + MagicBook.values()[reverseI].name().charAt(0) + " = ";
                                break;
                            }
                        }
                    }
                }
                spells.append(spellToAdd).append(child.getIndex() + 3).append(";\n");
            }
        }
        return spells.toString();
    }

    /**
     * Provides Magic Book(s) Information.
     *
     * @author GigiaJ
     */
    public enum MagicBook {
        STANDARD(WidgetIndices.SpellbookTab.LUMBRIDGE_HOME_TELEPORT_SPRITE),
        ANCIENT(WidgetIndices.SpellbookTab.ICE_RUSH_SPRITE),
        LUNAR(WidgetIndices.SpellbookTab.LUNAR_HOME_TELEPORT_SPRITE),
        ARCEUUS(WidgetIndices.SpellbookTab.ARCEEUS_HOME_TELEPORT_SPRITE),
        NULL(-1);

        private final int index;

        MagicBook(int index) {
            this.index = index;
        }

        int getIndex() {
            return this.index;
        }
    }
}
