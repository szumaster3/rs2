package content.global.dialogue

import content.region.kandarin.west_ardougne.diary.dialogue.TownCrierDiaryDialogue
import content.global.plugins.item.books_and_scrolls.impl.GeneralRuleBook
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.world.GameWorld
import core.game.world.GameWorld.settings
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class TownCrierDialogue(player: Player? = null) : Dialogue(player) {

    private var randomLine: String = ""

    override fun open(vararg args: Any?): Boolean {
        val isMod = player?.rights == Rights.PLAYER_MODERATOR
        if (isMod) {
            npcl(FaceAnim.FRIENDLY, "Oh great, a P-mod! I know all about the great job you guys do. Want me to prove it?")
        } else {
            npcl(FaceAnim.HAPPY, "Hello citizen! Want to learn about Player Moderators or the rules?")
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val npcId = npc?.id ?: -1
        when (stage) {
            0 -> {
                options(
                        "Tell me about Player Moderators.",
                        "Tell me about the Rules of " + settings!!.name + ".",
                        "Can you give me a handy tip please?",
                        "Nothing thanks.",
                        *(if (npc?.location?.let { it.x in 2600..2700 && it.y in 3270..3350 } == true) arrayOf("Tell me about the Achievement Diary.") else emptyArray())
                    ).also { stage++ }

            }
            1 -> {
                when (buttonId) {
                    1 -> npc(FaceAnim.HALF_ASKING, "Of course. What would you like to know?").also { stage = 50 }
                    2 -> npc("At once. Take a look at my book here.").also { stage = 70 }
                    3 -> player(FaceAnim.HALF_ASKING, "Can you give me a handy tip please?").also { stage = 100 }
                    4 -> player("Nothing thanks.").also { stage = END_DIALOGUE }
                    5 -> if (npc?.location?.let { it.x in 2600..2700 && it.y in 3270..3350 } == true) openDialogue(player!!, TownCrierDiaryDialogue(), npc!!)
                }
            }
            2 -> options("Tell me about Player Moderators.", "Tell me about the Rules of " + settings!!.name + ".", "Can you give me a handy tip please?", "Nothing thanks.").also { stage++ }
            3 -> when (buttonId) {
                1 -> npc(FaceAnim.HALF_ASKING,"Of course. What would you like to know?").also { stage = 50 }
                2 -> npc("At once. Take a look at my book here.").also { stage = 70 }
                3 -> player(FaceAnim.HALF_ASKING,"Can you give me a handy tip please?").also { stage = 100 }
                4 -> player("Nothing thanks.").also { stage++ }
            }
            4 -> npc("Nice meeting you.").also { stage = END_DIALOGUE }
            50 -> options("What is a Player Moderator?", "What can Player Moderators do?", "How do I become a Player Moderator?", "What can Player Moderators not do?", "Nothing thanks.").also { stage++ }
            51 -> when (buttonId) {
                1 -> player("What is a Player Moderator?").also { stage = 150 }
                2 -> npc(FaceAnim.HALF_ASKING,"What can Player Moderators do?").also { stage = 160 }
                3 -> player(FaceAnim.HALF_ASKING,"How do I become a Player Moderator?").also { stage = 170 }
                4 -> player(FaceAnim.HALF_ASKING,"What can Player Moderators not do?").also { stage = 180 }
                5 -> player("Nothing thanks.").also { stage = 4 }
            }
            70 -> {
                end()
                visualize(npc, 6866, 1178)
                runTask(player!!, 3) {
                    GeneralRuleBook.openBook(player)
                }
            }
            100 -> {
                randomLine = if (player?.rights == Rights.PLAYER_MODERATOR)  randomGuideDialogue.random() else randomTipDialogue.random()
                npcl(FaceAnim.FRIENDLY, randomLine)
                stage = 153
            }
            150 -> npc("Player Moderators are normal players of the game, just", "like you. However, since they have shown themselves to be", "trustworthy and active reporters, they have been invited", "by Jagex to monitor the game and take appropriate").also { stage++ }
            151 -> npc("reward when they see rule breaking. You can spot a Player", "Moderator in game by looking at the chat screen - when a", "Player Moderator speaks, a silver crown appears to the", "left of their name. Remember, if there's no silver crown").also { stage++ }
            152 -> npc("there, they are not a Player Moderator! You can check", "out the website if you'd like more information.").also { stage++ }
            153 -> player("Thanks!").also { stage++ }
            154 -> npc("Is there anything else you'd like to know?").also { stage = 50 }
            160 -> npc("Player Moderators, or 'P-mods', have the ability to mute", "rule breakers and " + settings!!.name + " view their reports as a priority so", "that reward is taken as quickly as possible. P-Mods also", "have access to the Player Moderator Centre. Within the").also { stage++ }
            161 -> npc("Centre are tools to help them Moderate " + settings!!.name + ".", "These tools include dedicated forums, the Player", "Moderator Guidelines and the Player Moderator Code of", "Conduct.").also { stage = 153 }
            170 -> npc(settings!!.name + " picks players who spend their time and effort to", "help better the " + settings!!.name + " community. To increase your", "chances of becoming a Player Moderator:").also { stage++ }
            171 -> npc("Keep your account secure! This is very important, as a", "player with poor security will never be a P-Mod. Read our", "Security Tips for more information.").also { stage++ }
            172 -> npc("Play by the rules! The rules of " + settings!!.name + " are enforced", "for a reason, to make the game a fair and enjoyable", "environment for all.").also { stage++ }
            173 -> npc("Report accurately! When " + settings!!.name + " consider an account for", "review they look for quality, not quantity. Ensure your", "reports are of a high quality by following the report", "guidelines.").also { stage++ }
            174 -> npc("Be nice to each other! Treat others as you would", "want to be treated yourself. Respect your fellow player.", "More information can be found on the website.").also { stage = 153 }
            180 -> npc("P-Mods cannot ban your account - they can only report", "offences. " + settings!!.name + " then take reward based on the evidence", "received. If you lose your password or get scammed by", "another player, P_Mods cannot help you get your account").also { stage++ }
            181 -> npc("back. All they can do is recommend you to go to Player", "Support. They cannot retrieve any items you may have", "lost and they certainly do not receive any free items", "from " + settings!!.name + " for moderating the game. They are players").also { stage++ }
            182 -> npc("who give their all to help the community, out of the", "goodness of their hearts! P-mods do not work for " + settings!!.name + "", "and so cannot make you a Moderator, or recommend", "other accounts to become Moderators. If you wish yo").also { stage++ }
            183 -> npc("become a Moderator, feel free to ask me!").also { stage = 153 }
            184 -> end()
        }

        return true
    }

    private val randomGuideDialogue = listOf(
    "Add other Player Moderators to your friends list for quick advice and moral support in-game. It does help!",
    "Don't be tempted to report anything you didn't see, no matter how much you trust the player who is informing you.",
    "Don't eat something messy when on a first date - it won't look good!",
    "Don't forget to add the mute if needed, before selecting the report category. This cuts down on those duplicate reports for one player.",
    "Feeling harassed? Don't forget your ignore list can be especially useful if a player seems to be picking on you!",
    "The forums are the best place to keep abreast of any procedure changes that might not yet be included in the Player Mod guidelines!",
    "Have the P-Mod Oracle open in a separate window when playing ${GameWorld.settings?.name} - that way you'll always have the most up-to-date guidelines to hand!",
    "If a player isn't sure of the rules, send them to me! I'll be happy to remind them!",
    "If you get harassed by a player in game, don't take it personally - the player is reacting to your silver crown, not to you!",
    "If you see a player abusing a bug in game, there's no need to report the player, just submit a bug report via the front page of the ${GameWorld.settings?.name} website.",
    "If you're asked questions that may breach confidentiality, please refer the player to the Game Guide or the Player Moderator FAQ. If it's not there, they shouldn't know.",
    "If you've got a player asking you questions about P-mods, feel free to send them to me; I can answer questions about P-mods for you!",
    "An important part of being a P-mod is to act as an ambassador for the game. Keeping your cool even when faced with the rudest players goes a long way towards this!",
    "It's always better to report in secret. Telling a player that you are about to report them rarely makes you a friend, and can often lead to a player being offensive to you!",
    "Make sure you log out of the game and the forums whenever you leave your computer. Don't allow others to see confidential info!",
    "The P-mod forums are your friend! They're the best place to talk to and meet other P-mods and are absolutely packed with useful advice!",
    "Remember, if in doubt, report without! 48 hours is long time not to be able to speak, especially if you're innocent!",
    "Remember, if offensive language has been compeletely filtered out then there's no need to report or mute it!",
    "Think twice before applying that mute. Will it really prevent further damage from surrounding players?",
    "To avoid reporting the wrong player, add an offender to your friends list, switch your public chat to friends and then right-click to report from there. Easy!",
    "Use the P-mod team if you need to - they're there to help!",
    "Your j-mods are there to help you. If you ever need their advice, send them a query. They are always very happy to help. Don't be afraid to use them!",
    )

    private val randomTipDialogue = listOf(
    "Be careful when fighting wizards! Wearing heavy armour can lower your Magic resistance!",
    "Beware of players trying to lure you into the wilderness. Your items cannot be returned if you lose them!",
    "Did you know having a bank pin can help you secure your valuable items?",
    "Did you know most skills have right click 'Make-X' options to help you train faster?",
    "Did you know that at high levels of Runecrafting you get more than one rune per essence?",
    "Did you know that mithril equipment is very light?",
    "Did you know that you can wear a shield with a crossbow?",
    "Did you know you burn food less often than the range in Lumbridge castle than other ranges?",
    "Did you know? Superheat Item means you never fail to smelt ore!",
    "Did you know? You burn food less often on a range than on a fire!",
    "Don't use your ${GameWorld.settings?.name} password on other sites. Keep your account safe!",
    "Feeling harassed? Don't forget your ignore list can be especially useful if a player seems to be picking on you!",
    "If a player isn't sure of the rules, send them to me! I'll be happy to remind them!",
    "If the chat window is moving too quickly to report a player accurately, run to a quiet spot and review the chat at your leisure!",
    "If you see someone breaking the rules, report them!",
    "If you think someone knows your password - change it!",
    "If you're lost have no idea where to go, use the Home Teleport spell for free!",
    "Administrator will never email you asking for your log-in details.",
    "Make your recovery questions and answers hard to guess but easy to remember.",
    "Melee armour actually gives you disadvantages when using magic attacks. It may be better to take off your armour entirely!",
    "Never let anyone else use your account.",
    "Never question a penguin.",
    "Never tell your password to anyone, not even your best friend!",
    "Players can not trim armour. Don't fall for this popular scam!",
    "The squirrels! The squirrels are coming! Noooo, get them out of my head!",
    "Take time to check the second trade window carefully. Don't be scammed!",
    "There are no cheats in ${GameWorld.settings?.name}! Never visit websites promising otherwise!",
    )

    override fun getIds() = intArrayOf(
        NPCs.PMOD_TOWN_CRIER_VARROCK_6135,
        NPCs.PMOD_TOWN_CRIER_DRAYNOR_6136,
        NPCs.PMOD_TOWN_CRIER_FALADOR_6137,
        NPCs.PMOD_TOWN_CRIER_EAST_ARDOYNE_6138,
        NPCs.PMOD_TOWN_CRIER_SEERS_6139
    )
}