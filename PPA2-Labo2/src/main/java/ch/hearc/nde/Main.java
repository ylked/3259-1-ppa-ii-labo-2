package ch.hearc.nde;

import ch.hearc.nde.manager.BufferManager;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class Main {
    public static void main(String[] args) throws Exception {
        // Register MBean
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("Labo2:name=bufferManager");

        BufferManager bufferManager = new BufferManager(100, 2, 2, true);
        server.registerMBean(bufferManager, objectName);

        bufferManager.run();
    }
}
