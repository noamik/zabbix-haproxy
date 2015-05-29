

public enum StatusValueType {
	/** This status should be a number. */
	INTEGER,
	/** This status should be a generic String. */
	STRING,
	/** This status represents the server status: (UP/DOWN/NOLB/MAINT/MAINT(via)...). */
	STATUS,
	/** <p>This status has one of the following values:<pre>
UNK     -> unknown
INI     -> initializing
SOCKERR -> socket error
L4OK    -> check passed on layer 4, no upper layers testing enabled
L4TMOUT -> layer 1-4 timeout
L4CON   -> layer 1-4 connection problem, for example
           "Connection refused" (tcp rst) or "No route to host" (icmp)
L6OK    -> check passed on layer 6
L6TOUT  -> layer 6 (SSL) timeout
L6RSP   -> layer 6 invalid response - protocol error
L7OK    -> check passed on layer 7
L7OKC   -> check conditionally passed on layer 7, for example 404 with
           disable-on-404
L7TOUT  -> layer 7 (HTTP/SMTP) timeout
L7RSP   -> layer 7 invalid response - protocol error
L7STS   -> layer 7 response error, for example HTTP 5xx
	 * </pre></p>
	 */
	CHECK_STATUS,
	/** <p>This status value is a number representing the rows connection type:<br />
	 * (0=frontend, 1=backend, 2=server, 3=socket/listener)</p> */
	TYPE
}
