package cn.vusv.qiankunbag.config;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.vusv.qiankunbag.QiankunBagMain;

import java.io.File;

public class McrmbConfig {
    public static String website = "";
    public static String sid = "";
    public static String key = "";
    public static void init() {
        Config cfg = new Config(new File(Server.getInstance().getFilePath() + "/plugins/NInvShop/mcrmbConfig.yml"), Config.YAML);
        website = cfg.getString("website", "");
        sid = cfg.getString("sid", "");
        key = cfg.getString("key", "");
        QiankunBagMain.getInstance().getLogger().info("mcrmb地址："+website);
    }

}
