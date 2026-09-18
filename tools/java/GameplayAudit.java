import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;

/** Regression guard for named Monkey classes, before loader remapping.
 * This checks known movement/input mutation paths, not server approval. */
public class GameplayAudit {
    private static final Set<String> CALLS = Set.of(
        "setSprinting", "setFlyingSpeed", "setDeltaMovement", "setYRot", "setXRot",
        "setRot", "lerpMotion", "startAttack", "continueAttack", "attack", "swing",
        "setDown", "click", "onUpdateAbilities", "turn");
    private static final Set<String> FIELDS = Set.of(
        "flying", "mayfly", "flyingSpeed", "walkingSpeed", "noPhysics",
        "accumulatedDX", "accumulatedDY", "keyPresses");
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]); int count = 0;
        try (var paths = Files.walk(root.resolve("gg/monkeyclient"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".class")).toList()) {
                ClassNode type = new ClassNode();
                new ClassReader(Files.readAllBytes(path)).accept(type, 0); count++;
                if (type.name.endsWith("/ToggleSprint") || type.name.endsWith("/SprintMixin"))
                    throw new AssertionError("Removed class packaged: " + type.name);
                for (FieldNode field : type.fields)
                    if (field.name.equals("accumulatedDX") || field.name.equals("accumulatedDY"))
                        throw new AssertionError("Raw mouse shadow remains: " + type.name);
                for (MethodNode method : type.methods) for (var instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call &&
                        ((call.owner.startsWith("net/minecraft/") && CALLS.contains(call.name)) ||
                         call.owner.startsWith("net/minecraft/network/protocol/game/Serverbound")))
                        throw new AssertionError(type.name + "." + method.name + " calls " + call.owner + "." + call.name);
                    if (instruction instanceof FieldInsnNode field &&
                        (field.getOpcode() == Opcodes.PUTFIELD || field.getOpcode() == Opcodes.PUTSTATIC) &&
                        FIELDS.contains(field.name))
                        throw new AssertionError(type.name + "." + method.name + " writes " + field.name);
                }
            }
        }
        if (count < 100) throw new AssertionError("Incomplete class output: " + count);
        System.out.println("Gameplay mutation audit: " + count + " Monkey classes; no prohibited calls, fields or removed classes");
    }
}
