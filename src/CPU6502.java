public class CPU6502 {
    short pc;  // Program counter
    byte acc;  // Accumulator
    byte x;    // X register
    byte y;    // Y register
    byte stkp; // Stack pointer
    byte ps;   // Processor status

    byte memory; // Memory, Value is set to the data that the instruction wants
    short maddr; // Memory address, This gets used for instructions that want an address instead of data

    boolean addressModeImplied;

    Bus bus;

    Opcode opcodes[] = new Opcode[256];

    private static Opcode o (Opcode.MneInstPtr mip, Opcode.AddrModePtr amp) {return new Opcode(mip, amp);}

    public CPU6502 (Bus bus) {
        this.bus = bus;

        opcodes = new Opcode[]{
                o(this::BRK, this::IMPL), o(this::ORA, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ORA, this::ZRPG), o(this::ASL, this::ZRPG), o(this::NOP, this::IMPL), o(this::PHP, this::IMPL), o(this::ORA, this::IMME), o(this::ASL, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ORA, this::ABSL), o(this::ASL, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BPL, this::RELT), o(this::ORA, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ORA, this::ZRPX), o(this::ASL, this::ZRPX), o(this::NOP, this::IMPL), o(this::CLC, this::IMPL), o(this::ORA, this::ABSY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ORA, this::ABSX), o(this::ASL, this::ABSX), o(this::NOP, this::IMPL),
                o(this::JSR, this::ABSL), o(this::AND, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::BIT, this::ZRPG), o(this::AND, this::ZRPG), o(this::ROL, this::ZRPG), o(this::NOP, this::IMPL), o(this::PLP, this::IMPL), o(this::AND, this::IMME), o(this::ROL, this::IMPL), o(this::NOP, this::IMPL), o(this::BIT, this::ABSL), o(this::AND, this::ABSL), o(this::ROL, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BMI, this::RELT), o(this::AND, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::AND, this::ZRPX), o(this::ROL, this::ZRPX), o(this::NOP, this::IMPL), o(this::SEC, this::IMPL), o(this::AND, this::ABSY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::AND, this::ABSX), o(this::ROL, this::ABSX), o(this::NOP, this::IMPL),
                o(this::RTI, this::IMPL), o(this::EOR, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::EOR, this::ZRPG), o(this::LSR, this::ZRPG), o(this::NOP, this::IMPL), o(this::PHA, this::IMPL), o(this::EOR, this::IMME), o(this::LSR, this::IMPL), o(this::NOP, this::IMPL), o(this::JMP, this::ABSL), o(this::EOR, this::ABSL), o(this::LSR, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BVC, this::RELT), o(this::EOR, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::EOR, this::ZRPX), o(this::LSR, this::ZRPX), o(this::NOP, this::IMPL), o(this::CLI, this::IMPL), o(this::EOR, this::ABSY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::EOR, this::ABSX), o(this::LSR, this::ABSX), o(this::NOP, this::IMPL),
                o(this::RTS, this::IMPL), o(this::ADC, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ADC, this::ZRPG), o(this::ROR, this::ZRPG), o(this::NOP, this::IMPL), o(this::PLA, this::IMPL), o(this::ADC, this::IMME), o(this::ROR, this::IMPL), o(this::NOP, this::IMPL), o(this::JMP, this::INDR), o(this::ADC, this::ABSL), o(this::ROR, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BVS, this::RELT), o(this::ADC, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ADC, this::ZRPX), o(this::ROR, this::ZRPX), o(this::NOP, this::IMPL), o(this::SEI, this::IMPL), o(this::ADC, this::ABSY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::ADC, this::ABSX), o(this::ROR, this::ABSX), o(this::NOP, this::IMPL),
                o(this::NOP, this::IMPL), o(this::STA, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::STY, this::ZRPG), o(this::STA, this::ZRPG), o(this::STX, this::ZRPG), o(this::NOP, this::IMPL), o(this::DEY, this::IMPL), o(this::NOP, this::IMPL), o(this::TXA, this::IMPL), o(this::NOP, this::IMPL), o(this::STY, this::ABSL), o(this::STA, this::ABSL), o(this::STX, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BCC, this::RELT), o(this::STA, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::STY, this::ZRPX), o(this::STA, this::ZRPX), o(this::STX, this::ZRPY), o(this::NOP, this::IMPL), o(this::TYA, this::IMPL), o(this::STA, this::ABSY), o(this::TXS, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::STA, this::ABSX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL),
                o(this::LDY, this::IMME), o(this::LDA, this::INDX), o(this::LDX, this::IMME), o(this::NOP, this::IMPL), o(this::LDY, this::ZRPG), o(this::LDA, this::ZRPG), o(this::LDX, this::ZRPG), o(this::NOP, this::IMPL), o(this::TAY, this::IMPL), o(this::LDA, this::IMME), o(this::TAX, this::IMPL), o(this::NOP, this::IMPL), o(this::LDY, this::ABSL), o(this::LDA, this::ABSL), o(this::LDX, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BCS, this::RELT), o(this::LDA, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::LDY, this::ZRPX), o(this::LDA, this::ZRPX), o(this::LDX, this::ZRPY), o(this::NOP, this::IMPL), o(this::CLV, this::IMPL), o(this::LDA, this::ABSY), o(this::TSX, this::IMPL), o(this::NOP, this::IMPL), o(this::LDY, this::ABSX), o(this::LDA, this::ABSX), o(this::LDX, this::ABSY), o(this::NOP, this::IMPL),
                o(this::CPY, this::IMME), o(this::CMP, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::CPY, this::ZRPG), o(this::CMP, this::ZRPG), o(this::DEC, this::ZRPG), o(this::NOP, this::IMPL), o(this::INY, this::IMPL), o(this::CMP, this::IMME), o(this::DEX, this::IMPL), o(this::NOP, this::IMPL), o(this::CPY, this::ABSL), o(this::CMP, this::ABSL), o(this::DEC, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BNE, this::RELT), o(this::CMP, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::CMP, this::ZRPX), o(this::DEC, this::ZRPX), o(this::NOP, this::IMPL), o(this::CLD, this::IMPL), o(this::CMP, this::ABSY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::CMP, this::ABSX), o(this::DEC, this::ABSX), o(this::NOP, this::IMPL),
                o(this::CPX, this::IMME), o(this::SBC, this::INDX), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::CPX, this::ZRPG), o(this::SBC, this::ZRPG), o(this::INC, this::ZRPG), o(this::NOP, this::IMPL), o(this::INX, this::IMPL), o(this::SBC, this::IMME), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::CPX, this::ABSL), o(this::SBC, this::ABSL), o(this::INC, this::ABSL), o(this::NOP, this::IMPL),
                o(this::BEQ, this::RELT), o(this::SBC, this::INDY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::SBC, this::ZRPX), o(this::INC, this::ZRPX), o(this::NOP, this::IMPL), o(this::SED, this::IMPL), o(this::SBC, this::ABSY), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::NOP, this::IMPL), o(this::SBC, this::ABSX), o(this::INC, this::ABSX), o(this::NOP, this::IMPL), };

        reset();
    }

    void clock () {
        byte hex = bus.cpuReadByte(pc);
        Opcode opcode = opcodes[UnsignedUtil.unsignedByteToInt(hex)];

        byte size = opcode.amp.XXXX(UnsignedUtil.makeWord(bus.cpuReadByte((short)(pc+1)), bus.cpuReadByte((short)(pc + 2))));

        opcode.mip.XXX();

        System.out.printf("Executing instruction: $%1$02X", hex);
        if(size >= 2) {
            System.out.printf(" $%1$02X", bus.cpuReadByte((short)(pc+1)));
            if(size >= 3) {
                System.out.printf(" $%1$02X", bus.cpuReadByte((short)(pc+2)));
            }
        }
        System.out.println();

        pc += size;

        System.out.printf("acc: $%1$02X x: $%2$02X y: $%3$02X pc: $%4$04X stkp: $%5$02X\n", acc, x, y, pc, stkp);
    }

    public void reset() {
        acc = 0x0;
        x = 0x0;
        y = 0x0;
        stkp = (byte)0xFF;
        ps = 0x0;
        memory = 0x0;

        pc = readWord((short)0xFFFC);
    }

    // Reads a word as an address
    public short readWord (short address) {
        byte lo = bus.cpuReadByte(address);
        byte hi = bus.cpuReadByte((short)(address+1));
        return UnsignedUtil.makeWord(hi, lo);
    }

    public short readWordZP (short address) {
        byte lo = bus.cpuReadByte(address);
        byte hi = bus.cpuReadByte((byte)(address+1));
        return UnsignedUtil.makeWord(hi, lo);
    }

    public void pushStack (byte data) {
        short pointer = UnsignedUtil.makeWord((byte)0x01, stkp);
        bus.cpuWriteByte(pointer, data);
        stkp--;
    }

    public byte pullStack() {
        short pointer = UnsignedUtil.makeWord((byte)0x01, (byte)(stkp+1));
        byte out = bus.cpuReadByte(pointer);
        stkp++;
        return out;
    }

    // Addressing Modes, What ever is in the next two bytes after the opcode gets passed in, no matter what

    // Implied, Nothing put in memory
    byte IMPL (short operand) {
        addressModeImplied = true;
        return 1;
    }

    // Immediate, Load a literal into memory
    byte IMME (short operand) {
        maddr = 0x0; // Should not be used if asking for address
        memory = UnsignedUtil.getByteHi(operand);

        addressModeImplied = false;

        return 2;
    }

    // Absolute, Load what is at the address into memory
    byte ABSL (short operand) {
        maddr = UnsignedUtil.swapBytes(operand);
        memory = bus.cpuReadByte(maddr);

        addressModeImplied = false;

        return 3;
    }

    // Zero page, Take the first byte and use that as the address
    byte ZRPG (short operand) {
        maddr = UnsignedUtil.getByteHi(operand);
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 2;
    }

    // Absolute X-Indexed, Load what is in the address at operand+x
    byte ABSX (short operand) {
        maddr = (short)(UnsignedUtil.swapBytes(operand) + x);
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 3;
    }

    // Absolute Y-Indexed, Load what is in the address at operand+y
    byte ABSY (short operand) {
        maddr = (short)(UnsignedUtil.swapBytes(operand) + y);
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 3;
    }

    // Zero page X indexed, Take the zero-page address and add x to it
    byte ZRPX (short operand) {
        maddr = (byte)(UnsignedUtil.getByteHi(operand) + x);
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 2;
    }

    // Zero page Y indexed, Take the zero-page address and add y to it
    byte ZRPY (short operand) {
        maddr = (byte)(UnsignedUtil.getByteHi(operand) + y);
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 2;
    }

    // Indirect, Get the address at the operand
    byte INDR (short operand) {
        maddr = readWord(UnsignedUtil.swapBytes(operand));
        memory = bus.cpuReadByte(maddr); // Not sure if this is used
        addressModeImplied = false;

        return 3;
    }

    // Indirect Zero page X Indexed (Pre), first byte read x is added then accesses this and that is the address
    byte INDX (short operand) {
        maddr = readWordZP((byte)(UnsignedUtil.getByteHi(operand) + x));
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 2;
    }

    // Indirect Zero page Y Indexed (Post), first byte read then accessed, then y is added and this is the address
    byte INDY (short operand) {
        maddr = (short)(readWordZP((UnsignedUtil.getByteHi(operand))) + y);
        memory = bus.cpuReadByte(maddr);
        addressModeImplied = false;

        return 2;
    }

    //
    byte RELT (short operand) {
        maddr = (short)(pc + UnsignedUtil.getByteHi(operand));
        memory = bus.cpuReadByte(maddr); // Unused
        addressModeImplied = false;

        return 2;
    }

    // Instructions

    // NOP, All illegal opcodes are NOP in this implementation
    void NOP () {}


    void ADC () { // Signed
        short out = (short)(acc + memory + UnsignedUtil.retrieveBit(ps, ProcessorStatus.C));

        int carry = out >> 8;
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, carry);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
        if((UnsignedUtil.retrieveBit(acc, 7) ^ UnsignedUtil.retrieveBit(memory, 7)) != 1) {
            if(UnsignedUtil.retrieveBit(out, 7) == UnsignedUtil.retrieveBit(acc, 7)) {
                ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, 0);
            }
            else {
                ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, 1); // Did overflow

            }
        }
        else  {
            ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, 0);
        }

        acc = (byte)(out & 0xFF);
    }

    void AND () {
        acc = (byte)(acc & memory);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void ASL () {
        int out;
        int cmp;
        if (addressModeImplied) {
            out = acc << 1;
            acc = UnsignedUtil.getByteLo((short)out);
            cmp = acc;
        }
        else {
            out = memory << 1;
            bus.cpuWriteByte(maddr, UnsignedUtil.getByteLo((short)out));
            cmp = out;
        }

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, UnsignedUtil.retrieveBit(out, 8));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (cmp == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(cmp, 7));
    }

    void BCC () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.C) == 0)
            pc = maddr;
    }

    void BCS () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.C) == 1)
            pc = maddr;
    }

    void BEQ () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.Z) == 1)
            pc = maddr;
    }

    void BIT () {
        acc = (byte)(acc & memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, UnsignedUtil.retrieveBit(memory, 6));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(memory, 7));
    }

    void BMI () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.N) == 1)
            pc = maddr;
    }

    void BNE () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.Z) == 0)
            pc = maddr;
    }

    void BPL () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.N) == 0)
            pc = maddr;
    }

    void BRK () {
        // TODO: Figure out how break works
    }

    void BVC () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.V) == 0)
            pc = maddr;
    }

    void BVS () {
        if(UnsignedUtil.retrieveBit(ps, ProcessorStatus.V) == 1)
            pc = maddr;
    }

    void CLC () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, 0);
    }

    void CLD () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.D, 0);
    }

    void CLI () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.I, 0);
    }

    void CLV () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, 0);
    }

    void CMP () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, (acc >= memory) ? 1 : 0);

        byte out = (byte)(acc - memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void CMX () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, (x >= memory) ? 1 : 0);

        byte out = (byte)(x - memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void CMY () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, (y >= memory) ? 1 : 0);

        byte out = (byte)(y - memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void CPX () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, (x >= memory) ? 1 : 0);

        byte out = (byte)(x - memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void CPY () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, (y >= memory) ? 1 : 0);

        byte out = (byte)(y - memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void DEC () {
        byte out = (byte)(bus.cpuReadByte(maddr)-1);
        bus.cpuWriteByte(maddr, out);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void DEX () {
        x = (byte)(x - 1);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (x == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(x, 7));
    }

    void DEY () {
        y = (byte)(y - 1);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (y == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(y, 7));
    }

    void EOR () {
        acc = (byte)(acc ^ memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void INC () {
        byte out = (byte)(bus.cpuReadByte(maddr)+1);
        bus.cpuWriteByte(maddr, out);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (out == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, 7));
    }

    void INX () {
        x = (byte)(x + 1);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (x == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(x, 7));
    }

    void INY () {
        y = (byte)(y + 1);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (y == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(y, 7));
    }

    void JMP () {
        pc = maddr;
    }

    void JSR () { // Not sure if this order is right
        pushStack(UnsignedUtil.getByteHi((short)(pc+3)));
        pushStack(UnsignedUtil.getByteLo((short)(pc+3)));
        pc = maddr;
    }

    void LDA () {
        acc = memory;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void LDX () {
        x = memory;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (x == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(x, 7));
    }

    void LDY () {
        y = memory;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void LSR () {
        short out;
        byte cmp;
        if (addressModeImplied) {
            out = (short) ((acc * 0x0100) >> 1);
            acc = UnsignedUtil.getByteHi(out);
            cmp = acc;
        }
        else {
            out = (short) ((memory * 0x0100) >> 1);
            bus.cpuWriteByte(maddr, UnsignedUtil.getByteHi(out));
            cmp = UnsignedUtil.getByteHi(out);
        }

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (cmp == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(cmp, 7));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, UnsignedUtil.retrieveBit(out, 7));
    }

    void ORA () {
        acc = (byte)(acc | memory);

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void PHA () {
        pushStack(acc);
    }

    void PHP () {
        pushStack((byte) UnsignedUtil.setBit(UnsignedUtil.setBit(ps, ProcessorStatus.B, 1), ProcessorStatus.U, 1));
    }

    void PLA () {
        acc = pullStack();

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void PLP () {
        byte out = pullStack();

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, UnsignedUtil.retrieveBit(out, ProcessorStatus.C));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, UnsignedUtil.retrieveBit(out, ProcessorStatus.Z));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.I, UnsignedUtil.retrieveBit(out, ProcessorStatus.I));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.D, UnsignedUtil.retrieveBit(out, ProcessorStatus.D));
        // break ignored
        // unused ignored
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, UnsignedUtil.retrieveBit(out, ProcessorStatus.V));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(out, ProcessorStatus.N));
    }

    void ROL () {
        short out;
        byte cmp;
        if (addressModeImplied) {
            out = (short)(acc << 1);
            out = (short) UnsignedUtil.setBit(out, 0x0, UnsignedUtil.retrieveBit(ps, ProcessorStatus.C));
            acc = UnsignedUtil.getByteLo(out);
            cmp = acc;
        }
        else {
            out = (short)(memory << 1);
            out = (short) UnsignedUtil.setBit(out, 0x0, UnsignedUtil.retrieveBit(ps, ProcessorStatus.C));
            bus.cpuWriteByte(maddr, UnsignedUtil.getByteLo(out));
            cmp = UnsignedUtil.getByteLo(out);
        }

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (cmp == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(cmp, 7));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, UnsignedUtil.retrieveBit(out, 8));
    }

    void ROR () {
        short out;
        byte cmp;
        if (addressModeImplied) {
            out = (short)((acc * 0x0100) >> 1);
            out = (short) UnsignedUtil.setBit(out, 0xF, UnsignedUtil.retrieveBit(ps, ProcessorStatus.C));
            acc = UnsignedUtil.getByteHi(out);
            cmp = acc;
        }
        else {
            out = (short) ((memory * 0x0100) >> 1);
            out = (short) UnsignedUtil.setBit(out, 0xF, UnsignedUtil.retrieveBit(ps, ProcessorStatus.C));
            bus.cpuWriteByte(maddr, UnsignedUtil.getByteHi(out));
            cmp = UnsignedUtil.getByteHi(out);
        }

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (cmp == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(cmp, 7));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, UnsignedUtil.retrieveBit(out, 7));
    }

    void RTI () {
        byte status = pullStack();

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, UnsignedUtil.retrieveBit(status, ProcessorStatus.C));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, UnsignedUtil.retrieveBit(status, ProcessorStatus.Z));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.I, UnsignedUtil.retrieveBit(status, ProcessorStatus.I));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.D, UnsignedUtil.retrieveBit(status, ProcessorStatus.D));
        // break ignored
        // unused ignored
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, UnsignedUtil.retrieveBit(status, ProcessorStatus.V));
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(status, ProcessorStatus.N));

        pc = UnsignedUtil.swapBytes((short)(pullStack()*0x100 + pullStack()));

    }

    void RTS () {
        pc = UnsignedUtil.swapBytes((short)(pullStack()*0x100 + pullStack()));
    }

    void SBC () {
        byte cmp = acc;
        acc = (byte)(acc + ~memory + UnsignedUtil.retrieveBit(ps, ProcessorStatus.C));

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, (cmp < memory) ? 0 : 1); // We needed to borrow
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));

        if((UnsignedUtil.retrieveBit(cmp, 7) ^ UnsignedUtil.retrieveBit(memory, 7)) == 1) { // Check is overflow possible
            ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, (UnsignedUtil.retrieveBit(acc, 7) == UnsignedUtil.retrieveBit(cmp, 7)) ? 0 : 1); // Check for overflow
        }
        else {
            ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.V, 0);
        }
    }

    void SEC () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.C, 1);
    }

    void SED () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.D, 1);
    }

    void SEI () {
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.I, 1);
    }

    void STA () {
        bus.cpuWriteByte(maddr, acc);
    }

    void STX () {
        bus.cpuWriteByte(maddr, x);
    }

    void STY () {
        bus.cpuWriteByte(maddr, y);
    }

    void TAX () {
        x = acc;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (x == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(x, 7));
    }

    void TAY () {
        y = acc;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (y == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(y, 7));
    }

    void TSX () {
        x = stkp;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (x == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(x, 7));
    }

    void TXA () {
        acc = x;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }

    void TXS () {
        stkp = x;
    }

    void TYA () {
        acc = y;

        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.Z, (acc == 0b0) ? 1 : 0);
        ps = (byte) UnsignedUtil.setBit(ps, ProcessorStatus.N, UnsignedUtil.retrieveBit(acc, 7));
    }







}
