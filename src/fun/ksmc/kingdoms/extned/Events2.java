package fun.ksmc.kingdoms.extned;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.kingdoms.events.KingdomStructreGUIopenEvent;
import org.kingdoms.events.StructureRightClickEvent;
import org.kingdoms.manager.gui.StructureGUIManager;

public class Events2 implements Listener {
    @EventHandler
    public void onRightClickConvertor(StructureRightClickEvent e) {
        if (!e.getStructureType().equals("convertor")) return;
        Inventory inv = Bukkit.createInventory(null, 54, OldExchange.utilManager.getTip().getMessage("Guis_Nexus_RP_Trade"));
        e.getKingdomPlayer().getPlayer().openInventory(inv);
    }

    @EventHandler
    public void onKSGUIopen(KingdomStructreGUIopenEvent ev) {
        ev.getGui().getInventory().setItem(25, OldExchange.str);
        StructureGUIManager.addStructureToGUI(ev.getGui(), 25, "convertor", ev.getKingdomPlayer());
    }
}
