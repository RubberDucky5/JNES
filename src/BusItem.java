public class BusItem {
    AddressRange addressRange;

    public BusItem (short min, short max) {
        addressRange = new AddressRange(min, true, max, true);
    }

    // Called when something reads a byte from this device
    public byte accessByte (short address) {
        return 0x0;
    }

    // Called when something wants to attempt to write data to this device
    public boolean writeByte (short address, byte data) {
        return false;
    }

    // Makes the address from 0x0-<size of range>
    protected short remapToAddressRange(short address) {
        return (short)(address - addressRange.min);
    }
    protected int unsignedShortToInt (short a) {
        int out = 0;

        for (int i = 0; i <= 0xF; i++) {
            out |= a & (0x1 << i);
        }

        return out;
    }
}
