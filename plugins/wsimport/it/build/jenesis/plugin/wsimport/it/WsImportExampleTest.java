package build.jenesis.plugin.wsimport.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.wsimport.WsImportFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class WsImportExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void generates_a_client_from_the_description_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, WsImportFactory.class)
                .write("jenesis.plugins.properties", "wsimport+binary/generated=build.jenesis.plugin.wsimport\n")
                .write("build.jenesis/plugin-wsimport.properties", """
                        package=example.greeter
                        location=http://localhost/greeter?wsdl
                        @descriptions/wsimport=../wsimport
                        """)
                .write("wsimport/greeter.wsdl", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <definitions name="Greeter"
                                     targetNamespace="https://jenesis.build/test/greeter"
                                     xmlns:tns="https://jenesis.build/test/greeter"
                                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                     xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                                     xmlns="http://schemas.xmlsoap.org/wsdl/">
                            <types>
                                <xsd:schema targetNamespace="https://jenesis.build/test/greeter">
                                    <xsd:element name="greet" type="xsd:string"/>
                                    <xsd:element name="greetResponse" type="xsd:string"/>
                                </xsd:schema>
                            </types>
                            <message name="greetRequest"><part name="parameters" element="tns:greet"/></message>
                            <message name="greetResponse"><part name="parameters" element="tns:greetResponse"/></message>
                            <portType name="GreeterPort">
                                <operation name="greet">
                                    <input message="tns:greetRequest"/>
                                    <output message="tns:greetResponse"/>
                                </operation>
                            </portType>
                            <binding name="GreeterBinding" type="tns:GreeterPort">
                                <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
                                <operation name="greet">
                                    <soap:operation soapAction="greet"/>
                                    <input><soap:body use="literal"/></input>
                                    <output><soap:body use="literal"/></output>
                                </operation>
                            </binding>
                            <service name="GreeterService">
                                <port name="GreeterPort" binding="tns:GreeterBinding">
                                    <soap:address location="https://example.invalid/greeter"/>
                                </port>
                            </service>
                        </definitions>
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("+sources/produce/assemble/binary/generated");

        assertThat(project.find("GreeterService.java"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("package example.greeter;");
    }
}
