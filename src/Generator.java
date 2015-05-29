import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class Generator {

	private static final String BACKEND_ITEMS = "res/items-backend.xml";
	private static final String FRONTEND_ITEMS = "res/items-frontend.xml";
	private static final String SERVER_ITEMS = "res/items-servers.xml";
	private static final String BACKEND_GRAPHS = "res/graphs-backend.xml";
	private static final String FRONTEND_GRAPHS = "res/graphs-frontend.xml";
	private static final String SERVER_GRAPHS = "res/graphs-servers.xml";
	private static final String USERPARAM = "res/userparam.conf";
	private static final String TEMPLATE_HEADER_1 = "src/haproxy_zbx_template_head.xml";
	private static final String TEMPLATE_HEADER_2 = "src/haproxy_zbx_template_head2.xml";
	private static final String TEMPLATE_HEADER_3 = "src/haproxy_zbx_template_head3.xml";
	private static final String TEMPLATE_FOOTER   = "src/haproxy_zbx_template_footer.xml";
	private static final String TEMPLATE_FILLER   = "src/haproxy_zbx_template_filler.xml";
	
	private static final String[] FILES = {TEMPLATE_HEADER_1, BACKEND_ITEMS, TEMPLATE_FILLER, BACKEND_GRAPHS,
	                                       TEMPLATE_HEADER_2, FRONTEND_ITEMS, TEMPLATE_FILLER, FRONTEND_GRAPHS,
	                                       TEMPLATE_HEADER_3, SERVER_ITEMS, TEMPLATE_FILLER, SERVER_GRAPHS, TEMPLATE_FOOTER};
	
	private static List<String> itemLines;
	private static List<String> graphLines;
	private static List<String> backendLines = new LinkedList<String>();
	private static List<String> frontendLines = new LinkedList<String>();
	private static List<String> backendServerLines = new LinkedList<String>();
	private static List<String> backendGraphLines = new LinkedList<String>();
	private static List<String> frontendGraphLines = new LinkedList<String>();
	private static List<String> backendServerGraphLines = new LinkedList<String>();
	private static List<String> userParameterLines = new LinkedList<String>();
	

	
	private static String userParameter = "UserParameter=haproxy.stat.##VALUE-ID##[*],echo \"show stat\" | socat $1 stdio | grep \"^$2,$3\" | cut -d, -f";
	private static String userParameterInt = "UserParameter=haproxy.stat.##VALUE-ID##[*],RES=$(echo \"show stat\" | socat $1 stdio | grep \"^$2,$3\" | cut -d, -f";
	private static String userParameterIntEndHigh = "); if [ -z \"$RES\" ]; then echo \"9999999999\"; else echo \"$RES\"; fi";
	private static String userParameterIntEndLow = ";); if [ -z \"$RES\" ]; then echo \"0\"; else echo \"$RES\"; fi";
	
	public static void main(String[] param) {
		try {
			itemLines = readFile("src/item.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			graphLines = readFile("src/graph.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HaproxyStatusColumn column;
		for (int i = 2; i < HaproxyStatusColumn.highestKnownPosition; i++) {
			column = HaproxyStatusColumn.getElementByPosition(i);
			System.out.println(column);
			for (String line: itemLines) {
				if (column.isAvailableForBackends()) {
					createLine(line, backendLines, Application.BACKEND, column);
				}
				if (column.isAvailableForServers()) {
					createLine(line, backendServerLines, Application.BACKEND_SERVER, column);
				}
				if (column.isAvailableForFrontends()) {
					createLine(line, frontendLines, Application.FRONTEND, column);
				}
			}
			if (column.isAvailableForBackends() || column.isAvailableForFrontends() || column.isAvailableForServers()) {
				String userParamLine;
				if (isNumeric(column)) {
					userParamLine = formatUserParameterLine(userParameterInt, column);
					if (column.getName().contains("lim") || column.getName().contains("max") || column.getName().contains("time") || column.getName().contains("comp")) {
						userParamLine = userParamLine + userParameterIntEndLow;
					} else {
						userParamLine = userParamLine + userParameterIntEndHigh;
					}
				} else {
					userParamLine = formatUserParameterLine(userParameter, column);
				}
				if (column.position == 17) {
					userParamLine = userParamLine + " | cut -d\\  -f1";
				}
				userParameterLines.add(userParamLine);
			}
		}
		
		for (int i = 2; i < HaproxyStatusColumn.highestKnownPosition; i++) {
			column = HaproxyStatusColumn.getElementByPosition(i);
			System.out.println(column);
			for (String line: graphLines) {
				if (isNumeric(column)) {
					if (column.isAvailableForBackends()) {
						createLine(line, backendGraphLines, Application.BACKEND, column);
					}
					if (column.isAvailableForServers()) {
						createLine(line, backendServerGraphLines, Application.BACKEND_SERVER, column);
					}
					if (column.isAvailableForFrontends()) {
						createLine(line, frontendGraphLines, Application.FRONTEND, column);
					}
				}
			}
		}
		writeFile(BACKEND_ITEMS,backendLines);
		writeFile(FRONTEND_ITEMS,frontendLines);
		writeFile(SERVER_ITEMS,backendServerLines);
		writeFile(BACKEND_GRAPHS,backendGraphLines);
		writeFile(FRONTEND_GRAPHS,frontendGraphLines);
		writeFile(SERVER_GRAPHS,backendServerGraphLines);
		writeFile(USERPARAM,userParameterLines);
		try {
			concatFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String formatUserParameterLine(String uPL, HaproxyStatusColumn column) {
		return uPL.replaceAll("##VALUE-ID##", column.getId()) + (column.getPosition() + 1);
	}
	
	public static boolean isNumeric(HaproxyStatusColumn column) {
		return column.getValueType() == ValueType.NUMERIC_FLOAT || column.getValueType() == ValueType.NUMERIC_UNSIGNED;
	}
	
	private static void concatFiles() throws IOException {
		OutputStream out = new FileOutputStream("res/haproxy_zbx_template.xml");
    byte[] buf = new byte[4096];
    for (String file : FILES) {
        InputStream in = new FileInputStream(file);
        int b = 0;
        while ( (b = in.read(buf)) >= 0) {
            out.write(buf, 0, b);
            out.flush();
        }
        in.close();
    }
    out.close();
	}

	public static void writeFile(String fileName, List<String> lines) {
		File confFile = new File(fileName);
		confFile.delete();
		confFile = null;
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(
			             new OutputStreamWriter(
			                 new FileOutputStream(fileName, true), "UTF-8"));
			
			for (final String line : lines) {
				writer.write(line + "\n");
			}
			
			writer.close();
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("Encoding not supported. Can't write configuration.");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Writing to file failed.");
			e.printStackTrace();
		}
	}
	
	public enum Application {
		BACKEND, BACKEND_SERVER, FRONTEND
	}
	
	private static void createLine(String line, List<String> list, Application app, HaproxyStatusColumn column) {
		list.add(line.replaceAll("##VALUE-ID##", column.getId())
		             .replaceAll("##NAME##", getItemName(app, column))
		             .replaceAll("##GRAPH-NAME##", getGraphName(app, column))
		             .replaceAll("##STATUS##", ((Integer)column.getStatus().getValue()).toString())
		             .replaceAll("##PARAMETER##", getParameter(app))
		             .replaceAll("##DESCRIPTION##", column.getDescription())
		             .replaceAll("##APPLICATION##", getApplication(app))
		             .replaceAll("##VALUE-TYPE##", column.getValueType().getValue())
		             .replaceAll("##DELTA##", column.getDelta().getValue())
		             .replaceAll("##UNIT##", getUnitString(column)));
	}
	
	private static String getUnitString(HaproxyStatusColumn column) {
		if (column.getUnit() == null || column.getUnit().isEmpty()) {
			return "<units/>";
		}
		return "<units>" + column.getUnit() + "</units>";
	}
	
	private static String getApplication(Application app) {
		switch (app) {
		case BACKEND:
			return "HAProxy Backend";
		case BACKEND_SERVER:
			return "HAProxy Backend Server";
		case FRONTEND:
			return "HAProxy Frontend";
		default:
			return "HAProxy";
		}
	}
	
	private static String getParameter(Application app) {
		switch (app) {
		case BACKEND:
			return "{#BACKEND_NAME},BACKEND";
		case BACKEND_SERVER:
			return "{#BACKEND_NAME},{#SERVER_NAME}";
		case FRONTEND:
			return "{#FRONTEND_NAME},FRONTEND";
		default:
			return "-";
		}
	}
	
	private static String getGraphName(Application app, HaproxyStatusColumn column) {
		switch (app) {
		case BACKEND_SERVER:
			return "[{#BACKEND_NAME}/{#SERVER_NAME}] backend server " + column.getName();
		case BACKEND:
			return "[{#BACKEND_NAME}] " + column.getName();
		case FRONTEND:
			return "[{#FRONTEND_NAME}] " + column.getName();
		default:
			return "-" + column.getName();
		}
	}
	
	private static String getItemName(Application app, HaproxyStatusColumn column) {
		switch (app) {
		case BACKEND_SERVER:
			return "[{#BACKEND_NAME}/{#SERVER_NAME}] backend server " + column.getName();
		case BACKEND:
			return "[{#BACKEND_NAME}] " + column.getName();
		case FRONTEND:
			return "[{#FRONTEND_NAME}] " + column.getName();
		default:
			return "-" + column.getName();
		}
	}

	public static List<String> readFile(final String spath) throws IOException {
		Path path = Paths.get(spath);
		System.out.println(path.toAbsolutePath().toString());
		List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
		return lines;
	}

}
