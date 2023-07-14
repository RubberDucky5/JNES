public enum ProcessorStatus {
    C (1 << 0), // Carry Flag
    Z (1 << 1), // Last was Zero
    I (1 << 2), // Interrupt
    D (1 << 3), // BCD Mode (unused)
    B (1 << 4), // Break
    U (1 << 5), // Unused
    V (1 << 6), // Overflow
    N (1 << 7); // Last was Negative

    public final byte offset;
    private ProcessorStatus(int offset){
        this.offset = (byte)offset;
    }
}