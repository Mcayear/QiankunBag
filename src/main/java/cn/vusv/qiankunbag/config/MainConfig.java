package cn.vusv.qiankunbag.config;

import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import cn.vusv.qiankunbag.QiankunBagMain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainConfig {

    public static Config config;
    public static int defaultSlot;
    public static Map<String, Object> priceSection;
    public static Item enlargeItem;
    public static int upgradeNeed;
    public static void init(){
        if(!new File(QiankunBagMain.getInstance().getDataFolder(), "config.yml").exists()) {
            QiankunBagMain.getInstance().saveResource("config.yml");
            config = new Config(new File(QiankunBagMain.getInstance().getDataFolder(), "config.yml"), Config.YAML);
        } else {
            config = new Config(new File(QiankunBagMain.getInstance().getDataFolder(), "config.yml"), Config.YAML);
        }
        upgradeNeed = config.getInt("upgrade-need");
        defaultSlot = config.getInt("default-slot");
        priceSection = config.getSection("price").getAllMap();

        ConfigSection enlarge = config.getSection("show-item").getSection("enlarge");
        enlargeItem = Item.fromString(enlarge.getString("id", "minecraft:stone"));
        enlargeItem.setCustomName(enlarge.getString("name"));
        enlargeItem.setDamage(enlarge.getInt("aux", 0));
        enlargeItem.setCount(enlarge.getInt("count", 1));
        if (enlarge.exists("lore")) enlargeItem.setLore(enlarge.getString("lore"));

        QiankunBagMain.getInstance().getLogger().info(TextFormat.GREEN+"config.yml 加载完成!");
    }
    public static int findPrice(int input) {
        for (Map.Entry<String, Object> entry : priceSection.entrySet()) {
            String rangeStr = entry.getKey();
            String[] range = rangeStr.split("~");
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);

            if (input >= start && input <= end) {
                return (int) entry.getValue();
            }
        }
        return -1;
    }

}