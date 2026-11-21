package dev.ivryzx.core;

import net.minecraft.client.Minecraft;
import java.util.*;

public final class ModuleManager {
    private static final List<Module> ALL = new ArrayList<>();

    private ModuleManager() {}

    /** Register once; pass one or both sections. */
    public static void register(Module m, Section... sections) {
        for (Section s : sections) m.bind(s);
        ALL.add(m);
    }

    public static List<Module> in(Section s) {
        return ALL.stream().filter(m -> m.sections().contains(s)).toList();
    }
    public static List<Module> in(Section s, Category c) {
        return in(s).stream().filter(m -> m.category == c).toList();
    }
    public static int count() { return ALL.size(); }
    public static long countByStatus(Status st) { return ALL.stream().filter(m -> m.status == st).count(); }

    public static void tick(Minecraft mc) { for (Module m : ALL) m.tick(mc); }
}
