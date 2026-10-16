package net.minecraft;
public final class class_7923 {
    public static final Registry field_41178 = new Registry();
    public static final class Registry {
        public Id method_10221(class_1792 item) { return new Id(item.id()); }
    }
    public record Id(String id) { public String method_12832() { return id; } }
}
