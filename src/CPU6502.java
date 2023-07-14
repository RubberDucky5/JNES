public class CPU6502 {
    short pc;  // Program counter
    byte acc;  // Accumulator
    byte x;    // X register
    byte y;    // Y register
    byte stkp; // Stack pointer
    byte ps;   // Processor status

    byte memory; // Memory, Value is set to the data that the instruction wants
    short maddr; // Memory address, This gets used for instructions that want an address instead of data

    Bus bus;

    Opcode opcodes[] = new Opcode[255];

    private static Opcode o (Opcode.MneInstPtr mip, Opcode.AddrModePtr amp) {return new Opcode(mip, amp);}

    public CPU6502 (Bus bus) {
        this.bus = bus;

        opcodes[0] = o(this::NOP, this::IMME);

        reset();
    }

    public void reset() {
        acc = 0x0;
        x = 0x0;
        y = 0x0;
        stkp = 0x0;
        ps = 0x0;
        memory = 0x0;

        pc = readWord((short)0xFFFC);
    }

    public short readWord (short address) {
        byte lo = bus.cpuReadByte(address);
        byte hi = bus.cpuReadByte((short)(address+1));
        return (short)(hi*0x0100 + lo);
    }

    public byte getByteHi (short word) {
        return (byte)((word & 0xFF00) >> 0x8);
    }

    public byte getByteLo (short word) {
        return (byte)(word & 0x00FF);
    }

    // Addressing Modes, What ever is in the next two bytes after the opcode gets passed in, no matter what

    // Implied, Nothing put in memory
    void IMPL (short operand) {
        return;
    }

    // Immediate, Load a literal into memory
    void IMME (short operand) {
        maddr = 0x0; // Should not be used if asking for address
        memory = getByteHi(operand);
    }

    // Absolute, Load what is at the address into memory
    void ABSL (short operand) {
        maddr = operand;
        memory = bus.cpuReadByte(operand);
    }

    // Zero page, Take the first byte and use that as the address
    void ZRPG (short operand) {
        maddr = getByteLo(operand);
        memory = bus.cpuReadByte(maddr);
    }

    // Absolute X-Indexed, Load what is in the address at operand+x
    void ABSX (short operand) {
        maddr = (short)(operand + x);
        memory = bus.cpuReadByte(maddr);
    }

    // Absolute Y-Indexed, Load what is in the address at operand+y
    void ABSY (short operand) {
        maddr = (short)(operand + y);
        memory = bus.cpuReadByte(maddr);
    }

    // Zero page X indexed, Take the zero-page address and add x to it
    void ZRPX (short operand) {
        maddr = (short)(byte)(operand+x);
        memory = bus.cpuReadByte(maddr);
    }

    // Instructions

    // NOP
    void NOP () {

    }

}
