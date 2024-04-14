package cn.vusv.qiankunbag.config;

import RcRPG.AttrManager.ItemAttr;
import cn.vusv.qiankunbag.Handle;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import cn.vusv.qiankunbag.QiankunBagMain;
import lombok.Getter;

import java.io.File;
import java.util.List;

import static cn.vusv.qiankunbag.QiankunBagMain.loadItems;

@Getter
public class QiankunStoneConfig extends ItemAttr {

    public Config config;
    private String name;
    private String showName;
    private String label;
    private Byte level;
    private Item item;
    private Object attr;
    private String message;
    private List<String> show;
    private String upgrade;

    public static void init() {
        QiankunBagMain.getInstance().getLogger().info("开始读取背包信息");
        boolean isNullDir = true;
        for (String name : Handle.getDefaultFiles("items")) {
            QiankunStoneConfig cfg;
            try {
                cfg = load(name, new Config(QiankunBagMain.getInstance().getDataFolder() + File.separator + "items" + File.separator + name + ".yml", Config.YAML));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (cfg == null) {
                return;
            }
            loadItems.put(name, cfg);
            isNullDir = false;
        }
        if (isNullDir) {
            QiankunBagMain.getInstance().saveResource("items/default.yml", "items/default.yml", false);
        }
    }

    /**
     * 加载指定玩家的配置项到 ItemConfig 对象中。
     *
     * @param name 玩家名称，用于创建ItemConfig对象的标识。
     * @param cfg  配置文件对象，从中读取玩家的配置数据。
     * @return 成功加载后返回包含玩家配置的ItemConfig对象，如果加载失败则返回null。
     */
    public static QiankunStoneConfig load(String name, Config cfg) {
        QiankunStoneConfig itemData = new QiankunStoneConfig(name, cfg);
        try {
            itemData.attr = cfg.get("attr");
            itemData.showName = cfg.getString("showName");
            itemData.label = cfg.getString("label");
            itemData.level = (byte) cfg.getInt("level");
            itemData.item = Item.fromString(cfg.getString("namespace"));
            itemData.message = cfg.getString("message");
            itemData.show = cfg.getStringList("show");
            itemData.upgrade = cfg.getString("upgrade");

            itemData.setItemAttrConfig(itemData.attr);
        } catch (Exception e) {
            QiankunBagMain.getInstance().getLogger().error("加载物品 " + name + " 配置文件失败");
            return null;
        }
        return itemData;
    }

    /**
     * 根据提供的名称和数量获取一个 Item 实例。
     *
     * @param yamlName Item的文件名，用于从预加载的Item配置中查找。
     * @param count    Item的数量。
     * @return 配置并初始化后的Item实例。
     */
    public static Item getItem(String yamlName, int count) {
        QiankunStoneConfig qiankun = loadItems.get(yamlName);
        Item item = qiankun.getItem().clone();
        item.setCount(count);
        CompoundTag tag = item.getNamedTag();
        if (tag == null) {
            tag = new CompoundTag();
        }
        tag.putString("type", "qiankun");
        tag.putString("yamlName", yamlName);
        tag.putByte("level", qiankun.level);
        item.setNamedTag(tag);
        item.setCustomName(qiankun.getShowName());
        QiankunStoneConfig.setLore(item);
        return item;
    }

    public static boolean isQiankunStone(Item item) {
        if (item.getNamedTag() == null) {
            return false;
        } else {
            return item.getNamedTag().contains("type") && item.getNamedTag().getString("type").equals("qiankun");
        }
    }

    public static void setLore(Item item) {
        if (isQiankunStone(item)) {
            QiankunStoneConfig qiankunStone = loadItems.get(item.getNamedTag().getString("yamlName"));
            List<String> lore = qiankunStone.getShow();

            for (int i = 0; i < lore.size(); ++i) {
                String s = qiankunStone.replaceAttrTemplate(lore.get(i))
                        .replace("@message", qiankunStone.message);
                lore.set(i, s);
            }

            item.setLore(lore.toArray(new String[0]));
        }

    }

    public QiankunStoneConfig(String name, Config cfg) {
        this.name = name;
        this.config = cfg;
    }
}