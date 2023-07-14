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

    // Reads a word as an address
    public short readWord (short address) {
        byte lo = bus.cpuReadByte(address);
        byte hi = bus.cpuReadByte((short)(address+1));
        return (short)(hi*0x0100 + lo);
    }

    public short readWordZP (short address) {
        byte lo = bus.cpuReadByte(address);
        byte hi = bus.cpuReadByte((byte)(address+1));
        return (short)(hi*0x0100 + lo);
    }

    public byte getByteHi (short word) {
        return (byte)((word & 0xFF00) >> 0x8);
    }
    public byte getByteLo (short word) {
        return (byte)(word & 0x00FF);
    }
    public short swapBytes (short word) {return (short)(getByteLo(word)*0x100 + getByteHi(word));}

    public int retrieveBit (int value, int bit) {
        return (value & (1 << bit) >> bit);
    }

    public int setBit (int value, int bit, int b) {
        int v = value;
        v &= ~(1 << bit);
        v |= b<< bit;

        return v;
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
        maddr = swapBytes(operand);
        memory = bus.cpuReadByte(maddr);
    }

    // Zero page, Take the first byte and use that as the address
    void ZRPG (short operand) {
        maddr = getByteHi(operand);
        memory = bus.cpuReadByte(maddr);
    }

    // Absolute X-Indexed, Load what is in the address at operand+x
    void ABSX (short operand) {
        maddr = (short)(swapBytes(operand) + x);
        memory = bus.cpuReadByte(maddr);
    }

    // Absolute Y-Indexed, Load what is in the address at operand+y
    void ABSY (short operand) {
        maddr = (short)(swapBytes(operand) + y);
        memory = bus.cpuReadByte(maddr);
    }

    // Zero page X indexed, Take the zero-page address and add x to it
    void ZRPX (short operand) {
        maddr = (byte)(getByteHi(operand) + x);
        memory = bus.cpuReadByte(maddr);
    }

    // Zero page Y indexed, Take the zero-page address and add y to it
    void ZRPY (short operand) {
        maddr = (byte)(getByteHi(operand) + y);
        memory = bus.cpuReadByte(maddr);
    }

    // Indirect, Get the address at the operand
    void INDR (short operand) {
        maddr = readWord(swapBytes(operand));
        memory = bus.cpuReadByte(maddr); // Not sure if this is used
    }

    // Indirect Zero page X Indexed (Pre), first byte read x is added then accesses this and that is the address
    void INDX (short operand) {
        maddr = readWordZP((byte)(getByteHi(operand) + x));
        memory = bus.cpuReadByte(maddr);
    }

    // Indirect Zero page Y Indexed (Post), first byte read then accessed, then y is added and this is the address
    void INDY (short operand) {
        maddr = (short)(readWordZP((getByteHi(operand))) + y);
        memory = bus.cpuReadByte(maddr);
    }

    //
    void RELT (short operand) {
        maddr = (short)(pc + getByteHi(operand));
        memory = bus.cpuReadByte(maddr); // Unused
    }

    // Instructions

    // NOP, All illegal opcodes are NOP in this implementation
    void NOP () {}


    void ADC () { // Signed
        short out = (short)(acc + memory + retrieveBit(ps, ProcessorStatus.C));
        int carry = out >> 8;
        ps = (byte)setBit(ps, ProcessorStatus.C, carry);
        ps = (byte)setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(acc, 7));
        if((retrieveBit(acc, 7) ^ retrieveBit(memory, 7)) != 1) {
            if(retrieveBit(out, 7) == retrieveBit(acc, 7)) {
                ps = (byte)setBit(ps, ProcessorStatus.V, 0);
            }
            else {
                ps = (byte)setBit(ps, ProcessorStatus.V, 1); // Did overflow

            }
        }
        else  {
            ps = (byte)setBit(ps, ProcessorStatus.V, 0);
        }
    }

    void AND () {
        acc = (byte)(acc & memory);
        ps = (byte)setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(acc, 7));
    }

    void ASL () {
        int out = acc << 1;

        ps = (byte)setBit(ps, ProcessorStatus.C, retrieveBit(out, 8));
        acc = (byte)out;

        ps = (byte)setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(acc, 7));
    }

    void BCC () {
        if(retrieveBit(ps, ProcessorStatus.C) == 0)
            pc = maddr;
    }

    void BCS () {
        if(retrieveBit(ps, ProcessorStatus.C) == 1)
            pc = maddr;
    }

    void BEQ () {
        if(retrieveBit(ps, ProcessorStatus.Z) == 1)
            pc = maddr;
    }

    void BIT () {
        acc = (byte)(acc & memory);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.Z, retrieveBit(memory, 6));
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(memory, 7));
    }

    void BMI () {
        if(retrieveBit(ps, ProcessorStatus.N) == 1)
            pc = maddr;
    }

    void BNE () {
        if(retrieveBit(ps, ProcessorStatus.Z) == 0)
            pc = maddr;
    }

    void BPL () {
        if(retrieveBit(ps, ProcessorStatus.N) == 0)
            pc = maddr;
    }

    void BRK () {

    }

    void BVC () {
        if(retrieveBit(ps, ProcessorStatus.V) == 0)
            pc = maddr;
    }

    void BVS () {
        if(retrieveBit(ps, ProcessorStatus.V) == 1)
            pc = maddr;
    }

    void CLC () {
        ps = (byte)setBit(ps, ProcessorStatus.C, 0);
    }

    void CLD () {
        ps = (byte)setBit(ps, ProcessorStatus.D, 0);
    }

    void CLI () {
        ps = (byte)setBit(ps, ProcessorStatus.I, 0);
    }

    void CLV () {
        ps = (byte)setBit(ps, ProcessorStatus.V, 0);
    }

    void CMP () {
        ps = (byte)setBit(ps, ProcessorStatus.C, (acc >= memory) ? 1 : 0);

        byte out = (byte)(acc - memory);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(out, 7));
    }

    void CMX () {
        ps = (byte)setBit(ps, ProcessorStatus.C, (x >= memory) ? 1 : 0);

        byte out = (byte)(x - memory);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(out, 7));
    }

    void CMY () {
        ps = (byte)setBit(ps, ProcessorStatus.C, (y >= memory) ? 1 : 0);

        byte out = (byte)(y - memory);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(out, 7));
    }

    void DEC () {
        byte out = (byte)(bus.cpuReadByte(maddr)-1);
        bus.cpuWriteByte(maddr, out);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(out, 7));
    }

    void DEX () {
        x = (byte)(x - 1);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (x == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(x, 7));
    }

    void DEY () {
        y = (byte)(y - 1);

        ps = (byte)setBit(ps, ProcessorStatus.Z, (y == 0b0) ? 1 : 0);
        ps = (byte)setBit(ps, ProcessorStatus.N, retrieveBit(y, 7));
    }


}
