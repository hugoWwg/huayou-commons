package com.huayou.commons.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @Author : wwg
 * @Date : 14-11-10 环信调用接口
 */
public class HXRequest {

    private static Logger logger = LoggerFactory.getLogger(HXRequest.class);
    public static final String HX_TOKEN_EXPIRE_TIME = "tokenExpireTime";
    public static final String HX_ACCESS_TOKEN = "access_token";
    private static final String HX_DOMAIN_NAME = "https://a1.easemob.com/";
    private static JsonNodeFactory factory = new JsonNodeFactory(false);

    /*
    * 获取环信token
    * */

    public Map<String, String> getHXToken(String orgName, String appName, String clientId,
                                          String clientSecret) {
        Map<String, String> retMap = Maps.newHashMap();
        String getTokenUrl = HX_DOMAIN_NAME + orgName + "/" + appName + "/token";
        String results = null;

        try {
            URL url = new URL(getTokenUrl);
            JSONObject objectNode = new JSONObject();
            objectNode.put("grant_type", "client_credentials");
            objectNode.put("client_id", clientId);
            objectNode.put("client_secret", clientSecret);
            List<NameValuePair> headers = new ArrayList<NameValuePair>();
            headers.add(new BasicNameValuePair("Content-Type", "application/json"));

            HttpPost httpPost = new HttpPost();
            httpPost.setURI(url.toURI());

            if (null != headers && !headers.isEmpty()) {
                for (NameValuePair nameValuePair : headers) {
                    httpPost.addHeader(nameValuePair.getName(), nameValuePair.getValue());
                }
            }
            httpPost.setEntity(new StringEntity(objectNode.toString(), "UTF-8"));
            HttpClient httpClient = getClient(true);
            HttpResponse tokenResponse = httpClient.execute(httpPost);

            if (tokenResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                HttpEntity entity = tokenResponse.getEntity();
                results = EntityUtils.toString(entity, "UTF-8");

                if (!Strings.isNullOrEmpty(results)) {
                    JSONObject jsonObject = JSONObject.parseObject(results);

                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        String value = entry.getValue().toString();
                        String key = entry.getKey().toString();
                        retMap.put(key, value);
                    }
                    long tokenExpireTime = DateUtils.daysAfter(7).getTime();
                    retMap.put(HX_TOKEN_EXPIRE_TIME, String.valueOf(tokenExpireTime));
                }

                System.out.println(results);
            }
        } catch (Exception e) {
            logger.error("getHXToken Exception--->" + e);
        }

        return retMap;
    }

    /**
     * 获取ssl认证
     */

    private HttpClient getClient(boolean isSSL) {

        HttpClient httpClient = new DefaultHttpClient();
        if (isSSL) {
            X509TrustManager xtm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            try {
                SSLContext ctx = SSLContext.getInstance("TLS");

                ctx.init(null, new TrustManager[]{xtm}, null);

                SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);

                httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", 443, socketFactory));

            } catch (Exception e) {
                logger.error("getHXToken Exception--->" + e);
            }
        }

        return httpClient;
    }

    /**
     * 单个IM用户注册；兼容老项目
     */
    @Deprecated
    public Map<String, String> createNewIMUserSingle(String orgName, String appName,
                                                     String IMUserName,
                                                     String IMPassword, String clientId,
                                                     String clientSecret, String access_token,
                                                     String tokenExpireTime) {
        Long expireTime = null;
        if (org.apache.commons.lang3.math.NumberUtils.isNumber(tokenExpireTime)) {
            expireTime = Long.parseLong(tokenExpireTime);
        }
        return createNewIMUserSingle(orgName, appName, IMUserName, IMPassword, clientId,
                                     clientSecret, access_token, expireTime);
    }

    /**
     * 单个IM用户注册
     */

    public Map<String, String> createNewIMUserSingle(String orgName, String appName,
                                                     String IMUserName,
                                                     String IMPassword, String clientId,
                                                     String clientSecret, String access_token,
                                                     Long expireTime) {

        Map<String, String> retMap = Maps.newHashMap();

        String registerIMUserUrl = HX_DOMAIN_NAME + orgName + "/" + appName + "/users";

        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("username", IMUserName);
            jSONObject.put("password", IMPassword);
            List<NameValuePair> headers = new ArrayList<NameValuePair>();
            headers.add(new BasicNameValuePair("Content-Type", "application/json"));

            boolean isAccessTokenExpired = false;
            Map<String, String> newTokenMap = null;
            if (null != expireTime && new Date().getTime() < expireTime.longValue()) {
                //token is not expireTime
                headers.add(new BasicNameValuePair("Authorization", "Bearer " + access_token));
            } else {
                isAccessTokenExpired = true;
                //token is expireTime
                newTokenMap = getHXToken(orgName, appName, clientId, clientSecret);
                headers.add(new BasicNameValuePair("Authorization",
                                                   "Bearer " + newTokenMap.get(HX_ACCESS_TOKEN)));
            }

            Map<String, String>
                retNewMap =
                sendRequest(headers, registerIMUserUrl, jSONObject, "post", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }
            if (null != retNewMap) {
                return retNewMap;
            }

            for (int i = 1; i <= 3; i++) {
                if (null == retNewMap) {
                    retNewMap = sendRequest(headers, registerIMUserUrl, jSONObject, "post", retMap);
                } else {
                    break;
                }
            }

            if (null != retNewMap) {
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("createNewIMUserSingle Exception--->" + e);
        }
        return retMap;
    }

    /**
     * 批量IM用户注册；兼容老项目
     */
    @Deprecated
    public Map<String, String> batchCreateNewIMUsersSingle(String orgName, String appName,
                                                           ArrayNode dataArrayNode,
                                                           String clientId, String clientSecret,
                                                           String access_token,
                                                           String tokenExpireTime) {
        Long expireTime = null;
        if (org.apache.commons.lang3.math.NumberUtils.isNumber(tokenExpireTime)) {
            expireTime = Long.parseLong(tokenExpireTime);
        }
        return batchCreateNewIMUsersSingle(orgName, appName, dataArrayNode, clientId, clientSecret,
                                           access_token, expireTime);
    }

    /**
     * 批量IM用户注册
     */

    public Map<String, String> batchCreateNewIMUsersSingle(String orgName, String appName,
                                                           ArrayNode dataArrayNode,
                                                           String clientId, String clientSecret,
                                                           String access_token,
                                                           Long expireTime) {

        ObjectNode objectNode = factory.objectNode();

        Map<String, String> retMap = Maps.newHashMap();

        String registerIMUserUrl = HX_DOMAIN_NAME + orgName + "/" + appName + "/users";

        try {
            // check properties that must be provided
            if (dataArrayNode.isArray()) {

                for (JsonNode jsonNode : dataArrayNode) {

                    if (null != jsonNode && !jsonNode.has("username")) {

                        logger.error("Property that named username must be provided .");

                        objectNode
                            .put("message", "Property that named username must be provided .");
                    }

                    if (null != jsonNode && !jsonNode.has("password")) {

                        logger.error("Property that named password must be provided .");

                        objectNode
                            .put("message", "Property that named password must be provided .");
                    }
                }
            }

            List<NameValuePair> headers = new ArrayList<NameValuePair>();
            headers.add(new BasicNameValuePair("Content-Type", "application/json"));

            boolean isAccessTokenExpired = false;
            Map<String, String> newTokenMap = null;
            if (null != expireTime && new Date().getTime() < expireTime.longValue()) {
                //token is not expireTime
                headers.add(new BasicNameValuePair("Authorization", "Bearer " + access_token));
            } else {
                //token is expireTime
                isAccessTokenExpired = true;
                newTokenMap = getHXToken(orgName, appName, clientId, clientSecret);
                headers.add(new BasicNameValuePair("Authorization",
                                                   "Bearer " + newTokenMap.get(HX_ACCESS_TOKEN)));
            }

            Map<String, String>
                retNewMap =
                sendRequest(headers, registerIMUserUrl, dataArrayNode, "post", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }
            if (null != retNewMap) {
                return retNewMap;
            }

            for (int i = 1; i <= 3; i++) {
                if (null == retNewMap) {
                    retNewMap =
                        sendRequest(headers, registerIMUserUrl, dataArrayNode, "post", retMap);
                } else {
                    break;
                }
            }

            if (null != retNewMap) {
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("batchCreateNewIMUsersSingle Exception--->" + e);
        }
        return retMap;
    }

    /**
     * 重置密码；兼容老项目
     */
    @Deprecated
    public Map<String, String> resetIMUserPassword(String orgName, String appName,
                                                   String IMUserName, String clientId,
                                                   String clientSecret, String newPassword,
                                                   String access_token, String tokenExpireTime) {
        Long expireTime = null;
        if (org.apache.commons.lang3.math.NumberUtils.isNumber(tokenExpireTime)) {
            expireTime = Long.parseLong(tokenExpireTime);
        }
        return resetIMUserPassword(orgName, appName, IMUserName, clientId, clientSecret,
                                   newPassword, access_token, expireTime);
    }

    /**
     * 重置密码
     */
    public Map<String, String> resetIMUserPassword(String orgName, String appName,
                                                   String IMUserName, String clientId,
                                                   String clientSecret, String newPassword,
                                                   String access_token, Long expireTime) {

        Map<String, String> retMap = Maps.newHashMap();

        String
            resetIMUserPasswordUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/users/" + IMUserName + "/password";

        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("newpassword", newPassword);
            List<NameValuePair> headers = new ArrayList<NameValuePair>();

            boolean isAccessTokenExpired = false;
            Map<String, String> newTokenMap = null;
            if (null != expireTime && new Date().getTime() < expireTime.longValue()) {
                //token is not expireTime
                headers.add(new BasicNameValuePair("Authorization", "Bearer " + access_token));
            } else {
                //token is expireTime
                isAccessTokenExpired = true;
                newTokenMap = getHXToken(orgName, appName, clientId, clientSecret);
                headers.add(new BasicNameValuePair("Authorization",
                                                   "Bearer " + newTokenMap.get(HX_ACCESS_TOKEN)));
            }
            Map<String, String>
                retNewMap =
                sendRequest(headers, resetIMUserPasswordUrl, jSONObject, "post", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }
            if (null != retNewMap) {
                return retNewMap;
            }

            for (int i = 1; i <= 3; i++) {
                if (null == retNewMap) {
                    retNewMap =
                        sendRequest(headers, resetIMUserPasswordUrl, jSONObject, "post", retMap);
                } else {
                    break;
                }
            }

            if (null != retNewMap) {
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("resetIMUserPassword Exception--->" + e);
        }
        return retMap;
    }

    /**
     * 批量删除IM用户；兼容老项目
     */
    @Deprecated
    public Map<String, String> batchDeleteUsersByCreateTime(String orgName, String appName,
                                                            String orderByTimeFlag, String clientId,
                                                            String clientSecret,
                                                            String access_token,
                                                            String tokenExpireTime, String limit) {
        Long expireTime = null;
        if (org.apache.commons.lang3.math.NumberUtils.isNumber(tokenExpireTime)) {
            expireTime = Long.parseLong(tokenExpireTime);
        }
        return batchDeleteUsersByCreateTime(orgName, appName, orderByTimeFlag, clientId,
                                            clientSecret, access_token, expireTime, limit);
    }

    /**
     * 批量删除IM用户
     */

    public Map<String, String> batchDeleteUsersByCreateTime(String orgName, String appName,
                                                            String orderByTimeFlag, String clientId,
                                                            String clientSecret,
                                                            String access_token,
                                                            Long expireTime, String limit) {

        Map<String, String> retMap = Maps.newHashMap();

        if (!"desc".equals(orderByTimeFlag) && !"asc".equals(orderByTimeFlag)) {
            retMap.put("error message", "Wrong format parameter orderByTimeFlag");
            return retMap;
        }

        if (!org.apache.commons.lang3.math.NumberUtils.isNumber(limit)) {
            retMap.put("error message", "Wrong format parameter limit");
        }

        String
            batchDeleteUsersUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/users?limit=" + limit;

        List<NameValuePair> headers = new ArrayList<NameValuePair>();

        boolean isAccessTokenExpired = false;
        Map<String, String> newTokenMap = null;
        if (null != expireTime && new Date().getTime() < expireTime.longValue()) {
            //token is not expireTime
            headers.add(new BasicNameValuePair("Authorization", "Bearer " + access_token));
        } else {
            //token is expireTime
            isAccessTokenExpired = true;
            newTokenMap = getHXToken(orgName, appName, clientId, clientSecret);
            headers.add(new BasicNameValuePair("Authorization",
                                               "Bearer " + newTokenMap.get(HX_ACCESS_TOKEN)));
        }

        try {

            Map<String, String>
                retNewMap =
                sendRequest(headers, batchDeleteUsersUrl, null, "delete", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }
            if (null != retNewMap) {
                return retNewMap;
            }

            for (int i = 1; i <= 3; i++) {
                if (null == retNewMap) {
                    retNewMap = sendRequest(headers, batchDeleteUsersUrl, null, "delete", retMap);
                } else {
                    break;
                }
            }

            if (null != retNewMap) {
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("batchDeleteUsersByCreateTime Exception--->" + e);
        }
        return retMap;
    }

    /**
     * 发送请求
     */

    private Map<String, String> sendRequest(List<NameValuePair> headers, String requestUrl,
                                            Object dataBody, String method,
                                            Map<String, String> retMap) {
        Map<String, String> retNewMap = null;
        String results = null;
        try {
            URL url = new URL(requestUrl);
            HttpClient httpClient = getClient(true);
            HttpResponse response = null;

            if (method.equals("post")) {
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());
                if (null != headers && !headers.isEmpty()) {
                    for (NameValuePair nameValuePair : headers) {
                        httpPost.addHeader(nameValuePair.getName(), nameValuePair.getValue());
                    }
                }
                httpPost.setEntity(new StringEntity(dataBody.toString(), "UTF-8"));
                response = httpClient.execute(httpPost);
            } else if (method.equals("delete")) {
                HttpDelete httpDelete = new HttpDelete(url.toURI());
                if (null != headers && !headers.isEmpty()) {
                    for (NameValuePair nameValuePair : headers) {
                        httpDelete.addHeader(nameValuePair.getName(), nameValuePair.getValue());
                    }
                }
                response = httpClient.execute(httpDelete);
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                results = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(results);

                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    String value = entry.getValue().toString();
                    String key = entry.getKey().toString();
                    retMap.put(key, value);
                }

                retNewMap = retMap;
                System.out.println(results);
                logger.info("results:---------->" + results);
            }
        } catch (Exception e) {
            logger.error("sendRequest Exception--->" + e);
        }
        return retNewMap;
    }


    public static void main(String[] args) {

        final HXRequest hXRequest = new HXRequest();
        final String orgName = "huayouapp";
        final String appName = "huayoutest";
        final String clientId = "YXA6FT__IGO6EeSBDXdqMW3jAQ";
        final String clientSecret = "YXA67pwcc_IbqWIDEIcxXdwJBopK4iQ";

        final Map<String, String>
            tokenMap =
            hXRequest.getHXToken(orgName, appName, clientId, clientSecret);

//        Map<String, String> registerMap = hXRequest.createNewIMUserSingle(orgName, appName, "24486", "518d03c29", clientId, clientSecret, "YWMtR-gF4mpCEeSWGOHZBMqGcAAAAUrYBDqRbTtMAo55mX1XFEfB0_R_6eORfPs", null);

//        String usernamePrefix = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 9);

//        ArrayNode arrayNode = factory.arrayNode();
//        for (int i = 0; i < 100; i++) {
//            ObjectNode userNode = factory.objectNode();
//            userNode.put("username", usernamePrefix + "_" + i);
//            userNode.put("password", i + 100000);
//
//            arrayNode.add(userNode);
//        }
//
//        Map<String, String> batchRegisterMap = hXRequest.batchCreateNewIMUsersSingle(orgName, appName, arrayNode, clientId, clientSecret, tokenMap.get(HX_ACCESS_TOKEN), tokenMap.get(HX_TOKEN_EXPIRE_TIME));
//
//        Map<String, String> updatePasswordMap = hXRequest.resetIMUserPassword(orgName, appName, "fei", clientId, clientSecret, "454554", tokenMap.get(HX_ACCESS_TOKEN), tokenMap.get(HX_TOKEN_EXPIRE_TIME));
//        for (int i = 0; i < 5; i++) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
        for (int i = 0; i < 1; i++) {
            Map<String, String>
                batchDeleteUsersByCreateTime =
                hXRequest.batchDeleteUsersByCreateTime(
                    orgName, appName, "desc", clientId, clientSecret,
                    tokenMap.get(HX_ACCESS_TOKEN), tokenMap.get(HX_TOKEN_EXPIRE_TIME),
                    "500");
            System.out.println(batchDeleteUsersByCreateTime);
        }
//                }
//            });
//        }

        System.out.println(tokenMap);
//        System.out.println(registerMap);
//        System.out.println(batchRegisterMap);
//        System.out.println(updatePasswordMap);
//        System.out.println(batchDeleteUsersByCreateTime);
    }

}
