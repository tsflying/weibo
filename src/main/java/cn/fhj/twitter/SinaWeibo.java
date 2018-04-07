package cn.fhj.twitter;

import org.json.JSONObject;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.fhj.util.HtmUtil;
import cn.fhj.util.HttpsUtil;
import cn.fhj.util.LogUtil;

public class SinaWeibo extends Weibo {

  public SinaWeibo(String cks) {
    util = HttpsUtil.getSinaInstance(cks);
    String msg;
    try {
      getSt();
      msg = "Succes logined";
    } catch (Exception e) {
      LogUtil.getLog(this.getClass()).error(e, e);
      msg = "Failed to login";
    }

    showMessage(msg + " " + this.getClass().getSimpleName());
  }

  protected void getSt() {
    this.setReady(false);
    String html = util.doGetForString("https://weibo.cn/");
    Elements form = HtmUtil.getBody(html).getElementsByTag("form");
    if (form.size() < 1) {
      throw new RuntimeException("No found st,maybe not logined");
    }
    String action = form.get(0).attr("action");
    int indexOf = action.indexOf("st=");
    if (indexOf < 0) {
      throw new RuntimeException("No found st,maybe not logined");
    }
    st = action.substring(indexOf + 3);
    this.setReady(true);
    System.out.println(st);
  }

  private String st = null;

  private boolean hidden = false;

  private long time = 0;

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public String send(Grid grid) {
    long curTime = System.currentTimeMillis();

    if (curTime - time > 1000 * 3600 * 6) {
      try {
        getSt();
      } catch (Exception e) {
        LogUtil.getLog(this.getClass()).error(e, e);
        return null;
      }
    }
    time = curTime;
    Map<String, String> formData = new HashMap();
    formData.put("content", trim(grid.getText()));
    formData.put("st", st);
    formData.put("visible", hidden ? "1" : "0");
    String picId = uploadPics(grid.getPicFiles());
    if (picId != null) {
      formData.put("picId", picId);
    }
    for (int count = 0; count < 3; count++) {
      String result = util.post("https://m.weibo.cn/mblogDeal/addAMblog", formData, null, null);
      if (result != null) {
        String sinaId = getSinaId(result);

        if (sinaId != null) {
          if (!"-1".equals(sinaId)) {
            grid.setSinaId(sinaId);
            this.getRecentGrids().add(grid);
          }
          return sinaId;
        }
      }
      try {
        getSt();
      } catch (Exception e) {
        LogUtil.getLog(this.getClass()).error(e, e);
        return null;
      }
    }
    return null;
  }

  private String getSinaId(String result) {
    try {
      //{"ok":20021,"msg":"\u53d1\u5e03\u5931\u8d25\uff1a content is illegal!"}
      JSONObject object = new JSONObject(result);
      return object.getString("id");
    } catch (Exception e) {
      LogUtil.getLog(this.getClass()).error("parse sinaId error:" + result, e);
      if (result.contains("20021")) {
        return "-1";
      }
      return null;
    }
  }

  private String uploadPics(List<String> pics) {
    StringBuilder sb = new StringBuilder();
    for (String pic : pics) {
      String id = uploadPic(pic);
      if (id != null) {
        sb.append(id).append(',');
      }
    }
    if (sb.length() > 0) {
      return sb.substring(0, sb.length() - 1);
    }
    return null;
  }

  protected String uploadPic(String pic) {
    Map<String, String> formData = new HashMap();
    formData.put("type", "json");
    String s = util.post("https://m.weibo.cn/mblogDeal/addPic", formData, "pic", pic);
    if (s == null) {
      System.out.print(s);
      return null;
    }
    try {
      JSONObject object = new JSONObject(s);
      return object.getString("pic_id");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void delete(String sinaId) {
    Map<String, String> formData = new HashMap();
    formData.put("id", sinaId);
    util.post("https://m.weibo.cn/mblogDeal/delMyMblog", formData, null, null);
  }

  @Override
  public String getName() {
    return "Sina";
  }

}
