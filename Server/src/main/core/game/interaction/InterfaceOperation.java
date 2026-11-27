package core.game.interaction;

/** Compatibility boundary for existing content listeners. Input is a zero-based
 * widget operation, not a wire opcode. Values returned here are the historical
 * listener ABI; do not use them in network framing or packet decoding.
 */
public final class InterfaceOperation {
    private InterfaceOperation() {}
    public static final int CONTINUE = 132;
    private static final int[] LISTENER_IDS = {155, 196, 124, 199, 234, 168, 166, 64, 53, 9};

    public static int listenerId(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= LISTENER_IDS.length) {
            throw new IllegalArgumentException("Invalid widget operation " + optionIndex);
        }
        return LISTENER_IDS[optionIndex];
    }
}
