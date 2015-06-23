package com.huayou.commons.util;

import java.util.Map;

/**
 * @Author : hugo
 * @Date : 15/1/29 上午10:17.
 */
public class TestHx {

    public static void main(String[] args) {
        new TestHx().testHX();

    }


    public void testHX() {
        HXRequest hxRequest = new HXRequest();
        String clientId = "YXA6FT__IGO6EeSBDXdqMW3jAQ";
        String client_secret = "YXA67pwcc_IbqWIDEIcxXdwJBopK4iQ";

        Map<String, String> tokenMap = hxRequest.getHXToken("huayouapp", "huayoutest",
                                                            clientId, client_secret);

        String hx_access_token = tokenMap.get(HXRequest.HX_ACCESS_TOKEN);
        String hx_token_expire_time = tokenMap.get(HXRequest.HX_TOKEN_EXPIRE_TIME);

        Long _hx_token_expire_time = Long.parseLong(hx_token_expire_time);

        Map<String, String> retMap = null;

//        retMap = hxRequest.createGroup("huayouapp", "huayoutest",
//                                       "hyGroup_test2", "1",
//                                       100, 1,
//                                       "画友群组测试小组，仅供内部测试使用，不需要申请审核。", hx_access_token,
//                                       clientId, client_secret,
//                                       _hx_token_expire_time, null);
//
//        if (retMap.size() == 0) {
//            return;
//        }
//
//        String groupId = retMap.get("groupId");
//
//        retMap = hxRequest.addOneUser2Group("huayouapp", "huayoutest",
//                                            groupId, "115",
//                                            hx_access_token,
//                                            clientId, client_secret,
//                                            _hx_token_expire_time, null);

//        List<HXRequest.HuanxinUser> users = Lists.newArrayList();
//
//        HXRequest.HuanxinUser h1 = new HXRequest.HuanxinUser("7");
//        HXRequest.HuanxinUser h2 = new HXRequest.HuanxinUser("2");
//        HXRequest.HuanxinUser h3 = new HXRequest.HuanxinUser("3");
//        HXRequest.HuanxinUser h4 = new HXRequest.HuanxinUser("4");
//        HXRequest.HuanxinUser h5 = new HXRequest.HuanxinUser("5");
//        HXRequest.HuanxinUser h6 = new HXRequest.HuanxinUser("6");
//
//        users.add(h1);
//        users.add(h2);
//        users.add(h3);
//        users.add(h4);
//        users.add(h5);
//        users.add(h6);

//        retMap = hxRequest.addUsers2Group("huayouapp", "huayoutest",
//                                          "1433297431142366", users,
//                                          hx_access_token,
//                                          clientId, client_secret,
//                                          _hx_token_expire_time, null);

//
//        retMap = hxRequest.removeOneUser2Group("huayouapp", "huayoutest",
//                                            "1433230960248149", "5",
//                                            hx_access_token,
//                                            clientId, client_secret,
//                                            _hx_token_expire_time, null);

//        retMap = hxRequest.updateGroupInfo("huayouapp", "huayoutest",
//                                           "1433230960248149", "huayou_test_group",
//                                           "huayou_test_group", "100000000000",
//                                           hx_access_token,
//                                           clientId, client_secret,
//                                           _hx_token_expire_time, null);

//        retMap = hxRequest.removeGroup("huayouapp", "huayoutest",
//                                       "1433230960248149", hx_access_token,
//                                       clientId, client_secret,
//                                       _hx_token_expire_time, null);
//
//        System.out.println(JSON.toJSONString(retMap));

//        HXRequest.HuanxinUser h1 = new HXRequest.HuanxinUser("34056", "B0E2D929693B9DEC8D7FDB387DAE29F7");
//        HXRequest.HuanxinUser h2 = new HXRequest.HuanxinUser("65001", "5180DE94622547EC653B22C6BB3FEB3F");
//        HXRequest.HuanxinUser h3 = new HXRequest.HuanxinUser("107155", "083082F4A394DD8EC23F9BF1ABBF79AA");
//        HXRequest.HuanxinUser h4 = new HXRequest.HuanxinUser("175873", "9182116B235159F03E282A0DC5F26FFA");
//        HXRequest.HuanxinUser h5 = new HXRequest.HuanxinUser("370923", "C4899553785632236C254347AC6EFE39");
//        HXRequest.HuanxinUser h6 = new HXRequest.HuanxinUser("603507", "1156C6A99BA58F8A1B7526406C368AC6");
//
//        HXRequest.HuanxinUser h7 = new HXRequest.HuanxinUser("24509", "41DB7427C30B90E8441B7A160ED1296C");
//        HXRequest.HuanxinUser h8 = new HXRequest.HuanxinUser("76942", "B8F083ABEAC8BE714E91A47ED27AE968");
//        HXRequest.HuanxinUser h9 = new HXRequest.HuanxinUser("195987", "08791D4ADC2E733ABEE9C7586A30CE62");
//        HXRequest.HuanxinUser h10 = new HXRequest.HuanxinUser("289136", "67B4BF4804DA85C713284CAA59D63407");
//        HXRequest.HuanxinUser h11 = new HXRequest.HuanxinUser("455828", "FF133931578CD9D027802CAD514FF949");
//        HXRequest.HuanxinUser h12 = new HXRequest.HuanxinUser("509466", "ECECF3C3E26035B6FA539FD4F5EDB9C6");
//        HXRequest.HuanxinUser h13 = new HXRequest.HuanxinUser("614562", "5E6091327757E31B41177BEE42849070");
//
//        HXRequest.HuanxinUser h14 = new HXRequest.HuanxinUser("24507", "F4DEE81ACBF9026C65644F5D969F674A");
//
//        HXRequest.HuanxinUser h16 = new HXRequest.HuanxinUser("104545", "700BF7AC20B52FB63B8F8D5243AA29A3");
//        HXRequest.HuanxinUser h17 = new HXRequest.HuanxinUser("423308", "FEAC555A5D4E184011D60E55C288A306");
//        HXRequest.HuanxinUser h18 = new HXRequest.HuanxinUser("469904", "A6A39E17B6B3F7385BBC6ED5392041B6");
//
//        HXRequest.HuanxinUser h19 = new HXRequest.HuanxinUser("24508", "B3E63A932F73D31D0A64174FD5A5ED63");
//
//        HXRequest.HuanxinUser h131 = new HXRequest.HuanxinUser("158762", "1A581B48C9F5119256EC0E66553F3D4A");
//        HXRequest.HuanxinUser h723 = new HXRequest.HuanxinUser("223440", "83CE6DC268960FDA4F7A465544FBA043");
//        HXRequest.HuanxinUser h3428 = new HXRequest.HuanxinUser("305886", "DBB1A5C62E9683967739221C04F4E89D");
//        HXRequest.HuanxinUser h2349 = new HXRequest.HuanxinUser("388413", "BCDC86C587B096DD4C8079AB193351E3");
//        HXRequest.HuanxinUser h2410 = new HXRequest.HuanxinUser("421070", "229518C4E2DF12B94B564C1B43E8726F");
//        HXRequest.HuanxinUser h12341 = new HXRequest.HuanxinUser("439677", "B8399230F04953339D54A118A3701CEC");
//        HXRequest.HuanxinUser h12342 = new HXRequest.HuanxinUser("468722", "7D232138813E0B49433ED10AA28B8A05");
//        HXRequest.HuanxinUser h13423 = new HXRequest.HuanxinUser("469675", "46BE2CEF905C6BF0A7FCF99464154093");
//
//
//        HXRequest.HuanxinUser ewrwe = new HXRequest.HuanxinUser("16736", "7D4AA93C7453876442FEF4F1DD1A6397");
//        HXRequest.HuanxinUser ewr = new HXRequest.HuanxinUser("128228", "8FD3CD9744D86FADBD26A23676EDE588");
//        HXRequest.HuanxinUser
//            fdsds =
//            new HXRequest.HuanxinUser("687763", "49DD6EF2576EA3BF4F95784FB3E49488");

//        HXRequest.HuanxinUser fd= new HXRequest.HuanxinUser("20499", "8A0301EA1C7CF9531CF465BEE6365D61");

//        users.add(h1);
//        users.add(h2);
//        users.add(h3);
//        users.add(h4);
//        users.add(h5);
//        users.add(h6);
//        users.add(h7);
//        users.add(h8);
//        users.add(h9);
//        users.add(h10);
//        users.add(h11);
//        users.add(h12);
//        users.add(h13);
//        users.add(h14);
//        users.add(h17);
//        users.add(h16);
//        users.add(h18);
//        users.add(h19);
//        users.add(h131);
//        users.add(h723);
//        users.add(h3428);
//        users.add(h2349);
//        users.add(h2410);
//        users.add(h12341);
//        users.add(h12342);
//
//        users.add(fdsds);
//        users.add(ewrwe);
//        users.add(ewr);
//        users.add(fd);
//////
//        hxRequest.batchCreateNewIMUsers("huayouapp", "huayoutest", users, "YXA6FT__IGO6EeSBDXdqMW3jAQ",
//                "YXA67pwcc_IbqWIDEIcxXdwJBopK4iQ", hx_access_token, Long.parseLong(hx_token_expire_time), null);

//        hxRequest.resetIMUserPassword("huayouapp", "huayoutest", "24505", clientId,
//                                      client_secret, "FA2A87A54A473524842663D950D69C1E",
//                                      hx_access_token, Long.parseLong(hx_token_expire_time), null);
    }

}
