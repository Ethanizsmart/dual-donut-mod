package net.minecraft;
public final class class_1799 {
    public static final class_1799 EMPTY = new class_1799(null);
    private final class_1792 item;
    public class_1799(class_1792 item) { this.item = item; }
    public boolean method_7960() { return item == null; }
    public class_1792 method_7909() { return item; }
    public boolean method_31574(class_1792 other) { return other.equals(item); }
}
