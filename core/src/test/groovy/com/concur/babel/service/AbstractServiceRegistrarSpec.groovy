package com.concur.babel.service

import com.concur.babel.BabelService
import com.concur.babel.processor.BaseInvoker
import com.concur.babel.processor.ServiceRequestDispatcher
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsString
import static spock.util.matcher.HamcrestSupport.that

class AbstractServiceRegistrarSpec extends Specification {

    @Shared mockDispatcher = Mock(ServiceRequestDispatcher)

    @Unroll
    def "should require dependencies on creation"() {
        when:
        registrar(basePackage, dispatcher, [:])

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("can not be NULL")

        where:
        [basePackage, dispatcher] << [
                ["com.company", null],
                [null, mockDispatcher]
        ]
    }

    def "should not register anything if no BabelServiceDefinitions are found"() {
        given:
        def mockDispatcher = Mock(ServiceRequestDispatcher)
        def registrar = registrar("com.company", mockDispatcher, null)

        when:
        registrar.registerBabelServices()

        then:
        0 * mockDispatcher.register(_)
    }

    def "should register service instance with dispatcher when found"() {
        given:
        def mockDispatcher = Mock(ServiceRequestDispatcher)

        def mockInvoker1 = Mock(BaseInvoker)
        def mockInvoker2 = Mock(BaseInvoker)

        def mockIfaceClass1 = Mock(BabelService)
        def mockIfaceClass2 = Mock(BabelService)

        def mockServiceImpl1 = Mock(BabelService)
        def mockServiceImpl2 = Mock(BabelService)

        def mockDefinition1 = Mock(BabelServiceDefinition) {
            getIfaceClass() >> mockIfaceClass1.class;

        }
        def mockDefinition2 = Mock(BabelServiceDefinition){
            getIfaceClass() >> mockIfaceClass2.class;
        }

        def map = [:]
        map.put(mockDefinition1, mockServiceImpl1)
        map.put(mockDefinition2, mockServiceImpl2)

        def registrar = registrar("com.company", mockDispatcher, map)

        when:
        registrar.registerBabelServices()

        then:
        1 * mockDefinition1.createInvoker(mockServiceImpl1) >> mockInvoker1
        1 * mockDefinition2.createInvoker(mockServiceImpl2) >> mockInvoker2
        1 * mockDispatcher.register(mockInvoker1)
        1 * mockDispatcher.register(mockInvoker2)

    }

    def "should not register anything when no service instance is found"() {
        given:
        def mockDefinition = Mock(BabelServiceDefinition)
        def map = [:]
        map.put(mockDefinition, null)

        def mockDispatcher = Mock(ServiceRequestDispatcher)
        def registrar = registrar("com.company", mockDispatcher, map)

        when:
        registrar.registerBabelServices()

        then:
        0 * mockDispatcher.register(_)

    }


    def registrar(basePackage, dispatcher, map) {

        new AbstractServiceRegistrar(basePackage, dispatcher, false) {

            @Override
            protected List<BabelServiceDefinition> getServiceDefinitions() {
                return (map != null ? new ArrayList(map.keySet()) : null)
            }

            @Override
            protected BabelService getServiceInstance(BabelServiceDefinition definition) {
                return (map != null ? map.get(definition) : null)
            }
        }
    }

}
