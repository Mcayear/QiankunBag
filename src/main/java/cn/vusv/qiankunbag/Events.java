package cn.vusv.qiankunbag;

import RcRPG.AttrManager.PlayerAttr;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;
import cn.vusv.qiankunbag.attrmanager.QianKunBagAttr;
import cn.vusv.qiankunbag.config.MainConfig;
import cn.vusv.qiankunbag.config.PlayerConfig;
import cn.vusv.qiankunbag.config.QiankunStoneConfig;

import static cn.vusv.qiankunbag.QiankunBagMain.loadItems;
import static cn.vusv.qiankunbag.QiankunBagMain.loadPlayers;

public class Events implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && QiankunStoneConfig.isQiankunStone(item)) {
            event.setCancelled();
            String yamlName = item.getNamedTag().getString("yamlName");
            String upgrade = loadItems.get(yamlName).getUpgrade();
            if (!loadItems.containsKey(upgrade)) {
                player.sendActionBar(QiankunBagMain.getI18n().tr(player.getLanguageCode(), "qiankunbag.event.item_not_upgrade"));
                return;
            }
            if (MainConfig.upgradeNeed > item.count) {
                player.sendActionBar(QiankunBagMain.getI18n().tr(player.getLanguageCode(), "qiankunbag.event.item_count_need", MainConfig.upgradeNeed, MainConfig.upgradeNeed - item.count));
                return;
            }
            item.count = MainConfig.upgradeNeed;
            player.getInventory().remove(item);
            player.getInventory().addItem(QiankunStoneConfig.getItem(upgrade, 1));
            player.sendActionBar(QiankunBagMain.getI18n().tr(player.getLanguageCode(), "qiankunbag.event.upgrade_success"));
        }
    }

    @EventHandler
    public void joinEvent(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        PlayerConfig.existAndCreate(player);
        QianKunBagAttr.setPlayerAttr(player);
        checkPlayerInventory(player);
    }

    /**
     * 检查并更新玩家背包中的qiankun物品。如果物品类型为qiankun，并且能够找到对应的物品定义，则将物品更新为定义中的物品。
     * 如果找不到对应的物品定义，则移除该物品，并向玩家发送物品未找到的提示信息。
     *
     * @param player 玩家对象，表示需要检查背包的玩家。
     */
    public static void checkPlayerInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        player.getInventory().getContents().forEach((slot, item) -> {
            // 遍历背包，将所有qiankun的物品重新设置一遍
            CompoundTag namedTag = item.getNamedTag();
            if (namedTag == null) return;
            if (!namedTag.getString("type").equals("qiankun")) return;
            String name = namedTag.getString("yamlName");
            if (loadItems.containsKey(name)) {
                Item qiankunStone = QiankunStoneConfig.getItem(name, item.getCount());
                inv.setItem(slot, qiankunStone);
            } else {
                inv.setItem(slot, Item.AIR_ITEM);
                player.sendMessage(QiankunBagMain.getI18n().tr(player.getLanguageCode(), "qiankunbag.event.item_not_found"));
            }
        });

        if (loadPlayers.containsKey(player.getName().toLowerCase())) {
            PlayerConfig playerConfig = loadPlayers.get(player.getName().toLowerCase());
            playerConfig.updateAttr();
            PlayerAttr.getPlayerAttr(player).setItemAttrConfig("乾坤宝袋", playerConfig.getAttr().myAttr.get("Main"));
        }
    }
}
