package javax.jnlp;
@SuppressWarnings("unused")
public class ServiceManager {
    //Mocks jnlp so far that STS does not show graphical errors and no step further
    public static Object lookup(String name) throws UnavailableServiceException {
        System.out.println(name);
        throw new UnavailableServiceException();}
}
