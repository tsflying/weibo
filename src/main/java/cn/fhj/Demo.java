package cn.fhj;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Demo {
//    public static void main(String[] args)throws Exception {
//        CloseableHttpClient httpClient=HttpClients.createDefault(); // 创建httpClient实例
//        HttpGet httpGet=new HttpGet("https://twitter.com/"); // 创建httpget实例
//        HttpHost proxy=new HttpHost("82.165.181.78", 3128);
//        RequestConfig requestConfig=RequestConfig.custom().setProxy(proxy).build();
//        httpGet.setConfig(requestConfig);
//        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
//        CloseableHttpResponse response=httpClient.execute(httpGet); // 执行http get请求
//        HttpEntity entity=response.getEntity(); // 获取返回实体
//        System.out.println("网页内容："+EntityUtils.toString(entity, "utf-8")); // 获取网页内容
//        response.close(); // response关闭
//        httpClient.close(); // httpClient关闭
//    }
    /** 代理参数 IP+PORT **/
    private static String PROXY_IP = "localhost";
    private static int PROXY_PORT = 1080;

    public static void main(String[] args) throws Exception {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory()).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
        try {
            InetSocketAddress socksaddr = new InetSocketAddress(PROXY_IP, PROXY_PORT);
            HttpClientContext context = HttpClientContext.create();
            context.setAttribute("socks.address", socksaddr);

            HttpHost target = new HttpHost("www.google.com", 80, "http");
            HttpGet request = new HttpGet("/");

            System.out.println("Executing request " + request + " to " + target + " via SOCKS proxy " + socksaddr);
            CloseableHttpResponse response = httpclient.execute(target, request, context);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                String htmlStr = EntityUtils.toString(response.getEntity());
                System.out.println(htmlStr);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    static class MyConnectionSocketFactory implements ConnectionSocketFactory {

        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

        public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
                                    final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext context)
                throws IOException, ConnectTimeoutException {
            Socket sock;
            if (socket != null) {
                sock = socket;
            } else {
                sock = createSocket(context);
            }
            if (localAddress != null) {
                sock.bind(localAddress);
            }
            try {
                sock.connect(remoteAddress, connectTimeout);
            } catch (SocketTimeoutException ex) {
                throw new ConnectTimeoutException(ex, host, remoteAddress.getAddress());
            }
            return sock;
        }
    }
}
