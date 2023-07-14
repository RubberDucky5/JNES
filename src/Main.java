public class Main {
    public static void main(String[] args) {
        Bus bus = new Bus();
        FullRam ram = new FullRam();
        bus.addDevice(ram);

        CPU6502 cpu = new CPU6502(bus);

        byte test = 0b00001000;
        test = (byte)setBit(test, 3, 0);
        System.out.println(Integer.toBinaryString(test));
    }
    static int setBit (int value, int bit, int b) {
        int v = value;
        v &= ~(1 << bit);
        v |= b<< bit;

        return v;
    }
}