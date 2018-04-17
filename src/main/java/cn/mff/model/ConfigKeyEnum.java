package cn.mff.model;

public enum ConfigKeyEnum {

    ID("id"),
    MAX_ID("maxId"),
    TWITTER_NAME("twitterName"),
    WEIBO_COOKIE("weiboCookie"),
    TWITTER_COOKIE("twitterCookie");

    private ConfigKeyEnum(String name){
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
