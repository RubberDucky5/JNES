public class AddressRange{
    public short min;
    public short max;

    public boolean minIncluded;
    public boolean maxIncluded;

    public AddressRange (short min, boolean minIncluded, short max, boolean maxIncluded) {
        this.min = min;
        this.max = max;
        this.minIncluded = minIncluded;
        this.maxIncluded = maxIncluded;
    }

    public boolean contains(short value) {
        boolean moreThanMin = Short.compareUnsigned(min, value)<=(minIncluded ? 0 : -1);
        boolean lessThanMax = Short.compareUnsigned(max, value)>=(maxIncluded ? 0 : 1);
        return moreThanMin && lessThanMax;
    }
}
