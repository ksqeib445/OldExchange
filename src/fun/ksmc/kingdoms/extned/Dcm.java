package fun.ksmc.kingdoms.extned;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Dcm extends Command {
    public OldExchange plugin=KExitGui.plugin;
    public Dcm(String name, String doc, String usage, List<String> alies){
        super(name,doc,usage,alies);
    }

    @Override
    public boolean execute(CommandSender user, String lable, String[] args){
        // 指令!
        //输入指令后

        if (plugin.getBooleans().get("useWhiteList")) {
            user.sendMessage(OldExchange.utilManager.getTip().getMessage("Command_Tradable_Conversion_Ratio_Msg")
                    .replaceAll("%rpi%", "" + plugin.getInts().get("items_needed_for_one_resource_point")));
            user.sendMessage(OldExchange.utilManager.getTip().getMessage("Command_Tradable_Enabled_Items_Title"));
            for (ItemStack mat : KExitGui.whiteListed.keySet()) {
//                String addition = mat.getType().toString();
                String addition = OldExchange.getitem(mat);
//                if (mat.getDurability() != 0)
//                    addition += ":" + (int) mat.getDurability();
                user.sendMessage(ChatColor.GREEN + addition + " | "
                        + OldExchange.utilManager.getTip().getMessage("Command_Tradable_Worth") + " "
                        + KExitGui.whiteListed.get(new ItemStack(mat.getType(), 1, (byte) mat.getDurability()))
                        + " " + OldExchange.utilManager.getTip().getMessage("Command_Tradable_ItemsUnit"));
            }
        } else {
            user.sendMessage(OldExchange.utilManager.getTip().getMessage("Command_Tradable_Disabled_Items_Title"));
            for (ItemStack mat : KExitGui.blackListed) {
//                String message = ChatColor.RED + mat.getType().name();
                String message = ChatColor.RED + OldExchange.getitem(mat);
//                if (mat.getDurability() != 0) {
//                    message += ":" + mat.getDurability();
//                }czqweasdzxc
                user.sendMessage(message);
            }
            user.sendMessage("");
            user.sendMessage(OldExchange.utilManager.getTip().getMessage("Command_Tradable_Special_Case_Items_Title"));
            for (ItemStack mat : KExitGui.specials.keySet()) {
//                String addition = mat.getType().toString();
                String addition = OldExchange.getitem(mat);
//                if (mat.getDurability() != 0)
//                    addition += ":" + (int) mat.getDurability();
                user.sendMessage(ChatColor.GREEN + addition + " | "
                        + OldExchange.utilManager.getTip().getMessage("Command_Tradable_Worth") + " "
                        + KExitGui.specials.get(new ItemStack(mat.getType(), 1, (byte) mat.getDurability()))
                        + " " + OldExchange.utilManager.getTip().getMessage("Command_Tradable_ItemsUnit"));
            }
        }
        return true;
    }
    // 指令!
}

