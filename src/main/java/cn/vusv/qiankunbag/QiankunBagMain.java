package cn.vusv.qiankunbag;

import cn.nukkit.Server;
import cn.nukkit.lang.PluginI18n;
import cn.nukkit.lang.PluginI18nManager;
import cn.nukkit.permission.Permission;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.vusv.qiankunbag.command.QiankunCommand;
import cn.vusv.qiankunbag.config.McrmbConfig;
import cn.vusv.qiankunbag.config.QiankunStoneConfig;
import cn.vusv.qiankunbag.config.MainConfig;
import cn.vusv.qiankunbag.config.PlayerConfig;
import lombok.Getter;

import java.util.LinkedHashMap;

/**
 * author: MagicDroidX
 * NukkitExamplePlugin Project
 */
public class QiankunBagMain extends PluginBase {
    @Getter
    public static QiankunBagMain instance;
    @Getter
    public static PluginI18n i18n;

    public static LinkedHashMap<String, PlayerConfig> loadPlayers = new LinkedHashMap<>();
    public static LinkedHashMap<String, QiankunStoneConfig> loadItems = new LinkedHashMap<>();


    @Override
    public void onLoad() {
        //save Plugin Instance
        instance = this;
        //register the plugin i18n
        i18n = PluginI18nManager.register(this);
        //register the command of plugin
        Server.getInstance().getPluginManager().addPermission(new Permission("qiankunbag.command.use", "QiankunBag 命令权限", "true"));

        this.getServer().getCommandMap().register("QiankunBag", new QiankunCommand("qiankun"));
    }

    @Override
    public void onEnable() {
        //Save resources
        this.saveResource("config.yml");
        MainConfig.init();
        McrmbConfig.init();
        QiankunStoneConfig.init();
        PlayerConfig.init();

        this.getServer().getPluginManager().registerEvents(new Events(),this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info(TextFormat.DARK_RED + "I've been disabled!");
    }
}
