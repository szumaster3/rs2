package core.game.system.command.sets

import core.api.Commands
import core.cache.def.impl.ComponentType
import core.cache.def.impl.InterfaceComponent
import core.game.system.command.Privilege

class IfCommandSet : Commands {

    override fun defineCommands() {

        /*
         * Command for listing all known triggers
         * for the given interface.
         */

        define(
            name = "iftriggers",
            privilege = Privilege.ADMIN,
            usage = "::iftriggers <lt>id<gt>",
            description = "Lists all known triggers for the given interface.",
        ) { player, args ->
            val id = args.getOrNull(1)?.toIntOrNull() ?: -1
            if (id == -1) reject(player, "Must supply a valid interface ID!")

            val def = InterfaceComponent.forId(id)
            notify(player, logToConsole = true, message = "Triggers for $def:")

            for (child in def!!.children!!) {
                if (child!!.scripts == null) continue
                if (child.triggers == null) continue

                if (child.scripts!!.onVarTransmit != null) {
                    notify(player, logToConsole = true, message = "$child [VARP]:")
                    notify(player, logToConsole = true, message = "  Transmit ${child.triggers!!.varpTriggers!!.joinToString(",")} triggers script ${child.scripts!!.onVarTransmit!!.id}",)
                    notify(player, logToConsole = true, message = "  Default script args: ${child.scripts!!.onVarTransmit!!.args?.joinToString(",")}")
                }
                if (child.scripts!!.onVarcTransmit != null) {
                    notify(player, logToConsole = true, message = "$child [VARC]:")
                    notify(player, logToConsole = true, message = "  Transmit ${child.triggers!!.varcTriggers!!.joinToString(",",)} triggers script ${child.scripts!!.onVarcTransmit!!.id}",)
                    notify(player, logToConsole = true, message = "  Default script args: ${child.scripts!!.onVarcTransmit!!.args?.joinToString(",")}",)
                }
            }
        }

        /*
         * Command for printing all text values and their child index
         * on an interface.
         */

        define(
            name = "listiftext",
            privilege = Privilege.ADMIN,
            usage = "::listiftext <lt>id<gt>",
            description = "Prints all text values and their child index on an interface.",
        ) { player, args ->
            val id = args.getOrNull(1)?.toIntOrNull() ?: -1
            if (id == -1) reject(player, "Must supply a valid interface ID!")

            val def = InterfaceComponent.forId(id)
            notify(player, logToConsole = true, message = "Text for $def:")

            for (child in def!!.children!!) {
                if (child!!.type != ComponentType.TEXT) continue
                notify(player, logToConsole = true, message = "$child - ${child.text} - ${child.activeText}")
            }
        }

        /*
         * Command for printing all default model values
         * and their child index on an interface.
         */

        define(
            name = "listifmodels",
            privilege = Privilege.ADMIN,
            usage = "::listifmodels <lt>id<gt>",
            description = "Prints all default model values and their child index.",
        ) { player, args ->
            val id = args.getOrNull(1)?.toIntOrNull() ?: -1
            if (id == -1) reject(player, "Must supply a valid interface ID!")

            val def = InterfaceComponent.forId(id)
            notify(player, logToConsole = true, message = "Models for $def:")

            for (child in def!!.children!!) {
                if (child!!.type != ComponentType.MODEL) continue
                notify(
                    player,
                    logToConsole = true,
                    message = "$child - ${child.modelId}/${child.activeModelId} - Anim: ${child.modelSeqId}/${child.activeModelAnimId}",
                )
            }
        }
    }
}
