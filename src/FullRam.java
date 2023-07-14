public class FullRam extends BusItem{
    byte[] memory;

    public FullRam () {
        super((short)0x0000, (short)0xFFFF);
        memory = new byte[0xFFFF];
    }

    @Override
    public byte accessByte(short address) {
        return memory[address];
    }

    @Override
    public boolean writeByte(short address, byte data) {
        memory[address] = data;
        return true;
    }
}
