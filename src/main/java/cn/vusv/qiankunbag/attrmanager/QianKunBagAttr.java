package cn.vusv.qiankunbag.attrmanager;

import RcRPG.AttrManager.Manager;
import cn.nukkit.Player;
import cn.vusv.qiankunbag.QiankunBagMain;

import java.util.*;

import static cn.vusv.qiankunbag.QiankunBagMain.loadPlayers;

public class QianKunBagAttr extends Manager {
    private final Player player;

    public QianKunBagAttr(Player player) {
        this.player = player;
        myAttr.put("Main", new HashMap<>());
    }

    public static QianKunBagAttr getPlayerAttr(Player player) {
        if (!loadPlayers.containsKey(player.getName().toLowerCase())) {
            return null;
        }
        return loadPlayers.get(player.getName().toLowerCase()).getAttr();
    }

    public static void setPlayerAttr(Player player) {
        loadPlayers.get(player.getName().toLowerCase()).setAttr(new QianKunBagAttr(player));
    }

    /**
     * 属性结构
     * {
     * "Main": {
     * "攻击力": [1,3]
     * }
     * }
     */
    public Map<String, Map<String, float[]>> myAttr = new HashMap<>();

    public void setItemAttrConfig(String id, Object newAttr) {
        Map<String, float[]> attrMap = new HashMap<>();
        Map<String, Object> attr = (Map<String, Object>) newAttr;
        for (Map.Entry<String, Object> entry : attr.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List) {
                List<?> values = (List<?>) value;
                float[] floatValue = new float[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) instanceof Double) {
                        floatValue[i] = ((Double) values.get(i)).floatValue();
                    } else if (values.get(i) instanceof Integer) {
                        floatValue[i] = ((Integer) values.get(i)).floatValue();
                    }
                }
                if (floatValue.length < 2) {
                    floatValue = new float[]{floatValue[0], floatValue[0]};
                }
                attrMap.put(key, floatValue);
            } else if (value instanceof float[]) {
                float[] floatValue = (float[]) value;
                if (floatValue.length < 2) {
                    floatValue = new float[]{floatValue[0], floatValue[0]};
                }
                attrMap.put(key, floatValue);
            } else {
                QiankunBagMain.getInstance().getLogger().warning(key + "不知道是啥类型");
            }
        }

        Map<String, float[]> mainAttrMap = myAttr.get("Main");
        Map<String, float[]> oldAttrMap = deepCopyMap(myAttr.get(id));
        myAttr.put(id, attrMap);
        // 处理newAttr属性
        for (Map.Entry<String, float[]> entry : attrMap.entrySet()) {
            String key = entry.getKey();
            float[] mainValues = new float[]{0.0f, 0.0f};
            if (mainAttrMap.containsKey(key)) {
                mainValues = mainAttrMap.get(key);
            }
            if (mainValues.length < 2) {
                mainValues = new float[]{mainValues[0], mainValues[0]};
            }

            float[] values = attrMap.get(key);
            mainValues[0] = mainValues[0] - (oldAttrMap.containsKey(key) ? oldAttrMap.get(key)[0] : 0) + values[0];
            mainValues[1] = mainValues[1] - (oldAttrMap.containsKey(key) ? oldAttrMap.get(key)[1] : 0) + values[1];

            mainAttrMap.put(key, mainValues);
        }

        // 副作用回收
        // 处理oldAttr有但是newAttr没有的属性
        for (Map.Entry<String, float[]> entry : oldAttrMap.entrySet()) {
            String key = entry.getKey();
            if (!attrMap.containsKey(key)) {
                float[] mainValues = mainAttrMap.get(key);
                float[] values = oldAttrMap.get(key);
                mainValues[0] = mainValues[0] - values[0];
                mainValues[1] = mainValues[1] - values[1];
                mainAttrMap.put(key, mainValues);
            }
        }
    }
}
