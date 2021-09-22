package WebTranslator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.net.*;
import javax.net.ssl.*;

/**
 * HTTPによる通信によりWebコンテンツを取得するオブジェクト
 * 基本的にはdoPostメソッドはそのまま流用できる
 * @author 19026194　星 幸之介
 */
public class GetWebContents {
    //=========================================================================
    /**
     * Webサーバとの通信処理部
     * @param url 接続先のURL
     * @param props 接続時に送信される属性情報
     * @return 取得したコンテンツ
     * @throws IOException 
     */
    public static String doPost(URL url, Properties props) throws IOException {
        // 指定URLとのコネクションをオープン
        //URLConnection connection = url.openConnection();
        // httpsに対応させるためにURLConnection→HttpsURLConnectionに変更
        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        HttpsURLConnection.setDefaultHostnameVerifier((String hostname, SSLSession session) -> true);
        
        // クライアント側からのデータ送信(翻訳前文字列など)を許可する
        connection.setDoOutput(true);
        
        // 通信路を開き，クライアントからのデータを送信する
        try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {
            Enumeration keys = props.keys();
            
            // 1つ1つキーを処理
            while(keys.hasMoreElements()){
                String name = (String)keys.nextElement();
                String value = props.getProperty(name);
                out.print(name);
                out.print('=');
                out.print(URLEncoder.encode(value, "UTF-8")); // 文字コードをUTF-8に指定
                if (keys.hasMoreElements()) out.print('&'); // 最後の＆は付けない
            }
        }
        
        // 結果(Webコンテンツ)の読込むためのStringBuilder
        StringBuilder response = new StringBuilder();
        
        //----------------------------------------------------
        // 通信結果読込用のリーダーを取得
        //----------------------------------------------------
        try(BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))){
            //----------------------------------------------------
            // 通信結果の読込み
            //----------------------------------------------------
            String line;
            while ((line = in.readLine()) != null) {
                // バッファを使うと文字列連結(+)よりも処理が早く、かつ安全
                response.append(line).append('\n');
            }
        }
        
        // 結果を文字列にまとめて返す
        return response.toString();
    }
}
