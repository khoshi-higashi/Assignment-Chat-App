package ChatServer;

import java.io.*;
import java.util.*;

/**
 * メッセージ配信スレッド
 * 役割：接続してきたクライアントをリストし、受信発言を全クライアントに配信する
 * @author 19026194　星 幸之介　// 自分の氏名・番号を記入して下さい
 */
public class MessageSender {
    ArrayList<ServerThread> clist;     // 配信先クライアントのリスト

    //================================================================
    /**
     * コンストラクタ
     */
    MessageSender() {
        clist = new ArrayList<>(); // リストを生成
    }

    //================================================================
    /**
     * メッセージ配信処理
     * @param message 配信するメッセージ
     */
    public synchronized void sendMessage(String message) {
        //----------------------------------------
        // 前処理
        //----------------------------------------
        String str = message;
        
        // 末尾改行の追加(既についている場合は除外)
        if (str != null && !str.endsWith("\n")) {
            str += "\n";
        }

        // 送信先リストが空なら以降の処理を行わずにメソッド終了
        if (clist.isEmpty()) {
            // ここには通常入らないはず
            System.out.println("メッセージ送信先が見つかりません.");
            return; // sendMessageを終了
        }
        
//******************************************************************
        //----------------------------------------
        // 【メイン処理】クライアントにメッセージを送信する
        //----------------------------------------
        for(int i = 0; i < clist.size() ; i++){
            // リストの先頭のクライアントとの接続を取得
            ServerThread serv = (ServerThread) (clist.get(i));

            // クライアントへの出力インタフェースを取得
            PrintWriter sw = serv.getWriter();

            // メッセージの書き出し
            sw.print(str);
            sw.flush();
        }
//******************************************************************

        //----------------------------------------
        // 後処理
        //----------------------------------------
        // 末尾改行を除いてログの出力
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == '\n') {
            // 末尾の改行を除く
            str = str.substring(0, str.length() - 1);
            // 何を送ったかをログに表示
            System.out.println("Send str \"" + str + "\" to clients.");
        }
    }

    //================================================================
    /**
     * クライアント接続処理
     * @param serv 接続先との通信スレッド
     */
    public void addConnection(ServerThread serv) {
        clist.add(serv); // 新たなクライアントとの接続を配信先リストに追加
    }

    //================================================================
    /**
     * 指定クライアントを配信先リストから除外
     * @param serv 接続を除外する通信スレッド
     */
    public void closeConnection(ServerThread serv) {
        // 指定クライアントのリスト番号を取得
        int index  = clist.indexOf(serv);
        // 除外処理
        if (index >= 0) {
            // 引数で指定されたスレッドをリストから除外
            clist.remove(serv);
            // 除外されたかを確認
            if (clist.indexOf(serv) == -1){ // リストに無いとき-1が返る
                System.out.println(index+"番目のクライアントを切断しました.");
            }else{
                System.out.println(index+"番目のクライアント切断に失敗しました.");
            }
        }else{
            // ここには入らない予定
            System.err.println(index+"番目のクライアントが選択されました.");
        }
    }
    //================================================================
    /**
     * 終了処理：すべての接続を一括で終了する
     */
    public void closeAll() {
        System.out.println("全クライアントとの接続を終了します.");
        // clistの解放
        for (ServerThread serv : clist){
            serv.close();
        }
    }
}
