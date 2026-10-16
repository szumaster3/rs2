package content.global.plugins.item.equipment.brawling_gloves

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import content.global.plugins.item.equipment.brawling_gloves.BrawlingGloves.Companion.forId
import content.global.plugins.item.equipment.brawling_gloves.BrawlingGloves.Companion.forIndicator
import core.api.LoginListener
import core.api.PersistPlayer
import core.api.setAttribute
import core.cache.def.impl.ItemDefinition
import core.game.node.entity.player.Player
import core.game.node.item.Item
import java.util.*

/**
 * The brawling gloves manager.
 */
class BrawlingGlovesManager : LoginListener, PersistPlayer {
    /**
     * The Player.
     */
    val player: Player?

    /**
     * The Glove charges.
     */
    var gloveCharges: HashMap<Int, Int?> = HashMap()

    /**
     * Instantiates a new Brawling gloves manager.
     *
     * @param player the player
     */
    constructor(player: Player?) {
        this.player = player
    }

    /**
     * Instantiates a new Brawling gloves manager.
     */
    constructor() {
        this.player = null
    }

    override fun login(player: Player) {
        val instance = BrawlingGlovesManager(player)
        setAttribute(player, "bg-manager", instance)
    }

    override fun parsePlayer(player: Player, data: JsonObject) {
        val instance = getInstance(player)
        if (data.has("brawlingGloves")) {
            val bgData = data.getAsJsonArray("brawlingGloves")
            for (bg in bgData) {
                val glove = bg.asJsonObject
                val gloveId = glove["gloveId"].asString.toInt()
                val charges = glove["charges"].asString.toInt()
                instance.registerGlove(forIndicator(gloveId)!!.id, charges)
            }
        }
    }

    override fun savePlayer(player: Player, save: JsonObject) {
        val instance = getInstance(player)
        if (instance.gloveCharges.size > 0) {
            val brawlingGloves = JsonArray()
            for ((key, value) in instance.gloveCharges) {
                val bGlove = JsonObject()
                bGlove.addProperty("gloveId", forId(key)!!.indicator.toString())
                bGlove.addProperty("charges", value.toString())
                brawlingGloves.add(bGlove)
            }
            save.add("brawlingGloves", brawlingGloves)
        }
    }

    /**
     * Register glove.
     *
     * @param id the id
     */
    fun registerGlove(id: Int) {
        try {
            Objects.requireNonNull(forId(id))?.let { registerGlove(id, it.charges) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Register glove.
     *
     * @param id      the id
     * @param charges the charges
     */
    private fun registerGlove(id: Int, charges: Int) {
        gloveCharges.putIfAbsent(id, charges)
    }

    /**
     * Update charges.
     *
     * @param glove   the glove
     * @param charges the charges
     */
    fun updateCharges(glove: Int, charges: Int) {
        if (gloveCharges[glove] != null) {
            if (gloveCharges[glove]!! - charges <= 0) {
                gloveCharges.remove(glove)
                player!!.equipment.remove(Item(glove))
                player.packetDispatch.sendMessage(
                    "<col=ff0000>You use the last charge of your " + ItemDefinition.forId(
                        glove
                    ).name + " and they vanish.</col>"
                )
                return
            }
            val currentCharges = gloveCharges[glove]!!
            gloveCharges.replace(glove, currentCharges - charges)
            player!!.debug("Charges remaining: " + (currentCharges - 1))
            if ((currentCharges - 1) % 50 == 0) {
                player.packetDispatch.sendMessage("<col=1fbd0d>Charges remaining: " + gloveCharges[glove])
            }
        }
    }

    val experienceBonus: Double
        /**
         * Gets experience bonus.
         *
         * @return the experience bonus
         */
        get() {
            val bonus: Double
            val level = player!!.skullManager.level
            bonus = if (level > 0) {
                3.0
            } else {
                0.5
            }
            return bonus
        }

    companion object {
        /**
         * Gets instance.
         *
         * @param player the player
         * @return the instance
         */
        fun getInstance(player: Player): BrawlingGlovesManager {
            return player.getAttribute("bg-manager", BrawlingGlovesManager())
        }
    }
}
