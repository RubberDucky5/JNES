import java.util.ArrayList;
import java.util.List;

public class Bus {
    List<BusItem> devices;

    public Bus () {
        devices = new ArrayList<>();
    }

    public void addDevice (BusItem device) {
        devices.add(device);
    }

    byte cpuReadByte (short address) {
        for(int i = 0; i < devices.size(); i++) {
            if(devices.get(i).addressRange.contains(address)) {
                return devices.get(i).accessByte(address);
            }
        }
        return 0x0;
    }

    boolean cpuWriteByte (short address, byte data) {
        for(int i = 0; i < devices.size(); i++) {
            if(devices.get(i).addressRange.contains(address)) {
                return devices.get(i).writeByte(address, data);
            }
        }
        return false;
    }
}
