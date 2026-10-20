package build.jenesis.plugin.mavenplugin;

import module java.base;

public final class MavenPluginDriver {

    private MavenPluginDriver() {
    }

    public static int main(String[] arguments) throws Throwable {
        Properties request = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(arguments[0]))) {
            request.load(reader);
        }
        ClassLoader loader = MavenPluginDriver.class.getClassLoader();
        try {
            Object model = instance(loader, "org.apache.maven.model.Model");
            invoke(model, "setGroupId", request.getProperty("group"));
            invoke(model, "setArtifactId", request.getProperty("artifact"));
            invoke(model, "setVersion", request.getProperty("version"));
            invoke(model, "setPackaging", "maven-plugin");
            invoke(model, "setName", request.getProperty("name"));
            invoke(model, "setDescription", request.getProperty("description"));
            Class<?> modelType = loader.loadClass("org.apache.maven.model.Model");
            Object project = loader.loadClass("org.apache.maven.project.MavenProject")
                    .getConstructor(modelType)
                    .newInstance(model);
            File work = new File(request.getProperty("work"));
            invoke(project, "setFile", new File(work, "pom.xml"));
            Object build = invoke(project, "getBuild");
            invoke(build, "setDirectory", work.getAbsolutePath());
            invoke(build, "setOutputDirectory", new File(request.getProperty("classes")).getAbsolutePath());
            for (int index = 0; request.containsKey("sources." + index); index++) {
                invoke(project,
                        "addCompileSourceRoot",
                        new File(request.getProperty("sources." + index)).getAbsolutePath());
            }
            Class<?> handlerType = loader.loadClass("org.apache.maven.artifact.handler.ArtifactHandler");
            Object handler = loader.loadClass("org.apache.maven.artifact.handler.DefaultArtifactHandler")
                    .getConstructor(String.class)
                    .newInstance("jar");
            Constructor<?> artifactConstructor = loader.loadClass("org.apache.maven.artifact.DefaultArtifact")
                    .getConstructor(String.class,
                            String.class,
                            String.class,
                            String.class,
                            String.class,
                            String.class,
                            handlerType);
            invoke(project, "setArtifact", artifactConstructor.newInstance(request.getProperty("group"),
                    request.getProperty("artifact"),
                    request.getProperty("version"),
                    null,
                    "maven-plugin",
                    null,
                    handler));
            Set<Object> artifacts = new LinkedHashSet<>();
            for (int index = 0; request.containsKey("dependency." + index); index++) {
                String[] coordinate = request.getProperty("dependency." + index).split(":");
                Object artifact = artifactConstructor.newInstance(coordinate[0],
                        coordinate[1],
                        coordinate[2],
                        coordinate[3],
                        "jar",
                        null,
                        handler);
                invoke(artifact, "setFile", new File(request.getProperty("dependency." + index + ".file")));
                artifacts.add(artifact);
            }
            invoke(project, "setArtifacts", artifacts);
            Object configuration = instance(loader, "org.codehaus.plexus.DefaultContainerConfiguration");
            invoke(configuration, "setClassWorld", loader.loadClass("org.codehaus.plexus.classworlds.ClassWorld")
                    .getConstructor(String.class, ClassLoader.class)
                    .newInstance("plexus.core", loader));
            invoke(configuration, "setClassPathScanning", "index");
            invoke(configuration, "setAutoWiring", true);
            invoke(configuration, "setName", "jenesis");
            Object container = loader.loadClass("org.codehaus.plexus.DefaultPlexusContainer")
                    .getConstructor(loader.loadClass("org.codehaus.plexus.ContainerConfiguration"))
                    .newInstance(configuration);
            try {
                Class<?> descriptorType = loader.loadClass("org.apache.maven.plugin.descriptor.PluginDescriptor");
                Object descriptor = descriptorType.getConstructor().newInstance();
                invoke(descriptor, "setGroupId", request.getProperty("group"));
                invoke(descriptor, "setArtifactId", request.getProperty("artifact"));
                invoke(descriptor, "setVersion", request.getProperty("version"));
                invoke(descriptor, "setGoalPrefix", request.getProperty("goalprefix") == null
                        ? descriptorType.getMethod("getGoalPrefixFromArtifactId", String.class)
                                .invoke(null, request.getProperty("artifact"))
                        : request.getProperty("goalprefix"));
                invoke(descriptor, "setName", request.getProperty("name"));
                invoke(descriptor, "setDescription", request.getProperty("description"));
                invoke(descriptor,
                        "setDependencies",
                        loader.loadClass("org.apache.maven.tools.plugin.generator.GeneratorUtils")
                                .getMethod("toComponentDependencies", Collection.class)
                                .invoke(null, artifacts));
                Object toolsRequest = loader.loadClass("org.apache.maven.tools.plugin.DefaultPluginToolsRequest")
                        .getConstructor(project.getClass(), descriptorType)
                        .newInstance(project, descriptor);
                invoke(toolsRequest, "setEncoding", request.getProperty("encoding"));
                invoke(toolsRequest, "setDependencies", artifacts);
                Object scanner = invoke(container,
                        "lookup",
                        loader.loadClass("org.apache.maven.tools.plugin.scanner.MojoScanner"));
                invoke(scanner, "populatePluginDescriptor", toolsRequest);
                Object extended = loader.loadClass("org.apache.maven.tools.plugin.ExtendedPluginDescriptor")
                        .getConstructor(descriptorType)
                        .newInstance(invoke(toolsRequest, "getPluginDescriptor"));
                invoke(extended, "setRequiredJavaVersion", invoke(toolsRequest, "getRequiredJavaVersion"));
                invoke(extended, "setRequiredMavenVersion", invoke(toolsRequest, "getUsedMavenApiVersion"));
                invoke(toolsRequest, "setPluginDescriptor", extended);
                invoke(instance(loader, "org.apache.maven.tools.plugin.generator.PluginDescriptorFilesGenerator"),
                        "execute",
                        new File(request.getProperty("output")),
                        toolsRequest);
            } finally {
                invoke(container, "dispose");
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return 0;
    }

    private static Object instance(ClassLoader loader, String type) throws ReflectiveOperationException {
        return loader.loadClass(type).getConstructor().newInstance();
    }

    private static Object invoke(Object target, String name, Object... arguments) throws ReflectiveOperationException {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Class<?> candidate : Stream.concat(Stream.of(type), Arrays.stream(type.getInterfaces())).toList()) {
                for (Method method : candidate.getMethods()) {
                    if (method.getName().equals(name) && accepts(method.getParameterTypes(), arguments)) {
                        return method.invoke(target, arguments);
                    }
                }
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name + " taking " + arguments.length
                + " arguments");
    }

    private static boolean accepts(Class<?>[] parameters, Object[] arguments) {
        if (parameters.length != arguments.length) {
            return false;
        }
        for (int index = 0; index < parameters.length; index++) {
            if (arguments[index] == null) {
                if (parameters[index].isPrimitive()) {
                    return false;
                }
            } else if (!MethodType.methodType(parameters[index]).wrap().returnType().isInstance(arguments[index])) {
                return false;
            }
        }
        return true;
    }
}
