public class FullRam extends BusItem{
    byte[] memory;

    public FullRam () {
        super((short)0x0000, (short)0xFFFF);
        memory = new byte[0xFFFF+1];

        // PC RESET
        memory[0xFFFC] = (byte)0x00;
        memory[0xFFFD] = (byte)0x06;
    }

    @Override
    public byte accessByte(short address) {
        int iaddress = unsignedShortToInt(address);
        return memory[iaddress];
    }

    @Override
    public boolean writeByte(short address, byte data) {
        int iaddress = unsignedShortToInt(address);
        memory[iaddress] = data;
        return true;
    }
}
