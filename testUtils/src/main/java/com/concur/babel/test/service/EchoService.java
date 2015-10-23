package com.concur.babel.test.service;

import com.concur.babel.BabelService;
import com.concur.babel.ResponseServiceMethod;
import com.concur.babel.ServiceMethod;
import com.concur.babel.processor.BaseInvoker;
import com.concur.babel.service.BabelServiceDefinition;
import com.concur.babel.transport.BaseClient;
import com.concur.babel.transport.Transport;

import java.util.HashMap;
import java.util.Map;

public class EchoService implements BabelServiceDefinition {

    /**
     * Gets the interface class for this service.
     */
    public Class<Iface> getIfaceClass() { return Iface.class; }

    /**
     * Convenience method to create a new Invoker instance containing the service iface implementation.
     */
    public Invoker createInvoker(BabelService iFaceImpl) { return new Invoker((Iface)iFaceImpl); }

    /**
     * The interface defining the methods for this service. You should provide an implmentation of this interface. ServiceName.Iface.
     */
    public interface Iface extends BabelService {


        String echo(String message);


    }
    /**
     * Client can be created to make a service call for any of the methods defined in this service.
     */
    public static class Client extends BaseClient implements Iface {

        public Client(String url) { super(url); }
        public Client(String url, int timeoutInMillis) { super(url, timeoutInMillis); }
        public Client(Transport transport) { super(transport); }

        public String echo(String message) {

            Echo serviceMethod = new Echo(message);
            return this.transport.invoke(serviceMethod);

        }

    }

    /**
     * Invoker is a component in the babel framework used to call service methods.  This component contains the service iface implementation and needs to be registered with the ServiceRequestDispatcher.
     */
    public static class Invoker extends BaseInvoker<Iface> {

        public Invoker(Iface serviceImpl) { super(serviceImpl); }

        public Map<String, Class<? extends ServiceMethod>> initServiceMethods() {
            Map<String, Class<? extends ServiceMethod>> map = new HashMap<String, Class<? extends ServiceMethod>>();
            map.put("echo", Echo.class);
            return map;
        }

        public String getServiceName() { return "EchoService"; }
        public Class<Iface> getInterface() { return Iface.class; }

    }


    private static class Echo extends ResponseServiceMethod<String> {

        private String message;

        public Echo() {}
        public Echo(String message) {

            this.message = message;
        }

        public String getServiceName() { return "EchoService"; }
        public String getMethodName() { return "echo"; }

        public Object[] getMethodParameters() {
            return new Object[] { this.message };
        }
    }
}
