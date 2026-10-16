package build.jenesis.plugin.xjc.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.xjc.XjcFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class XjcExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void generates_classes_from_the_schema_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, XjcFactory.class)
                .write("jenesis.plugins.properties", "xjc+binary/generated=build.jenesis.plugin.xjc\n")
                .write("build.jenesis/plugin-xjc.properties", """
                        package=example.order
                        @schemas/xjc=../xjc
                        """)
                .write("xjc/order.xsd", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:tns="https://jenesis.build/test/order"
                                   targetNamespace="https://jenesis.build/test/order"
                                   elementFormDefault="qualified">
                            <xs:element name="order" type="tns:Order"/>
                            <xs:complexType name="Order">
                                <xs:attribute name="id" type="xs:string" use="required"/>
                            </xs:complexType>
                        </xs:schema>
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("+sources/produce/assemble/binary/generated");

        assertThat(project.find("Order.java"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("package example.order;");
    }
}
