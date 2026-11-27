package core.game.interaction;

/** Native zero-based widget operations and the temporary compatibility boundary
 * for historical content listeners. listenerId returns the legacy listener ABI,
 * not a wire opcode.
 */
public final class InterfaceOperation {
    private InterfaceOperation() {}
    public static final int OP1 = 0, OP2 = 1, OP3 = 2, OP4 = 3, OP5 = 4,
            OP6 = 5, OP7 = 6, OP8 = 7, OP9 = 8, OP10 = 9;
    public static final int CONTINUE_INDEX = 10;
    /** Historical continuation ID for content not yet migrated. */
    public static final int CONTINUE = 132;
    private static final int[] LISTENER_IDS = {155, 196, 124, 199, 234, 168, 166, 64, 53, 9};

    public static int listenerId(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= LISTENER_IDS.length) {
            throw new IllegalArgumentException("Invalid widget operation " + optionIndex);
        }
        return LISTENER_IDS[optionIndex];
    }
}
