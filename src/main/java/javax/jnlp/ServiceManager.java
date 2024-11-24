package javax.jnlp;

import javax.jnlp.impl.IntegrationServiceLinuxImpl;
import javax.jnlp.impl.JnlpServiceImpl;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ServiceManager {
	private static final Map<String, JnlpServiceImpl> services = new HashMap<>();

	static {
		if (System.getProperty("os.name").contains("Linux"))
			services.put("javax.jnlp.IntegrationService", new IntegrationServiceLinuxImpl());
	}

	//Mocks jnlp so far that STS does not show graphical errors and no step further
	public static Object lookup(String name) throws UnavailableServiceException {
		if (services.containsKey(name)) return services.get(name);
		throw new UnavailableServiceException();
	}
}
