package auto.controller;

import com.atguigu.gulimall.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName Test
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/25
 * @Version V1.0
 **/
public class Test {
    public static void main(String[] args) throws Exception {
        //1.使用code换取token，换取成功则继续2，否则重定向至登录页
        Map<String, String> query = new HashMap<>();
        query.put("client_id", "2144471074");
        query.put("client_secret", "ff63a0d8d591a85a29a19492817316ab");
        query.put("grant_type", "authorization_code");
        query.put("redirect_uri", "http://localhost:20000/oauth2.0/weibo/success");
        query.put("code", "716a49e19d7b01e5482519344964e1b0");
        //发送post请求换取token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<String, String>(), query, new HashMap<String, String>());
        System.out.println(EntityUtils.toString(response.getEntity()));
    }
}
