package clustering;

import ejb.MyEJBService;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.Value;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;

/**
 * Singleton MSC Service
 */
public class SingletonMSCService implements Service<String> {

    private static final Logger logger = Logger.getLogger(SingletonMSCService.class);

    private final AtomicBoolean started = new AtomicBoolean(false);

    private Value<ServerEnvironment> env;

    public SingletonMSCService(Value<ServerEnvironment> env) {
        this.env = env;
    }

    public void start(StartContext startContext) throws StartException {
        if (!started.compareAndSet(false, true)) {
            logger.info("The " + this.getClass().getName() + " is already started!");
        } else {
            logger.info("Starting the " + this.getClass().getName() + " now!");

            // NOTE: this might not work in wildfly-11.x
            MyEJBService ejbService = (MyEJBService) lookupService(MyEJBService.JNDI_NAME);
            ejbService.setActive(started.get());
        }
    }

    public void stop(StopContext stopContext) {
        if (!started.compareAndSet(true, false)) {
            logger.info("The " + this.getClass().getName() + " isn't started yet!");
        } else {
            logger.info("Stopping the " + this.getClass().getName() + " now!");

            MyEJBService ejbService = (MyEJBService) lookupService(MyEJBService.JNDI_NAME);
            ejbService.setActive(started.get());
        }
    }

    public String getValue() throws IllegalStateException, IllegalArgumentException {
        if (!this.started.get()) {
            throw new IllegalStateException();
        }
        return this.env.getValue().getNodeName();
    }


    private static Object lookupService(String jndiName) {
        try {
            return new InitialContext().lookup(jndiName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

}
