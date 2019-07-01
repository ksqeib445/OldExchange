package fun.ksmc.kingdoms.extned;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kingdoms.constants.kingdom.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.SimpleChunkLocation;
import org.kingdoms.constants.land.SimpleLocation;
import org.kingdoms.constants.land.StructureType;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;
import org.kingdoms.manager.game.GameManagement;
import org.kingdoms.utils.ItemStorger;
import org.kingdoms.utils.XMaterial;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class KExitGui implements Listener {
    public static OldExchange plugin;
    //white listed items -- Material and the item worth corresponding to the Material
    public static Map<ItemStack, Integer> whiteListed = new HashMap<>();
    //black listed items -- this can be null if plugin.getBooleans().get("useWhiteList") is true
    public static ArrayList<ItemStack> blackListed = new ArrayList<ItemStack>() {{
        add(XMaterial.AIR.parseItem());
    }};
    //special items -- Material and the item worth corresponding to the Material
    public static Map<ItemStack, Integer> specials = new HashMap<>();
    public KExitGui(OldExchange plugin){
        this.plugin=plugin;
    }
    public static ItemStack getConvertor() {
        return OldExchange.utilManager.getItemsr().rep(ItemStorger.getNexusItem("convertor"), new String[]{plugin.getInts().get("items_needed_for_one_resource_point") + ""});
    }
    @EventHandler
    public void onDonateInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        KingdomPlayer kp = GameManagement.getPlayerManager().getSession((Player) event.getPlayer());

        String invName = event.getInventory().getName();
        if (invName.equals(OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade"))) {
            int donatedamt = consumeDonationItems(event.getInventory().getContents(), kp);
            kp.getKingdom().setResourcepoints(kp.getKingdom().getResourcepoints() + donatedamt);
            kp.sendMessage(OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade_Success").replaceAll("%amount%", "" + donatedamt));
            kp.setLastTimeDonated(new Date());
            kp.setDonatedAmt(kp.getDonatedAmt() + donatedamt);
            kp.setLastDonatedAmt(donatedamt);
            return;
        }
        if (invName.startsWith(ChatColor.DARK_BLUE + "Donate to " + ChatColor.DARK_GREEN)) {
            String[] nsplit = event.getInventory().getName().split(" ");
            String sentKingdomName = ChatColor.stripColor(nsplit[(nsplit.length - 1)]);

            Kingdom sentTo = GameManagement.getKingdomManager().getOrLoadKingdom(sentKingdomName);
            int donatedamt = consumeDonationItems(event.getInventory().getContents(), kp);
            sentTo.setResourcepoints(sentTo.getResourcepoints() + donatedamt);
            kp.sendMessage(OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade_Success").replaceAll("%amount%", "" + donatedamt).replaceAll("%kingdom%", "" + sentTo.getKingdomName()));
            sentTo.sendAnnouncement(null, OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade_Success").replaceAll("%amount%", "" + donatedamt).replaceAll("%player%", "" + kp.getName()), true);

            return;
        }
    }
    /**
     * DOES NOT award the resource points. It does, however, consume the used items or return the items depending on the situation
     *
     * @param items items to donated
     * @param p     donator
     * @return total points the items are worth.
     */
    public int consumeDonationItems(ItemStack[] items, KingdomPlayer p) {
        //items to return
        ArrayList<ItemStack> returningIS = new ArrayList<ItemStack>();
        //items to consume
        //ArrayList<ItemStack> consumingIS = new ArrayList<ItemStack>();
        //special items that doesn't affected by config.getInts().get("items_needed_for_one_resource_point")
        //ArrayList<ItemStack> extraIS = new ArrayList<ItemStack>();
        int itemworth = 0;
        int itemsperrp = plugin.getInts().get("items_needed_for_one_resource_point");

        if (plugin.getBooleans().get("useWhiteList")) {
            for (ItemStack item : items) {
                if (item == null) continue;
                ItemStack type = new ItemStack(item.clone().getType(), 1, item.getDurability());
                if (whiteListed.containsKey(type)) {
                    itemworth += item.getAmount() * whiteListed.get(type);
                } else {
                    returningIS.add(item);
                }
            }
        } else {
            for (ItemStack item : items) {
                if (item == null) continue;


                if (isBlackListed(item)) {
                    String name = item.getType().toString();
                    if (item.getItemMeta() != null &&
                            item.getItemMeta().getDisplayName() != null) {
                        name = item.getItemMeta().getDisplayName();
                    } else if (item.getDurability() != 0) {
                        name += ":" + item.getDurability();
                    }
                    String message = OldExchange.utilManager.getTip().getMessage("Misc_Cannot_Be_Traded_For_Points").replaceAll("%item%", name);


                    p.sendMessage(message);
                    returningIS.add(item);
                    continue;
                } else if (getSpecialWorth(item) != -1) {
                    itemworth += item.getAmount() * getSpecialWorth(item);
                } else {
                    itemworth += item.getAmount();
                }
            }
        }

        if (itemworth % plugin.getInts().get("items_needed_for_one_resource_point") != 0) {
            int itemoverflow = itemworth % plugin.getInts().get("items_needed_for_one_resource_point");
            for (ItemStack item : items) {
                if (item == null) continue;
                ItemStack type = new ItemStack(item.clone().getType(), 1, item.getDurability());
                if (isBlackListed(item)) continue;
                int worth = 1;
                if (getSpecialWorth(item) != -1) {
                    worth = getSpecialWorth(item);
                }
                if (plugin.getBooleans().get("useWhiteList") && whiteListed.containsKey(type)) {
                    worth = whiteListed.get(type);
                }
                int originalAmount = item.getAmount();
                int removedAmount = 0;
                while ((itemworth) % plugin.getInts().get("items_needed_for_one_resource_point") != 0 &&
                        (itemworth - worth) >= ((itemworth / plugin.getInts().get("items_needed_for_one_resource_point")) * plugin.getInts().get("items_needed_for_one_resource_point")) &&
                        originalAmount - removedAmount > 0) {

                    //Kingdoms.logDebug("===Type: " + type.getType().toString());
                    //Kingdoms.logDebug("===Condition 1: " + (itemworth-worth) + " / " + plugin.getInts().get("items_needed_for_one_resource_point") + " Remainder not 0");
                    //Kingdoms.logDebug("===Condition 2: " + (itemworth - worth) + " > " + ((itemworth/plugin.getInts().get("items_needed_for_one_resource_point"))*plugin.getInts().get("items_needed_for_one_resource_point")));
                    //Kingdoms.logDebug("old: " + itemworth);
                    itemworth -= worth;
                    //Kingdoms.logDebug("new: " + itemworth);
                    returningIS.add(type);
                    removedAmount++;
                }

                if ((itemworth) % plugin.getInts().get("items_needed_for_one_resource_point") == 0) {
                    break;
                }
            }
        }
        //Kingdoms.logDebug("================Final");
        //Kingdoms.logDebug("===Condition 1: " + (itemworth) + " / " + plugin.getInts().get("items_needed_for_one_resource_point") + " Remainder not 0");
        //Kingdoms.logDebug("===Condition 2: " + (itemworth) + " > " + ((itemworth/plugin.getInts().get("items_needed_for_one_resource_point"))*plugin.getInts().get("items_needed_for_one_resource_point")));


        if ((itemworth) % plugin.getInts().get("items_needed_for_one_resource_point") != 0) {
            returningIS.clear();
            for (ItemStack item : items) {
                if (item == null) continue;

                p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), item);
            }
            p.sendMessage(OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade_Insufficient_Itemcount").replaceAll("%amount%", "" + (plugin.getInts().get("items_needed_for_one_resource_point") - (itemworth % plugin.getInts().get("items_needed_for_one_resource_point")))));
            return 0;
        }

        int returned = returningIS.size();
        if (returned != 0)
            p.sendMessage(OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade_Overflowing_Itemcount").replaceAll("%amount%", "" + returned));

        for (ItemStack item : returningIS) {
            p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), item);
        }

        return itemworth / plugin.getInts().get("items_needed_for_one_resource_point");
    }
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != XMaterial.fromString(Kingdoms.config.nexusMaterial).parseMaterial())
            return;
        if (!event.getClickedBlock().hasMetadata(StructureType.getMetaData("nexus"))) return;

        KingdomPlayer clicked = GameManagement.getPlayerManager().getSession(event.getPlayer());
        if (clicked == null) {
            return;
        }
        Block nexusBlock = event.getClickedBlock();
        SimpleLocation loc = new SimpleLocation(nexusBlock.getLocation());
        SimpleChunkLocation chunk = loc.toSimpleChunk();
        Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
        Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwner());
        if (kingdom.isAllianceWith(clicked.getKingdom())) {
            Inventory dumpgui = Bukkit.createInventory(null, 54,
                    ChatColor.DARK_BLUE + "Donate to " + ChatColor.DARK_GREEN + kingdom.getKingdomName());
            clicked.getPlayer().openInventory(dumpgui);
            event.setCancelled(true);
        }
    }

    private int getSpecialWorth(ItemStack item) {
        ItemStack type = new ItemStack(item.clone().getType(), 1, item.getDurability());
        if (!specials.containsKey(type)) {
            return -1;
        }

        return specials.get(type);

    }

    private boolean isBlackListed(ItemStack item) {
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (blackListed.contains(clone)) return true;
        if (item.getItemMeta() == null) return false;
        if (item.getItemMeta().getLore() != null &&
                !item.getItemMeta().getLore().isEmpty()) {
            if (plugin.getBooleans().get("disallowTradingNamedItems")) return true;
        }
        if (item.getItemMeta().getDisplayName() == null) return false;
        if (plugin.getBooleans().get("disallowTradingNamedItems")) return true;

        for (ItemStack bl : blackListed) {
            if (bl == null) continue;
            if (bl.getItemMeta() == null) continue;
            if (item.getItemMeta().getDisplayName().equals(bl.getItemMeta().getDisplayName())) {
                return true;
            }
        }

        return false;
    }


    //Initiate item trade lists.
    public void init() {
        if (plugin.getBooleans().get("useWhiteList")) {
            for (String str : plugin.whitelist_items) {
                String[] split = str.split(",");
                if (split.length != 2) {
                    continue;
                }
                try {
                    int point = Integer.parseInt(split[1]);
                    XMaterial mat = XMaterial.fromString(split[0]);
                    if (mat == null) {
                    } else {
                        whiteListed.put(mat.parseItem(), point);
                    }
                } catch (Exception e) {
                }
            }
        } else {
            ArrayList<ItemStack> blacklists = new ArrayList<>();
            for (String matName : plugin.blacklist_items) {
                try {
                    XMaterial mat = XMaterial.fromString(matName);
                    if (mat != null) {
                        blacklists.add(mat.parseItem());
                    }

                } catch (Exception e) {
                }
            }
            for (String name : plugin.blacklist_item_names) {
                ItemStack item = new ItemStack(Material.BEDROCK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                item.setItemMeta(meta);
                blacklists.add(item);
            }
            if (!blacklists.isEmpty()) {
                blackListed = blacklists;
            } else {
                blacklists.add(new ItemStack(Material.AIR));
                blackListed = blacklists;
            }

            for (int i = 0; i < plugin.special_items.size(); i++) {
                String str = plugin.special_items.get(i);
                if (str == null)
                    continue;

                String[] split = plugin.special_items.get(i).split(",");
                if (split.length != 2) {
                    continue;
                }

                try {
                    int point = Integer.parseInt(split[1]);
//					String[] matSplit = split[0].split(":");
//					if(matSplit.length == 2){
//						item = new ItemStack(Material.valueOf(matSplit[0]), 1, (byte) Integer.parseInt(matSplit[1]));
//					}else{
//						item = new ItemStack(Material.valueOf(split[0]), 1);
//					}
                    XMaterial mat = XMaterial.fromString(split[0]);
                    if (mat != null) {
                        specials.put(mat.parseItem(), point);
                    }
                } catch (Exception e) {
                }
            }
        }
    }
    public void disable(){
        //2016-08-29
        whiteListed.clear();
        specials.clear();

        if (blackListed != null)
            blackListed.clear();
    }
}
