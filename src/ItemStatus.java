
public enum ItemStatus {
ENABLED(0), DISABLED(1), NOT_SUPPORTED(3);

	private int value;

	private ItemStatus(int statusValue) {
		this.value = statusValue;
	}
	
	public int getValue() {
		return value;
	}
}
