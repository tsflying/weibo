package cn.mff.model;

public class Config {

    private int id;
    private String twitterName;
    private String weiboCookie;
    private String twitterCookie;
    private String maxId;

    public Config(){

    }

    public Config(String twitterName,String weiboCookie){
        this.twitterCookie = twitterName;
        this.weiboCookie = weiboCookie;
    }

    public Config(String twitterName,String weiboCookie,String twitterCookie){
        this(twitterName,weiboCookie);
        this.twitterCookie = twitterCookie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTwitterName() {
        return twitterName;
    }

    public void setTwitterName(String twitterName) {
        this.twitterName = twitterName;
    }

    public String getWeiboCookie() {
        return weiboCookie;
    }

    public void setWeiboCookie(String weiboCookie) {
        this.weiboCookie = weiboCookie;
    }

    public String getTwitterCookie() {
        return twitterCookie;
    }

    public void setTwitterCookie(String twitterCookie) {
        this.twitterCookie = twitterCookie;
    }

    public String getMaxId() {
        return maxId;
    }

    public void setMaxId(String maxId) {
        this.maxId = maxId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("twitterName="+getTwitterName()).append("\n");
        sb.append("weiboCookie="+getWeiboCookie()).append("\n");
        sb.append("twitterCookie="+getTwitterCookie()).append("\n");
        sb.append("maxId="+getMaxId()).append("\n");
        return sb.toString();
    }
}
