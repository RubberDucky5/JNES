import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bus bus = new Bus();
        FullRam ram = new FullRam();
        bus.addDevice(ram);

        bus.writeBytes((short)0x0600, new byte[]{(byte)0xA9, 0x55, (byte)0xA2, 0x34});

        CPU6502 cpu = new CPU6502(bus);

        Scanner input = new Scanner(System.in);

        while (true) {
            if(input.nextLine() != ""){
                break;
            }

            cpu.clock();
        }
    }
}