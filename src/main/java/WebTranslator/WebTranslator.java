package WebTranslator;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * 翻訳サイト(Excite)のサービスを使って文字列を翻訳する
 * 役割：チャットの入力を指定した言語に自動的に翻訳する
 * @author 19026194　星 幸之介　// 自分の氏名・番号を記入して下さい
 */
public class WebTranslator {
    //================================================================
    // Excite翻訳と通信するための各種情報
    //================================================================
    int before = 0; // 翻訳前の文字列(初期値 0..日本語)
    int after = 1;  // 翻訳後の文字列(初期値 1..英語)

    // エキサイト翻訳のサーバホスト情報
    String sysHost = "www.excite.co.jp";
    String sysPort = "80";

    // エキサイト翻訳のアクセスパス
    String urlje = "/world/";      // 日英翻訳のパス

    //wb_lp　※Excite翻訳で使用
    final String type[] = {"JA", "EN"};  // JA..日　EN..英
    
    //=========================================================================
    /**
     * メインメソッド 
     * このクラス単体での動作確認用。完成版では使用しない
     * @param args 
     */
    public static void main(String args[]) {
        // 翻訳機能を利用するためにWebTranslatorの実体を生成
        WebTranslator gwc = new WebTranslator();
        
        // 翻訳を実行（例：こんにちはを英語に翻訳）した結果をresultに代入
        String result = gwc.translation("こんにちは", 0, 1);
        
        // こんにちはを英訳した結果(Webコンテンツ(HTML))を表示
        System.out.println(result);
    }

    //=========================================================================
    /**
     * コンストラクタ
     */
    public WebTranslator(){
        // コンストラクタの処理をここに書く
    }
    
    //=========================================================================
    /**
     * 翻訳処理本体メソッド
     * 文章(word)をbeforeからafterに翻訳する
     * @param word   翻訳対象の文章
     * @param before 翻訳前の言語種別
     * @param after  翻訳後の言語種別
     * @return 
     */
    public String translation(String word,int before,int after){
        // 翻訳結果のWebコンテンツ全体が入る文字列変数
        String result = null;
        
        // アクセス先のパス情報
        String url; 
        
        // 翻訳サイトへのURLを設定
        if (before == after){
            // 指定された言語がいずれも同じ場合は翻訳しない
            return word;        
        }else if (before != 0 && after != 0) {
            // いずれかに日本語が含まれていない場合は対応できない
            System.err.println("Translation is failed: langage setting error : 001");
            return word;
        } else if (before == 1 || after == 1) { // 日英or英日の場合
            url = "https://" + sysHost + urlje;    // 日英変換
        } //else if (before == 2 || after == 2) { // 日中or中日の場合 ※チャレンジ用
            /*
            url = "https://" + sysHost + urljc;    // 日中変換
            */
        //} 
        else {
            // それ以外のケースはありえないはずだが念のため
            System.err.println("Translation is failed: langage setting error : 002");
            // チャットに組み込んで「翻訳しないで普通にチャットする」を許可する場合は上をコメント化
            return word;
        }

        try {
            // オプション情報をハッシュに挿入
            Properties props = new Properties();
            props.put("before", word);
            props.put("wb_lp", type[before] + type[after]);
            //props.put("big5", big5); // チャレンジ用(中国語の簡繁の選択)

            // URLクラスにアクセス先情報を設定
            URL target = new URL(url);
            // 通信処理部に情報を渡し、コンテンツを取得する
            result = GetWebContents.doPost(target, props);
        } catch (IOException e) {
            // HTTP通信のどこかでIO処理例外が発生した場合のエラー出力
            System.err.println("Translation is failed: IO error : 003");
        }

        //===========================================================
        // 以下に翻訳結果の抽出処理が入る
        String[] str = result.split("rows=\"13\" name=\"after\">", 0);
        // System.out.println(str[0]);
        // </textarea>
        
        String[] str02 = str[1].split("</textarea>", 0);
        result = str02[0];
        //===========================================================
        
        // 最後にresultを返す
        return result;
    }
  
}
