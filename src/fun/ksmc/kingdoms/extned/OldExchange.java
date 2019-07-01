package fun.ksmc.kingdoms.extned;

import com.ksqeib.ksapi.command.Cmdregister;
import com.ksqeib.ksapi.util.Io;
import com.ksqeib.ksapi.util.UtilManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.land.StructureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OldExchange extends JavaPlugin {
    public KExitGui kExitGui;
    public static ItemStack str;
    @Getter
    public HashMap<String, Boolean> booleans = new HashMap<>();
    @Getter
    public HashMap<String, Integer> ints = new HashMap<>();
    public static UtilManager utilManager;
    public static FileConfiguration config;
    public List<String> blacklist_item_names = new ArrayList<String>();
    public List<String> blacklist_items = new ArrayList<String>();
    public List<String> whitelist_items = new ArrayList<String>();
    public List<String> special_items = new ArrayList<String>();

    public static ConcurrentHashMap<String, String> item = new ConcurrentHashMap<>();
    @Override
    public void onEnable(){

        utilManager=new UtilManager(this);
        utilManager.createio(false);
        utilManager.createtip(true,"message.yml");
        utilManager.getIo().loadaConfig("config",true);
        utilManager.createitemsr();
        config=utilManager.getIo().getaConfig("config");
        init();
    }
    @Override
    public void onDisable(){
        kExitGui.disable();
    }
    public void init(){
        Io.loadbooleanlist(booleans, "u1", config);
        Io.loadintlist(ints, "u2", config);
        str=utilManager.getItemsr().rep(config.getItemStack("convertor"),new String[]{String.valueOf(getInts().get("items_needed_for_one_resource_point"))});
        StructureType.addforeign(this,"convertor",str, Material.SLIME_BLOCK,500,convertor.class);
        Cmdregister.registercmd(this,new Dcm("KOldExchange",utilManager.getTip().getMessage("Command_Help_Tradable"),"",new ArrayList<>()));
        this.blacklist_item_names = (List<String>) config.get(
                "resource-point-item-names-that-cannot-be-traded");
        this.blacklist_items = (List<String>) config.get(
                "resource-point-trade-blacklist");
        this.whitelist_items = (List<String>) config.get("whitelist-items");
        this.special_items = (List<String>) config.get("special-item-cases");

        item = getAll(utilManager.getIo().loadYamlFile("itemname.yml", true));
        kExitGui=new KExitGui(this);
        kExitGui.init();
        getServer().getPluginManager().registerEvents(new Events2(), this);
        getServer().getPluginManager().registerEvents(kExitGui, this);
    }


    public static String getitem(ItemStack itemStack) {
        Material material = itemStack.getType();
        if (item.size() == 0) {
            return material.name();
        }
        try {
            if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW) {
                try {
                    return item.get((material.name() + ":" + ((PotionMeta) itemStack.getItemMeta()).getBasePotionData().getType().name()).toLowerCase());
                } catch (NoSuchMethodError e) {
                    if (itemStack.getDurability() == (short) 0) {
                        return "水瓶";
                    }
                    return "药水";
                }
            }
        } catch (NoSuchFieldError e2) {
        }
        String type = material.name().toLowerCase();
        String typeDur = material.name() + ":" + itemStack.getDurability();
        if (item.containsKey(typeDur)) {
            return item.get(typeDur);
        }
        if (item.containsKey(type)) {
            return item.get(type);
        }
        return typeDur;
    }


    public ConcurrentHashMap<String, String> getAll(FileConfiguration file) {
        //读取配置
        ConcurrentHashMap<String, String> hash = new ConcurrentHashMap<>();
        for (String string : file.getValues(false).keySet()) {
            //获取全部样式
            hash.put(string.toLowerCase(), file.getString(string).replace("&", "§"));
        }
        return hash;
    }



}
