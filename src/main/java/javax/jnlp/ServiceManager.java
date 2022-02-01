package javax.jnlp;

import javax.jnlp.impl.IntegrationServiceLinuxImpl;

@SuppressWarnings("unused")
public class ServiceManager {
    //Mocks jnlp so far that STS does not show graphical errors and no step further
    public static Object lookup(String name) throws UnavailableServiceException {
        if (name.equals("javax.jnlp.IntegrationService")) {
            if (System.getProperty("os.name").contains("Linux"))
                return new IntegrationServiceLinuxImpl();
        }
        throw new UnavailableServiceException();}
}
