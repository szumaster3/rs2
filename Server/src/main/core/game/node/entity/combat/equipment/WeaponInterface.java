package core.game.node.entity.combat.equipment;

import core.game.component.Component;
import core.game.component.ComponentDefinition;
import core.game.container.impl.EquipmentContainer;
import core.game.node.entity.impl.Animator.Priority;
import core.game.node.entity.player.Player;
import core.game.node.item.Item;
import core.game.system.config.ItemConfigParser;
import core.game.world.update.flag.context.Animation;
import core.net.packet.PacketRepository;
import core.net.packet.out.Encoders530;
import core.net.packet.out.PacketContext.InterfaceContext;
import shared.consts.Components;
import core.game.node.entity.player.link.AutocastManager;


/**
 * Represents the weapon interface component.
 *
 * @author Emperor
 */
public final class WeaponInterface extends Component {

    /**
     * The modern spell ids.
     */
    private static final int[] MODERN_SPELL_IDS = {1, 4, 6, 8, 10, 14, 17, 20, 24, 27, 33, 38, 45, 48, 52, 55};

    /**
     * The ancient spell ids.
     */
    private static final int[] ANCIENT_SPELL_IDS = {8, 12, 4, 0, 10, 14, 6, 2, 9, 13, 5, 1, 11, 15, 7, 3, 16, 17, 18, 19};

    /**
     * The slayer staff spell ids
     */
    private static final int[] SLAYER_STAFF_SPELL_IDS = {22, 31, 45, 48, 52, 55};

    /**
     * The void staff spell ids
     */
    private static final int[] VOID_STAFF_SPELL_IDS = {22, 42, 45, 48, 52, 55};

    /**
     * The default attack animations.
     */
    public static final Animation[] DEFAULT_ANIMS = {new Animation(422, Priority.HIGH), new Animation(423, Priority.HIGH), new Animation(422, Priority.HIGH), new Animation(422, Priority.HIGH)};

    /**
     * The stab equipment bonus index.
     */
    public static final int BONUS_STAB = 0;

    /**
     * The slash equipment bonus index.
     */
    public static final int BONUS_SLASH = 1;

    /**
     * The crush equipment bonus index.
     */
    public static final int BONUS_CRUSH = 2;

    /**
     * The magic equipment bonus index.
     */
    public static final int BONUS_MAGIC = 3;

    /**
     * The range equipment bonus index.
     */
    public static final int BONUS_RANGE = 4;

    /**
     * The accurate melee attack style
     */
    public static final int STYLE_ACCURATE = 0;

    /**
     * The aggressive attack style
     */
    public static final int STYLE_AGGRESSIVE = 1;

    /**
     * The controlled attack style
     */
    public static final int STYLE_CONTROLLED = 2;

    /**
     * The defensive attack style
     */
    public static final int STYLE_DEFENSIVE = 3;

    /**
     * The accurate range attack style
     */
    public static final int STYLE_RANGE_ACCURATE = 4;

    /**
     * The rapid range attack style
     */
    public static final int STYLE_RAPID = 5;

    /**
     * The long range attack style
     */
    public static final int STYLE_LONG_RANGE = 6;

    /**
     * The defensive spell cast attack style
     */
    public static final int STYLE_DEFENSIVE_CAST = 7;

    /**
     * The spell cast attack style
     */
    public static final int STYLE_CAST = 8;

    /**
     * The player.
     */
    private final Player player;

    /**
     * The current weapon interface.
     */
    private WeaponInterfaces current;

    /**
     * If the player has the special attack bar shown.
     */
    private boolean specialBar;

    /**
     * The player's attack animations.
     */
    private Animation[] attackAnimations;

    /**
     * Constructs a new {@code WeaponInterface} {@code Object}.
     *
     * @param player The player.
     */
    public WeaponInterface(Player player) {
        super(884);
        this.player = player;
        player.addExtension(WeaponInterface.class, this);
    }

    @Override
    public void open(Player player) {
        current = null;
        updateInterface();
    }

    /**
     * Opens the interface.
     */
    private void open() {
        ComponentDefinition definition = ComponentDefinition.forId(884);
        boolean resizable = player.getInterfaceManager().isResizable();
        PacketRepository.send(Encoders530.INTERFACE, new InterfaceContext(player, definition.getWindowPaneId(resizable), definition.getChildId(resizable), id, definition.isWalkable()));
        // CS2 1143 assigns the operations dynamically; enable option 1 on their layers.
        for (int child = 11; child <= 15; child++) {
            player.getPacketDispatch().sendIfaceSettings(2, child, 884, -1, -1);
        }
        player.getPacketDispatch().sendIfaceSettings(2, 4, 884, -1, -1);
        int slot = ensureStyleIndex(player, player.getSettings().getAttackStyleIndex());
        if (slot != player.getSettings().getAttackStyleIndex()) {
            player.getSettings().toggleAttackStyleIndex(slot);
        }
        player.getProperties().setAttackStyle(current.getAttackStyles()[slot]);
        checkStaffConfigs(slot);
    }

    /**
     * Ensures the style index.
     *
     * @param player The player.
     * @param slot   The attack style index.
     * @return The index, ensured to be smaller than styles.length and larger
     * than 0.
     */
    private int ensureStyleIndex(Player player, int slot) {
        AttackStyle style = player.getProperties().getAttackStyle();
        if (slot < 0) return 0;
        if (current == WeaponInterfaces.STAFF && slot > 2) return 2;
        if (slot >= current.getAttackStyles().length) {
            slot = current.getAttackStyles().length - 1;
            if (style != null) {
                for (int i = slot; i >= 0; i--) {
                    if (current.getAttackStyles()[i].style == style.style) {
                        return i;
                    }
                }
            }
            return slot;
        }
        if (style != null && current.getAttackStyles()[slot].style != style.style) {
            for (int i = current.getAttackStyles().length - 1; i >= 0; i--) {
                if (current.getAttackStyles()[i].style == style.style) {
                    return i;
                }
            }
        }
        return slot;
    }

    /**
     * Get the proper config for the special bar.
     *
     * @return the config.
     */
    /**
     * Updates the interface.
     */
    public void updateInterface() {
        player.getInterfaceManager().tabs[0] = this;
        Item weapon = player.getEquipment().get(EquipmentContainer.SLOT_WEAPON);
        WeaponInterfaces inter = getWeaponInterface(weapon);
        if (weapon != null) {
            specialBar = weapon.getDefinition().hasSpecialBar() && weapon.getDefinition().getConfiguration(ItemConfigParser.HAS_SPECIAL, false);
            attackAnimations = weapon.getDefinition().getConfiguration(ItemConfigParser.ATTACK_ANIMS, DEFAULT_ANIMS);
        } else {
            specialBar = false;
            attackAnimations = DEFAULT_ANIMS;
        }
        if (inter != current) {
            current = inter;
            open();
            player.getProperties().getCombatPulse().updateStyle();
        }
        if (player.getSettings().getAttackStyleIndex() < attackAnimations.length && !player.getAppearance().isNpc()) {
            player.getProperties().setAttackAnimation(attackAnimations[player.getSettings().getAttackStyleIndex()]);
        }
        if (player.getProperties().getAutocastSpell() != null) {
            AutocastManager.INSTANCE.restore(player);
        }
        if (player.getSettings().isSpecialToggled()) {
            player.getSettings().toggleSpecialBar();
        }
    }

    /**
     * Sets the current attack style.
     *
     * @param button The button the player has pressed.
     * @return {@code True} if the attack style got set.
     */
    public boolean setAttackStyle(int button) {
        if (current == null || button < 11 || button > 14) return false;
        int slot = button - 11;
        if (current == WeaponInterfaces.STAFF && slot >= 3) return false;
        return applyAttackStyle(slot);
    }

    private boolean applyAttackStyle(int slot) {
        if (slot < 0 || slot >= current.getAttackStyles().length) {
            return false;
        }
        AttackStyle style = current.getAttackStyles()[slot];
        player.getProperties().setAttackStyle(style);
        player.getSettings().toggleAttackStyleIndex(slot);
        if (slot < attackAnimations.length && !player.getAppearance().isNpc()) {
            player.getProperties().setAttackAnimation(attackAnimations[slot]);
        }
        checkStaffConfigs(slot);
        return true;
    }

    /**
     * Checks the staff configurations.
     *
     * @param slot The slot of the current attack style selected.
     */
    private void checkStaffConfigs(int slot) {
    }

    public int getAutospellId(int spellId) {
        return spellId;
    }

    public void selectAutoSpell(int spellId, boolean adjustAttackStyle) {
        if (spellId < 0) {
            AutocastManager.INSTANCE.clear(player);
            if (adjustAttackStyle && current != null) {
                applyAttackStyle(ensureStyleIndex(player, player.getSettings().getAttackStyleIndex()));
            }
            player.getProperties().getCombatPulse().updateStyle();
        } else {
            AutocastManager.INSTANCE.selectLegacy(player, spellId);
        }
    }

    public void openAutocastSelect() {
        // selects autocast directly in the active spellbook.
    }

    public boolean canAutocast(boolean message) {
        return player.getAttribute("spell:swap", 0) == 0 &&
            player.getSpellBookManager().getSpellBook() != Components.MAGIC_LUNAR_430;
    }

    /**
     * Gets the current weapon interface id.
     *
     * @return The component id.
     */
    public static WeaponInterfaces getWeaponInterface(Item weapon) {
        if (weapon == null) {
            return WeaponInterfaces.values()[0];
        }
        Object category = weapon.getDefinition().getClientScriptData() == null ? null : weapon.getDefinition().getClientScriptData().get(686);
        int slot = category instanceof Integer ? (Integer) category : 0;
        if (slot < 0 || slot >= WeaponInterfaces.values().length) slot = 0;
        return WeaponInterfaces.values()[slot];
    }

    /**
     * Represents an attack style.
     *
     * @author Emperor
     */
    public static class AttackStyle {

        /**
         * The style type.
         */
        private final int style;

        /**
         * The bonus type.
         */
        private final int bonusType;

        /**
         * Constructs a new {@code AttackStyle} {@code Object}.
         *
         * @param style     The style type.
         * @param bonusType The bonus type.
         */
        public AttackStyle(int style, int bonusType) {
            this.style = style;
            this.bonusType = bonusType;
        }

        /**
         * Gets the style.
         *
         * @return The style.
         */
        public int getStyle() {
            return style;
        }

        /**
         * Gets the bonusType.
         *
         * @return The bonusType.
         */
        public int getBonusType() {
            return bonusType;
        }
    }

    /**
     * Represents the weapon interfaces.
     *
     * @author Emperor
     */
    public static enum WeaponInterfaces {

        /**
         * The unarmed weapon interface (ordinal=0)
         */
        UNARMED(Components.WEAPON_FISTS_SEL_92, new AttackStyle(STYLE_ACCURATE, BONUS_CRUSH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_CRUSH)),

        /**
         * The staff weapon interface (ordinal=1)
         */
        STAFF(Components.WEAPON_STAFF_SEL_90, new AttackStyle(STYLE_ACCURATE, BONUS_CRUSH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE_CAST, BONUS_MAGIC), new AttackStyle(STYLE_CAST, BONUS_MAGIC)),

        /**
         * The (battle) axe weapon interface (ordinal=2)
         */
        AXE(Components.WEAPON_BAXE_SEL_75, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH)),

        /**
         * The scepter weapon interface (ordinal=3)
         */
        SCEPTER(Components.WEAPON_SCEPTER_SEL_85, new AttackStyle(STYLE_ACCURATE, BONUS_CRUSH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_CRUSH)),

        /**
         * The pickaxe weapon interface (ordinal=4)
         */
        PICKAXE(Components.WEAPON_PICKAXE_SEL_83, new AttackStyle(STYLE_ACCURATE, BONUS_STAB), new AttackStyle(STYLE_AGGRESSIVE, BONUS_STAB), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_STAB)),

        /**
         * The sword/dagger weapon interface (ordinal=5)
         */
        SWORD_DAGGER(Components.WEAPON_DAGGER_SEL_89, new AttackStyle(STYLE_ACCURATE, BONUS_STAB), new AttackStyle(STYLE_AGGRESSIVE, BONUS_STAB), new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_DEFENSIVE, BONUS_STAB)),

        /**
         * The scimitar/silverlight/silver sickle/... weapon interface
         * (ordinal=6)
         */
        SCIMITAR(Components.WEAPON_SCIMITAR_SEL_81, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_CONTROLLED, BONUS_STAB), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH)),

        /**
         * The 2-h sword weapon interface (ordinal=7)
         */
        TWO_H_SWORD(Components.WEAPON_2H_SWORD_SEL_82, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH)),

        /**
         * The mace weapon interface (ordinal=8)
         */
        MACE(Components.WEAPON_MACE_SEL_88, new AttackStyle(STYLE_ACCURATE, BONUS_CRUSH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_CONTROLLED, BONUS_STAB), new AttackStyle(STYLE_DEFENSIVE, BONUS_CRUSH)),

        /**
         * The claws weapon interface (ordinal=9)
         */
        CLAWS(Components.WEAPON_CLAWS_SEL_78, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_CONTROLLED, BONUS_STAB), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH)),

        /**
         * The warhammer/maul weapon interface (ordinal=10)
         */
        WARHAMMER_MAUL(Components.WEAPON_WARHAMMER_SEL_76, new AttackStyle(STYLE_ACCURATE, BONUS_CRUSH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_CRUSH)),

        /**
         * The abyssal whip weapon interface (ordinal=11)
         */
        WHIP(Components.WEAPON_WHIP_SEL_93, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_CONTROLLED, BONUS_SLASH), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH)),

        /**
         * The flowers weapon interface (ordinal=12)
         */
        FLOWERS(Components.WEAPON_WARHAMMER_SEL_76, new AttackStyle(STYLE_ACCURATE, BONUS_CRUSH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_CRUSH)),

        /**
         * The mud pie weapon interface (ordinal=13)
         */
        MUD_PIE(Components.WEAPON_THROWN_SEL_91, new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_RAPID, BONUS_RANGE), new AttackStyle(STYLE_LONG_RANGE, BONUS_RANGE)),

        /**
         * The spear weapon interface (ordinal=14)
         */
        SPEAR(Components.WEAPON_SPEAR_SEL_87, new AttackStyle(STYLE_CONTROLLED, BONUS_STAB), new AttackStyle(STYLE_CONTROLLED, BONUS_SLASH), new AttackStyle(STYLE_CONTROLLED, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_STAB)),

        /**
         * The halberd weapon interface (ordinal=15)
         */
        HALBERD(Components.WEAPON_HALBERD_SEL_84, new AttackStyle(STYLE_CONTROLLED, BONUS_STAB), new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_DEFENSIVE, BONUS_STAB)),

        /**
         * The bow weapon interface (ordinal=16)
         */
        BOW(Components.WEAPON_BOW_SEL_77, new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_RAPID, BONUS_RANGE), new AttackStyle(STYLE_LONG_RANGE, BONUS_RANGE)),

        /**
         * The crossbow weapon interface (ordinal=17)
         */
        CROSSBOW(Components.WEAPON_XBOW_SEL_79, new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_RAPID, BONUS_RANGE), new AttackStyle(STYLE_LONG_RANGE, BONUS_RANGE)),

        /**
         * The thrown weapons weapon interface (ordinal=18)
         */
        THROWN_WEAPONS(Components.WEAPON_THROWN_SEL_91, new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_RAPID, BONUS_RANGE), new AttackStyle(STYLE_LONG_RANGE, BONUS_RANGE)),

        /**
         * The thrown weapons weapon interface (ordinal=19)
         */
        CHINCHOMPA(Components.WEAPON_CHINCHOMPA_SEL_473, new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_RAPID, BONUS_RANGE), new AttackStyle(STYLE_LONG_RANGE, BONUS_RANGE)),

        /**
         * The fixed device weapon interface (ordinal=20)
         */
        FIXED_DEVICE(Components.WEAPON_FIXED_DEVICE_SEL_80, new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH)),

        /**
         * The salamander weapon interface (ordinal=21)
         */
        SALAMANDER(Components.WEAPON_SALAMANDER_SEL_474, new AttackStyle(STYLE_AGGRESSIVE, BONUS_SLASH), new AttackStyle(STYLE_RANGE_ACCURATE, BONUS_RANGE), new AttackStyle(STYLE_DEFENSIVE_CAST, BONUS_MAGIC)),

        /**
         * The scythe weapon interface (ordinal=22)
         */
        SCYTHE(Components.WEAPON_SCYTHE_SEL_86, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_STAB), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH)),

        /**
         * The ivandis flail weapon interface (ordinal=23)
         */
        IVANDIS_FLAIL(Components.WEAPON_SCEPTER_SEL_85, new AttackStyle(STYLE_ACCURATE, BONUS_SLASH), new AttackStyle(STYLE_AGGRESSIVE, BONUS_CRUSH), new AttackStyle(STYLE_DEFENSIVE, BONUS_SLASH));

        /**
         * The interface id.
         */
        private final int interfaceId;

        /**
         * The attack styles.
         */
        private final AttackStyle[] attackStyles;

        /**
         * Constructs a new {@code WeaponInterface} {@code Object}.
         *
         * @param interfaceId  The interface id.
         * @param attackStyles The attack styles.
         */
        private WeaponInterfaces(int interfaceId, AttackStyle... attackStyles) {
            this.interfaceId = interfaceId;
            this.attackStyles = attackStyles;
        }

        /**
         * Gets the interfaceId.
         *
         * @return The interfaceId.
         */
        public int getInterfaceId() {
            return interfaceId;
        }

        /**
         * Gets the attackStyles.
         *
         * @return The attackStyles.
         */
        public AttackStyle[] getAttackStyles() {
            return attackStyles;
        }
    }

    /**
     * Gets the currently opened weapon interface.
     *
     * @return The current weapon interface.
     */
    public WeaponInterfaces getWeaponInterface() {
        return current;
    }

    /**
     * If the special bar is enabled.
     *
     * @return {@code True} if so.
     */
    public boolean isSpecialBar() {
        return specialBar;
    }

}