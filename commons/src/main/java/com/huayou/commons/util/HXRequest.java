package com.huayou.commons.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @Author : wwg
 * @Date : 14-11-10 环信调用接口
 */
public class HXRequest {

    private static Logger logger = LoggerFactory.getLogger(HXRequest.class);
    public static final String HX_TOKEN_EXPIRE_TIME = "tokenExpireTime";
    public static final String HX_ACCESS_TOKEN = "access_token";
    private static final String HX_DOMAIN_NAME = "https://a1.easemob.com/";
//    private static JsonNodeFactory factory = new JsonNodeFactory(false);

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
                                                     String tokenExpireTime,
                                                     String recordErrorFile) {
        Long expireTime = null;
        if (org.apache.commons.lang3.math.NumberUtils.isNumber(tokenExpireTime)) {
            expireTime = Long.parseLong(tokenExpireTime);
        }
        return createNewIMUserSingle(orgName, appName, IMUserName, IMPassword, clientId,
                                     clientSecret, access_token, expireTime, recordErrorFile);
    }

    /**
     * 单个IM用户注册
     */

    public Map<String, String> createNewIMUserSingle(String orgName, String appName,
                                                     String IMUserName,
                                                     String IMPassword, String clientId,
                                                     String clientSecret, String access_token,
                                                     Long expireTime, String recordErrorFile) {

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

            retNewMap =
                retrySendRequest(headers, registerIMUserUrl, jSONObject, "post", retMap, retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName =
                    DateTime.now() + " createNewIMUserSingle fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("createNewIMUserSingle Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName =
                    DateTime.now() + " createNewIMUserSingle Exception,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("createNewIMUserSingle IOException--->" + e);
            }

        }
        return retMap;
    }


    public static class HuanxinUser implements Serializable {

        private String username;
        private String password;

        public HuanxinUser(String username, String password) {
            if (username == null || password == null) {
                throw new RuntimeException("username and password should not be null!");
            }
            this.username = username;
            this.password = password;
        }

        public HuanxinUser(String username) {
            if (username == null) {
                throw new RuntimeException("username should not be null!");
            }
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    /**
     * 批量IM用户注册
     */
    public Map<String, String> batchCreateNewIMUsers(String orgName, String appName,
                                                     List<HuanxinUser> users,
                                                     String clientId, String clientSecret,
                                                     String access_token,
                                                     Long expireTime, String recordErrorFile) {
        if (users == null || users.size() == 0) {
            throw new RuntimeException("users should not be empty!");
        }
        String jsonData = null;
        Map<String, String> retMap = Maps.newHashMap();

        String batchRegisterIMUserUrl = HX_DOMAIN_NAME + orgName + "/" + appName + "/users";
        List<HuanxinUser> readyUsers = new ArrayList<HuanxinUser>(users.size());

        try {
            // check properties that must be provided
            for (HuanxinUser user : users) {
                if (user != null) {
                    readyUsers.add(user);
                }
            }
            if (readyUsers.size() == 0) {
                throw new RuntimeException("this is no user ready to create in huanxin!");
            }
            jsonData = JSON.toJSONString(readyUsers);
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
                sendRequest(headers, batchRegisterIMUserUrl, jsonData, "post", retMap);

            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }
            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap =
                retrySendRequest(headers, batchRegisterIMUserUrl, jsonData, "post", retMap,
                                 retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                String errorUserNameStr = "";
                for (HuanxinUser huanxinUser : readyUsers) {
                    errorUserNameStr +=
                        DateTime.now() + " batchCreateNewIMUsers fail,用户名：" + huanxinUser
                            .getUsername() + "\n";
                }
                fileWriter.write(errorUserNameStr);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("batchCreateNewIMUsersSingle Exception--->" + e);
            try {
                String errorUserNameStr = "";
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                for (HuanxinUser huanxinUser : readyUsers) {
                    errorUserNameStr +=
                        DateTime.now() + " batchCreateNewIMUsers Exception,用户名：" + huanxinUser
                            .getUsername() + "\n";
                }
                fileWriter.write(errorUserNameStr);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("batchCreateNewIMUsers IOException--->" + e);
            }
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
                                                   String access_token, String tokenExpireTime,
                                                   String recordErrorFile) {
        Long expireTime = null;
        if (org.apache.commons.lang3.math.NumberUtils.isNumber(tokenExpireTime)) {
            expireTime = Long.parseLong(tokenExpireTime);
        }
        return resetIMUserPassword(orgName, appName, IMUserName, clientId, clientSecret,
                                   newPassword, access_token, expireTime, recordErrorFile);
    }

    /**
     * 重置密码
     */
    public Map<String, String> resetIMUserPassword(String orgName, String appName,
                                                   String IMUserName, String clientId,
                                                   String clientSecret, String newPassword,
                                                   String access_token, Long expireTime,
                                                   String recordErrorFile) {

        Map<String, String> retMap = Maps.newHashMap();
        String
            resetIMUserPasswordUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/users/" + IMUserName + "/password";

        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("newpassword", newPassword);
            List<NameValuePair> headers = Lists.newArrayList();

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
                sendRequest(headers, resetIMUserPasswordUrl, jSONObject, "put", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }
            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap = retrySendRequest(headers, resetIMUserPasswordUrl,
                                         jSONObject, "put", retMap, retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName = DateTime.now() + " resetIMUserPassword fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("resetIMUserPassword Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName =
                    DateTime.now() + " resetIMUserPassword Exception fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("resetIMUserPassword IOException--->" + e);
            }
        }
        return retMap;
    }


    /**
     * 创建群组
     */
    public Map<String, String> createGroup(String orgName, String appName,
                                           String groupName, String owner,
                                           Integer maxUsers, Integer approval,
                                           String desc, String access_token,
                                           String clientId, String clientSecret,
                                           Long expireTime, String recordErrorFile) {

        if (Strings.isNullOrEmpty(owner) || Strings.isNullOrEmpty(groupName)) {
            throw new RuntimeException("owner or groupName should not be empty!");
        }

        Map<String, String> retMap = Maps.newHashMap();
        String createGroupUrl = HX_DOMAIN_NAME + orgName + "/" + appName + "/chatgroups";

        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("groupname", groupName);
            jSONObject.put("desc", desc);
            jSONObject.put("public", true);
            jSONObject.put("maxusers", maxUsers);
            jSONObject.put("owner", owner);
            String[] members = {owner};
            jSONObject.put("members", members);
            Boolean _approval = approval == 1 ? true : false;
            jSONObject.put("approval", _approval);

            List<NameValuePair> headers = Lists.newArrayList();
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
                sendRequest(headers, createGroupUrl, jSONObject, "post", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }

            if (null != retNewMap) {
                String groupIdString = retMap.get("data");
                JSONObject groupIdMap = JSONObject.parseObject(groupIdString);
                String groupId = (String) groupIdMap.get("groupid");
                retNewMap.put("groupId", groupId);
                return retNewMap;
            }

            retNewMap =
                retrySendRequest(headers, createGroupUrl, jSONObject, "post", retMap, retNewMap);

            if (null != retNewMap) {
                String groupIdString = retMap.get("data");
                JSONObject groupIdMap = JSONObject.parseObject(groupIdString);
                String groupId = (String) groupIdMap.get("groupid");
                retNewMap.put("groupId", groupId);
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                owner = DateTime.now() + " createGroup fail,用户名：" + owner + "\n";
                fileWriter.write(owner);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("createGroup Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                owner =
                    DateTime.now() + " createGroup Exception fail,用户名：" + owner + "\n";
                fileWriter.write(owner);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("createGroup IOException--->" + e);
            }
        }
        return retMap;
    }


    /**
     * 群组加人[单个]
     */
    public Map<String, String> addOneUser2Group(String orgName, String appName,
                                                String groupId, String IMUserName,
                                                String access_token,
                                                String clientId, String clientSecret,
                                                Long expireTime, String recordErrorFile) {

        if (Strings.isNullOrEmpty(IMUserName)) {
            throw new RuntimeException("IMUserName should not be empty!");
        }

        Map<String, String> retMap = Maps.newHashMap();
        String addOneUser2GroupUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/chatgroups/" +
            groupId + "/users/" + IMUserName;

        try {
            List<NameValuePair> headers = Lists.newArrayList();
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
                sendRequest(headers, addOneUser2GroupUrl, null, "post", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }

            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap =
                retrySendRequest(headers, addOneUser2GroupUrl, null, "post", retMap, retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName = DateTime.now() + " addOneUser2Group fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("addOneUser2Group Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName =
                    DateTime.now() + " addOneUser2Group Exception fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("addOneUser2Group IOException--->" + e);
            }
        }
        return retMap;
    }


    /**
     * 群组加人[批量]
     */
    public Map<String, String> addUsers2Group(String orgName, String appName,
                                              String groupId, List<HuanxinUser> users,
                                              String access_token,
                                              String clientId, String clientSecret,
                                              Long expireTime, String recordErrorFile) {

        if (users == null || users.size() == 0) {
            throw new RuntimeException("users should not be empty!");
        }

        Map<String, String> retMap = Maps.newHashMap();
        String addUsers2GroupUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/chatgroups/" + groupId + "/users";
        List<String> readyUsers = Lists.newArrayListWithCapacity(users.size());

        try {

            // check properties that must be provided
            for (HuanxinUser user : users) {
                if (user != null) {
                    readyUsers.add(user.getUsername());
                }
            }
            if (readyUsers.size() == 0) {
                throw new RuntimeException("this is no user ready to create in huanxin!");
            }

            Map<String, List<String>> dataBody = Maps.newHashMapWithExpectedSize(1);
            dataBody.put("usernames", readyUsers);
            String requestDataBody = JSON.toJSONString(dataBody);

            List<NameValuePair> headers = Lists.newArrayList();
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
                sendRequest(headers, addUsers2GroupUrl, requestDataBody, "post", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }

            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap = retrySendRequest(headers, addUsers2GroupUrl,
                                         requestDataBody, "post", retMap, retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                String errorUserNameStr = "";
                for (String userName : readyUsers) {
                    errorUserNameStr +=
                        DateTime.now() + " addUsers2Group fail,用户名：" + userName + "\n";
                }
                fileWriter.write(errorUserNameStr);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("addUsers2Group Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                String errorUserNameStr = "";
                for (String userName : readyUsers) {
                    errorUserNameStr +=
                        DateTime.now() + " addUsers2Group Exception,用户名：" + userName + "\n";
                }
                fileWriter.write(errorUserNameStr);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("addUsers2Group IOException--->" + e);
            }
        }
        return retMap;
    }


    /**
     * 群组踢人
     */
    public Map<String, String> removeOneUser2Group(String orgName, String appName,
                                                   String groupId, String IMUserName,
                                                   String access_token,
                                                   String clientId, String clientSecret,
                                                   Long expireTime, String recordErrorFile) {

        if (Strings.isNullOrEmpty(IMUserName) || Strings.isNullOrEmpty(groupId)) {
            throw new RuntimeException("IMUserName or groupId should not be empty!");
        }

        Map<String, String> retMap = Maps.newHashMap();
        String removeOneUser2GroupUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/chatgroups/" +
            groupId + "/users/" + IMUserName;

        try {
            List<NameValuePair> headers = Lists.newArrayList();
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
                sendRequest(headers, removeOneUser2GroupUrl, null, "delete", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }

            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap =
                retrySendRequest(headers, removeOneUser2GroupUrl, null, "delete", retMap,
                                 retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName = DateTime.now() + " removeOneUser2Group fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("removeOneUser2Group Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                IMUserName =
                    DateTime.now() + " removeOneUser2Group Exception fail,用户名：" + IMUserName + "\n";
                fileWriter.write(IMUserName);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("removeOneUser2Group IOException--->" + e);
            }
        }
        return retMap;
    }


    /**
     * 修改群组信息
     */
    public Map<String, String> updateGroupInfo(String orgName, String appName,
                                               String groupId, String groupName,
                                               String description, String maxUsers,
                                               String access_token, String clientId,
                                               String clientSecret,
                                               Long expireTime, String recordErrorFile) {

        if (Strings.isNullOrEmpty(groupId)) {
            throw new RuntimeException("groupId should not be empty!");
        }

        if (Strings.isNullOrEmpty(groupName) && Strings.isNullOrEmpty(description)
            && Strings.isNullOrEmpty(maxUsers)) {
            throw new RuntimeException("parameter should not be empty!");
        }

        Map<String, String> retMap = Maps.newHashMap();
        String updateGroupInfoUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/chatgroups/" + groupId;

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("groupname", groupName);
        jSONObject.put("description", description);
        jSONObject.put("maxusers", maxUsers);

        try {
            List<NameValuePair> headers = Lists.newArrayList();
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
                sendRequest(headers, updateGroupInfoUrl, jSONObject, "put", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }

            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap = retrySendRequest(headers, updateGroupInfoUrl,
                                         jSONObject, "put", retMap, retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                groupName = DateTime.now() + " updateGroupInfo fail,"
                            + "修改信息如下: 群组名(groupName)：" +
                            groupName + ",描述(description)为：" + description +
                            ",成员上限(maxUsers)为：" + maxUsers + "\n";
                fileWriter.write(groupName);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("updateGroupInfo Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                groupName = DateTime.now() + " updateGroupInfo Exception fail,"
                            + "修改信息如下: 群组名(groupName)：" +
                            groupName + ",描述(description)为：" + description +
                            ",成员上限(maxUsers)为：" + maxUsers + "\n";
                fileWriter.write(groupName);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("updateGroupInfo IOException--->" + e);
            }
        }
        return retMap;
    }


    /**
     * 解散群组
     */
    public Map<String, String> removeGroup(String orgName, String appName,
                                           String groupId, String access_token,
                                           String clientId, String clientSecret,
                                           Long expireTime, String recordErrorFile) {

        if (Strings.isNullOrEmpty(groupId)) {
            throw new RuntimeException("groupId should not be empty!");
        }

        Map<String, String> retMap = Maps.newHashMap();
        String removeGroupUrl =
            HX_DOMAIN_NAME + orgName + "/" + appName + "/chatgroups/" + groupId;

        try {
            List<NameValuePair> headers = Lists.newArrayList();
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
                sendRequest(headers, removeGroupUrl, null, "delete", retMap);
            if (isAccessTokenExpired) {
                // access_token 失效，本方法内进行重新获取过
                retMap.put(HX_TOKEN_EXPIRE_TIME, newTokenMap.get(HX_TOKEN_EXPIRE_TIME));
                retMap.put(HX_ACCESS_TOKEN, newTokenMap.get(HX_ACCESS_TOKEN));
            }

            if (null != retNewMap) {
                return retNewMap;
            }

            retNewMap =
                retrySendRequest(headers, removeGroupUrl, null, "delete", retMap, retNewMap);

            if (null != retNewMap) {
                return retNewMap;
            }

            if (null == retNewMap || retMap.size() == 0) {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                groupId = DateTime.now() + " removeGroup fail,群组号：" + groupId + "\n";
                fileWriter.write(groupId);
                fileWriter.close();
                return retNewMap;
            }

        } catch (Exception e) {
            logger.error("removeGroup Exception--->" + e);
            try {
                FileWriter fileWriter = new FileWriter(recordErrorFile, true);
                groupId =
                    DateTime.now() + " removeGroup Exception fail,群组号：" + groupId + "\n";
                fileWriter.write(groupId);
                fileWriter.close();
            } catch (IOException e1) {
                logger.error("removeGroup IOException--->" + e);
            }
        }
        return retMap;
    }


    private Map<String, String> retrySendRequest(List<NameValuePair> headers, String requestUrl,
                                                 Object dataBody, String method,
                                                 Map<String, String> retMap,
                                                 Map<String, String> retNewMap) {

        for (int i = 1; i <= 3 && null == retNewMap; ++i) {
            retNewMap = this.sendRequest(headers, requestUrl, dataBody, method, retMap);
        }
        return retNewMap;
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
                if (null != dataBody) {
                    httpPost.setEntity(new StringEntity(dataBody.toString(), "UTF-8"));
                }
                response = httpClient.execute(httpPost);
            } else if (method.equals("delete")) {
                HttpDelete httpDelete = new HttpDelete(url.toURI());
                if (null != headers && !headers.isEmpty()) {
                    for (NameValuePair nameValuePair : headers) {
                        httpDelete.addHeader(nameValuePair.getName(), nameValuePair.getValue());
                    }
                }
                response = httpClient.execute(httpDelete);
            } else if (method.equals("put")) {
                HttpPut httpPut = new HttpPut(url.toURI());
                if (null != headers && !headers.isEmpty()) {
                    for (NameValuePair nameValuePair : headers) {
                        httpPut.addHeader(nameValuePair.getName(), nameValuePair.getValue());
                    }
                }
                if (null != dataBody) {
                    httpPut.setEntity(new StringEntity(dataBody.toString(), "UTF-8"));
                }
                response = httpClient.execute(httpPut);
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
                logger.info("results:---------->" + results);
            }
        } catch (Exception e) {
            logger.error("sendRequest Exception--->" + e);
        }
        return retNewMap;
    }

}
