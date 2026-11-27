package core.game.system.communication;

import core.auth.UserAccountInfo;
import core.game.node.entity.player.Player;
import core.game.system.mysql.SQLTable;
import core.game.system.task.Pulse;
import core.game.world.GameWorld;
import core.game.world.repository.Repository;
import core.net.amsc.MSPacketRepository;
import core.net.amsc.WorldCommunicator;
import core.net.packet.PacketRepository;
import core.net.packet.out.Encoders530;
import core.net.packet.out.PacketContext.ContactContext;
import core.tools.Log;
import core.tools.StringUtils;
import core.worker.ManagementEvents;
import org.jetbrains.annotations.NotNull;
import proto.management.PrivateMessage;

import java.util.*;
import java.util.Map.Entry;

import static core.api.ContentAPIKt.log;
import static core.api.ContentAPIKt.setVarp;

/**
 * Holds communication information.
 * @author Emperor
 */
public final class CommunicationInfo {

    /**
     * The maximum list size.
     */
    public static final int MAX_LIST_SIZE = 200;

    /**
     * The clan ranks.
     */
    private Map<String, Contact> contacts = new HashMap<>();

    /**
     * The list of blocked players.
     */
    private final List<String> blocked = new ArrayList<>(20);
    // Session-only ignores are never included in SQL/account serialization.
    private final Set<String> temporaryBlocked = new LinkedHashSet<>();

    private static String ignoreKey(String name) {
        return name.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public boolean isIgnoring(String name) {
        String key = ignoreKey(name);
        return temporaryBlocked.contains(key) || blocked.stream().anyMatch(n -> ignoreKey(n).equals(key));
    }

    public void clearTemporaryIgnores() {
        temporaryBlocked.clear();
    }

    public Set<String> getTemporaryBlocked() {
        return Collections.unmodifiableSet(temporaryBlocked);
    }

    private static void syncIgnores(Player player) {
        PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(player, ContactContext.IGNORE_LIST_TYPE));
    }

    /**
     * The player's clan name.
     */
    private String clanName = "";

    /**
     * The current clan this player is in.
     */
    private String currentClan = "";

    /**
     * The rank required for joining.
     */
    private ClanRank joinRequirement = ClanRank.ANY_FRIEND;

    /**
     * The rank required for messaging.
     */
    private ClanRank messageRequirement = ClanRank.ANYONE;

    /**
     * The rank required for kicking members.
     */
    private ClanRank kickRequirement = ClanRank.ONLY_ME;

    /**
     * The rank required for loot-share.
     */
    private ClanRank lootRequirement = ClanRank.NO_ONE;

    /**
     * If lootshare is enabled.
     */
    private boolean lootShare;

    /**
     * The current clan joined.
     */
    private ClanRepository clan = null;

    /**
     * The loot-share pulse.
     */
    private Pulse lootSharePulse;

    /**
     * Constructs a new {@code CommunicationInfo} {@code Object}.
     */
    public CommunicationInfo() {
        /*
         * empty.
         */
    }

    /**
     * Saves the communication info.
     * @param table The SQL table to update.
     */
    public void save(SQLTable table) {
        String contacts = "";
        String blocked = "";
        for (int i = 0; i < this.blocked.size(); i++) {
            blocked += (i == 0 ? "" : ",") + this.blocked.get(i);
        }
        int count = 0;
        for (Entry<String, Contact> entry : this.contacts.entrySet()) {
            contacts += "{" + entry.getKey() + "," + entry.getValue().rank.ordinal() + "}" + (count == this.contacts.size() - 1 ? "" : "~");
            count++;
        }
        table.getColumn("blocked").updateValue(blocked);
        table.getColumn("contacts").updateValue(contacts);
        table.getColumn("clanName").updateValue(clanName);
        table.getColumn("currentClan").updateValue(currentClan);
        table.getColumn("clanReqs").updateValue(joinRequirement.ordinal() + "," + messageRequirement.ordinal() + "," + kickRequirement.ordinal() + "," + lootRequirement.ordinal());
    }

    /**
     * Parses the communication info from the database.
     * @param table The sql table to parse.
     */
    public void parse(SQLTable table) {
        String[] tokens = null;
        if (table.getColumn("contacts").getValue() != null ) {
            String contacts = (String) table.getColumn("contacts").getValue();
            if (!contacts.isEmpty()) {
                String[] datas = contacts.split("~");
                Contact contact = null;
                for (String d : datas) {
                    tokens = d.replace("{", "").replace("}", "").split(",");
                    if (tokens.length == 0) {
                        continue;
                    }
                    contact = new Contact(tokens[0]);
                    contact.rank = ClanRank.values()[Integer.valueOf(tokens[1])];
                    this.contacts.put(tokens[0], contact);
                }
            }
        }
        if (table.getColumn("blocked").getValue() != null ) {
            String blocked = (String) table.getColumn("blocked").getValue();
            if (!blocked.isEmpty()) {
                tokens = blocked.split(",");
                for (String name : tokens) {
                    this.blocked.add(name);
                }
            }
        }
        clanName = ((String) table.getColumn("clanName").getValue());
        currentClan = (String) table.getColumn("currentClan").getValue();
        String clanReqs = (String) table.getColumn("clanReqs").getValue();
        if (clanReqs == "") {
            return;
        }
        tokens = clanReqs.split(",");
        ClanRank rank = null;
        int ordinal = 0;
        for (int i = 0; i < tokens.length; i++) {
            ordinal = Integer.parseInt(tokens[i]);
            if (ordinal < 0 || ordinal > ClanRank.values().length -1) {
                continue;
            }
            rank = ClanRank.values()[ordinal];
            switch (i) {
                case 0:
                    joinRequirement = rank;
                    break;
                case 1:
                    messageRequirement = rank;
                    break;
                case 2:
                    if (ordinal < 3 || ordinal > 8) {
                        break;
                    }
                    kickRequirement = rank;
                    break;
                case 3:
                    lootRequirement = rank;
                    break;
            }
        }
    }

    /**
     * Toggles the loot-share.
     */
    public void toggleLootshare(final Player player) {
        if (lootShare) {
            lootShare = false;
            setVarp(player, 1083, 0);
        } else if (lootSharePulse != null) {
            lootSharePulse.stop();
            lootSharePulse = null;
            setVarp(player, 1083, 0);
        } else if (!lootShare) {
            setVarp(player, 1083, 2);
            lootSharePulse = new Pulse(GameWorld.getSettings().isDevMode() ? 5 : 200, player) {
                @Override
                public boolean pulse() {
                    lootShare = true;
                    lootSharePulse = null;
                    setVarp(player, 1083, 1);
                    return true;
                }
            };
            GameWorld.getPulser().submit(lootSharePulse);
        }
    }

    /**
     * Sends a message to the target.
     * @param player The player sending the message.
     * @param target The target.
     * @param message The message to send.
     */
    public static void sendMessage(Player player, String target, String message) {
        PrivateMessage.Builder builder = PrivateMessage.newBuilder();
        builder.setSender(player.getName());
        builder.setRank(player.getDetails().getRights().ordinal());
        builder.setMessage(message);
        builder.setReceiver(target);
        ManagementEvents.publish(builder.build());
    }

    /**
     * Adds a contact.
     * @param contact The contact to add.
     */
    public static void add(Player player, String contact) {
        CommunicationInfo info = player.getDetails().getCommunication();
        if (contact.isEmpty()) {
            player.sendMessage("Unable to find a player by that name. Please try again.");
            return;
        }
        if (WorldCommunicator.isEnabled()) {
            MSPacketRepository.sendContactUpdate(player.getName(), contact, false, false, null);
            return;
        }
        if (info.contacts.size() >= MAX_LIST_SIZE) {
            player.getPacketDispatch().sendMessage("Your friend list is full.");
            return;
        }
        if (info.contacts.containsKey(contact)) {
            player.getPacketDispatch().sendMessage(StringUtils.formatDisplayName(contact) + " is already on your friend list.");
            return;
        }
        ClanRepository clan = ClanRepository.get(player.getName(), false);
        if (clan != null) {
            clan.rank(contact, ClanRank.ANY_FRIEND);
        }
        info.contacts.put(contact, new Contact(contact));
        Player target = Repository.getPlayerByName(contact);
        if (target != null) {
            if (showActive(player, target)) {
                PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(player, contact, GameWorld.getSettings().getWorldId()));
            }
            if (player.getSettings().getPrivateChatSetting() == 1 && showActive(target, player)) {
                PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(target, player.getName(), GameWorld.getSettings().getWorldId()));
            }
        }
    }

    /**
     * Removes a contact.
     * @param contact The contact to remove.
     * @param block If the contact should be removed from the block list.
     */
    public static void remove(Player player, String contact, boolean block) {
        if (contact.isEmpty()) {
            player.sendMessage("Unable to find a player by that name. Please try again.");
            return;
        }
        if (WorldCommunicator.isEnabled()) {
            MSPacketRepository.sendContactUpdate(player.getName(), contact, true, block, null);
            return;
        }
        CommunicationInfo info = player.getDetails().getCommunication();
        if (block) {
            String key = ignoreKey(contact);
            info.blocked.removeIf(name -> ignoreKey(name).equals(key));
            info.temporaryBlocked.remove(key);
            syncIgnores(player);
            Player target = Repository.getPlayerByName(contact);
            if (target != null && hasContact(target, player.getName())) {
                int worldId = 0;
                if (showActive(target, player)) {
                    worldId = GameWorld.getSettings().getWorldId();
                }
                PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(target, player.getName(), worldId));
            }
        } else {
            info.contacts.remove(contact);
            ClanRepository clan = ClanRepository.get(player.getName(), false);
            if (clan != null) {
                clan.rank(contact, ClanRank.ANYONE);
            }
            if (player.getSettings().getPrivateChatSetting() == 1) {
                Player target = Repository.getPlayerByName(contact);
                if (target != null) {
                    PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(target, player.getName(), 0));
                }
            }
        }
    }

    /**
     * Adds a blocked contact.
     * @param contact The contact to block.
     */
    public static void block(Player player, String contact) {
        block(player, contact, false);
    }

    public static void block(Player player, String contact, boolean temporary) {
        final String key = ignoreKey(contact);
        if (StringUtils.invalidAccountName(key)) {
            player.sendMessage("Unable to find a player by that name. Please try again.");
            return;
        }
        if (WorldCommunicator.isEnabled()) {
            if (!temporary) MSPacketRepository.sendContactUpdate(player.getName(), contact, false, true, null);
            return;
        }
        CommunicationInfo info = player.getDetails().getCommunication();
        if (info.blocked.stream().filter(n -> !n.isEmpty()).count() + info.temporaryBlocked.size() >= 100) {
            player.getPacketDispatch().sendMessage("Your ignore list is full.");
            return;
        }
        if (info.isIgnoring(key)) {
            player.getPacketDispatch().sendMessage(StringUtils.formatDisplayName(contact) + " is already on your ignore list.");
            return;
        }
        if (key.equals(ignoreKey(player.getName())) || info.contacts.keySet().stream().anyMatch(n -> ignoreKey(n).equals(key))) return;
        if (temporary) info.temporaryBlocked.add(key);
        else info.blocked.add(key);
        syncIgnores(player);
        Player target = Repository.getPlayerByName(key);
        if (target != null && hasContact(target, player.getName())) {
            PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(target, player.getName(), 0));
        }
    }

    /**
     * Updates the clan rank of a certain contact.
     * @param contact The contact.
     * @param clanRank The clan rank to set.
     */
    public static void updateClanRank(Player player, String contact, ClanRank clanRank) {
        if (contact.isEmpty()) {
            player.sendMessage("Unable to find a player by that name. Please try again.");
            return;
        }
        CommunicationInfo info = player.getDetails().getCommunication();
        Contact c = info.contacts.get(contact);
        if (c == null) {
            log(CommunicationInfo.class, Log.ERR, "Could not find contact " + contact + " to update clan rank!");
            return;
        }
        c.rank = clanRank;
        ClanRepository clan = ClanRepository.get(player.getName());
        if (clan != null) {
            clan.rank(contact, clanRank);
        }
        int worldId = 0;
        if (CommunicationInfo.showActive(player, contact)) {
            worldId = c.worldId;
        }
        PacketRepository.send(Encoders530.CONTACT_PACKETS, new ContactContext(player, contact, worldId));
    }

    /**
     * Checks if the player has the contact added.
     * @param player The player.
     * @param contact The contact.
     * @return {@code True} if so.
     */
    public static boolean hasContact(Player player, String contact) {
        return player.getDetails().getCommunication().contacts.containsKey(contact);
    }

    /**
     * Checks if the target should be shown as online.
     * @param name The target's name.
     * @return {@code True} if so.
     */
    public static boolean showActive(Player player, String name) {
        Player p = Repository.getPlayerByName(name);
        if (p != null) {
            return showActive(player, p);
        }
        return false;
    }

    /**
     * Checks if the target should be shown as online.
     * @param target The target.
     * @return {@True} if so.
     */
    public static boolean showActive(Player player, Player target) {
        if (target.getName().equals(player.getName())) {
            return false;
        }
        if (target.getCommunication().isIgnoring(player.getName())) {
            return false;
        }
        switch (target.getSettings().getPrivateChatSetting()) {
            case 1:
                if (!hasContact(target, player.getName())) {
                    return false;
                }
                return true;
            case 2:
                return false;
        }
        return true;
    }

    /**
     * Gets the contacts value.
     * @return The contacts.
     */
    public Map<String, Contact> getContacts() {
        return contacts;
    }

    /**
     * Gets the blocked value.
     * @return The blocked.
     */
    public List<String> getBlocked() {
        return blocked;
    }

    /**
     * Gets the clanName value.
     * @return The clanName.
     */
    public String getClanName() {
        return clanName;
    }

    /**
     * Sets the clanName value.
     * @param clanName The clanName to set.
     */
    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    /**
     * Gets the currentClan value.
     * @return The currentClan.
     */
    public String getCurrentClan() {
        return currentClan == null ? "" : currentClan;
    }

    /**
     * Sets the currentClan value.
     * @param currentClan The currentClan to set.
     */
    @Deprecated
    public void setCurrentClan(String currentClan) {
        this.currentClan = currentClan;
    }

    /**
     * Gets the joinRequirement value.
     * @return The joinRequirement.
     */
    public ClanRank getJoinRequirement() {
        return joinRequirement;
    }

    /**
     * Sets the joinRequirement value.
     * @param joinRequirement The joinRequirement to set.
     */
    public void setJoinRequirement(ClanRank joinRequirement) {
        this.joinRequirement = joinRequirement;
    }

    /**
     * Gets the messageRequirement value.
     * @return The messageRequirement.
     */
    public ClanRank getMessageRequirement() {
        return messageRequirement;
    }

    /**
     * Sets the messageRequirement value.
     * @param messageRequirement The messageRequirement to set.
     */
    public void setMessageRequirement(ClanRank messageRequirement) {
        this.messageRequirement = messageRequirement;
    }

    /**
     * Gets the kickRequirement value.
     * @return The kickRequirement.
     */
    public ClanRank getKickRequirement() {
        return kickRequirement;
    }

    /**
     * Sets the kickRequirement value.
     * @param kickRequirement The kickRequirement to set.
     */
    public void setKickRequirement(ClanRank kickRequirement) {
        this.kickRequirement = kickRequirement;
    }

    /**
     * Gets the lootRequirement value.
     * @return The lootRequirement.
     */
    public ClanRank getLootRequirement() {
        return lootRequirement;
    }

    /**
     * Sets the lootRequirement value.
     * @param lootRequirement The lootRequirement to set.
     */
    public void setLootRequirement(ClanRank lootRequirement) {
        this.lootRequirement = lootRequirement;
    }

    /**
     * Gets the clan.
     * @return the clan
     */
    public ClanRepository getClan() {
        return clan;
    }

    /**
     * Sets the clan.
     * @param clan the clan to set.
     */
    public void setClan(ClanRepository clan) {
        this.clan = clan;
        this.currentClan = clan == null ? null : clan.getOwner();
    }

    /**
     * Gets the lootShare.
     * @return the lootShare
     */
    public boolean isLootShare() {
        return lootShare;
    }

    /**
     * Sets the lootShare.
     * @param lootShare the lootShare to set.
     */
    public void setLootShare(boolean lootShare) {
        this.lootShare = lootShare;
    }

    public void parse(@NotNull UserAccountInfo accountInfo) {
        blocked.addAll(Arrays.asList(accountInfo.getBlocked().split(",")));
        clanName = accountInfo.getClanName();
        currentClan = accountInfo.getCurrentClan();

        String clanReqs = accountInfo.getClanReqs();
        ClanRank[] reqs = parseClanRequirements(clanReqs);
        joinRequirement = reqs[0];
        messageRequirement = reqs[1];
        kickRequirement = reqs[2];
        lootRequirement = reqs[3];

        String contacts = accountInfo.getContacts();
        this.contacts = parseContacts(contacts);
    }

    public static ClanRank[] parseClanRequirements(String clanReqs) {
        ClanRank[] requirements = new ClanRank[4];
        String[] tokens = clanReqs.split(",");
        ClanRank rank;
        int ordinal;
        for (int i = 0; i < tokens.length; i++) {
            ordinal = Integer.parseInt(tokens[i]);
            if (ordinal < 0 || ordinal > ClanRank.values().length -1) {
                continue;
            }
            rank = ClanRank.values()[ordinal];
            switch (i) {
                case 0:
                    requirements[0] = rank;
                    break;
                case 1:
                    requirements[1] = rank;
                    break;
                case 2:
                    if (ordinal < 3 || ordinal > 8) {
                        requirements[2] = ClanRank.NO_ONE;
                        break;
                    }
                    requirements[2] = rank;
                    break;
                case 3:
                    requirements[3] = rank;
                    break;
            }
        }

        return requirements;
    }

    public static HashMap<String, Contact> parseContacts(String contacts) {
        HashMap<String, Contact> theseContacts = new HashMap<>();
        String[] tokens;
        if (!contacts.isEmpty()) {
            String[] datas = contacts.split("~");
            Contact contact;
            for (String d : datas) {
                tokens = d.replace("{", "").replace("}", "").split(",");
                if (tokens.length == 0) {
                    continue;
                }
                contact = new Contact(tokens[0]);
                contact.rank = ClanRank.values()[Integer.parseInt(tokens[1])];
                theseContacts.put(tokens[0], contact);
            }
        }

        return theseContacts;
    }

    /**
     * Gets contact string.
     *
     * @return the contact string
     */
    public String getContactString() {
        StringBuilder sb = new StringBuilder();

        int index = 0;
        for (Contact contact : contacts.values()) {
            sb.append("{");
            sb.append(contact.username);
            sb.append(",");
            sb.append(contact.rank.ordinal());
            sb.append("}");
            if (index++ < contacts.size() - 1) sb.append("~");
        }

        return sb.toString();
    }

    /**
     * Gets blocked string.
     *
     * @return the blocked string
     */
    public String getBlockedString() {
        StringBuilder sb = new StringBuilder();

        int index = 0;
        for (String block : blocked) {
            sb.append(block);
            if (index++ < blocked.size() - 1) sb.append(",");
        }

        return sb.toString();
    }

    /**
     * Gets clan req string.
     *
     * @return the clan req string
     */
    public String getClanReqString() {
        return
            joinRequirement.ordinal()
                + "," + messageRequirement.ordinal()
                + "," + kickRequirement.ordinal()
                + "," + lootRequirement.ordinal();
    }
}
