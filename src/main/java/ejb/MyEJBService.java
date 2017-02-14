package ejb;

import clustering.SingletonActivator;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;

/**
 * EJB Singleton Service - tracks the node activity (active or passive) in cluster
 */
@Singleton
@Startup
public class MyEJBService {

    public static final String JNDI_NAME = "java:global/wildfly-singleton-service/MyEJBService!ejb.MyEJBService";

    private static final Logger logger = Logger.getLogger(MyEJBService.class);

    private final AtomicBoolean active = new AtomicBoolean(false);

    private String nodeName = "undefined";

    @Resource(lookup = "java:jboss/clustering/group/server")
    private Group serverChannelGroup;

    MyEJBService() {
        nodeName = System.getProperty("jboss.node.name");
    }

    @PostConstruct
    private void registerClusterListener() {

        // register a cluster listener to the jgroups channel
        serverChannelGroup.addListener((previousMembers, members, merged) -> {

            logger.debug("======> Cluster View Changed <=======");
            for (Node node : previousMembers) {
                logger.debug("Previous Cluster View: " + node.getName() + " " + node.getSocketAddress());
            }
            logger.debug("==================================================");
            for (Node node : members) {
                logger.debug("New Cluster View " + node.getName() + " " + node.getSocketAddress());
            }
            logger.debug("==================================================");
            logger.debug("Is Cluster View Merged? " + merged);

        });

    }

    public void setActive(boolean active) {

        boolean previouslyActive = this.active.getAndSet(active);
        logger.info("Changing the node status: " + ((previouslyActive) ? "Active" : "Passive") +
                " -> " + ((active) ? "Active" : "Passive"));
    }

    /**
     * Checks the local state to find out this node status.
     *
     * @return true if this node is the master/active
     */
    public boolean isActive() {
        return this.active.get();
    }

    /**
     * Checks the MSC Singleton Service to find out the real master node.
     *
     * @return true if this node is the master/active
     */
    public boolean isReallyActive() {
        @SuppressWarnings("unchecked")
        ServiceController<String> service = (ServiceController<String>) CurrentServiceContainer.getServiceContainer()
                .getService(SingletonActivator.SINGLETON_SERVICE_NAME);
        return nodeName.equals(service.getValue());
    }

}
