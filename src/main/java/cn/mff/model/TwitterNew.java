package cn.mff.model;

import cn.fhj.TwitterFrm;
import cn.fhj.twitter.Conversation;
import cn.fhj.twitter.Grid;
import cn.fhj.util.*;
import cn.mff.util.ConfigUtils;
import cn.mff.util.HttpsUtilNew;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class TwitterNew {
  private String LOC_HOME = "c:\\twitter\\";

  public WeiboNew weibo;

  public String getDataFold() {
    return LOC_HOME + name + "/data/";
  }

  private final String MAX_ID = "MAX_ID";

  private String getConfigFile() {
    return LOC_HOME + name + "/config.json";
  }

  public final String HOME = "https://twitter.com/";

  protected String name = "fangshimin";

  protected String maxId = null;

  private Config config;

  private HttpsUtilNew httpsUtilNew;

  public void initMaxId() {
    String idStr = config.getMaxId();
    if (idStr != null) {
      maxId = idStr;
      return;
    }
    String html = httpsUtilNew.getTwitterInstance().doGetForString(HOME + name);
    Elements grids = HtmUtil.getBody(html).getElementById("stream-items-id").getElementsByClass
        ("js-stream-item");

    Element grid = grids.size() > 10 ? grids.get(10) : grids.last();
    maxId = grid.attr("data-item-id");
//    saveConfig(MAX_ID, maxId);
    ConfigUtils.updateMaxId(config.getId(),maxId);
  }

  public String readConfig(String key) {
    Map<String, String> map = readConfig();
    return map.get(key);
  }

  public Map<String, String> readConfig() {
    Map<String, String> map = new HashMap();
    File file = new File(getConfigFile());
    if (file.exists())
      for (String ss : IoUtil.readText(file).trim().split("\\s*\n\\s*")) {
        if (StringUtil.isEmpty(ss)) {
          continue;
        }
        int index = ss.indexOf('=');
        map.put(ss.substring(0, index).trim(), ss.substring(index + 1));
      }
    return map;
  }

  public void saveConfig(String key, Object value) {
    Map<String, String> map = readConfig();
    map.put(key, String.valueOf(value));
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> me : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(me.getKey()).append('=').append(me.getValue());
    }
    File file = new File(getConfigFile());
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        // Ignore
        e.printStackTrace();
      }
    }
    IoUtil.write(sb.toString(), file);
  }

  private final static Log log = LogFactory.getLog(TwitterNew.class);

  public TwitterNew(String name, String dir, WeiboNew weibo, Config config,HttpsUtilNew httpsUtilNew) {
    this.name = name;
    this.LOC_HOME = dir;
    this.weibo = weibo;
    this.config = config;
    this.httpsUtilNew = httpsUtilNew;
  }

  public void start() {
    initMaxId();
    new Thread() {
      @Override
      public void run() {
        for (; ; ) {
          try {
            checkDelete();
            if (!refresh()) {
              return;
            }
            weibo.refresh();
          } catch (Exception e) {
            log.error(e.getMessage(), e);
          }
          ThreadUtil.sleep(weibo.getSleep());
        }
      }

    }.start();
  }

  private static boolean sending = false;

  public static boolean canExist() {
    return !sending;
  }

  protected boolean refresh() throws Exception {
    TwitterFrm.setMessage(DateUtil.format("HH:mm:ss") + " 抓取新微博...");
    String html = readHtml();
    List<Grid> grids = readGrids(html);
    TwitterFrm.setMessage("抓取新微博条数：" + grids.size());
    for (Grid grid : grids) {
      String pic = grid.creatPic();
      try {
        sending = true;
        log.info(grid);
        TwitterFrm.setMessage("微博发送中：" + grid.getText());
        String weiboId = weibo.send(grid);
        if (weiboId != null) {
          maxId = grid.getId();

          if (weibo.shouldSave()) {
            saveGrid(grid, pic);
            if ("-1".equals(weiboId)) {
              saveFailure(grid);
            }
          }
          ConfigUtils.updateMaxId(config.getId(),maxId);
//          saveConfig(MAX_ID, maxId);
          sending = false;
          TwitterFrm.setMessage("微博发成功：" + weiboId);
          weibo.sleepAfterSend();
        } else {
          TwitterFrm.setMessage("微博发送失败！");
          TwitterFrm.getInstance().setButtons(true);
          return false;
        }
      } finally {
        sending = false;
      }
    }
    return true;
  }

  private void checkDelete() {
    long time = System.currentTimeMillis();
    long timeout = 1000 * 60 * 5;
    List<Grid> tobeRemoved = new ArrayList();
    for (Grid grid : weibo.getRecentGrids()) {
      if (time - grid.getDate().getTime() > timeout) {
        tobeRemoved.add(grid);
      } else {
        if (checkDelete(grid)) {
          tobeRemoved.add(grid);
        }
      }
    }
    weibo.getRecentGrids().removeAll(tobeRemoved);
  }

  public boolean checkDelete(Grid grid) {
//    HttpsUtil util = HttpsUtil.getTwitterInstance();
    String s = httpsUtilNew.doGetForString(HOME + name + "/status/" + grid.getId());
    if (s.indexOf(grid.getId()) < 0) {
      weibo.delete(grid.getSinaId());
      return true;
    }
    return false;
  }

  public void saveGrid(Grid grid, String pic) {
    if (pic != null) {
      String newPic = getDataFold() + grid.getId() + pic.substring(pic.lastIndexOf('.'));
      new File(pic).renameTo(new File(newPic));
    }
    IoUtil.write(grid.getText(), new File(getDataFold() + grid.getId() + ".txt"));
  }

  public void saveFailure(Grid grid) {
    String s = grid.getText().length() > 10 ? grid.getText().substring(0, 10) + "..." : grid
        .getText();
    IoUtil.write("新浪微博不让机器人转发：" + s ,
        new File(getDataFold() + (Long.parseLong(grid.getId()) + 1) + ".txt"));
  }

  private List<Grid> readGrids(String html) {
    List<Element> gridElements = HtmUtil.getBody(html).getElementsByClass("js-stream-item");
    Collections.reverse(gridElements);
    List<Grid> grids = new ArrayList();
    for (Element ge : gridElements) {
      Grid grid = parse(ge);
      if (grid != null) {
        grids.add(grid);
      }

    }
    return grids;
  }

  private Grid parse(Element ge) {
    Grid grid = new Grid();
    grid.setId(ge.attr("data-item-id"));
    if (Long.parseLong(maxId) >= Long.parseLong(grid.getId())) {
      log.warn("已经发送过的id:" + grid.getId());
      return null;
    }
    String conversationClass = "ReplyingToContextBelowAuthor";
    if (ge.getElementsByClass(conversationClass).isEmpty()) {
      grid.setText(grapText(ge));
    } else {
      grid.setText(grapOrign(ge) + grapText(ge));
    }
    // ge.getElementsByAttribute("data-expanded-url").attr("data-expanded-url")
    // grapReply(ge, grid);
    if (!grapReply(ge, grid) && !ge.getElementsByClass(conversationClass).isEmpty()) {
      grapConversation(grid, name);
    }
    grapPic(grid, ge);
    grid.simpleConverSations();
    return grid;
  }

  protected boolean grapReply(Element ge, Grid grid) {
    try {
      Elements elments = ge.getElementsByAttribute("data-expanded-url");
      if (elments.isEmpty()) {
        return false;
      }
      String url = elments.attr("data-expanded-url");
      String twitter = "https://twitter.com/";
      int index1 = url.indexOf(twitter);
      String status = "/status/";
      int index2 = url.indexOf(status);
      if (index1 != 0 && index2 < 1) {
        return false;
      }
      String name = url.substring(twitter.length(), index2);
      String id = url.substring(index2 + status.length());
      Grid replyGrid = grap(id, name);
      grid.conversations.addAll(replyGrid.conversations);
      grid.conversations.add(new Conversation(replyGrid.getOwner(), replyGrid.getText()));
      grid.getPicFiles().addAll(replyGrid.getPicFiles());
      grid.setText(grid.getText().replaceAll(twitter + name + status + id, ""));
      return true;
    } catch (Exception e) {
      LogFactory.getLog(TwitterNew.class).warn("grapReply出错：" + e, e);
      return false;
    }
  }

  protected Grid grap(String id, String name) {

    Grid grid = new Grid();
    grid.setId(id);

    String post = HOME + name + "/status/" + grid.getId();
//    HttpsUtil util = HttpsUtil.getTwitterInstance();
    String html = httpsUtilNew.doGetForString(post);
    Element body = HtmUtil.getBody(html);
    Element ancestors = body.getElementById("ancestors");
    if (ancestors != null) {
      grapConversations(grid, ancestors);
    }
    Element item = body.getElementsByAttributeValue("data-item-id", id).get(0);
    grid.setOwner(item.attr("data-name").replaceAll("\\s+", ""));
    grid.setText(grapText(item));
    grapPic(grid, item);

    grid.simpleConverSations();
    return grid;
  }

  private void grapConversations(Grid grid, Element ancestors) {
    // data-item-type="tweet"
    for (Element li : ancestors.getElementById("stream-items-id").getElementsByClass
        ("js-stream-item")) {
      Element div = li.child(0);
      Elements quoteTwts = div.getElementsByClass("QuoteTweet-container");
      if (!quoteTwts.isEmpty()) {
        div.getElementsByClass("twitter-timeline-link").first().remove();
        grapQuote(grid, quoteTwts.first());
      }
      String owner = div.attr("data-name").replaceAll("\\s+", "");
      Elements list = div.getElementsByClass("js-tweet-text");
      if (!list.isEmpty()) {
        Element gli = list.first();
        grapCoversationPic(grid, div, gli);
        String text = grapText(gli);
        grid.addConversation(owner, text);
      }
    }
  }

  public static String getId(Element ge) {
    String id = ge.child(0).attr("data-retweet-id");
    if (StringUtil.isEmpty(id)) {
      id = ge.attr("data-item-id");
    }
    return id;
  }

  private static String grapOrign(Element element) {
    Elements origins = element.getElementsByClass("ProfileTweet-originalAuthor");
    if (origins.isEmpty()) {
      return "";
    }
    Element e = origins.get(0);
    String name = e.getElementsByTag("b").get(0).text().trim();
    if (name.indexOf(' ') > 0) {
      name = e.getElementsByClass("ProfileTweet-screenname").get(0).text().trim().substring(0);
    }
    return '@' + name + ':';
  }

  protected void grapConversation(Grid grid, String name) {
//    HttpsUtil util = HttpsUtil.getTwitterInstance();
    String html = httpsUtilNew.doGetForString(HOME + name + "/status/" + grid.getId());
    Element body = HtmUtil.getBody(html);
    grapConversations(grid, httpsUtilNew, body);
  }

  private void grapConversations(Grid grid, HttpsUtilNew util, Element body) {
    for (Element li : body.getElementById("stream-items-id").getElementsByClass("js-stream-item")) {
      Element div = li.child(0);
      Elements quoteTwts = div.getElementsByClass("QuoteTweet-container");
      if (!quoteTwts.isEmpty()) {
        div.getElementsByClass("twitter-timeline-link").first().remove();
        grapQuote(grid, quoteTwts.first());
      }
      String owner = div.attr("data-name").replaceAll("\\s+", "");
      Elements list = div.getElementsByClass("js-tweet-text");
      if (!list.isEmpty()) {
        Element gli = list.first();
        grapCoversationPic(grid, div, gli);
        String text = grapText(gli);
        grid.addConversation(owner, text);
      }
    }
  }

  private void grapQuote(Grid grid, Element div) {
    grapPic(grid, div);
    String owner = div.getElementsByClass("QuoteTweet-fullname").text().replaceAll("\\s+", "");
    Element content = div.getElementsByClass("QuoteTweet-text").first();
    content.getElementsByAttribute("data-pre-embedded").remove();
    grid.addConversation(owner, content.text());
  }

  public void grapCoversationPic(Grid grid, Element li, Element gli) {
    Elements as = gli.getElementsByTag("a");
    if (!as.isEmpty() && as.last().text().startsWith("pic.twitter.com/")) {
      String path = li.attr("data-permalink-path");
      String status = "/status/";
      Element body = HtmUtil.getBody(httpsUtilNew.doGetForString
          ("https://twitter.com" + path));
      Elements elements = body.getElementsByAttributeValue("data-tweet-id",
          path.substring(path.lastIndexOf(status) + status.length(), path.length()));
      if (!elements.isEmpty()) {
        grapPic(grid, elements.first());
      }

    }
  }

  public String grapText(Element textElement) {
    textElement = textElement.getElementsByClass("js-tweet-text").get(0);
    textElement.getElementsByAttributeValue("data-pre-embedded", "true").remove();
    textElement.getElementsByClass("tco-ellipsis").remove();
    String text = textElement.text().trim();
    Elements elements = textElement.getElementsByClass("pretty-link");
    Collections.reverse(elements);
    for (Element at : elements) {
      String atTxt = at.text();
      if (text.endsWith(atTxt)) {
        text = text.substring(0, text.length() - atTxt.length()).trim();
        at.remove();
      }
    }
    for (Element at : textElement.getElementsByClass("pretty-link")) {
      String atText = at.text();
      if (text.startsWith(atText)) {
        text = text.substring(atText.length(), text.length()).trim();
        at.remove();
        continue;
      }
      Element ate = at.getElementsByTag("b").get(0);
      replaceWithName(ate);
    }
    return textElement.text();
  }

  public void replaceWithName(Element ate) {
    try {
      String no = ate.text();
//      HttpsUtil util = HttpsUtil.getTwitterInstance();
      String html = httpsUtilNew.doGetForString("https://twitter.com/" + no);
      String title = "<title>";
      String name = html.substring(html.indexOf(title) + title.length(), html.indexOf("(@")).trim();
      if (name.indexOf(' ') < 0) {
        ate.text(name);
      }
    } catch (Exception e) {
      LogFactory.getLog(TwitterNew.class).warn("获取账号出错：" + ate.text(), e);
    }
  }

  private static void grapPic(Grid grid, Element ge) {
    for (Element e : ge.getElementsByAttribute("data-image-url")) {
      grid.addPic(e.attr("data-image-url"));
    }
  }

  protected String readHtml() throws Exception {
    // data-retweet-id="574437716307267584"
    String currentId = maxId;// "-1";//
    // "573834450653446145";//574104614695362560

    // String url = HOME
    // + "i/profiles/show/"
    // + name
    // +
    // "/timeline?composed_count=0&include_available_features=1&include_entities=1
    // &include_new_items_bar=true&interval=30000&last_note_ts=266&latent_count=0
    // &oldest_unread_id=0&min_position="
    // + currentId;
    //
    String url = HOME + "i/profiles/show/" + name
        + "/timeline/with_replies?composed_count=0&include_available_features=1&include_entities" +
        "=1&include_new_items_bar=true"
        + "&interval=60000&latent_count=0&min_position=" + currentId;
    // String url = HOME + "i/profiles/show/" + name +
    // "/timeline?composed_count=0&contextual_tweet_id=" + currentId
    // +
    // "&include_available_features=1&include_entities=1&include_new_items_bar=true"
    // + "&interval=30000&latent_count=0&since_id=" + currentId;
//    HttpsUtil util = HttpsUtil.getTwitterInstance();

    String retVal = httpsUtilNew.doGetForString(url);

    // IoUtil.write(util.doGetForStream("https://pbs.twimg.com/media/B_QnDLXU8AABETf.jpg"),
    // "c:/8AABETf.jpg");

    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");
    engine.eval("function test(){var json=" + retVal + "; return json.items_html" + "}");
    Invocable inv = (Invocable) engine;
    String value = String.valueOf(inv.invokeFunction("test"));

    String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; " +
        "charset=utf-8\" /></head><body>"
        + value + "</body></html>";
    // IoUtil.write(html, new File("c:/logined.html"));
    return html;
  }

  public static JSONObject parseJson(String retVal) {
    JSONObject json;
    try {
      json = new JSONObject(retVal);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return json;
  }
}
