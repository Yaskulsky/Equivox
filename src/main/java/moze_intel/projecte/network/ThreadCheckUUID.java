package moze_intel.projecte.network;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.PECore;

public class ThreadCheckUUID extends Thread {

	private static boolean hasRunServer = false;
	private static boolean hasRunClient = false;
	private static final URI uuidURL;
	private final boolean isServerSide;

	static {
		try {
			uuidURL = new URI("https://raw.githubusercontent.com/sinkillerj/ProjectE/mc1.14.x/haUUID.txt");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public ThreadCheckUUID(boolean isServer) {
		this.isServerSide = isServer;
		this.setName("ProjectE UUID Checker " + (isServer ? "Server" : "Client"));
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(uuidURL.toURL().openStream()))) {
			String line = reader.readLine();

			if (line == null) {
				PECore.LOGGER.error(LogUtils.FATAL_MARKER, "UUID check failed!");
				throw new IOException("No data from github UUID list!");
			}

			List<String> uuids = new ArrayList<>();

			while ((line = reader.readLine()) != null) {
				if (line.startsWith("###UUID")) {
					break;
				}

				if (!line.isEmpty()) {
					uuids.add(line);
				}
			}

			PECore.uuids.addAll(uuids);
		} catch (IOException e) {
			PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Caught exception in UUID Checker thread!", e);
		} finally {
			if (isServerSide) {
				hasRunServer = true;
			} else {
				hasRunClient = true;
			}
		}
	}

	public static boolean hasRunServer() {
		return hasRunServer;
	}

	public static boolean hasRunClient() {
		return hasRunClient;
	}
}