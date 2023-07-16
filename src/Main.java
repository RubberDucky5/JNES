import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bus bus = new Bus();
        FullRam ram = new FullRam();
        bus.addDevice(ram);
        
        byte[] program = parseStrToBytes("A2 01 A9 00 18 86 55 65 55 AA A5 55 4C 04 06");

        bus.writeBytes((short)0x0600, program);

        CPU6502 cpu = new CPU6502(bus);

        Scanner input = new Scanner(System.in);

        while (true) {
            String i = input.nextLine();

            if(i.equals("r")) {
                cpu.reset();
                continue;
            }

            if(!i.equals("")){
                break;
            }

            cpu.clock();
        }
    }

    private static byte[] parseStrToBytes(String str) {
        String[] strs = str.split("\\s");

        byte[] program = new byte[strs.length];

        for (int i = 0; i < strs.length; i++) {
            program[i] = (byte)Integer.parseInt(strs[i], 16);
        }
        
        return program;
    }


}