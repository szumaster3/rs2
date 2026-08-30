package core.game.system.command.sets

import core.game.system.command.Privilege
import core.plugin.Initializable

@Initializable
class MinigameCommandSet : CommandSet(Privilege.ADMIN) {

    override fun defineCommands() {
        /*
         * Command for adding 500 Pest Control points to the player.
         */

        define(
            name = "addpcpoints",
            privilege = Privilege.ADMIN,
            usage = "::addpcpoints",
            description = "Adds 500 Pest Control points.",
        ) { player, _ ->
            player.savedData.activityData.pestControlPoints += 500
            notify(player, "You have been given 500 Pest Control points.")
        }
    }
}
