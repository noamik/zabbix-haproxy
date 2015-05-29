/**
 * @author mnoa
 * @see <a href="https://www.zabbix.com/documentation/2.0/manual/appendix/api/itemprototype/definitions">Zabbix documentation on item prototypes</a>
 */
public enum ValueType {
	NUMERIC_FLOAT(0), CHARACTER(1), LOG(2), NUMERIC_UNSIGNED(3), TEXT(4);

	private int value;

	private ValueType(int value) {
		this.value = value;
	}

	public String getValue() {
		return ((Integer) value).toString();
	}
}
