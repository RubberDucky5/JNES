public class Main {
    public static void main(String[] args) {
        //Bus bus = new Bus();
        //FullRam ram = new FullRam();
        //bus.addDevice(ram);

        //CPU6502 cpu = new CPU6502(bus);

        short a = (byte)0xFF;

        System.out.println(a & (1 << 2) >> 2);
    }
}