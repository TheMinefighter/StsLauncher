package javax.jnlp;

import javax.jnlp.impl.IntegrationServiceLinuxImpl;
import javax.jnlp.impl.JWSContext;
import javax.xml.parsers.ParserConfigurationException;

@SuppressWarnings("unused")
public class ServiceManager {
    private static JWSContext jwsc;
    private JWSContext getCtx() throws ParserConfigurationException {
        if (jwsc!=null) jwsc=new JWSContext();
        return jwsc;
    }
    //Mocks jnlp so far that STS does not show graphical errors and no step further
    public static Object lookup(String name) throws UnavailableServiceException {
        if (name.equals("javax.jnlp.IntegrationService")) {
            if (System.getProperty("os.name").contains("Linux"))
                return new IntegrationServiceLinuxImpl();
        }
        throw new UnavailableServiceException();}
}
