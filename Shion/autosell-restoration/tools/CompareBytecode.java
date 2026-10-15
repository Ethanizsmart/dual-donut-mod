import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;

// Reads classfiles only. Never loads or executes mod classes.
class CompareBytecode {
    static Map<String, ClassNode> read(String file) throws IOException {
        var result = new TreeMap<String, ClassNode>();
        try (var jar = new JarFile(file)) {
            for (var entry : Collections.list(jar.entries())) {
                if (!entry.getName().endsWith(".class")) continue;
                var node = new ClassNode();
                new ClassReader(jar.getInputStream(entry)).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                result.put(node.name, node);
            }
        }
        return result;
    }
    static Set<String> fields(ClassNode c) {
        var result = new TreeSet<String>();
        for (var f : c.fields) result.add(f.name + " " + f.desc + " " + f.access + " " + Objects.toString(f.value));
        return result;
    }
    static Map<String, MethodNode> methods(ClassNode c) {
        var result = new TreeMap<String, MethodNode>();
        for (var m : c.methods) result.put(m.name + m.desc, m);
        return result;
    }
    static String code(MethodNode method) {
        var printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        var writer = new StringWriter();
        printer.print(new PrintWriter(writer));
        return writer.toString();
    }
    public static void main(String[] args) throws Exception {
        var original = read(args[0]);
        var rebuilt = read(args[1]);
        var output = Path.of(args[2]);
        Files.createDirectories(output);
        int identical = 0, changed = 0;
        for (var name : original.keySet()) {
            var a = original.get(name);
            var b = rebuilt.get(name);
            if (b == null) { System.out.println("MISSING_CLASS " + name); continue; }
            if (!fields(a).equals(fields(b))) System.out.println("FIELD_DIFFERENCE " + name);
            if (a.access != b.access || !Objects.equals(a.superName, b.superName) || !a.interfaces.equals(b.interfaces))
                System.out.println("CLASS_HEADER_DIFFERENCE " + name);
            var am = methods(a);
            var bm = methods(b);
            var matched = new HashSet<String>();
            for (var key : am.keySet()) {
                var m = am.get(key);
                var n = bm.get(key);
                if (n == null && m.name.startsWith("lambda$")) {
                    var candidates = b.methods.stream().filter(x -> x.name.startsWith("lambda$") && x.desc.equals(m.desc)).toList();
                    if (candidates.size() == 1) {
                        n = candidates.get(0);
                        System.out.println("RENAMED_SYNTHETIC " + name + "." + key + " -> " + n.name);
                    }
                }
                if (n == null) { System.out.println("MISSING_METHOD " + name + "." + key); continue; }
                matched.add(n.name + n.desc);
                if (m.access != n.access || !m.exceptions.equals(n.exceptions)) System.out.println("METHOD_HEADER_DIFFERENCE " + name + "." + key);
                var left = code(m);
                var right = code(n);
                if (left.equals(right)) { identical++; continue; }
                changed++;
                System.out.println("CODE_DIFFERENCE " + name + "." + key);
                String stem = name.replace('/', '.') + "." + m.name.replace('<', '_').replace('>', '_') + "-" + Integer.toHexString(m.desc.hashCode());
                Files.writeString(output.resolve(stem + ".original.txt"), left);
                Files.writeString(output.resolve(stem + ".rebuilt.txt"), right);
            }
            for (var key : bm.keySet()) if (!matched.contains(key)) System.out.println("EXTRA_METHOD " + name + "." + key);
        }
        for (var name : rebuilt.keySet()) if (!original.containsKey(name)) System.out.println("EXTRA_CLASS " + name);
        System.out.println("IDENTICAL_METHOD_TEXT=" + identical + " CHANGED_METHOD_TEXT=" + changed);
    }
}
