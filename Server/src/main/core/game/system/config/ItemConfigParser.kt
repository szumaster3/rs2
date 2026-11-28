package core.game.system.config

import com.google.gson.Gson
import com.google.gson.JsonArray
import core.ServerConstants
import core.api.log
import core.cache.def.impl.ObjType
import core.game.node.entity.impl.Animator
import core.game.node.entity.player.link.audio.Audio
import core.game.world.update.flag.context.Animation
import core.tools.Log
import java.io.FileReader

class ItemConfigParser {
    companion object {
        const val TRADEABLE = "tradeable"
        const val LENDABLE = "lendable"
        const val SPAWNABLE = "spawnable"
        const val DESTROY = "destroy"
        const val TWO_HANDED = "two_handed"
        const val ALCHEMIZABLE = "alchemizable"
        const val HIGH_ALCHEMY = "high_alchemy"
        const val LOW_ALCHEMY = "low_alchemy"
        const val SHOP_PRICE = "shop_price"
        const val GE_PRICE = "grand_exchange_price"
        const val EXAMINE = "examine"
        const val WEIGHT = "weight"
        const val BONUS = "bonuses"
        const val ABSORB = "absorb"
        const val EQUIP_SLOT = "equipment_slot"
        const val ATTACK_SPEED = "attack_speed"
        const val REMOVE_HEAD = "remove_head"
        const val IS_HAT = "hat"
        const val REMOVE_BEARD = "remove_beard"
        const val REMOVE_SLEEVES = "remove_sleeves"
        const val STAND_ANIM = "stand_anim"
        const val STAND_TURN_ANIM = "stand_turn_anim"
        const val WALK_ANIM = "walk_anim"
        const val RUN_ANIM = "run_anim"
        const val TURN180_ANIM = "turn180_anim"
        const val TURN90CW_ANIM = "turn90cw_anim"
        const val TURN90CCW_ANIM = "turn90ccw_anim"
        const val WEAPON_INTERFACE = "weapon_interface"
        const val HAS_SPECIAL = "has_special"
        const val ATTACK_ANIMS = "attack_anims"
        const val DESTROY_MESSAGE = "destroy_message"
        const val REQUIREMENTS = "requirements"
        const val GE_LIMIT = "ge_buy_limit"
        const val DEFENCE_ANIMATION = "defence_anim"
        const val ATTACK_AUDIO = "attack_audios"
        const val EQUIP_AUDIO = "equip_audio"
        const val POINT_PRICE = "point_price"
        const val BANKABLE = "bankable"
        const val RARE_ITEM = "rare_item"
        const val TOKKUL_PRICE = "tokkul_price"
        const val RENDER_ANIM_ID = "render_anim"
        const val ARCHERY_TICKET_PRICE = "archery_ticket_price"
        const val CASTLE_WARS_TICKET_PRICE = "castle_wars_ticket_price"
    }

    private val gson = Gson()

    fun load() {
        var count = 0
        FileReader(ServerConstants.CONFIG_PATH + "item_configs.json").use { reader ->
            val configList = gson.fromJson(reader, JsonArray::class.java)
            for (element in configList) {
                val e = element.asJsonObject
                val id = e.get("id").asInt
                val def = ObjType.forId(id)
                val configs = def.handlers
                val requirements = HashMap<Int, Int>()

                for ((key, value) in e.entrySet()) {
                    val keyStr = key
                    if (value.isJsonNull) continue
                    val valStr = value.toString().removeSurrounding("\"")
                    if (valStr.isEmpty() || valStr == "null") continue

                    when (keyStr) {
                        "defence_anim" -> configs[keyStr] = Animation(value.asInt, Animator.Priority.HIGH)

                        "requirements" -> {
                            configs[keyStr] = requirements
                            val reqStr = value.asString
                            reqStr.split("-").forEach { en ->
                                val tokens = en.replace("{", "").replace("}", "").split(",")
                                if (tokens.size == 2) {
                                    requirements[tokens[0].toInt()] = tokens[1].toInt()
                                }
                            }
                        }

                        "attack_audios" -> configs[keyStr] =
                            valStr.split(",").map { Audio(it.toInt()) }.toTypedArray()

                        "attack_anims" -> configs[keyStr] =
                            valStr.split(",").map { Animation(it.toInt(), Animator.Priority.HIGH) }.toTypedArray()

                        "absorb", "bonuses" -> configs[keyStr] =
                            valStr.split(",").map { it.toInt() }.toIntArray()

                        "fun_weapon",
                        "rare_item",
                        "bankable",
                        "two_handed",
                        "has_special",
                        "remove_sleeves",
                        "remove_beard",
                        "remove_head",
                        "hat",
                        "destroy",
                        "lendable",
                        "tradeable" -> configs[keyStr] = value.asBoolean

                        "alchemizable" -> configs[keyStr] = value.asBoolean

                        "weight" -> configs[keyStr] = value.asDouble

                        "equip_audio",
                        "point_price",
                        TOKKUL_PRICE,
                        ARCHERY_TICKET_PRICE,
                        CASTLE_WARS_TICKET_PRICE,
                        "ge_buy_limit",
                        "weapon_interface",
                        "attack_speed",
                        "equipment_slot",
                        "render_anim",
                        "turn90ccw_anim",
                        "turn90cw_anim",
                        "turn180_anim",
                        "run_anim",
                        "walk_anim",
                        "stand_anim",
                        "low_alchemy",
                        "high_alchemy",
                        "grand_exchange_price",
                        "id" -> configs[keyStr] = value.asInt

                        "examine",
                        "destroy_message",
                        "name" -> configs[keyStr] = valStr

                        else -> {
                        }
                    }
                }
                count++
            }
        }
        log(this::class.java, Log.INFO, "Parsed $count item configs.")
    }
}
