The babel java-spring library builds on the babel java library by adding Spring support.

### SETUP

Setting up a project to use the babel java-spring library is simple:

1. Add the babel java-spring library dependency to your project.  
__Note that the version in the examples below could be out of date__.
	
	__maven:__
	
		<dependency>
			<groupId>com.concur.babel</groupId>
			<artifactId>babel-spring</artifactId>
			<version>0.0.1</version>
		</dependency>
		
	__gradle:__
	
		dependencies {
    		compile 'com.concur.babel:babel-spring:0.0.1'
    		...
    	}
    	
    __jars:__
    
    You can download the babel project and pull in the babel-spring jar and dependency jars from the distro directory under: 
    
    `[babel-root]/lib/java/spring/`
    
2.  Add a Babel configuration class to setup the ServiceRequestDispatcher bean:

		@Configuration
		public class Babel {
    		private static final Logger logger = LogManager.getLogger(Babel.class.getName());

    		@Bean
    		public ServiceRequestDispatcher getServiceRequestDispatcher() {

        		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
        		dispatcher.addCallHandler(new CallHandler() {
            		@Override
            		public void onSuccess(ServerTransport transport, long duration) {

            		}

            		@Override
            		public void onFailure(ServerTransport transport, long duration, Integer errorCode, Exception exception) {
                		logger.error("Error while invoking Babel endpoint.  Code = " + errorCode, exception);
            		}
        		});

        		return dispatcher;
    		}
		}
		
	_Note the addition of a call handler in this example as well._
    	
3.  Create a Servlet to handle babel requests and add a mapping to the new servlet in your web.xml.
	
	__Servlet example:__
	
		public class RequestResponseServlet extends HttpServlet {
    		static final Logger logger = LogManager.getLogger(RequestResponseServlet.class.getName());

    		@Autowired
    		public ServiceRequestDispatcher dispatcher;

    		protected Protocol protocol = new JSONProtocol();

    		@Override
    		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        		HttpServerTransport transport = new HttpServerTransport(req, resp, protocol);

        		try {
            		dispatcher.dispatch(transport);
        		} catch (Exception e) {
            		// Babel ServiceRequestDispatcher catches any exception but in case an exception happens
            		// when initializing request resources before calling the dispatcher we can handle those
            		// here.
            		logger.error("Error while dispatching Babel call", e);
            		dispatcher.sendErrorResponse(e, transport, null);
        		}
    		}

    		@Override
    		public void init(ServletConfig config) throws ServletException {
        		super.init(config);
        		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    		}
		}
		
	__web.xml example:__
		
		<servlet>
        	<servlet-name>babel</servlet-name>
        	<display-name>babel</display-name>
        	<servlet-class>com.concur.RequestResponseServlet</servlet-class>
        	<load-on-startup>1</load-on-startup>
    	</servlet>


	
4.  Register your babel service implementions with the ServiceRequestDispatcher. This can be done in a couple of ways:
	* Dymamic registration using the @BabelServiceRegistration annotation.  Simply add this annotation to your Babel configuration class and supply a basePackage value to scan for babel generated classes that need to be registered.
	* Dynamic registration using XML configuration.  Simply configure the ComponentScanningServiceRegistrarConfigurer bean in a spring context XML file.
	
		@BabelServiceRegistration example:
	
			@Configuration
			@BabelServiceRegistration(basePackage = "com.concur")
			public class Babel {
    			private static final Logger logger = LogManager.getLogger(Babel.class.getName());

    			@Bean
    			public ServiceRequestDispatcher getServiceRequestDispatcher() {
    				...
    			}
    		}
    		
    	XML config example:
    	
    		<bean class="com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer">
        		<property name="basePackage" value="com.concur" />        
    		</bean>
    		
    	__In both cases, your service implementation classes need to be Spring components.__
    	
    	ExampleServiceImpl example:
    	
    		@Component
			public class ExampleServiceImpl implements ExampleService.Iface {
				...
			}

    	
    		
	* Manual registration of babel services.  Since this library provides a means of dynamic registration, manual registration is not a recommended option under normal circumstances.  
	
## Components

[com.concur.babel.spring.service.ComponentScanningServiceRegistrar](#registrar) - Can be leveraged to dynamically register babel services.
	
### <a name="registrar">ComponentScanningServiceRegistrar</a>
The `com.concur.babel.spring.service.ComponentScanningServiceRegistrar` component can be used to dynamically register babel service implementations with the babel ServiceRequestDispatcher.  The component can be configured via annotation, or as an XML bean.

In both cases, a com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer bean is registered with Spring. 

* The @BabelServiceRegistration annotation will register the ComponentScanningServiceRegistrarConfigurer bean during the Spring initialization lifecycle.
* The ComponentScanningServiceRegistrarConfigurer bean can be registered directly in a Spring XML file.  

This bean implements the ApplicationListener interface and responds to the ContextRefreshedEvent event by invoking the ComponentScanningServiceRegistrar.  This way, the Spring initialization lifecyle is allowed to complete, and all configuration, components, and beans are available prior to invoking the ComponentScanningServiceRegistrar.

The ComponentScanningServiceRegistrar will scan the classpath for babel generated classes, find the service implementation beans, and register them with the ServiceRequestDispatcher. See the Setup section above for detailed examples.

The ComponentScanningServiceRegistrar supports other optional properties:

* dispatcherReference - indicates that the ServiceRequestDispatcher should be looked up by reference, rather than by type.
* failWhenNoServiceInstanceFound - if true, an exception will be thrown when a service implementation instance cannot be found for a babel generated service interface.  Default value is false.
    	
	
 