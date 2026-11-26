package core.net.registry;

import core.ServerConstants;
import core.auth.UserAccountInfo;
import core.cache.crypto.XTEACryption;
import core.cache.misc.buffer.ByteBufferUtils;
import core.game.world.GameWorld;
import core.net.IoSession;
import core.net.packet.in.Login;
import core.tools.StringUtils;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

/**
 * Handles account registration using the revision 578 protocol.
 */
public class AccountRegister {
    private static final Pattern PATTERN = Pattern.compile("[a-z0-9_]{1,12}");
    private static final long BASE37_LIMIT = 6582952005840035281L;

    public static void read(IoSession session, int opcode, ByteBuffer buffer) {
        RegistryResponse result;
        try {
            switch (opcode) {
                case 20:
                    buffer.get(); // Day
                    buffer.get(); // Month
                    buffer.getShort(); // Year
                    buffer.getShort(); // Country
                    result = RegistryResponse.SUCCESS;
                    break;
                case 21:
                    result = checkName(buffer.getLong());
                    break;
                case 22:
                    result = createAccount(buffer);
                    break;
                default:
                    result = RegistryResponse.CANNOT_CREATE;
                    break;
            }
        } catch (RuntimeException e) {
            // Never log decrypted credentials or email addresses.
            result = RegistryResponse.CANNOT_CREATE;
        }
        session.queue(ByteBuffer.wrap(new byte[]{(byte) result.getId()}));
    }

    private static RegistryResponse checkName(long encodedName) {
        String username = decodeName(encodedName);
        if (invalidUsername(username)) return RegistryResponse.INVALID_USERNAME;
        UserAccountInfo info = UserAccountInfo.createDefault();
        info.setUsername(username);
        return GameWorld.getAuthenticator().canCreateAccountWith(info)
                ? RegistryResponse.SUCCESS : RegistryResponse.NOT_AVAILBLE_USER;
    }

    private static RegistryResponse createAccount(ByteBuffer buffer) {
        int length = buffer.getShort() & 0xFFFF;
        if (length != buffer.remaining()) return RegistryResponse.CANNOT_CREATE;
        if ((buffer.getShort() & 0xFFFF) != ServerConstants.REVISION)
            return RegistryResponse.CANNOT_CREATE;

        ByteBuffer rsa = Login.decryptRegistrationRSA(buffer);
        if ((rsa.get() & 0xFF) != 10) return RegistryResponse.CANNOT_CREATE;
        rsa.getShort(); // Newsletter opt-in flags
        String username = decodeName(rsa.getLong());
        int[] key = new int[4];
        key[0] = rsa.getInt();
        String password = ByteBufferUtils.getString(rsa);
        key[1] = rsa.getInt();
        rsa.getShort(); // Affiliate
        rsa.get(); // Day
        rsa.get(); // Month
        key[2] = rsa.getInt();
        rsa.getShort(); // Year
        rsa.getShort(); // Country
        key[3] = rsa.getInt();
        if (rsa.hasRemaining()) return RegistryResponse.CANNOT_CREATE;

        if (!buffer.hasRemaining() || buffer.remaining() % 8 != 0)
            return RegistryResponse.CANNOT_CREATE;
        ByteBuffer extra = buffer.slice();
        XTEACryption.decrypt(key, extra);
        ByteBufferUtils.getString(extra); // Email (not persisted by UserAccountInfo)
        if (extra.remaining() < 1 || extra.remaining() > 8)
            return RegistryResponse.CANNOT_CREATE;

        if (invalidUsername(username)) return RegistryResponse.INVALID_USERNAME;
        if (password.length() < 5 || password.length() > 20)
            return RegistryResponse.INVALID_PASS_LENGTH;
        if (password.equals(username)) return RegistryResponse.PASS_SIMILAR_TO_USER;

        UserAccountInfo info = UserAccountInfo.createDefault();
        info.setUsername(username);
        info.setPassword(password);
        synchronized (AccountRegister.class) {
            if (!GameWorld.getAuthenticator().canCreateAccountWith(info))
                return RegistryResponse.CANNOT_CREATE;
            return GameWorld.getAuthenticator().createAccountWith(info)
                    ? RegistryResponse.SUCCESS : RegistryResponse.CANNOT_CREATE;
        }
    }

    private static String decodeName(long value) {
        return value > 0 && value < BASE37_LIMIT ? StringUtils.longToString(value) : null;
    }

    public static boolean invalidUsername(String username) {
        return username == null || !PATTERN.matcher(username).matches();
    }
}
