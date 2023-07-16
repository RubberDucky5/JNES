public class Opcode {
    // Addressing Mode Function Pointer
    public interface AddrModePtr {
        // Returns Memory
        byte XXXX(short operand);
    }

    // Mnemonic Instruction Function Pointer
    public interface MneInstPtr {
        void XXX();
    }

    AddrModePtr amp;
    MneInstPtr mip;

    public Opcode (MneInstPtr mip, AddrModePtr amp) {
        this.mip = mip;
        this.amp = amp;
    }
}
