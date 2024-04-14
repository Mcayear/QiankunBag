package cn.vusv.qiankunbag.panel;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.handler.FormResponseHandler;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.protection.EnchantmentProtectionAll;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import cn.vusv.qiankunbag.QiankunBagMain;
import cn.vusv.qiankunbag.adapter.CodeException;
import cn.vusv.qiankunbag.adapter.PointCoupon;
import cn.vusv.qiankunbag.config.MainConfig;
import cn.vusv.qiankunbag.config.McrmbConfig;
import cn.vusv.qiankunbag.config.PlayerConfig;
import cn.vusv.qiankunbag.config.QiankunStoneConfig;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static cn.vusv.qiankunbag.QiankunBagMain.loadPlayers;

public class QiankunBagPanel {
    public static void send(Player player) {
        FakeInventory inv = new QiankunBagInventory(TextFormat.RED + "乾坤宝袋");
        PlayerConfig.existAndCreate(player);
        PlayerConfig cfg = loadPlayers.get(player.getName().toLowerCase());

        // 创建一个新的 HashMap 用于存储物品及其对应的位置
        HashMap<Integer, Item> inventoryContents = new HashMap<>();
        // 假设你的物品位置是从0开始按顺序排列的
        for (int i = 0; i < cfg.getPlayerItems().size() && i < cfg.getSlot(); i++) {
            inventoryContents.put(i, cfg.getPlayerItems().get(i));
        }
        int playerMaxSlot = Math.max(MainConfig.defaultSlot, cfg.getSlot());
        for (int i = playerMaxSlot; i < 54; i++) {
            int price = MainConfig.findPrice(i + 1);
            Item mask = MainConfig.enlargeItem.clone().setLore(MainConfig.enlargeItem.getLore()[0].replace("{price}", "" + price));
            CompoundTag tag = mask.getNamedTag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            tag.putInt("slot", i + 1);
            tag.putInt("price", price);
            mask.setNamedTag(tag);
            inventoryContents.put(i, mask);
        }

        inv.setContents(inventoryContents);
        inv.setDefaultItemHandler((item, event) -> {
            for (InventoryAction action : event.getTransaction().getActions()) {
                Item sourceItem = action.getSourceItem();// 将要拿在手上的物品
                Item targetItem = action.getTargetItem();// 将要放在箱子中的物品
                if (action instanceof SlotChangeAction slotChange) {
                    if (slotChange.getInventory() instanceof FakeInventory) {
                        if (slotChange.getSlot() >= playerMaxSlot) {
                            event.setCancelled();
                            handleBuy(player, inv, item);
                            return;
                        }
                        if (!targetItem.isNull() && !QiankunStoneConfig.isQiankunStone(targetItem)) {
                            event.setCancelled();
                            return;
                        }
                        if (targetItem.count > 1) {
                            event.setCancelled();
                            return;
                        }
                        break;
                    }
                }
            }
        });
        player.addWindow(inv);
    }

    public static void handleBuy(Player player, FakeInventory inv, Item item) {
        int slot = loadPlayers.get(player.getName().toLowerCase()).getSlot();
        if (item.getNamedTag().contains("total")) {
            inv.close(player);
            handlePayWindow(player, item.getNamedTag().getInt("total"), slot, item.getNamedTag().getInt("slot"));
            return;
        }
        int total = 0;
        int itemSlot = item.getNamedTag().getInt("slot");

        for (int i = slot; i < itemSlot; i++) {
            Item current = inv.getItem(i);
            total += current.getNamedTag().getInt("price");
        }
        //if (item.getEnchantment(EnchantmentProtectionAll.ID_PROTECTION_ALL) == null)
        for (int i = slot; i < itemSlot; i++) {
            int price = MainConfig.findPrice(i);
            Item mask = MainConfig.enlargeItem.clone();
            mask.addEnchantment(new EnchantmentProtectionAll());
            if (i == itemSlot - 1) {
                mask.setLore(MainConfig.enlargeItem.getLore()[0]
                        .replace("{price}", "" + price) + "\n\n§s§t§r§c再次点击，花费 " + total + " 点券开通§e§n§r");
                CompoundTag tag = mask.getNamedTag();
                tag.putInt("total", total);
                mask.setNamedTag(tag);
            } else {
                mask.setLore(MainConfig.enlargeItem.getLore()[0]
                        .replace("{price}", "" + price) + "\n\n§s§t§r§c合计 " + total + " 点券§e§n§r");
            }
            CompoundTag tag = mask.getNamedTag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            tag.putInt("slot", i + 1);
            tag.putInt("price", price);
            mask.setNamedTag(tag);
            inv.setItem(i, mask);
        }
        for (int i = itemSlot; i < inv.getSize(); i++) {
            int price = MainConfig.findPrice(i);
            Item mask = MainConfig.enlargeItem.clone().setLore(MainConfig.enlargeItem.getLore()[0].replace("{price}", "" + price));
            CompoundTag tag = mask.getNamedTag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            tag.putInt("slot", i + 1);
            tag.putInt("price", price);
            mask.setNamedTag(tag);
            inv.setItem(i, mask);
        }
    }

    public static void handlePayWindow(Player player, int total, int slotCount, int buyCount) {
        FormWindowCustom form = new FormWindowCustom("§r§l拓展§c 「 乾坤宝袋 」 §r§l空间 §f· 二次确认");
        form.addElement(new ElementLabel("信息\n\n格数：" + slotCount + " -> §a" + buyCount + "\n§r点券：§e" + total));
        form.addElement(new ElementDropdown("购买？", List.of(new String[]{"§r§c§l[ 确认 ]", "§r§c§l[ 取消 ]"}), 0));
        form.addHandler(FormResponseHandler.withoutPlayer(ignored -> {
            if (form.wasClosed()) return;
            if (form.getResponse().getDropdownResponse(1).getElementID() == 0) {
                try {
                    boolean isPay = PointCoupon.toPay(player.getName().replace(" ", "_"), total);
                    if (!isPay) {
                        player.sendMessage(QiankunBagMain.getI18n().tr(player.getLanguageCode(), "qiankunbag.cannot.point", McrmbConfig.website).replace("{n}", "\n"));
                        return;
                    }
                } catch (CodeException e) {
                    player.sendActionBar("§ctoPlay() 出现了未知错误，已取消购买");
                    throw new RuntimeException(e);
                }
                PlayerConfig playerCfg = loadPlayers.get(player.getName().toLowerCase());
                playerCfg.setSlot(buyCount);
                playerCfg.save();
                player.sendActionBar("§a购买成功！");
            } else {
                player.sendActionBar("§7已取消购买");
            }
        }));
        player.showFormWindow(form);
    }
}