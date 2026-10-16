package net.minecraft;
public final class class_746 {
    private final class_310 client;
    public final class_634 field_3944 = new class_634();
    public class_1703 field_7512;
    public final class_1703 field_7498 = new class_1703();
    public final class_1661 inventory = new class_1661();
    public int closes, messages;
    public class_746(class_310 client) { this.client = client; field_7512 = field_7498; }
    public class_1661 method_31548() { return inventory; }
    public void method_7346() { closes++; client.field_1755 = null; field_7512 = field_7498; }
    public void method_7353(class_2561 message, boolean overlay) { messages++; }
}
