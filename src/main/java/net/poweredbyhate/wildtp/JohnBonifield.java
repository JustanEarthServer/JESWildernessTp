package net.poweredbyhate.wildtp;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Created on 8/16/2017.
 *
 * A "fake" player used to probe protection plugins without side effects.
 * Only read-only lookups are forwarded to the real player; everything else is a no-op.
 *
 * @author RoboMWM
 */
final class JohnBonifield
{
    private static final Set<String> FORWARDED = Set.of(
            "spigot", "getLocation", "getWorld", "getPersistentDataContainer", "getUniqueId",
            "getName", "getDisplayName", "getAddress", "getInventory", "getMainHand",
            "getOpenInventory", "getItemInHand", "getGameMode", "getEyeLocation", "getEquipment",
            "getMetadata", "hasMetadata", "isPermissionSet", "hasPermission", "isOp", "getServer");

    private JohnBonifield() {}

    static Player of(Player player)
    {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) ->
                {
                    String name = method.getName();
                    if (FORWARDED.contains(name))
                    {
                        try
                        {
                            return method.invoke(player, args);
                        }
                        catch (InvocationTargetException e)
                        {
                            throw e.getCause();
                        }
                    }
                    switch (name)
                    {
                        case "equals": return proxy == args[0];
                        case "hashCode": return System.identityHashCode(proxy);
                        case "toString": return "WildTPFakePlayer{" + player.getName() + "}";
                    }
                    Class<?> type = method.getReturnType();
                    if (!type.isPrimitive() || type == void.class) return null;
                    if (type == boolean.class) return false;
                    if (type == char.class) return '\0';
                    if (type == float.class) return 0f;
                    if (type == double.class) return 0d;
                    if (type == long.class) return 0L;
                    if (type == byte.class) return (byte) 0;
                    if (type == short.class) return (short) 0;
                    return 0;
                });
    }
}
