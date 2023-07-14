import java.util.ArrayList;
import java.util.List;

public class Bus {
    List<BusItem> processors;

    public Bus () {
        processors = new ArrayList<>();
    }

    byte cpuReadByte (short address) {
        for(int i = 0; i < processors.size(); i++) {
            if(processors.get(i).addressRange.contains(address)) {
                return processors.get(i).accessByte(address);
            }
        }
        return 0x0;
    }

    boolean cpuWriteByte (short address, byte data) {
        for(int i = 0; i < processors.size(); i++) {
            if(processors.get(i).addressRange.contains(address)) {
                return processors.get(i).writeByte(address, data);
            }
        }
        return false;
    }
}
