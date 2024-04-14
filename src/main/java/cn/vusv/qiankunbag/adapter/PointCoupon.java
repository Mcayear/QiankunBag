package cn.vusv.qiankunbag.adapter;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.ConfigSection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cn.vusv.qiankunbag.config.McrmbConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class PointCoupon {
    private static final String MCRMBURL = "http://api.mcrmb.com/Api/{api}?{value}";

    public static boolean toPay(String playerName, int money) throws CodeException {
        return toPay(playerName, money, "拓展乾坤宝袋");
    }
    public static boolean toPay(String playerName, int money, String reason) throws CodeException {
        Player player = Server.getInstance().getPlayer(playerName);
        ConfigSection section = send("Pay", md5Value(playerName,
                new GetValue("use", URLEncoder.encode(reason, StandardCharsets.UTF_8)),
                new GetValue("money", money + ""),
                new GetValue("time", String.valueOf(System.currentTimeMillis() / 1000L))));
        if (section.isEmpty()) {
            return false;
        }

        if (Integer.parseInt(section.getString("code")) == 101) {
            Map map = section.getMapList("data").get(0);
            if (player != null) {
                player.sendMessage("§a" + section.getString("msg") + " 剩余: " + map.get("money").toString());
            }
            return true;
        } else if (Integer.parseInt(section.getString("code")) == 102) {
            Map map = section.getMapList("data").get(0);
            if (player != null) {
                player.sendMessage("§c" + section.getString("msg") +
                        " 还差 " + map.get("need").toString() +
                        " 当前: " + map.get("money").toString());
            }
        } else {
            throw new CodeException(Integer.parseInt(section.getString("code")), section.getString("msg"));
        }
        return false;
    }

    /**
     * 获取玩家充值的rmb
     */
    public static int checkMoney(String playerName) throws CodeException {
        ConfigSection section = send("CheckMoney", md5Value(playerName));
        if (Integer.parseInt(section.getString("code")) == 101) {
            Map map = section.getMapList("data").get(0);
            return Integer.parseInt(map.get("money").toString());
        } else if (Integer.parseInt(section.getString("code")) == 102) {
            return 0;
        } else {
            throw new CodeException(Integer.parseInt(section.getString("code")), section.getString("msg"));
        }
    }

    private String toTime(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = Long.parseLong(time);
        Date date = new Date(lt * 1000L);
        return simpleDateFormat.format(date);
    }


    private static String md5Value(String wname, GetValue... value) {
        StringBuilder v = new StringBuilder();
        StringBuilder v1 = new StringBuilder();
        for (GetValue va : value) {
            v.append(va.value);
            v1.append("&").append(va.name).append("=").append(va.value);
        }
        return "sign=" + getMd5(McrmbConfig.sid + wname + v.toString() + McrmbConfig.key) + "&sid=" + McrmbConfig.sid + "&wname=" + wname + v1.toString();

    }


    /**
     * 使用md5加密字符串
     */
    public static String getMd5(String plainText) {

        // 返回字符串
        String md5Str = null;
        try {
            StringBuilder buf = new StringBuilder();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] b = md.digest();
            int i;
            for (byte value : b) {
                i = value;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            md5Str = buf.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5Str;
    }


    private static ConfigSection send(String api, String value) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return new ConfigSection(gson.fromJson(loadJson(api, value), (new TypeToken<LinkedHashMap<String, Object>>() {
        }).getType()));
    }

    private static String loadJson(String api, String value) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(MCRMBURL.replace("{api}", api).replace("{value}", value));
            HttpURLConnection uc = (HttpURLConnection) urlObject.openConnection();
            uc.addRequestProperty(".USER_AGENT", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 java");
            uc.setRequestMethod("GET");
            uc.setConnectTimeout(15000);
            uc.setReadTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            return null;
        }
        return json.toString();
    }

    private static class GetValue {
        private final String name;

        private final String value;

        public GetValue(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}