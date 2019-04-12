package com.github.unchama.seichiassist;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

    public class OfflineUUID {
       private OfflineUUID() {}

       static {
          UUIDs = new HashMap<String, UUID>();
       }

       private static final String postContent1 = "{\"name\": \"";
       private static final String postContent2 = "\", \"agent\": \"minecraft\"}";
       private static String getPostContent(String name) {
          return OfflineUUID.postContent1 + name + OfflineUUID.postContent2;
       }

       static Map<String, UUID> UUIDs;
       public static UUID getUUID(String player, boolean forceReload) throws IOException, IllegalArgumentException {
          if(!forceReload && OfflineUUID.UUIDs != null && OfflineUUID.UUIDs.containsKey(player)){
        	  //System.out.println("debug!");
        	  return OfflineUUID.UUIDs.get(player);
          }
          String response = getUUIDJSON(player);
          if(response == null) {
        	  //throw new IOException("MojangのAPIの呼び出しに失敗しました。認証サーバーがダウンしている可能性があります。");
        	  //System.out.println("MojangのAPIの呼び出しに失敗しました。認証サーバーがダウンしている可能性があります。");
              return null;
          }
          JSONParser jp = new JSONParser();
          Object obj = null;
          try {
             obj = jp.parse(response);
          } catch (ParseException ex) {
                //throw new IllegalArgumentException("プレイヤーのアカウントが見つかりません。");
        	    //System.out.println("プレイヤーのアカウントが見つかりません。(A)");
        	    return null;
          }
          if(obj instanceof Map) {
             Map map = (Map)obj;
             if(map.containsKey("profiles")) {
                obj = map.get("profiles");
                if(obj instanceof List) {
                   List lst = (List)obj;
                   if(lst.size() > 0) {
                      obj = lst.get(0);
                      if(obj instanceof Map) {
                         Map profiles = (Map)obj;
                         if(profiles.containsKey("id")) {
                            String uuid = (String)profiles.get("id");
                            if(uuid.length() == 32) {
                               uuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
                               UUID id = UUID.fromString(uuid);
                               //取得成功
                               if(OfflineUUID.UUIDs == null)
                                  OfflineUUID.UUIDs = new HashMap<String, UUID>();
                               OfflineUUID.UUIDs.put(player, id);
                               return id;
                            }
                         }
                      }
                   }
                }
             }
          }
          //throw new IllegalArgumentException("プレイヤーのアカウントが見つかりません。");
          //System.out.println("プレイヤーのアカウントが見つかりません。(B)");
          return null;
       }

       private static String getUUIDJSON(String name) throws IOException {
          String re = "";

          URL url = new URL("https://api.mojang.com/profiles/page/1");
          HttpURLConnection huc = (HttpURLConnection)url.openConnection();
          huc.setRequestMethod("POST");
          huc.setDoOutput(true);// POSTのデータを後ろに付ける
          huc.setInstanceFollowRedirects(false);// 勝手にリダイレクトさせない
          huc.setRequestProperty("Content-Type","application/json;charset=utf-8");
          huc.setConnectTimeout(5000);
          huc.setReadTimeout(5000);
          huc.connect();

          PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(huc.getOutputStream(),"utf-8")));
          pw.print(getPostContent(name));
          pw.close();

          BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream(), "utf-8"));
          String line = null;
          while ((line = br.readLine()) != null) {
             re += line + "\n";
          }
          re = re;
          br.close();
          huc.disconnect();
          return re;
       }
    }
