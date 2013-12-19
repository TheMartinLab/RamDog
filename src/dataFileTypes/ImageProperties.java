package dataFileTypes;

public class ImageProperties {
	private int bytesPerEntry;
	private int headerSize;
	private int endianType;
	private int[] dimensions;
	public ImageProperties(int bytesPerEntry, int headerSize, int endianType, int[] dimensions) {
		setBytesPerEntry(bytesPerEntry);
		setHeaderSize(headerSize);
		setEndianType(endianType);
		setDimensions(dimensions);
	}
	public ImageProperties() {
		setDefaults();
	}
	public int getBytesPerEntry() { return bytesPerEntry; }
	public void setBytesPerEntry(int bytesPerEntry) {
		this.bytesPerEntry = bytesPerEntry;
	}
	
	public int getHeaderSize() { return headerSize; }
	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}
	
	public int getEndianType() { return endianType; }
	public void setEndianType(int endianType) {
		this.endianType = endianType;
	}
	
	public int[] getDimensions() { return dimensions; }
	public void setDimensions(int[] dim) {
		dimensions = new int[dim.length];
		for(int i = 0; i < dim.length; i++) {
			dimensions[i] = dim[i];
		}
	}
	public void setSPEDefaults() {
		bytesPerEntry = 4;
		headerSize = 4096;
		endianType = BIN.LITTLE_ENDIAN;
		dimensions = new int[] {2084, 2084};
	}
	
	public void setCORDefaults() {
		bytesPerEntry = 2;
		headerSize = 4096;
		endianType = BIN.LITTLE_ENDIAN;
		dimensions = new int[] {2048, 2048};
	}
	public void setDefaults() {
		bytesPerEntry = 2;
		headerSize = 4096;
		endianType = BIN.LITTLE_ENDIAN;
		dimensions = new int[] {512, 512};
	}
	
}
