public class UnsignedUtil {
    static int unsignedByteToInt (byte a) {
        int out = 0;

        for (int i = 0; i < 0x8; i++) {
            out |= a & (0x1 << i);
        }

        return out;
    }

    static byte getByteHi (short word) {
        return (byte)((word & 0xFF00) >> 0x8);
    }

    public static byte getByteLo(short word) {
        return (byte)(word & 0x00FF);
    }

    public static short swapBytes(short word) {
        return (short)(((getByteLo(word) & 0xFF) << 8) | (getByteHi(word) & 0xFF));
    }

    public static int retrieveBit(int value, int bit) {
        return (value & (1 << bit) >> bit);
    }

    public static int setBit(int value, int bit, int b) {
        int v = value;
        v &= ~(1 << bit);
        v |= b << bit;

        return v;
    }

    public static short makeWord (byte a, byte b) {
        return (short)(((a & 0xFF) << 8) | (b & 0xFF));
    }
}
