package cn.vusv.qiankunbag.command;

import RcRPG.AttrManager.PlayerAttr;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.LangCode;
import cn.nukkit.lang.PluginI18n;
import cn.nukkit.utils.TextFormat;
import cn.vusv.qiankunbag.QiankunBagMain;
import cn.vusv.qiankunbag.config.PlayerConfig;
import cn.vusv.qiankunbag.config.QiankunStoneConfig;
import cn.vusv.qiankunbag.panel.QiankunBagPanel;

import java.util.HashMap;

import static cn.vusv.qiankunbag.QiankunBagMain.loadPlayers;

public class QiankunCommand extends Command {
    protected QiankunBagMain api;
    protected PluginI18n i18n;

    public QiankunCommand(String name) {
        /*
        1.the name of the command must be lowercase
        2.Here the description is set in with the key in the language file,Look at en_US.lang or zh_CN.lang.
        This can send different command description to players of different language.
        You must extends PluginCommand to have this feature.
        */
        super(name, "打开乾坤宝袋");

        this.setPermission("qiankunbag.command.use");

        this.commandParameters.clear();

        /*
         * 1. `getCommandParameters` 返回一个 Map<String, cn.nukkit.command.data.CommandParameter[]>，
         *    其中每个条目可以被视为一个子命令或命令模式。
         * 2. 每个子命令不能重复。
         * 3. 可选参数必须在子命令的末尾或连续使用。
         */
        this.getCommandParameters().put("qiankunbag-set", new CommandParameter[]{
                CommandParameter.newEnum("set", new String[]{"set"}),
                CommandParameter.newType("playerName", false, CommandParamType.STRING),
                CommandParameter.newType("slot", false, CommandParamType.INT)
        });
        this.getCommandParameters().put("qiankunbag-give", new CommandParameter[]{
                CommandParameter.newEnum("give", new String[]{"give"}),
                CommandParameter.newType("yamlName", false, CommandParamType.STRING),
                CommandParameter.newType("playerName", true, CommandParamType.STRING)
        });
        api = QiankunBagMain.getInstance();
        i18n = QiankunBagMain.getI18n();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            if (sender.isPlayer()) {
                QiankunBagPanel.send((Player) sender);
                return true;
            }
            sender.sendMessage("命令参数缺失");
            return false;
        }
        if (!testPermission(sender)) {
            sender.sendMessage(TextFormat.RED + "你没有使用权限");
            return false;
        }
        switch (args[0]) {
            case "set": {
                String name = args[1];
                int slot = Integer.parseInt(args[2]);
                Player player = Server.getInstance().getPlayer(name);
                if (player == null) {
                    sender.sendMessage(TextFormat.RED + "找不到目标 " + name);
                    return false;
                }
                if (slot < 0 || slot > 54) {
                    sender.sendMessage(TextFormat.RED + "槽位范围 0~54");
                    return false;
                }
                PlayerConfig playerCfg = loadPlayers.get(player.getName());
                int before = playerCfg.getSlot();
                playerCfg.setSlot(slot);
                playerCfg.save();
                sender.sendMessage(TextFormat.GREEN + "设置成功！"+ player.getName() + " 槽位 " + before + " -> " + slot);
                break;
            }
            case "give": {
                String yamlName = args[1];
                if (!QiankunBagMain.loadItems.containsKey(yamlName)) {
                    sender.sendMessage(TextFormat.RED + "找不到物品 " + yamlName);
                    return false;
                }
                if (args.length > 2) {
                    String name = args[2];
                    Player player = Server.getInstance().getPlayer(name);
                    if (player == null) {
                        sender.sendMessage(TextFormat.RED + "找不到目标 " + name);
                        return false;
                    }
                    player.getInventory().addItem(QiankunStoneConfig.getItem(yamlName, 1));
                    return true;
                } else if (sender.isPlayer()) {
                    Player player = (Player) sender;
                    player.getInventory().addItem(QiankunStoneConfig.getItem(yamlName, 1));
                    return true;
                }
                return false;
            }
            default:
                sender.sendMessage(TextFormat.RED + "未知的子命令 " + args[0]);
                return false;
        }
        return true;
    }
}
