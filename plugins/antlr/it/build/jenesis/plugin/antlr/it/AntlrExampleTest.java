package build.jenesis.plugin.antlr.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.antlr.AntlrFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class AntlrExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void generates_a_parser_from_the_grammar_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, AntlrFactory.class)
                .write("jenesis.plugins.properties", "antlr+binary/generated=build.jenesis.plugin.antlr\n")
                .write("build.jenesis/plugin-antlr.properties", """
                        package=example.grammar
                        @grammars/antlr=../grammar
                        """)
                .write("grammar/Greeting.g4", """
                        grammar Greeting;

                        greeting : HELLO NAME EOF ;

                        HELLO : 'hello' ;
                        NAME : [a-zA-Z]+ ;
                        WHITESPACE : [ \\\\t\\\\r\\\\n]+ -> skip ;
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("+sources/produce/assemble/binary/generated");

        assertThat(project.find("GreetingParser.java"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("package example.grammar;");
    }
}
