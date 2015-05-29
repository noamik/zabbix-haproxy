public enum Delta {
	AS_IS(0), DELTA_PER_SECOND(1), DELTA_SIMPLE_CHANGE(2);

	private int value;

	private Delta(int value) {
		this.value = value;
	}

	public String getValue() {
		return ((Integer) value).toString();
	}
}
