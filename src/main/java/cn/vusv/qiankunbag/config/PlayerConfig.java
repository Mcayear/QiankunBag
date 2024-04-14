package cn.vusv.qiankunbag.config;

import RcRPG.AttrManager.PlayerAttr;
import cn.vusv.qiankunbag.Handle;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.vusv.qiankunbag.QiankunBagMain;
import cn.vusv.qiankunbag.attrmanager.QianKunBagAttr;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

import static cn.vusv.qiankunbag.QiankunBagMain.loadItems;
import static cn.vusv.qiankunbag.QiankunBagMain.loadPlayers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class PlayerConfig {
    private Config config;
    private String name;
    @Setter
    private int slot;
    @Setter
    private ArrayList<Item> playerItems = new ArrayList<>();
    @Setter
    public QianKunBagAttr attr;

    public static void init() {
        QiankunBagMain.getInstance().getLogger().info("开始读取玩家信息");
        for (String name : Handle.getDefaultFiles("players")) {
            PlayerConfig cfg;
            try {
                cfg = load(name.toLowerCase(), new Config(QiankunBagMain.getInstance().getDataFolder() + File.separator + "players" + File.separator + name + ".yml", Config.YAML));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (cfg != null) {
                loadPlayers.put(name.toLowerCase(), cfg);
            }
        }
    }

    public static PlayerConfig load(String name, Config cfg) {
        PlayerConfig playerData = new PlayerConfig(name, cfg);
        try {
            playerData.slot = cfg.getInt("slot");
            playerData.playerItems = new ArrayList<>(playerData.slot); // 若 slot 是 2，则ArrayList初始容量为 2

            List<String> dataList = cfg.getStringList("data");
            for (int i = 0; i < Math.min(playerData.slot, dataList.size()); i++) {
                String yamlName = dataList.get(i);
                if (yamlName != null) {
                    playerData.playerItems.add(QiankunStoneConfig.getItem(yamlName, 1));
                } else {
                    playerData.playerItems.add(Item.AIR_ITEM);
                }
            }
        } catch (Exception e) {
            QiankunBagMain.getInstance().getLogger().error("加载玩家 " + name + " 配置文件失败", e);
            return null;
        }
        return playerData;
    }

    public static void existAndCreate(Player player) {
        if (!loadPlayers.containsKey(player.getName().toLowerCase())) {
            PlayerConfig cfg = new PlayerConfig(player.getName(), new Config(QiankunBagMain.getInstance().getDataFolder() + File.separator + "players" + File.separator + player.getName() + ".yml", Config.YAML));
            cfg.config.set("slot", MainConfig.defaultSlot);
            cfg.config.set("data", new ArrayList<String>());
            cfg.config.save();
            loadPlayers.put(player.getName().toLowerCase(), cfg);
        }
    }

    public PlayerConfig(String name, Config cfg) {
        this.name = name;
        this.config = cfg;
    }

    public boolean save() {
        config.set("slot", slot);
        ArrayList<String> saveItems = new ArrayList<>();
        playerItems.forEach(item -> {
            if (item.isNull() || !item.getNamedTag().contains("type")) {
                saveItems.add(null);
                getAttr().setItemAttrConfig("slot" + saveItems.size(), new HashMap<>());
                return;
            }
            String yamlName = item.getNamedTag().getString("yamlName");
            saveItems.add(yamlName);
            if (loadItems.containsKey(yamlName)) {
                getAttr().setItemAttrConfig("slot" + saveItems.size(), loadItems.get(yamlName).getAttr());
            }
        });

        config.set("data", saveItems);
        return config.save();
    }
}