package build.jenesis.plugin.spotless;

import module java.base;

public record SpotlessSet(String name, List<String> targets, List<SpotlessStep> steps) implements Serializable {

    public boolean matches(Path file) {
        return targets.stream()
                .map(target -> FileSystems.getDefault().getPathMatcher("glob:" + target))
                .anyMatch(matcher -> IntStream.range(0, file.getNameCount())
                        .anyMatch(index -> matcher.matches(file.subpath(index, file.getNameCount()))));
    }
}
