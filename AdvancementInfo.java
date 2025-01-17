package a.custom.package.idk;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.InvocationTargetException;

public class AdvancementInfo {

    private final Advancement adv;

    private String title;
    private String description;
    private String frameType;

    public AdvancementInfo(Advancement adv) {
        this.adv = adv;
        registerKeys();
    }

    private final int MAJOR_VERSION =
            Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

    private Class<?> getNMSClass(String start, String name, boolean hasVersion) {
        String version = Bukkit.getServer().getClass().getPackage().
                getName().split("\\.")[3];
        try {
            return Class.forName((start != null ? start : "net.minecraft.server" ) +
                    (hasVersion ? "." + version : "") + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object getObject(Class<?> clazz, Object initial, String method) {
        try {
            return (clazz != null ? clazz : initial.getClass()).getDeclaredMethod(method).invoke(initial);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object getObject(Object initial, String method) {
        return getObject(null, initial, method);
    }

    private void registerKeys() {
        Class<?> craftClass = getNMSClass("org.bukkit.craftbukkit", "advancement.CraftAdvancement", true);
        if (craftClass == null) return;

        Object craftAdv = craftClass.cast(adv);
        Object advHandle = getObject(craftClass, craftAdv, "getHandle");
        if (advHandle == null) return;

        Object craftDisplay = getObject(advHandle, "c");
        if (craftDisplay == null) return;

        Object frameType = getObject(craftDisplay, "e");
        Object chatComponentTitle = getObject(craftDisplay, "a");
        Object chatComponentDesc = getObject(craftDisplay, "b");
        if (frameType == null || chatComponentTitle == null || chatComponentDesc == null) return;

        Class<?> chatClass = MAJOR_VERSION >= 17 ?
                getNMSClass("net.minecraft.network.chat", "IChatBaseComponent", false) :
                getNMSClass(null, "IChatBaseComponent", true);
        if (chatClass == null) return;

        String method = MAJOR_VERSION < 13 ? "toPlainText" : "getString";
        Object title = getObject(chatClass, chatComponentTitle, method);
        Object description = getObject(chatClass, chatComponentDesc, method);
        if (title == null || description == null) return;

        this.frameType = frameType.toString();
        this.title = title.toString();
        this.description = description.toString();
    }

    public String getFrameType() { return frameType; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
