package cn.mff.util;

import cn.fhj.util.IoUtil;
import cn.fhj.util.StringUtil;
import cn.mff.model.Config;
import cn.mff.model.ConfigKeyEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigUtils {

    private final static Log log = LogFactory.getLog(ConfigUtils.class);

    private static String LOC_HOME = ".";

    private static List<Config> configList = new ArrayList<Config>();

    private static String getConfigFile() {
        return LOC_HOME + "/config.json";
    }

    private void init() {
        List<Map<String, String>> maps = new ArrayList<>();
        File file = new File(getConfigFile());
        if (file.exists()) {
            String text = IoUtil.readText(file).trim().replaceAll("\n","");
//            JSONObject jsonObject = JSON.parseObject(text);
            JSONArray jsonArray = JSONArray.parseArray(text);
            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                try {
                    Config config = new Config();
                    config.setId((Integer) jsonObject.get(ConfigKeyEnum.ID.getName()));
                    config.setMaxId((String) jsonObject.get(ConfigKeyEnum.MAX_ID.getName()));
                    config.setTwitterName((String) jsonObject.get(ConfigKeyEnum.TWITTER_NAME.getName()));
                    config.setWeiboCookie((String) jsonObject.get(ConfigKeyEnum.WEIBO_COOKIE.getName()));
                    config.setTwitterCookie((String)jsonObject.get(ConfigKeyEnum.TWITTER_COOKIE.getName()));
                    configList.add(config);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("init config error:"+e);
                }
            }
        }
    }

    public synchronized static void updateMaxId(int id,String value){
        for (Config config:configList){
            if (id == config.getId()){
                config.setMaxId(value);
                saveConfigs();
            }
        }

    }

    private synchronized static void saveConfigs(){
        File file = new File(getConfigFile());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // Ignore
                e.printStackTrace();
            }
        }
        IoUtil.write(JSONArray.toJSONString(configList), file);
    }

    public List<Config> getConfigList() {
        return configList;
    }

    public ConfigUtils() {
        init();
    }

    public static void main(String[] args) {
        ConfigUtils configUtils = new ConfigUtils();
        if (configUtils.getConfigList().size()>0){
            for (Config config:configUtils.getConfigList()){
                System.out.println(config.toString());
            }
        }
    }
}
