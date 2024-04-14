package cn.vusv.qiankunbag.panel;

import RcRPG.AttrManager.PlayerAttr;
import cn.nukkit.Player;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.vusv.qiankunbag.QiankunBagMain;
import cn.vusv.qiankunbag.config.PlayerConfig;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.ArrayList;
import java.util.Map;

import static cn.vusv.qiankunbag.QiankunBagMain.loadPlayers;

public class QiankunBagInventory extends FakeInventory {

    public long id;

    public QiankunBagInventory(String name) {
        super(InventoryType.DOUBLE_CHEST, name);
    }

    @Override
    public void onClose(Player who) {
        super.onClose(who);
        String name = who.getName().toLowerCase();
        PlayerConfig cfg = loadPlayers.get(name);
        ArrayList<Item> list = new ArrayList<>();
        for (int i = 0; i < cfg.getSlot(); i++) {
            Item item = this.getItem(i);
            if (!item.isNull()) {
                if (item.getNamedTag() != null) {
                    if (item.getNamedTag().contains("type")) {
                        list.add(i, item.getNamedTag().getString("type").equals("qiankun") ? item.clone() : Item.AIR_ITEM);
                        continue;
                    }
                }
            }
            list.add(i, Item.AIR_ITEM);
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).isNull() || !list.get(i).getNamedTag().contains("type")) {
                list.remove(i);
            } else {
                break; // 遇到第一个非空字符串，停止移除操作
            }
        }
        cfg.setPlayerItems(list);
        cfg.save();
        if (loadPlayers.get(name).getAttr() != null) {
            PlayerAttr.getPlayerAttr(who).setItemAttrConfig("乾坤宝袋", loadPlayers.get(name).getAttr().myAttr.get("Main"));
        }
    }


}
