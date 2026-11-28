package content.global.skill.summoning.pets;

import content.global.skill.summoning.familiar.Familiar;
import content.global.skill.summoning.familiar.FamiliarSpecial;
import core.game.node.entity.player.Player;
import core.game.node.item.Item;
import core.game.world.GameWorld;
import shared.consts.NPCs;

import static core.api.ContentAPIKt.setVarp;

/**
 * The type Pet represents a pet familiar that follows a player and can grow and age over time.
 * It includes methods to manage the pet's hunger and growth, handle interactions, and grow to the next stage.
 */
public final class Pet extends Familiar {

    private final int itemId;
    private final PetDetails details;
    private double growthRate;
    private final Pets pet;
    private int hasWarned = 0;

    /**
     * Instantiates a new Pet.
     *
     * @param owner   the owner of the pet
     * @param details the details of the pet, including hunger and growth
     * @param itemId  the item ID associated with the pet
     * @param id      the NPC ID associated with the pet
     */
    public Pet(Player owner, final PetDetails details, int itemId, int id) {
        super(owner, id, -1, -1, -1);
        this.pet = Pets.forId(itemId);
        this.details = details;
        this.itemId = itemId;
        this.growthRate = pet.growthRate;
    }

    /**
     * Sends the configuration data to the player's client.
     * This includes the pet's growth and hunger values, NPC ID, and item ID.
     */
    @Override
    public void sendConfiguration() {
        setVarp(owner, 1175, ((int) details.getGrowth() << 1) | ((int) details.getHunger() << 9));
        setVarp(owner, 1174, getId());
        setVarp(owner, 448, itemId);
    }

    /**
     * Handles actions to be taken each game tick, such as updating hunger and growth,
     * warning the player if the pet is hungry, and managing the pet's stage growth.
     */
    @Override
    public void handleTickActions() {
        final PetDetails petDetails = details;
        boolean canGetHungry = getPet().isKitten(itemId) || (!Pets.isGrownCat(itemId) && getPet().food.length > 0);

        if (canGetHungry) {
            double amount = itemId == getPet().babyItemId ? 0.025 : 0.018;

            int rate = owner.getAttribute("petrate", 1);
            if (rate == 0) {
                amount = 0;
            } else if (rate == 2) {
                amount *= 100;
            }

            petDetails.updateHunger(amount);
        }

        double hunger = petDetails.getHunger();
        if (hunger >= 75.0 && hunger <= 90.0 && hasWarned < 1) {
            owner.sendMessage("<col=ff0000>Your pet is getting hungry.</col>");
            hasWarned = 1;
        } else if (hunger >= 90.0 && hasWarned < 2) {
            owner.sendMessage("<col=ff0000>Your pet is starving, feed it before it runs off.</col>");
            hasWarned = 2;
        }

        if (hunger >= 100.0) {
            owner.getFamiliarManager().dismiss();
            owner.getFamiliarManager().removeDetails(getItemId());
            owner.getFamiliarManager().setFamiliar(null);
            setVarp(owner, 1175, 0);
            owner.sendMessage("<col=ff0000>Your pet has run away.</col>");
            return;
        }

        double growth = petDetails.getGrowth();
        double growthrate = pet.growthRate;

        if (growthrate > 0.0) {
            int rate = owner.getAttribute("petrate", 1);
            if (rate == 0) {
                growthrate = 0;
            } else if (rate == 2) {
                growthrate *= 100;
            }

            petDetails.updateGrowth(growthrate);

            if (growth >= 100.0) {
                growNextStage();
            }
        }

        if ((!isInvisible() && owner.getLocation().getDistance(getLocation()) > 12)
                || (isInvisible() && ticks % 25 == 0)) {
            if (!call()) {
                setInvisible(true);
            }
        } else if (!getPulseManager().hasPulseRunning()) {
            startFollowing();
        }

        sendConfiguration();
    }

    /**
     * Causes the pet to grow to the next stage by transforming into a new pet.
     * This method also updates the pet's growth and removes the current pet from the familiar manager.
     */
    public void growNextStage() {
        if (pet == null) {
            return;
        }
        int newItemId = pet.getNextStageItemId(itemId);
        if (newItemId == -1) {
            return;
        }
        owner.getFamiliarManager().removeDetails(this.getItemIdHash());
        owner.getFamiliarManager().dismiss();
        owner.getPacketDispatch().sendMessage("<col=ff0000>Your pet has grown larger.</col>");
        int npcId = pet.getNpcId(newItemId);
        details.updateGrowth(-100.0);
        Pet newPet = new Pet(owner, details, newItemId, npcId);
        newPet.growthRate = growthRate;
        newPet.hasWarned = hasWarned;
        owner.getFamiliarManager().setFamiliar(newPet);
        owner.getFamiliarManager().spawnFamiliar();
    }

    /**
     * Constructs a new instance of this pet.
     *
     * @param owner the owner of the pet
     * @param id    the NPC ID of the pet
     * @return the constructed pet
     */
    @Override
    public Familiar construct(Player owner, int id) {
        return this;
    }

    /**
     * Performs a special move with the pet. Currently not implemented.
     *
     * @param special the special move to perform
     * @return false since no special move is implemented
     */
    @Override
    protected boolean specialMove(FamiliarSpecial special) {
        return false;
    }

    /**
     * Returns whether this pet is a combat familiar.
     *
     * @return false since pets are not combat familiars
     */
    @Override
    public boolean isCombatFamiliar() {
        return false;
    }

    /**
     * Gets the item ID of this pet.
     *
     * @return the item ID
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the item ID hash associated with this pet.
     * This is used to uniquely identify the pet in the game world.
     *
     * @return the item ID hash
     */
    public int getItemIdHash() {
        Item item = new Item(itemId);
        item.setCharge(details.getIndividual());
        return item.getIdHash();
    }

    /**
     * Gets the details of this pet, including hunger and growth.
     *
     * @return the pet's details
     */
    public PetDetails getDetails() {
        return details;
    }

    /**
     * Gets the pet enum associated with this pet.
     *
     * @return the pet enum
     */
    public Pets getPet() {
        return pet;
    }

    /**
     * Gets the NPC IDs for all stages of this pet's growth.
     *
     * @return an array of NPC IDs
     */
    @Override
    public int[] getIds() {
        return new int[] {
                NPCs.KITTENPET1_761, NPCs.KITTENPET_LIGHT_762, NPCs.KITTENPET_BROWN_763, NPCs.KITTENPET_BLACK_764, NPCs.KITTENPET_BROWNGREY_765,
                NPCs.KITTENPET_BLUEGREY_766, NPCs.GROWNCAT_768, NPCs.GROWNCAT_LIGHT_769, NPCs.GROWNCAT_BROWN_770, NPCs.GROWNCAT_BLACK_771, NPCs.GROWNCAT_BROWNGREY_772,
                NPCs.GROWNCAT_BLUEGREY_773, NPCs.OVERGROWNCAT_774, NPCs.OVERGROWNCAT_LIGHT_775, NPCs.OVERGROWNCAT_BROWN_776,
                NPCs.OVERGROWNCAT_BLACK_777, NPCs.OVERGROWNCAT_BROWNGREY_778, NPCs.OVERGROWNCAT_BLUEGREY_779, NPCs.OVERGROWNCAT_HELL_3503,
                NPCs.HELLCAT_3504, NPCs.KITTENPET_HELL_3505, NPCs.POH_TOY_CAT_3598, NPCs.LORE_HATCHLING_DRAGON_1_6900,
                NPCs.LORE_BABY_DRAGON_1_6901, NPCs.LORE_HATCHLING_DRAGON_2_6902, NPCs.LORE_BABY_DRAGON_2_6903, NPCs.LORE_HATCHLING_DRAGON_3_6904,
                NPCs.LORE_BABY_DRAGON_3_6905, NPCs.LORE_HATCHLING_DRAGON_4_6906, NPCs.LORE_BABY_DRAGON_4_6907, NPCs.LORE_BABY_PENGUIN_1_6908,
                NPCs.LORE_PENGUIN_1_6909, NPCs.LORE_RAVEN_CHICK_1_6911, NPCs.LORE_RAVEN_1_6912, NPCs.LORE_BABY_RACCOON_1_6913, NPCs.LORE_RACCOON_1_6914,
                NPCs.LORE_BABY_GECKO_1_6915, NPCs.LORE_GECKO_1_6916, NPCs.LORE_BABY_SQUIRREL_1_6919, NPCs.LORE_SQUIRREL_1_6920, NPCs.LORE_CHAMELEON_BABY_GRASS_6922,
                NPCs.LORE_CHAMELEON_GRASS_6923, NPCs.LORE_BABY_MONKEY_1_6942, NPCs.LORE_MONKEY_1_6943, NPCs.LORE_VULTURE_CHICK_1_6945, NPCs.LORE_VULTURE_1_6946,
                NPCs.LORE_BABY_CRAB_1_6947, NPCs.LORE_GIANT_CRAB_1_6948, NPCs.LORE_BIRD_CHICK_SARA_6949, NPCs.LORE_BIRD_YOUNG_SARA_6950,
                NPCs.SARADOMIN_OWL_6951, NPCs.LORE_BIRD_CHICK_ZAMORAK_6952, NPCs.LORE_BIRD_YOUNG_ZAMORAK_6953, NPCs.LORE_BIRD_ZAMORAK_6954,
                NPCs.LORE_BIRD_CHICK_GUTHIX_6955, NPCs.LORE_BIRD_YOUNG_GUTHIX_6956, NPCs.LORE_BIRD_GUTHIX_6957, NPCs.LORE_DOG_TERRIER_PUPPY_1_6958,
                NPCs.LORE_DOG_GREYHOUND_PUPPY_1_6960, NPCs.LORE_DOG_GREYHOUND_1_6961, NPCs.LORE_DOG_LABRADOR_PUPPY_1_6962, NPCs.LORE_DOG_LABRADOR_1_6963,
                NPCs.LORE_DOG_DALMATION_PUPPY_1_6964, NPCs.DALMATIAN_6965, NPCs.LORE_DOG_SHEEPDOG_PUPPY_1_6966, NPCs.LORE_DOG_SHEEPDOG_1_6967,
                NPCs.LORE_DOG_BULLDOG_1_6968, NPCs.LORE_DOG_BULLDOG_PUPPY_1_6969, NPCs.AFR_LORE_PLATYPUS1_7015, NPCs.AFR_LORE_PLATYPUS2_7016, NPCs.AFR_LORE_PLATYPUS3_7017,
                NPCs.AFR_LORE_PLATYPUS_BABY1_7018, NPCs.AFR_LORE_PLATYPUS_BABY2_7019, NPCs.AFR_LORE_PLATYPUS_BABY3_7020, NPCs.LORE_BABY_MONKEY_2_7210,
                NPCs.LORE_MONKEY_2_7211, NPCs.LORE_BABY_MONKEY_3_7212, NPCs.LORE_MONKEY_3_7213, NPCs.LORE_BABY_MONKEY_4_7214, NPCs.LORE_MONKEY_4_7215,
                NPCs.LORE_BABY_MONKEY_5_7216, NPCs.LORE_MONKEY_5_7217, NPCs.LORE_BABY_MONKEY_6_7218, NPCs.LORE_MONKEY_6_7219, NPCs.LORE_BABY_MONKEY_7_7220,
                NPCs.LORE_MONKEY_7_7221, NPCs.LORE_BABY_MONKEY_8_7222, NPCs.LORE_MONKEY_8_7223, NPCs.LORE_BABY_MONKEY_9_7224, NPCs.LORE_MONKEY_9_7225,
                NPCs.LORE_BABY_MONKEY_10_7226, NPCs.LORE_MONKEY_10_7227, NPCs.LORE_DOG_TERRIER_PUPPY_2_7237, NPCs.LORE_DOG_TERRIER_2_7238, NPCs.LORE_DOG_TERRIER_PUPPY_3_7239,
                NPCs.LORE_DOG_TERRIER_3_7240, NPCs.LORE_DOG_GREYHOUND_PUPPY_2_7241, NPCs.LORE_DOG_GREYHOUND_2_7242, NPCs.LORE_DOG_GREYHOUND_PUPPY_3_7243, NPCs.LORE_DOG_GREYHOUND_3_7244,
                NPCs.LORE_DOG_LABRADOR_PUPPY_2_7245, NPCs.LORE_DOG_LABRADOR_2_7246, NPCs.LORE_DOG_LABRADOR_PUPPY_3_7247, NPCs.LORE_DOG_LABRADOR_3_7248, NPCs.LORE_DOG_DALMATION_PUPPY_2_7249,
                NPCs.DALMATIAN_7250, NPCs.LORE_DOG_DALMATION_PUPPY_3_7251, NPCs.DALMATIAN_7252, NPCs.LORE_DOG_SHEEPDOG_PUPPY_2_7253, NPCs.LORE_DOG_SHEEPDOG_2_7254,
                NPCs.LORE_DOG_SHEEPDOG_PUPPY_3_7255, NPCs.LORE_DOG_SHEEPDOG_3_7256, NPCs.LORE_DOG_BULLDOG_2_7257, NPCs.LORE_DOG_BULLDOG_3_7258, NPCs.LORE_DOG_BULLDOG_PUPPY_2_7259,
                NPCs.LORE_DOG_BULLDOG_PUPPY_3_7260, NPCs.LORE_RAVEN_CHICK_2_7261, NPCs.LORE_RAVEN_2_7262, NPCs.LORE_RAVEN_CHICK_3_7263, NPCs.LORE_RAVEN_3_7264,
                NPCs.LORE_RAVEN_CHICK_4_7265, NPCs.LORE_RAVEN_4_7266, NPCs.LORE_RAVEN_CHICK_5_7267, NPCs.LORE_RAVEN_5_7268, NPCs.LORE_RAVEN_CHICK_6_7269, NPCs.LORE_RAVEN_6_7270,
                NPCs.LORE_BABY_RACCOON_2_7271, NPCs.LORE_RACCOON_2_7272, NPCs.LORE_BABY_RACCOON_3_7273, NPCs.LORE_RACCOON_3_7274, NPCs.LORE_BABY_GECKO_2_7277,
                NPCs.LORE_BABY_GECKO_3_7278, NPCs.LORE_BABY_GECKO_4_7279, NPCs.LORE_BABY_GECKO_5_7280, NPCs.LORE_GECKO_2_7281, NPCs.LORE_GECKO_3_7282, NPCs.LORE_GECKO_4_7283,
                NPCs.LORE_GECKO_5_7284, NPCs.LORE_BABY_CRAB_2_7293, NPCs.LORE_GIANT_CRAB_2_7294, NPCs.LORE_BABY_CRAB_3_7295, NPCs.LORE_GIANT_CRAB_3_7296,
                NPCs.LORE_BABY_CRAB_4_7297, NPCs.LORE_GIANT_CRAB_4_7298, NPCs.LORE_BABY_CRAB_5_7299, NPCs.LORE_GIANT_CRAB_5_7300, NPCs.LORE_BABY_SQUIRREL_2_7301,
                NPCs.LORE_SQUIRREL_2_7302, NPCs.LORE_BABY_SQUIRREL_3_7303, NPCs.LORE_SQUIRREL_3_7304, NPCs.LORE_BABY_SQUIRREL_4_7305, NPCs.LORE_SQUIRREL_4_7306,
                NPCs.LORE_BABY_SQUIRREL_5_7307, NPCs.LORE_SQUIRREL_5_7308, NPCs.LORE_BABY_PENGUIN_2_7313, NPCs.LORE_PENGUIN_2_7314, NPCs.LORE_BABY_PENGUIN_3_7316,
                NPCs.LORE_PENGUIN_3_7317, NPCs.LORE_VULTURE_CHICK_2_7319, NPCs.LORE_VULTURE_2_7320, NPCs.LORE_VULTURE_CHICK_3_7321, NPCs.LORE_VULTURE_3_7322, NPCs.LORE_VULTURE_CHICK_4_7323,
                NPCs.LORE_VULTURE_4_7324, NPCs.LORE_VULTURE_CHICK_5_7325, NPCs.LORE_VULTURE_5_7326, NPCs.LORE_VULTURE_CHICK_6_7327, NPCs.LORE_VULTURE_6_7328,NPCs.WILEYCAT_LIGHT_2668,NPCs.LAZYCAT_LIGHT_2662,
                NPCs.WILEYCAT_2669,NPCs.LAZYCAT_2663, NPCs.WILEYCAT_BROWN_2670,NPCs.LAZYCAT_BROWN_2664, NPCs.WILEYCAT_BLACK_2671,NPCs.LAZYCAT_BLACK_2665, NPCs.WILEYCAT_BROWNGREY_2672,NPCs.LAZYCAT_BROWNGREY_2666, NPCs.WILEYCAT_BROWNGREY_2672,NPCs.LAZYCAT_BLUEGREY_2667
        };
    }
}
