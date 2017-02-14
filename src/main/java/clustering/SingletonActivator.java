package clustering;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.clustering.singleton.SingletonServiceBuilderFactory;
import org.wildfly.clustering.singleton.SingletonServiceName;

import org.jboss.logging.Logger;

/**
 *  Activates the SingletonMSCService
 */
public class SingletonActivator implements ServiceActivator {

    private static final String CONTAINER_NAME = "server";

    private static final Logger logger = Logger.getLogger(SingletonActivator.class);

    public static final ServiceName SINGLETON_SERVICE_NAME = ServiceName.parse("clustering.SingletonActivator");

    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        logger.info("Installing the SingletonMSCService...");

        InjectedValue<ServerEnvironment> env = new InjectedValue<>();
        Service<?> service = new SingletonMSCService(env);
        try {
            SingletonServiceBuilderFactory factory = (SingletonServiceBuilderFactory) context.getServiceRegistry()
                    .getRequiredService(SingletonServiceName.BUILDER.getServiceName(CONTAINER_NAME)).awaitValue();

            factory.createSingletonServiceBuilder(SINGLETON_SERVICE_NAME, service)
                    .build(context.getServiceTarget())
                    .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, env)
                    .install();

        } catch (InterruptedException e) {
            throw new ServiceRegistryException(e);
        }
    }

}
