Wildfly Clustering Singleton MSC Service Sample
===============================================

This sample installs a simple singleton MSC Service service and simple EJB Singleton Service to monitor the status of the node.
And has a simple servlet based health check endpoint to return the status of the node.


1. To run the app inn single standalone instance,


        % mvn wildfly:run

   or


2. To test the app in an cluster (change the port as per your need), 


    a. Download and extract the wildfly instance to two different locations:

            % wget http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip
            % unzip wildfly-10.1.0.Final.zip -d wildfly-node1
            % unzip wildfly-10.1.0.Final.zip -d wildfly-node2


    b. Start the first wildfly instance:
    
            % cd wildfly-node1
            % ./bin/standalone.sh -Djava.net.preferIPv4Stack=true --debug 8787 --server-config=standalone-full-ha.xml -Djboss.node.name=node1
    

    c. Start the second wildfly instance:

            % cd wildfly-node2
            % ./bin/standalone.sh -Djava.net.preferIPv4Stack=true --debug 9797 --server-config=standalone-full-ha.xml -Djboss.node.name=node2 -Djboss.socket.binding.port-offset=100
    

    d. Deploy the app in first instance (node1):

            % mvn wildfly:deploy

    
    e. Deploy in app in second instance (node2):

            % mvn wildfly:deploy -Dwildfly.port=10090


3. Check the response of the health check end point,

        % curl http://localhost:8080/singleton/health
        % curl http://localhost:8180/singleton/health



Testing (Failure Case)
----------------------

1. Check the health status of the both nodes (both should be consistent):


        % curl http://localhost:8080/singleton/health
        % curl http://localhost:8180/singleton/health


2. Let's pause the JVM process


        % jps -l
        {PID list}
    
        % kill -STOP {PID}


3. Check the health status of the both nodes (both shouldn't be responding for 60 secs and then it'll be inconsistent):


        % curl http://localhost:8080/singleton/health
        % curl http://localhost:8180/singleton/health


3. Resume the paused process after 60 secs and the let both the instances merge its cluster view (wait for few more seconds).


        % kill -CONT {PID}


4. Check the health status of the both nodes (both will be inconsistent and both assumes itself as master/active node):


        % curl http/localhost:8080/singleton/health
        % curl http://localhost:8180/singleton/health
