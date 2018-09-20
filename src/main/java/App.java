import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class App {


    private static LinkedList<Tuple<Segment, String>> appendTupleTo(Tuple element, List<Tuple<Segment, String>> es) {
        LinkedList<Tuple<Segment, String>> newSolution = new LinkedList<>(es);
        newSolution.add(element);
        return newSolution;
    }

    private static <X> List<X> tail(List<X> objects) {
        if (objects.isEmpty()) {
            return objects;
        }
        return objects.subList(1, objects.size());
    }

    public static Optional<Response> doApp(String url, String template) {
        List<Segment> segments = parseTemplate(template);
        return parseURL(url)
                .flatMap(parsedURL ->
                        merge(parsedURL.getPath(), segments)
                        .map(parameters -> new Response(parsedURL.protocol, parsedURL.host, parsedURL.path, parameters)
                        )
                );
    }

    private static Optional<List<Tuple<String, String>>> merge(String path, List<Segment> segments) {
        String[] split = path.split("/");

        List<Tuple<Segment, String>> tuples = doFindSolution(segments, Arrays.asList(split), new LinkedList<Tuple<Segment, String>>());
        if (tuples.isEmpty()) {
            return Optional.empty();
        }
        List<Tuple<String, String>> collect = tuples.stream()
                .filter(tuple -> tuple.x instanceof PathParam && !tuple.y.isEmpty())
                .map(t -> new Tuple<>(t.x.value(), t.y))
                .collect(Collectors.toList());
        return Optional.of(collect);

    }

    private static Optional<MyUrl> parseURL(String input) {
        Scanner s = new Scanner(input);
        String inLine = s.findInLine("([^://]+)://([^/]+)/?(.*)");
        if (inLine != null) {
            MatchResult result = s.match();
            String protocol = result.group(1);
            String host = result.group(2);
            String path = result.group(3);
            s.close();
            return Optional.of(new MyUrl(protocol, host, path));
        }
        return Optional.empty();
    }

    private static List<Tuple<Segment, String>> doFindSolution(List<Segment> objects, List<String> pathSegments, List<Tuple<Segment, String>> es) {
        if (objects.size() > 0) {
            Segment segment = objects.get(0);
            if (segment.isOptionable() || (pathSegments.size() > 0 && segment.canBeAccepted(pathSegments.get(0)))) {
                List<Tuple<Segment, String>> tuples = Collections.emptyList();
                if (pathSegments.size() > 0) {
                    tuples = doFindSolution(tail(objects), tail(pathSegments), appendTupleTo(new Tuple(segment, pathSegments.get(0)), es));
                }
                if (tuples.isEmpty() && segment.isOptionable()) {
                    tuples = doFindSolution(tail(objects), pathSegments, appendTupleTo(new Tuple(segment, ""), es));
                }
                return tuples;
            }
            return Collections.emptyList();

        }
        if (pathSegments.size() > 0) return Collections.emptyList();
        return es;
    }

    private static List<Segment> parseTemplate(String input) {
        Pattern pattern = Pattern.compile("\\[?/[^/\\[]*");
        Matcher matcher = pattern.matcher(input);

        List<Segment> segments = new ArrayList<>();

        while (matcher.find()) {
            String token = matcher.group();
            String pureSegment = token;
            boolean optional = false;
            if (token.startsWith("[")) {
                pureSegment = token.substring(1, token.length() - 1);
                optional = true;
            }
            segments.add(buildSegment(pureSegment, optional));
        }
        return segments;
    }

    private static Segment buildSegment(String token, boolean optional) {
        if (token.startsWith("/:")) {
            return new PathParam(token.substring(2), optional);
        } else {
            return new PathElement(token.substring(1), optional);
        }
    }

    private interface Segment {
        boolean isOptionable();

        boolean canBeAccepted(String input);

        String value();

    }

    public static class Response {
        private final String scheme;
        private final String host;
        private final String path;
        private final List<Tuple<String, String>> parameters;

        public Response(String scheme, String host, String path, List<Tuple<String, String>> parameters) {
            this.scheme = scheme;
            this.host = host;
            this.path = path;
            this.parameters = parameters;
        }

        public String getScheme() {
            return scheme;
        }

        public String getHost() {
            return host;
        }

        public String getPath() {
            return path;
        }

        public List<Tuple<String, String>> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return '{' +
                    Arrays.asList(scalarVal("scheme", scheme), scalarVal("host", host), scalarVal("path", '/' + path), objectVal("parameters", stringifyParams(parameters))).stream().collect(Collectors.joining(","))
                    + '}';
        }

        private String scalarVal(String key, String value) {
            return '\"' + key + "\":\"" + value + '\"';
        }

        private String objectVal(String key, String value) {
            return '\"' + key + "\":{" + value + '}';
        }

        private String stringifyParams(List<Tuple<String, String>> parameters) {
            return parameters.stream().map(t -> scalarVal(t.x, t.y)).collect(Collectors.joining(","));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Response response = (Response) o;
            return Objects.equals(scheme, response.scheme) &&
                    Objects.equals(host, response.host) &&
                    Objects.equals(path, response.path) &&
                    Objects.equals(parameters, response.parameters);
        }

        @Override
        public int hashCode() {

            return Objects.hash(scheme, host, path, parameters);
        }
    }

    private static class MyUrl {
        private final String protocol;
        private final String host;
        private final String path;

        public MyUrl(String protocol, String host, String path) {
            this.protocol = protocol;
            this.host = host;
            this.path = path;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public String getPath() {
            return path;
        }
    }

    public static final class Tuple<X, Y> {
        private final X x;
        private final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public X getX() {
            return x;
        }

        public Y getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tuple<?, ?> tuple = (Tuple<?, ?>) o;
            return Objects.equals(x, tuple.x) &&
                    Objects.equals(y, tuple.y);
        }

        @Override
        public int hashCode() {

            return Objects.hash(x, y);
        }
    }

    private static class PathElement implements Segment {
        private final String value;
        private final boolean optionable;

        public PathElement(String value, boolean optionable) {
            this.value = value;
            this.optionable = optionable;
        }

        @Override
        public boolean isOptionable() {
            return optionable;
        }

        @Override
        public boolean canBeAccepted(String input) {
            return input.equals(value);
        }

        @Override
        public String value() {
            return value;
        }


    }

    private static class PathParam implements Segment {
        private final String name;
        private final boolean optionable;

        public PathParam(String name, boolean optionable) {
            this.name = name;
            this.optionable = optionable;
        }

        @Override
        public boolean isOptionable() {
            return optionable;
        }

        @Override
        public boolean canBeAccepted(String input) {
            return true;
        }

        @Override
        public String value() {
            return name;
        }
    }
}
