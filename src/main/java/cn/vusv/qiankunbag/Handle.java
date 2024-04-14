package cn.vusv.qiankunbag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Handle {

    public static String[] getDefaultFiles(String folderName) {
        List<String> names = new ArrayList<>();
        File files = new File(QiankunBagMain.getInstance().getDataFolder() + "/" + folderName);
        if (files.isDirectory()) {
            File[] filesArray = files.listFiles();
            if (filesArray != null) {
                for (File file : filesArray) {
                    names.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
                }
            }
        }
        return names.toArray(new String[0]);
    }
}
