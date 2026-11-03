package core.game.dialogue;

import content.data.GameAttributes;
import content.region.other.tutorial_island.plugin.TutorialStage;
import core.cache.def.impl.ItemDefinition;
import core.cache.def.impl.NPCDefinition;
import core.game.component.Component;
import core.game.event.DialogueCloseEvent;
import core.game.event.DialogueOpenEvent;
import core.game.event.DialogueOptionSelectionEvent;
import core.game.node.entity.Entity;
import core.game.node.entity.npc.NPC;
import core.game.node.entity.player.Player;
import core.game.node.item.Item;
import core.game.system.config.ItemConfigParser;
import core.game.system.task.Pulse;
import core.game.world.GameWorld;
import core.plugin.PluginManifest;
import core.plugin.PluginType;
import shared.consts.Components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.tools.DialogueHelperKt.END_DIALOGUE;

/**
 * Handles the dialogues.
 *
 * @author Emperor
 */
@PluginManifest(type = PluginType.DIALOGUE)
public final class DialogueInterpreter {
    private static final Map<Integer, Dialogue> PLUGINS = new HashMap<>();
    static final Pattern GENDERED_SUBSTITUTION = Pattern.compile("@g\\[([^,]*),([^\\]]*)\\]");

    public ArrayList<Topic<?>> activeTopics = new ArrayList<>();
    private final List<DialogueAction> actions = new ArrayList<>(10);
    private final Player player;
    private Dialogue dialogue;
    private int key;

    public DialogueInterpreter(Player player) {
        this.player = player;
    }

    public void setDialogue(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    public boolean open(String dialogueType, Object... args) {
        return open(getDialogueKey(dialogueType), args);
    }

    public boolean open(int dialogueKey, Object... args) {
        key = dialogueKey;

        if (args.length > 0 && args[0] instanceof NPC) {
            NPC npc = (NPC) args[0];
            npc.setDialoguePlayer(player);
            try {
                npc.getWalkingQueue().reset();
                npc.getPulseManager().clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length == 0) {
            args = new Object[]{dialogueKey};
        }

        Dialogue plugin = PLUGINS.get(dialogueKey);
        if (plugin == null) return false;

        if (player.isDebug()) {
            player.sendMessage("dialogue opening - " + plugin.getClass().getSimpleName() + ", key=" + dialogueKey);
        }

        dialogue = plugin.newInstance(player);
        if (dialogue == null || !dialogue.open(args)) {
            dialogue = null;
            return false;
        }

        player.dispatch(new DialogueOpenEvent(dialogue));
        return true;
    }

    public void open(DialogueFile file, Object... args) {
        dialogue = new EmptyDialogue(player, file);
        dialogue.open(args);
    }

    public void handle(int componentId, int buttonId) {
        player.setAttribute("chatbox-buttonid", buttonId);
        Dialogue current = dialogue;
        if (current == null) return;

        DialogueFile file = current.file;
        if ((file != null && file.getCurrentStage() == END_DIALOGUE) || current.stage == END_DIALOGUE) {
            player.getInterfaceManager().closeChatbox();
            player.getInterfaceManager().openChatbox(Components.CHATDEFAULT_137);
            current.finished = true;
            close();
            return;
        }

        if (!activeTopics.isEmpty() && buttonId >= 2) {
            Topic<?> topic = activeTopics.get(buttonId - 2);
            if (!topic.getSkipPlayer()) sendDialogues(player, topic.getExpr(), topic.getText());

            Object destination = topic.getToStage();
            if (destination instanceof DialogueFile) {
                current.loadFile((DialogueFile) destination);
            } else if (destination instanceof Integer) {
                int stage = (Integer) destination;
                if (file == null) current.stage = stage;
                else file.setStage(stage);
            }
            activeTopics.clear();
            if (topic.getSkipPlayer()) handle(componentId, buttonId);
            return;
        }

        int option = buttonId - 1;
        if (file != null) {
            player.dispatch(new DialogueOptionSelectionEvent(file, file.getStage(), option));
            file.handle(componentId, option);
        } else {
            player.dispatch(new DialogueOptionSelectionEvent(current, current.stage, option));
            current.handle(componentId, option);
        }
    }

    public boolean close() {
        if (dialogue != null) {
            actions.clear();
            Component chatbox = player.getInterfaceManager().chatbox;
            if (chatbox != null && chatbox.getCloseEvent() != null) return true;

            Dialogue closing = dialogue;
            dialogue = null;
            closing.close();
            player.dispatch(new DialogueCloseEvent(closing));
        }

        boolean tutorialComplete = player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false);
        int tutorialStage = player.getAttribute(GameAttributes.TUTORIAL_STAGE, -1);
        if (tutorialComplete || tutorialStage >= 72) {
            if (player.getAttribute("runscript", null) != null) {
                player.removeAttribute("runscript");
                player.removeAttribute("input-type");
                player.removeAttribute("parseamount");
                player.getPacketDispatch().sendRunScript(101, "");
            }
        } else if (!player.isArtificial()) {
            TutorialStage.load(player, tutorialStage, false);
        }

        activeTopics.clear();
        return dialogue == null;
    }

    public static void add(int id, Dialogue plugin) {
        if (PLUGINS.containsKey(id)) {
            throw new IllegalArgumentException("dialogue " + (id & 0xFFFF) + " is already in use - [old="
                    + PLUGINS.get(id).getClass().getSimpleName() + ", new=" + plugin.getClass().getSimpleName() + "]!");
        }
        PLUGINS.put(id, plugin);
    }

    public Component sendDialogue(String... messages) {
        if (messages.length < 1 || messages.length > 4) return null;
        int interfaceId = 209 + messages.length;
        for (int i = 0; i < messages.length; i++) {
            player.getPacketDispatch().sendString(messages[i], interfaceId, i + 1);
        }
        player.getInterfaceManager().openChatbox(interfaceId);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendDialogue(int ticks, String... messages) {
        GameWorld.getPulser().submit(new Pulse(ticks, player) {
            @Override
            public boolean pulse() {
                sendDialogue(messages);
                return true;
            }
        });
        return player.getInterfaceManager().chatbox;
    }

    public Component sendDialogues(String... messages) {
        if (messages.length < 1 || messages.length > 5) return null;
        for (int i = 0; i < messages.length; i++) {
            player.getPacketDispatch().sendString(messages[i], Components.MESSAGE5_214, i + 1);
        }
        player.getInterfaceManager().openChatbox(Components.MESSAGE5_214);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendPlainMessage(boolean hideContinue, String... messages) {
        sendDialogue(messages);
        player.getPacketDispatch().sendInterfaceConfig(
                player.getInterfaceManager().chatbox.id, messages.length + 1, hideContinue);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendDestroyItem(int id, String message) {
        if (message == null) {
            message = ItemDefinition.forId(id).getConfiguration(
                    ItemConfigParser.DESTROY_MESSAGE, "Are you sure you want to destroy this object?");
        }
        player.getInterfaceManager().openChatbox(Components.CONFIRM_DESTROY_94);
        player.getPacketDispatch().sendString(message, Components.CONFIRM_DESTROY_94, 7);
        player.getPacketDispatch().sendString(ItemDefinition.forId(id).getName(), Components.CONFIRM_DESTROY_94, 8);
        player.getPacketDispatch().sendItemOnInterface(id, 1, Components.CONFIRM_DESTROY_94, 9);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendPlaneMessageWithBlueTitle(String title, String... messages) {
        player.getPacketDispatch().sendString(title, Components.TUTORIAL_TEXT_372, 0);
        for (int i = 0; i < messages.length; i++) {
            player.getPacketDispatch().sendString(messages[i], Components.TUTORIAL_TEXT_372, i + 1);
        }
        player.getInterfaceManager().openChatbox(Components.TUTORIAL_TEXT_372);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendScrollMessageWithBlueTitle(String title, String... messages) {
        for (int i = 0; i < 11; i++) {
            player.getPacketDispatch().sendString(" ", Components.TUTORIAL_TEXT2_421, i + 2);
        }
        player.getPacketDispatch().sendString(title, Components.TUTORIAL_TEXT2_421, 1);
        for (int i = 0; i < messages.length; i++) {
            player.getPacketDispatch().sendString(messages[i], Components.TUTORIAL_TEXT2_421, i + 2);
        }
        player.getInterfaceManager().openChatbox(Components.TUTORIAL_TEXT2_421);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendItemMessage(int itemId, String... messages) {
        if (messages == null || messages.length == 0) {
            throw new IllegalArgumentException("Messages cannot be null or empty.");
        }
        String combinedMessage = String.join("<br>", messages);
        player.getInterfaceManager().openChatbox(Components.OBJBOX_519);
        player.getPacketDispatch().sendString(combinedMessage, Components.OBJBOX_519, 1);
        if (ItemDefinition.forId(itemId) == null) throw new IllegalArgumentException("Invalid itemId: " + itemId);
        player.getPacketDispatch().sendItemOnInterface(itemId, 1, Components.OBJBOX_519, 0);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendItemMessage(Item item, String... messages) {
        return sendItemMessage(item.getId(), messages);
    }

    public Component sendDoubleItemMessage(int first, int second, String message) {
        player.getInterfaceManager().openChatbox(Components.DOUBLEOBJBOX_131);
        player.getPacketDispatch().sendString(message, Components.DOUBLEOBJBOX_131, 1);
        player.getPacketDispatch().sendItemOnInterface(first, 1, Components.DOUBLEOBJBOX_131, 0);
        player.getPacketDispatch().sendItemOnInterface(second, 1, Components.DOUBLEOBJBOX_131, 2);
        return player.getInterfaceManager().chatbox;
    }

    public Component sendDoubleItemMessage(Item first, Item second, String message) {
        return sendDoubleItemMessage(first.getId(), second.getId(), message);
    }

    public Component sendDialogues(Entity entity, FaceAnim expression, String... messages) {
        return sendDialogues(entity, expression == null ? -1 : expression.getAnimationId(), messages);
    }

    public Component sendDialogues(Entity entity, int expression, String... messages) {
        int npcId = entity instanceof Player ? -1 : ((NPC) entity).getShownNPC(player).getId();
        return sendDialogues(npcId, expression, false, messages);
    }

    public Component sendDialogues(int npcId, FaceAnim expression, boolean hide, String... messages) {
        return sendDialogues(npcId, expression == null ? -1 : expression.getAnimationId(), hide, messages);
    }

    public Component sendDialogues(Entity entity, FaceAnim expression, boolean hide, String... messages) {
        return sendDialogues(entity.getId(), expression == null ? -1 : expression.getAnimationId(), hide, messages);
    }

    public Component sendDialogues(Entity entity, int expression, boolean hide, String... messages) {
        return sendDialogues(entity.getId(), expression, hide, messages);
    }

    public Component sendDialogues(int npcId, FaceAnim expression, String... messages) {
        return sendDialogues(npcId, expression == null ? -1 : expression.getAnimationId(), false, messages);
    }

    public Component sendDialogues(int ticks, int npcId, FaceAnim expression, String... messages) {
        return sendDialogues(ticks, npcId, expression == null ? -1 : expression.getAnimationId(), false, messages);
    }

    public static String doSubstitutions(Player player, String msg) {
        String input = msg.replace("@name", player.getUsername());
        Matcher matcher = GENDERED_SUBSTITUTION.matcher(input);
        if (!matcher.find()) return input;

        StringBuffer result = new StringBuffer(input.length());
        int group = player.isMale() ? 1 : 2;
        do {
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(group)));
        } while (matcher.find());
        matcher.appendTail(result);
        return result.toString();
    }

    public Component sendDialogues(int npcId, int expression, String... messages) {
        return sendDialogues(npcId, expression, false, messages);
    }

    public Component sendDialogues(int npcId, int expression, boolean hide, String... messages) {
        if (messages.length < 1 || messages.length > 4) {
            System.err.println("Invalid amount of messages: " + messages.length);
            return null;
        }

        boolean npc = npcId > -1;
        int interfaceId = (npc ? 240 : 63) + messages.length + (hide ? 4 : 0);
        if (expression == -1) expression = FaceAnim.HALF_GUILTY.getAnimationId();

        player.getInterfaceManager().openChatbox(interfaceId);
        player.getPacketDispatch().sendAnimationInterface(expression, interfaceId, 2);
        if (npc) {
            player.getPacketDispatch().sendNpcOnInterface(npcId, interfaceId, 2);
            player.getPacketDispatch().sendString(NPCDefinition.forId(npcId).getName(), interfaceId, 3);
        } else {
            player.getPacketDispatch().sendPlayerOnInterface(interfaceId, 2);
            player.getPacketDispatch().sendString(player.getUsername(), interfaceId, 3);
        }
        for (int i = 0; i < messages.length; i++) {
            player.getPacketDispatch().sendString(doSubstitutions(player, messages[i]), interfaceId, i + 4);
        }
        return player.getInterfaceManager().chatbox;
    }

    public Component sendDialogues(int ticks, int npcId, int expression, boolean hide, String... messages) {
        GameWorld.getPulser().submit(new Pulse(ticks, player) {
            @Override
            public boolean pulse() {
                sendDialogues(npcId, expression, hide, messages);
                return true;
            }
        });
        return player.getInterfaceManager().chatbox;
    }

    public Component sendOptions(Object title, String... options) {
        if (options.length < 2 || options.length > 5) return null;
        int interfaceId = 224 + 2 * options.length;
        player.getInterfaceManager().openChatbox(interfaceId);
        if (title != null) player.getPacketDispatch().sendString(title.toString(), interfaceId, 1);
        for (int i = 0; i < options.length; i++) {
            player.getPacketDispatch().sendString(options[i].toString(), interfaceId, i + 2);
        }
        return player.getInterfaceManager().chatbox;
    }

    public void sendInput(boolean string, Object... objects) {
        player.getPacketDispatch().sendRunScript(string ? 109 : 108, "s", objects);
    }

    public void sendLongInput(Object... objects) {
        player.getPacketDispatch().sendRunScript(110, "s", objects);
    }

    public void sendMessageInput(String receiver) {
        player.getPacketDispatch().sendRunScript(107, "s", receiver);
    }

    public static boolean contains(int id) {
        return PLUGINS.containsKey(id);
    }

    public Dialogue getDialogue() {
        return dialogue;
    }

    public static int getDialogueKey(String name) {
        return 1 << 16 | name.hashCode();
    }

    public void addAction(DialogueAction action) {
        actions.add(action);
    }

    public List<DialogueAction> getActions() {
        return actions;
    }
}
