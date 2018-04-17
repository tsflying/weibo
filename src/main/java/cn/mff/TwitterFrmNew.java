package cn.mff;

import cn.fhj.twitter.Twitter;
import cn.fhj.util.DateUtil;
import cn.fhj.util.HttpsUtil;
import cn.fhj.util.IoUtil;
import cn.fhj.util.StringUtil;
import cn.mff.model.Config;
import cn.mff.model.QqWeiboNew;
import cn.mff.model.SinaWeiboNew;
import cn.mff.model.WeiboNew;
import cn.mff.util.ConfigUtils;
import cn.mff.model.TwitterNew;
import cn.mff.util.HttpsUtilNew;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TwitterFrmNew extends JFrame {
    private final static Log log = LogFactory.getLog(TwitterFrmNew.class);
    public static final String TWT_CKS = "personalization_id=\"v1_+6gEbVjnpLiLRCTV85CPpw==\"; " +
            "personalization_id=\"v1_+6gEbVjnpLiLRCTV85CPpw==\"; guest_id=v1%3A148928981066601565; " +
            "moments_profile_moments_nav_tooltip_self=true; lang=zh-cn; " +
            "ct0=172195915d987fcb3938116b616935dd;"
            + " personalization_id=\"v1_+6gEbVjnpLiLRCTV85CPpw==\"; " +
            "personalization_id=\"v1_+6gEbVjnpLiLRCTV85CPpw==\"; "
            + "kdt=YhkqffM4LPizQS94IheWnj1C5SSY6NycimA2N5kS; remember_checked_on=1; " +
            "twid=\"u=2869790670\"; "
            + "auth_token=6ce5b8b6495a2bbf139e89c2acc7c07f79b4fa63; _ga=GA1.2.1449059937.1489289816;"
            + " _gid=GA1.2.1159023704.1493737594; ";

    private static final long serialVersionUID = 1983923261476635370L;

    private JTextField name = new JTextField("fangshimin");

    private JButton begin = new JButton("转weibo.com");

    private JButton beginQq = new JButton("转t.qq.com");

    private JTextField cksTxt = new JTextField(
            "3g_guest_id=-9098289108752502784; pac_uid=1_46214582; tvfe_boss_uuid=d6733165f610b9d5; " +
                    "RK=xQ2umemfes; wbilang_10000=zh_CN; wbilang_3227344491=zh_CN; " +
                    "wb_regf=%3B0%3B%3Bmessage" +
                    ".t.qq.com%3B0; ts_last=w.t.qq.com/touch; ts_uid=5188913800; mb_reg_from=8; " +
                    "ptui_loginuin=weibofzztt; ptisp=ctc; luin=o3227344491; " +
                    "lskey=00010000b5092bc741187df6bdf2aff5c6cc8d9a678bcb9c2a84bde96479c43ed4f66a9edb0199b840c4d6f9; pt2gguin=o3227344491; uin=o3227344491; skey=@MOoLsPbPm; p_uin=o3227344491; p_skey=kfs5uU16Q0*hf1JseNdlRhpaKJxk1sLAUYaUtMJoLJY_; pt4_token=g9schGghIZB71dQYmf26f-caA3ubUU1nHwAWlmpz7sM_; p_luin=o3227344491; p_lskey=00040000007d7b704073bf5367db50fb76e9b9c91880b955dc6cd534f8dd8f0c83b0b6b043a4108451c44cc7; ts_last=t.qq.com/weibofzztt; o_cookie=3227344491; ts_uid=5188913800; pgv_info=ssid=s7419015856; pgv_pvid=2487860710");
    private JTextField twtCksTxt = new JTextField(TWT_CKS);

    private JFileChooser jfc = new JFileChooser();

    private JTextArea statusText = new JTextArea();

    private JScrollPane scroll = new JScrollPane(statusText);

    private JTextField saveDir = new JTextField(getCurrentDir());// ("c:/twitter");

    private JButton dirBt = new JButton(":");

    private static String getCurrentDir() {
        return IoUtil.getProjectPath();

    }

    public TwitterFrmNew() {
        setResizable(false);
        setLayout(null);
        this.setTitle("转发Twitter17-8-12");

        int frmWidth = 680, frmHeight = 380;
        int xSpace = 20, ySpace = 10, margin = 30;
        int x = margin, y = 10, lblWidth = 100, height = 30, btHeight = height;
        int btWidth = 60;
        int proxyCheckBoxWidth = 80;

        JLabel dirLbl = new JLabel("本地目录");
        add(dirLbl);
        dirLbl.setBounds(x, y, lblWidth, height);
        x += lblWidth + xSpace;
        add(saveDir);
        saveDir.setBounds(x, y, frmWidth - x - 3 * xSpace - margin - btWidth - proxyCheckBoxWidth,
                height);
        saveDir.setEditable(false);
        add(dirBt);
        dirBt.setBounds(x + saveDir.getWidth(), y, btHeight, btHeight);

        // add(beginQq);
        // beginQq.setBounds(frmWidth - margin - 2 * btWidth, y, 2 * btWidth,
        // btHeight);

        x = margin;
        y += height + ySpace;
        JLabel bokLbl = new JLabel("转发推特账户");
        add(bokLbl);
        bokLbl.setBounds(x, y, lblWidth, height);

        x += bokLbl.getWidth() + xSpace;
        this.add(name);
        name.setBounds(x, y, frmWidth - x - 3 * xSpace - margin - btWidth - proxyCheckBoxWidth, height);

        add(begin);
        begin.setBounds(frmWidth - margin - 2 * btWidth, y, 2 * btWidth, btHeight);

        x = margin;
        y += height + ySpace;
        JLabel sinaLbl = new JLabel("Sina Cookies");
        add(sinaLbl);
        sinaLbl.setBounds(x, y, lblWidth, height);
        x += sinaLbl.getWidth() + xSpace;
        cksTxt.setBounds(x, y, frmWidth - x - margin, height);
        this.add(cksTxt);

        x = margin;
        y += height + ySpace;
        JLabel twitterLbl = new JLabel("Twitter Cookies");
        add(twitterLbl);
        twitterLbl.setBounds(x, y, lblWidth, height);
        x += twitterLbl.getWidth() + xSpace;
        twtCksTxt.setBounds(x, y, frmWidth - x - margin, height);
        this.add(twtCksTxt);

        // progress.setStringPainted(true); // 设置进度条呈现进度字符串,默认为false
        statusText.setEditable(false);
        statusText.setBackground(new Color(255, 255, 200));

        y += height + ySpace;
        scroll.setBounds(xSpace, y, frmWidth - 2 * xSpace, 6 * height);
        statusText.setLineWrap(true); // 激活自动换行功能
        statusText.setWrapStyleWord(true);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll);
        setSize(frmWidth, frmHeight);
        setVisible(true);
        double lx = Toolkit.getDefaultToolkit().getScreenSize().getWidth();

        double ly = Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        setLocation(new Point((int) (lx / 2) - this.getWidth() / 2, (int) (ly / 2) - this.getHeight()
                / 2));// 设定窗口出现位置
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                if (Twitter.canExist()) {
                    System.exit(0);
                } else {
                    setMessage("Wait for sending...");
                }
            }
        });
        dirBt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                String dir = getSaveDir();
                if (dir == null) {
                    name.grabFocus();
                    return;
                }
                saveDir.setText(dir);

            }

            private String getSaveDir() {
                jfc.setFileSelectionMode(1);// 设定只能选择到文件夹
                int state = jfc.showOpenDialog(TwitterFrmNew.this);// 此句是打开文件选择器界面的触发语句
                if (state == 1) {
                    return null;
                } else {
                    File f = jfc.getSelectedFile();// f为选择到的目录
                    return f.getAbsolutePath();
                }
            }
        });
        ActionListener beginListener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ConfigUtils configUtils = new ConfigUtils();
                List<Config> configs = configUtils.getConfigList();
                if (configs.isEmpty()) {
                    log.error("config is empty!!!");
                }
                ExecutorService executorService = Executors.newFixedThreadPool(configs.size());
                for (int i = 0; i < configs.size(); i++) {
                    executorService.execute(
                            new TaskThread(configs.get(i), e, begin, beginQq));
                }
                ;
            }
        };
        begin.addActionListener(beginListener);
        beginQq.addActionListener(beginListener);

        message("点击开始按钮即可开始转推");
        message("敬请关注");
        message("脑中有科学，心中有道义");
        message("程序制作人：@后军");
    }

    class TaskThread implements Runnable {
        private ThreadLocal<Config> configThreadLocal;
        private ActionEvent e;
        private JButton begin;
        private JButton beginQq;

        public TaskThread(Config config, ActionEvent e, JButton begin, JButton beginQq) {
            this.configThreadLocal = new ThreadLocal<Config>(){
                protected Config initialValue(){
                    return config;
                }
            };
            this.e = e;
            this.begin = begin;
            this.beginQq = beginQq;
        }

        @Override
        public void run() {
            Config config = configThreadLocal.get();
            if (null == config
                    || StringUtil.isEmpty(config.getTwitterName())
                    || StringUtil.isEmpty(config.getWeiboCookie())) {
                return;
            }
            setButtons(false);
            new Thread() {
                public void run() {
                    try {
                        WeiboNew weibo;
                        String cks = config.getWeiboCookie();
                        if (e.getSource().equals(beginQq)) {
                            weibo = new QqWeiboNew(cks, config);
                        } else {
                            weibo = new SinaWeiboNew(cks, config);
                        }
//                      TwitterFrmNew.this.setTitle(weibo.getName() + (++times) + ":" + new Date());
                        if (weibo.isReady()) {
                            HttpsUtilNew httpsUtilNew = new HttpsUtilNew(config.getTwitterCookie(),"");
//                            httpsUtilNew.setTwtCks(config.getTwitterCookie());
                            TwitterNew twitterNew = new TwitterNew(config.getTwitterName().trim(),
                                    getSaveDir(), weibo, config,httpsUtilNew);
                            twitterNew.start();
                        } else {
                            setButtons(true);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }

                private String getSaveDir() {
                    String saveText = saveDir.getText().trim();
                    if (saveText.endsWith("/") || saveText.endsWith("\\")) {
                        return saveText;
                    }
                    return saveText + "/";
                }
            }.start();
        }
    }

    private static int times = 0;
    protected static TwitterFrmNew frm;

    public static void main(String[] args) {
        if (!new File(Twitter.getDataFold()).exists()) {
            new File(Twitter.getDataFold()).mkdirs();
        }
        frm = new TwitterFrmNew();
    }

    private Queue<String> que = new LinkedList();

    public static void setMessage(String msg) {
        if (frm != null) {
            frm.message(msg);
        } else {
            System.out.println(msg);
        }
    }

    public void message(String msg) {
        log.info(msg);
        if (que.size() > 20) {
            que.poll();
        }
        que.add(msg);
        StringBuilder sb = new StringBuilder();
        for (String s : que) {
            sb.append(DateUtil.format("MM-dd HH:mm")).append(s).append("\n");
        }
        statusText.setText(sb.toString());
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    public void setButtons(boolean enable) {
        begin.setEnabled(enable);
        beginQq.setEnabled(enable);
        name.setEditable(enable);
        autoRestart();
    }

    public static String downdPic(String fileUrl) {
        String savePath = Twitter.getDataFold() + "login.jpg";
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(savePath));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            connection.disconnect();

        } catch (Exception e) {
            System.out.println(e + fileUrl + savePath);
        }
        return savePath;
    }

    public static TwitterFrmNew getInstance() {
        return frm;
    }

    public void autoRestart() {
        if (!this.begin.isEnabled()) {
            return;
        }
        try {
            Thread.sleep(6000 * 5);
        } catch (InterruptedException e) {
            //ingore
        }
        this.begin.doClick();
    }
}
