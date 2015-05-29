
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mnoa
 * 
 *         <p>
 *         Each enum represents one column in the status page of haproxy.
 *         </p>
 * 
 *         <p>
 *         Example response:<br />
 * 
 *         <pre>
 * # pxname,svname,qcur,qmax,scur,smax,slim,stot,bin,bout,dreq,dresp,ereq,econ,eresp,wretr,wredis,status,weight,act,bck,chkfail,chkdown,lastchg,downtime,qlimit,pid,iid,sid,throttle,lbtot,tracked,type,rate,rate_lim,rate_max,check_status,check_code,check_duration,hrsp_1xx,hrsp_2xx,hrsp_3xx,hrsp_4xx,hrsp_5xx,hrsp_other,hanafail,req_rate,req_rate_max,req_tot,cli_abrt,srv_abrt,comp_in,comp_out,comp_byp,comp_rsp,lastsess,last_chk,last_agt,qtime,ctime,rtime,ttime,
 * https_frontend,FRONTEND,,,0,2,2000,768,283810,11219680,0,0,44,,,,,OPEN,,,,,,,,,1,1,0,,,,0,0,0,2,,,,0,724,0,44,0,0,,0,2,768,,,0,0,0,0,,,,,,,,
 * test_frontend,FRONTEND,,,1,2,2000,39,7808,24420,0,0,2,,,,,OPEN,,,,,,,,,1,2,0,,,,0,2,0,4,,,,0,16,0,22,0,0,,2,3,39,,,0,0,0,0,,,,,,,,
 * web_server,s2,0,0,0,1,,3,1101,1494,,0,,0,0,0,0,UP,1,1,0,0,0,8146,0,,1,3,1,,0,,2,0,,3,L4OK,,0,0,0,0,3,0,0,0,,,,0,0,,,,,8086,,,0,1,0,1,
 * web_server,BACKEND,0,0,0,1,400,3,1101,1494,0,0,,0,0,0,0,UP,1,1,0,,0,8146,0,,1,3,0,,0,,1,0,,3,,,,0,0,0,3,0,0,,,,,0,0,0,0,0,0,8086,,,0,1,0,1,
 * </pre>
 * 
 *         </p>
 *         <p>
 *         Quote from <a href="http://www.haproxy.org/download/1.5/doc/configuration.txt">documentation</a>:
 * 
 *         <pre>
 * In brackets after each field name are the types which may have a value for
 * that field. The types are L (Listeners), F (Frontends), B (Backends), and
 * S (Servers).
 * 
 *   0. pxname [LFBS]: proxy name
 *   1. svname [LFBS]: service name (FRONTEND for frontend, BACKEND for backend,
 *      any name for server/listener)
 *   2. qcur [..BS]: current queued requests. For the backend this reports the
 *      number queued without a server assigned.
 *   3. qmax [..BS]: max value of qcur
 *   4. scur [LFBS]: current sessions
 *   5. smax [LFBS]: max sessions
 *   6. slim [LFBS]: configured session limit
 *   7. stot [LFBS]: cumulative number of connections
 *   8. bin [LFBS]: bytes in
 *   9. bout [LFBS]: bytes out
 *  10. dreq [LFB.]: requests denied because of security concerns.
 *      - For tcp this is because of a matched tcp-request content rule.
 *      - For http this is because of a matched http-request or tarpit rule.
 *  11. dresp [LFBS]: responses denied because of security concerns.
 *      - For http this is because of a matched http-request rule, or
 *        "option checkcache".
 *  12. ereq [LF..]: request errors. Some of the possible causes are:
 *      - early termination from the client, before the request has been sent.
 *      - read error from the client
 *      - client timeout
 *      - client closed connection
 *      - various bad requests from the client.
 *      - request was tarpitted.
 *  13. econ [..BS]: number of requests that encountered an error trying to
 *      connect to a backend server. The backend stat is the sum of the stat
 *      for all servers of that backend, plus any connection errors not
 *      associated with a particular server (such as the backend having no
 *      active servers).
 *  14. eresp [..BS]: response errors. srv_abrt will be counted here also.
 *      Some other errors are:
 *      - write error on the client socket (won't be counted for the server stat)
 *      - failure applying filters to the response.
 *  15. wretr [..BS]: number of times a connection to a server was retried.
 *  16. wredis [..BS]: number of times a request was redispatched to another
 *      server. The server value counts the number of times that server was
 *      switched away from.
 *  17. status [LFBS]: status (UP/DOWN/NOLB/MAINT/MAINT(via)...)
 *  18. weight [..BS]: server weight (server), total weight (backend)
 *  19. act [..BS]: server is active (server), number of active servers (backend)
 *  20. bck [..BS]: server is backup (server), number of backup servers (backend)
 *  21. chkfail [...S]: number of failed checks. (Only counts checks failed when
 *      the server is up.)
 *  22. chkdown [..BS]: number of UP->DOWN transitions. The backend counter counts
 *      transitions to the whole backend being down, rather than the sum of the
 *      counters for each server.
 *  23. lastchg [..BS]: number of seconds since the last UP<->DOWN transition
 *  24. downtime [..BS]: total downtime (in seconds). The value for the backend
 *      is the downtime for the whole backend, not the sum of the server downtime.
 *  25. qlimit [...S]: configured maxqueue for the server, or nothing in the
 *      value is 0 (default, meaning no limit)
 *  26. pid [LFBS]: process id (0 for first instance, 1 for second, ...)
 *  27. iid [LFBS]: unique proxy id
 *  28. sid [L..S]: server id (unique inside a proxy)
 *  29. throttle [...S]: current throttle percentage for the server, when
 *      slowstart is active, or no value if not in slowstart.
 *  30. lbtot [..BS]: total number of times a server was selected, either for new
 *      sessions, or when re-dispatching. The server counter is the number
 *      of times that server was selected.
 *  31. tracked [...S]: id of proxy/server if tracking is enabled.
 *  32. type [LFBS]: (0=frontend, 1=backend, 2=server, 3=socket/listener)
 *  33. rate [.FBS]: number of sessions per second over last elapsed second
 *  34. rate_lim [.F..]: configured limit on new sessions per second
 *  35. rate_max [.FBS]: max number of new sessions per second
 *  36. check_status [...S]: status of last health check, one of:
 *         UNK     -> unknown
 *         INI     -> initializing
 *         SOCKERR -> socket error
 *         L4OK    -> check passed on layer 4, no upper layers testing enabled
 *         L4TMOUT -> layer 1-4 timeout
 *         L4CON   -> layer 1-4 connection problem, for example
 *                    "Connection refused" (tcp rst) or "No route to host" (icmp)
 *         L6OK    -> check passed on layer 6
 *         L6TOUT  -> layer 6 (SSL) timeout
 *         L6RSP   -> layer 6 invalid response - protocol error
 *         L7OK    -> check passed on layer 7
 *         L7OKC   -> check conditionally passed on layer 7, for example 404 with
 *                    disable-on-404
 *         L7TOUT  -> layer 7 (HTTP/SMTP) timeout
 *         L7RSP   -> layer 7 invalid response - protocol error
 *         L7STS   -> layer 7 response error, for example HTTP 5xx
 *  37. check_code [...S]: layer5-7 code, if available
 *  38. check_duration [...S]: time in ms took to finish last health check
 *  39. hrsp_1xx [.FBS]: http responses with 1xx code
 *  40. hrsp_2xx [.FBS]: http responses with 2xx code
 *  41. hrsp_3xx [.FBS]: http responses with 3xx code
 *  42. hrsp_4xx [.FBS]: http responses with 4xx code
 *  43. hrsp_5xx [.FBS]: http responses with 5xx code
 *  44. hrsp_other [.FBS]: http responses with other codes (protocol error)
 *  45. hanafail [...S]: failed health checks details
 *  46. req_rate [.F..]: HTTP requests per second over last elapsed second
 *  47. req_rate_max [.F..]: max number of HTTP requests per second observed
 *  48. req_tot [.F..]: total number of HTTP requests received
 *  49. cli_abrt [..BS]: number of data transfers aborted by the client
 *  50. srv_abrt [..BS]: number of data transfers aborted by the server
 *      (inc. in eresp)
 *  51. comp_in [.FB.]: number of HTTP response bytes fed to the compressor
 *  52. comp_out [.FB.]: number of HTTP response bytes emitted by the compressor
 *  53. comp_byp [.FB.]: number of bytes that bypassed the HTTP compressor
 *      (CPU/BW limit)
 *  54. comp_rsp [.FB.]: number of HTTP responses that were compressed
 *  55. lastsess [..BS]: number of seconds since last session assigned to
 *      server/backend
 *  56. last_chk [...S]: last health check contents or textual error
 *  57. last_agt [...S]: last agent check contents or textual error
 *  58. qtime [..BS]: the average queue time in ms over the 1024 last requests
 *  59. ctime [..BS]: the average connect time in ms over the 1024 last requests
 *  60. rtime [..BS]: the average response time in ms over the 1024 last requests
 *      (0 for TCP)
 *  61. ttime [..BS]: the average total session time in ms over the 1024 last
 *      requests
 * </pre>
 * 
 *         </p>
 *
 */
public enum HaproxyStatusColumn {
	/** Pool name / proxy name */
	PXNAME("pxname", 0, StatusValueType.STRING, "pool name", "Pool/Proxy name", "LFBS", ""),
	/** service name (FRONTEND for frontend, BACKEND for backend, any name for server) */
	SVNAME("svname", 1, StatusValueType.STRING, "service name",
			"service name (FRONTEND for frontend, BACKEND for backend, any name for server)", "LFBS", ""),
	/** current queued requests - backend only */
	QCUR("qcur", 2, StatusValueType.INTEGER, "current queued requests", "current queued requests", "..BS", "qreq",
			ItemStatus.ENABLED),
	/** max queued requests - backend only */
	QMAX("qmax", 3, StatusValueType.INTEGER, "max queued requests", "max queued requests", "..BS", "qreq", ItemStatus.ENABLED),
	/** current sessions */
	SCUR("scur", 4, StatusValueType.INTEGER, "current sessions", "current sessions", "LFBS", "ses", ItemStatus.ENABLED),
	/** max sessions */
	SMAX("smax", 5, StatusValueType.INTEGER, "max sessions", "max sessions", "LFBS", "ses", ItemStatus.ENABLED),
	/** sessions limit */
	SLIM("slim", 6, StatusValueType.INTEGER, "session limit", "configured session limit", "LFBS", "ses", ItemStatus.ENABLED),
	/** total sessions */
	STOT("stot", 7, StatusValueType.INTEGER, "total sessions", "cumulative number of connections", "ses", "LFBS"),
	/** Bytes in */
	BIN("bin", 8, StatusValueType.INTEGER, "bytes in", "bytes in", "LFBS", "bps", ValueType.NUMERIC_FLOAT, ItemStatus.ENABLED, Delta.DELTA_PER_SECOND),
	/** Bytes out */
	BOUT("bout", 9, StatusValueType.INTEGER, "bytes out", "bytes out", "LFBS", "bps", ValueType.NUMERIC_FLOAT, ItemStatus.ENABLED, Delta.DELTA_PER_SECOND),
	/** Denied requests (never set for server queues) */
	DREQ(
			"dreq",
			10,
			StatusValueType.INTEGER,
			"denied requests",
			"requests denied because of security concerns.\n- For tcp this is because of a matched tcp-request content rule.\n- For http this is because of a matched http-request or tarpit rule.",
			"LFB.",
			"req",
			Delta.DELTA_SIMPLE_CHANGE),
	/** Denied responses */
	DRESP(
			"dresp",
			11,
			StatusValueType.INTEGER,
			"denied responses",
			"responses denied because of security concerns.\n- For http this is because of a matched http-request rule, or &quot;option checkcache&quot;.",
			"LFBS",
			"resp",
			Delta.DELTA_SIMPLE_CHANGE),
	/** request errors (Frontend queues only) */
	EREQ(
			"ereq",
			12,
			StatusValueType.INTEGER,
			"request errors",
			"request errors. Some of the possible causes are:\n- early termination from the client, before the request has been sent.\n- read error from the client\n- client timeout\n- client closed connection\n- various bad requests from the client.\n- request was tarpitted.",
			"LF..",
			"req",
			Delta.DELTA_SIMPLE_CHANGE),
	/** connection errors (Backend and server queues only) */
	ECON(
			"econ",
			13,
			StatusValueType.INTEGER,
			"connection errors",
			"number of requests that encountered an error trying to connect to a backend server. The backend stat is the sum of the stat for all servers of that backend, plus any connection errors not associated with a particular server (such as the backend having no active servers)",
			"..BS",
			"conn",
			Delta.DELTA_SIMPLE_CHANGE),
	/** response errors (among which srv_abrt) (Backend and server queues only) */
	ERESP(
			"eresp",
			14,
			StatusValueType.INTEGER,
			"response errors",
			"srv_abrt will be counted here also.\nSome other errors are:\n- write error on the client socket (won't be counted for the server stat)\n- failure applying filters to the response",
			"..BS",
			"resp",
			Delta.DELTA_SIMPLE_CHANGE),
	/** retries (warning) */
	WRETR("wretr", 15, StatusValueType.INTEGER, "conn retries", "number of times a connection to a server was retried",
			"..BS", "conn"),
	/** redispatches (warning) */
	WREDIS(
			"wredis",
			16,
			StatusValueType.INTEGER,
			"conn redispatches",
			"number of times a request was redispatched to another server. The server value counts the number of times that server was switched away from.",
			"..BS",
			"conn"),
	/** status (UP/DOWN/NOLB/MAINT/MAINT(via)...) */
	STATUS("status", 17, StatusValueType.STATUS, "status", "status (UP/DOWN/NOLB/MAINT/MAINT(via)...)", "LFBS", "",
			ItemStatus.ENABLED, ValueType.CHARACTER),
	/** server weight (server), total weight (backend) */
	WEIGHT("weight", 18, StatusValueType.INTEGER, "weight", "server weight (server), total weight (backend)", "..BS", "weight"),
	/** server is active (server), number of active servers (backend) */
	ACT("act", 19, StatusValueType.INTEGER, "active", "server is active (server), number of active servers (backend)",
			"..BS", ""),
	/** server is backup (server), number of backup servers (backend) */
	BCK("bck", 20, StatusValueType.INTEGER, "backup", "server is backup (server), number of backup servers (backend)",
			"..BS", ""),
	/** number of failed checks */
	CHKFAIL("chkfail", 21, StatusValueType.INTEGER, "failed checks",
			"number of failed checks. (Only counts checks failed when the server is up.)", "...S", "",
			Delta.DELTA_SIMPLE_CHANGE),
	/** number of UP->DOWN transitions */
	CHKDOWN(
			"chkdown",
			22,
			StatusValueType.INTEGER,
			"up-down transitions",
			"number of UP-&gt;DOWN transitions. The backend counter counts transitions to the whole backend being down, rather than the sum of the counters for each server.",
			"..BS",
			"",
			Delta.DELTA_SIMPLE_CHANGE),
	/** last status change (in seconds) */
	LASTCHG("lastchg", 23, StatusValueType.INTEGER, "last change",
			"number of seconds since the last UP&lt;-&gt;DOWN transition", "..BS", "s"),
	/** total downtime (in seconds) */
	DOWNTIME(
			"downtime",
			24,
			StatusValueType.INTEGER,
			"downtime",
			"total downtime (in seconds). The value for the backend is the downtime for the whole backend, not the sum of the server downtime.",
			"..BS",
			"s"),
	/** queue limit */
	QLIMIT("qlimit", 25, StatusValueType.INTEGER, "queue limit",
			"configured maxqueue for the server, or nothing if the value is 0 (default, meaning no limit)", "...S", ""),
	/** process id (0 for first instance, 1 for second, ...) */
	PID("pid", 26, StatusValueType.INTEGER, "pid", "process id (0 for first instance, 1 for second, ...)", "LFBS", "", ValueType.CHARACTER),
	/** unique proxy id */
	IID("iid", 27, StatusValueType.INTEGER, "proxy id", "unique proxy id", "LFBS", "", ValueType.CHARACTER),
	/** service id (unique inside a proxy) */
	SID("sid", 28, StatusValueType.INTEGER, "server id", "server id (unique inside a proxy)", "L..S", "", ValueType.CHARACTER),
	/** warm up status */
	THROTTLE("throttle", 29, StatusValueType.INTEGER, "throttle",
			"current throttle percentage for the server, when slowstart is active, or no value if not in slowstart", "...S", "%"),
	/** total number of times a server was selected */
	LBTOT(
			"lbtot",
			30,
			StatusValueType.INTEGER,
			"selection count",
			"total number of times a server was selected, either for new sessions, or when re-dispatching. The server counter is the number of times that server was selected",
			"..BS",
			""),
	/** id of proxy/server if tracking is enabled */
	TRACKED("tracked", 31, StatusValueType.INTEGER, "server id", "id of proxy/server if tracking is enabled", "...S", ""),
	/** type (0=frontend, 1=backend, 2=server, 3=socket) */
	TYPE("type", 32, StatusValueType.TYPE, "type", "(0=frontend, 1=backend, 2=server, 3=socket/listener)", "LFBS", ""),
	/** number of sessions per second over last elapsed second */
	RATE("rate", 33, StatusValueType.INTEGER, "session rate", "number of sessions per second over last elapsed second",
			".FBS", "ses/s", ItemStatus.ENABLED),
	/** limit on new sessions per second */
	RATE_LIM("rate_lim", 34, StatusValueType.INTEGER, "session rate limit",
			"configured limit on new sessions per second", ".F..", "ses/s", ItemStatus.ENABLED),
	/** max number of new sessions per second */
	RATE_MAX("rate_max", 35, StatusValueType.INTEGER, "max session rate", "max number of new sessions per second",
			".FBS", "ses/s", ItemStatus.ENABLED),
	/**
	 * status of last health check, one of: UNK -> unknown INI -> initializing SOCKERR -> socket error L4OK -> check
	 * passed on layer 4, no upper layers testing enabled L4TMOUT -> layer 1-4 timeout L4CON -> layer 1-4 connection
	 * problem, for example "Connection refused" (tcp rst) or "No route to host" (icmp) L6OK -> check passed on layer 6
	 * L6TOUT -> layer 6 (SSL) timeout L6RSP -> layer 6 invalid response - protocol error L7OK -> check passed on layer 7
	 * L7OKC -> check conditionally passed on layer 7, for example 404 with disable-on-404 L7TOUT -> layer 7 (HTTP/SMTP)
	 * timeout L7RSP -> layer 7 invalid response - protocol error L7STS -> layer 7 response error, for example HTTP 5xx
	 */
	CHECK_STATUS(
			"check_status",
			36,
			StatusValueType.CHECK_STATUS,
			"health status",
			"status of last health check, one of:\n"
					+ "UNK     -&gt; unknown\n"
					+ "INI     -&gt; initializing\n"
					+ "SOCKERR -&gt; socket error\n"
					+ "L4OK    -&gt; check passed on layer 4, no upper layers testing enabled\n"
					+ "L4TOUT  -&gt; layer 1-4 timeout\n"
					+ "L4CON   -&gt; layer 1-4 connection problem, for example &quot;Connection refused&quot; (tcp rst) or &quot;No route to host&quot; (icmp)\n"
					+ "L6OK    -&gt; check passed on layer 6\n" + "L6TOUT  -&gt; layer 6 (SSL) timeout\n"
					+ "L6RSP   -&gt; layer 6 invalid response - protocol error\n" + "L7OK    -&gt; check passed on layer 7\n"
					+ "L7OKC   -&gt; check conditionally passed on layer 7, for example 404 with disable-on-404\n"
					+ "L7TOUT  -&gt; layer 7 (HTTP/SMTP) timeout\n" + "L7RSP   -&gt; layer 7 invalid response - protocol error\n"
					+ "L7STS   -&gt; layer 7 response error, for example HTTP 5xx", "...S", "", ValueType.CHARACTER),
	/** layer5-7 code, if available */
	CHECK_CODE("check_code", 37, StatusValueType.INTEGER, "check code", "layer5-7 code, if available", "...S", "", ValueType.CHARACTER),
	/** time in ms took to finish last health check */
	CHECK_DURATION("check_duration", 38, StatusValueType.INTEGER, "check duration",
			"time in ms took to finish last health check", "...S", "ms"),
	/** http responses with 1xx code */
	HRSP_1XX("hrsp_1xx", 39, StatusValueType.INTEGER, "1xx responses", "http responses with 1xx code", ".FBS", "", ValueType.CHARACTER),
	/** http responses with 2xx code */
	HRSP_2XX("hrsp_2xx", 40, StatusValueType.INTEGER, "2xx responses", "http responses with 2xx code", ".FBS", "", ValueType.CHARACTER),
	/** http responses with 3xx code */
	HRSP_3XX("hrsp_3xx", 41, StatusValueType.INTEGER, "3xx responses", "http responses with 3xx code", ".FBS", "", ValueType.CHARACTER),
	/** http responses with 4xx code */
	HRSP_4XX("hrsp_4xx", 42, StatusValueType.INTEGER, "4xx responses", "http responses with 4xx code", ".FBS", "", ValueType.CHARACTER),
	/** http responses with 5xx code */
	HRSP_5XX("hrsp_5xx", 43, StatusValueType.INTEGER, "5xx responses", "http responses with 5xx code", ".FBS", "", ValueType.CHARACTER),
	/** http responses with other codes (protocol error) */
	HRSP_OTHER("hrsp_other", 44, StatusValueType.INTEGER, "other responses",
			"http responses with other codes (protocol error)", ".FBS", "", ValueType.CHARACTER),
	/** failed health checks details */
	HANAFAIL("hanafail", 45, StatusValueType.INTEGER, "health check details", "failed health checks details", "...S", "", ValueType.CHARACTER),
	/** HTTP requests per second over last elapsed second */
	REQ_RATE("req_rate", 46, StatusValueType.INTEGER, "request rate",
			"HTTP requests per second over last elapsed second", ".F..", "req/s"),
	/** max number of HTTP requests per second observed */
	REQ_RATE_MAX("req_rate_max", 47, StatusValueType.INTEGER, "max request rate",
			"max number of HTTP requests per second observed", ".F..", "req/s"),
	/** total number of HTTP requests received */
	REQ_TOT("req_tot", 48, StatusValueType.INTEGER, "requests total", "total number of HTTP requests received", ".F..", "req"),
	/** number of data transfers aborted by the client */
	CLI_ABRT("cli_abrt", 49, StatusValueType.INTEGER, "client aborts", "number of data transfers aborted by the client",
			"..BS", "req"),
	/** number of data transfers aborted by the server (inc. in eresp) */
	SRV_ABRT("srv_abrt", 50, StatusValueType.INTEGER, "server aborts",
			"number of data transfers aborted by the server (inc. in eresp)", "..BS", "req"),
	/** number of HTTP response bytes fed to the compressor */
	COMP_IN("comp_in", 51, StatusValueType.INTEGER, "compressor in",
			"number of HTTP response bytes fed to the compressor", ".FB.", "bytes", ValueType.NUMERIC_FLOAT),
	/** number of HTTP response bytes emitted by the compressor */
	COMP_OUT("comp_out", 52, StatusValueType.INTEGER, "compressor out",
			"number of HTTP response bytes emitted by the compressor", ".FB.", "bytes", ValueType.NUMERIC_FLOAT),
	/** number of bytes that bypassed the HTTP compressor (CPU/BW limit) */
	COMP_BYP("comp_byp", 53, StatusValueType.INTEGER, "compressor bypass",
			"number of bytes that bypassed the HTTP compressor (CPU/BW limit)", ".FB.", "bytes", ValueType.NUMERIC_FLOAT),
	/** number of HTTP responses that were compressed */
	COMP_RSP("comp_rsp", 54, StatusValueType.INTEGER, "compressed responses",
			"number of HTTP responses that were compressed", ".FB.", "bytes", ValueType.NUMERIC_FLOAT),
	/** number of seconds since last session assigned to server/backend */
	LASTSESS("lastsess", 55, StatusValueType.INTEGER, "last session",
			"number of seconds since last session assigned to server/backend", "..BS", "s"),
	/** last health check contents or textual error */
	LAST_CHK("last_chk", 56, StatusValueType.INTEGER, "last health check", "last health check contents or textual error",
			"...S", "", ValueType.CHARACTER),
	/** last agent check contents or textual error */
	LAST_AGT("last_agt", 57, StatusValueType.INTEGER, "last agent check", "last agent check contents or textual error",
			"...S", "", ValueType.CHARACTER),
	/** the average queue time in ms over the 1024 last requests */
	QTIME("qtime", 58, StatusValueType.INTEGER, "avg queue time",
			"the average queue time in ms over the 1024 last requests", "..BS", "ms",
			Delta.DELTA_SIMPLE_CHANGE),
	/** the average connect time in ms over the 1024 last requests */
	CTIME("ctime", 59, StatusValueType.INTEGER, "avg connect time",
			"the average connect time in ms over the 1024 last requests", "..BS", "ms",
			Delta.DELTA_SIMPLE_CHANGE),
	/** the average response time in ms over the 1024 last requests (0 for TCP) */
	RTIME("rtime", 60, StatusValueType.INTEGER, "avg response time",
			"the average response time in ms over the 1024 last requests (0 for TCP)", "..BS", "ms",
			Delta.DELTA_SIMPLE_CHANGE),
	/** the average total session time in ms over the 1024 last requests */
	TTIME("ttime", 61, StatusValueType.INTEGER, "avg session time",
			"the average total session time in ms over the 1024 last requests", "..BS", "ms",
			Delta.DELTA_SIMPLE_CHANGE);

	/**
	 * The identifier in the comma separated line which identifies this item.
	 */
	String id;
	/**
	 * The expected position of the identifier in the line.
	 */
	int position;
	/**
	 * The type of the value.
	 */
	StatusValueType type;
	/**
	 * The name of the field.
	 */
	private String name;
	/**
	 * The description for the field.
	 */
	private String description;
	/**
	 * Which section can contain values for this field: LFBS The types are L (Listeners), F (Frontends), B (Backends), and
	 * S (Servers).
	 */
	private byte[] activeFor;

	/**
	 * Whether this value should be monitored by default.
	 */
	private ItemStatus active = ItemStatus.DISABLED;
	
	private ValueType vType = ValueType.NUMERIC_UNSIGNED;
	
	private Delta delta = Delta.AS_IS;
	
	private String unit;

	/**
	 * Indicates the position of the last column. Any column above will be ignored. So far haproxy added new information
	 * by appending a new column.
	 */
	public static final int highestKnownPosition = TTIME.position;

	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.unit = unit;
	}
	
	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit, final ValueType vType) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.unit = unit;
		this.vType = vType;
	}
	
	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit, final Delta delta) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.unit = unit;
		this.delta = delta;
	}

	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit, final ItemStatus active) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.active = active;
		this.unit = unit;
	}
	
	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit, final ItemStatus active, final ValueType vType) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.active = active;
		this.unit = unit;
		this.vType = vType;
	}
	
	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit, final ItemStatus active, final Delta delta) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.active = active;
		this.unit = unit;
		this.delta = delta;
	}
	
	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @param position
	 *          The expected position of the identifier in the line.
	 */
	private HaproxyStatusColumn(final String id, final int position, final StatusValueType type, final String name,
			final String description, final String activeFor, final String unit, final ValueType vType, final ItemStatus active, final Delta delta) {
		this.id = id;
		this.position = position;
		this.name = name;
		this.description = description;
		if (activeFor.length() == 4) {
			this.activeFor = activeFor.toUpperCase().getBytes();
		} else {
			this.activeFor = "....".getBytes();
		}
		this.active = active;
		this.unit = unit;
		this.delta = delta;
		this.vType = vType;
	}

	/**
	 * The identifier in the comma separated line which identifies this item.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The expected position of the identifier in the line.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * The short name of the field for display.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The description for the field for display.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The expected type of the value.
	 */
	public StatusValueType getType() {
		return type;
	}
	
	public Delta getDelta() {
		return delta;
	}
	
	public String getUnit() {
		return unit;
	}

	public boolean isAvailableForListeners() {
		return activeFor[0] == 'L';
	}

	public boolean isAvailableForFrontends() {
		return activeFor[1] == 'F';
	}

	public boolean isAvailableForBackends() {
		return activeFor[2] == 'B';
	}

	public boolean isAvailableForServers() {
		return activeFor[3] == 'S';
	}

	/**
	 * @return whether this item is enabled or disabled by default
	 */
	public ItemStatus getStatus() {
		return active;
	}
	
	/**
	 * @return which value type to use for this item
	 */
	public ValueType getValueType() {
		return vType;
	}

	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @return the enum representing this column
	 */
	public final static HaproxyStatusColumn getElementById(final String id) {
		return nameMap.get(id);
	}

	/**
	 * @param id
	 *          The identifier in the comma separated line which identifies this item (see example response).
	 * @return the enum representing this column
	 */
	public final static HaproxyStatusColumn getElementByPosition(final int pos) {
		return positionMap[pos];
	}

	/**
	 * Allows efficient access to the enum representing the column which has this identifier.
	 */
	private final static ConcurrentHashMap<String, HaproxyStatusColumn> nameMap;
	/**
	 * Allows efficient access to the enum representing the column which has this column number (starting with 0).
	 */
	private final static HaproxyStatusColumn[] positionMap;

	static {
		nameMap = new ConcurrentHashMap<String, HaproxyStatusColumn>();
		positionMap = new HaproxyStatusColumn[highestKnownPosition + 1];
		for (HaproxyStatusColumn statusItem : EnumSet.allOf(HaproxyStatusColumn.class)) {
			nameMap.put(statusItem.id, statusItem);
			positionMap[statusItem.position] = statusItem;
		}
	}

	/**
	 * @param position
	 *          the new position of this column (may shadow the existing column)
	 * @param elem
	 *          the enum representing this column
	 */
	public static void updateElementPosition(final int position, final HaproxyStatusColumn elem) {
		positionMap[elem.position] = null;
		elem.position = position;
		positionMap[elem.position] = elem;
	}

	/**
	 * @param position
	 *          the position to check
	 * @param id
	 *          the identifier found at this position
	 * @return true, if the identifier found matches the expected identifier or if position is outside the range of
	 *         {@link HaproxyStatusColumn.highestKnownPosition}
	 */
	public static boolean isValidPosition(final int position, final String id) {
		return position > highestKnownPosition
				|| (positionMap[position] != null && positionMap[position].id.equalsIgnoreCase(id));
	}
}
