package ChatServer;

import java.net.*;

/**
 * チャットサーバ
 * 役割：全体の管理。クライアントからの接続を待ち、個別の通信スレッドを起動する
 * @author 19026194　星 幸之介　// 自分の氏名・番号を記入して下さい
 */
public class MyServer extends Thread {

    int port = 13579; // アプリケーションの通信ポート番号(適当に決定)
    
    private boolean status = true; // 状態管理フラグ

    public MessageSender sender; // メッセージ配信スレッド

    //================================================================
    /**
     * メインメソッド
     * @param args 
     */
    public static void main(String args[]) {
        MyServer server = new MyServer(); // このクラス自身のインスタンスを生成
        server.start(); // スレッド起動
        System.out.println("サーバを起動しました.");
    }

    //================================================================
    /**
     * コンストラクタ
     */
    public MyServer() {
        // メッセージ配信スレッドを生成する
        sender = new MessageSender();
    }

    //================================================================
    /**
     * メイン処理（接続管理）部
     */
    @Override
    public void run() {
        while(status == true){
            // サーバの接続ルーチンを開始する
            try {
                // 接続依頼を受け付けるためのソケットを生成
                ServerThread serverThread;
                ServerSocket serverSocket = new ServerSocket(port); // 待ち受け窓口
                //※同じportの待ち受け窓口を複数用意することはできません。

    //******************************************************************

                //----------------------------------------
                // クライアントとの接続受け付け処理
                //----------------------------------------
                // クライアントからの接続依頼を待つ
                System.out.println("Waiting request to connect...");

                // 接続依頼が来るまで待機
                Socket socket = serverSocket.accept(); 
                // ※接続依頼があると以下の処理に進みます

                // 接続依頼のあったクライアントとのソケットを渡し、通信処理部(ServerThread)を生成
                serverThread = new ServerThread(this, socket);

                // 通信処理部を起動
                serverThread.start();

                // 接続をメッセージ配信スレッドに登録する
                sender.addConnection(serverThread);
    //******************************************************************
            } catch (Exception ex) {
                // クライアントとの接続処理中に何らかの例外が発生した(接続失敗)
                System.err.println("Connection is failed.");
            }
            System.out.println("Server is finished.");
        }
    }

    //================================================================
    /**
     * 指定されたクライアントとの通信を停止する
     * @param servThread 停止する通信スレッド
     */
    public void closeConnection(ServerThread servThread) {
        sender.closeConnection(servThread);
    }

    //================================================================
    /**
     * メッセージを送信する
     * @param message メッセージ
     */
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    //================================================================
    /**
     * 終了処理
     */
    public void close() {
        if (sender != null) {
            sender.closeAll(); // 全接続先との通信停止
            sender = null;  // 配信スレッド自身の削除
        }
        // クライアントを受け付けるループを維持するフラグをfalseにして停止させる
        status = false;
    }
}