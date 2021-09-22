package ChatServer;

import java.io.*;
import java.net.*;

/**
 * クライアントとの個別通信処理スレッド
 * 役割：1つのクライアントとの通信を管理する。具体的には受信管理。
 *       送信はMessageSenderが一括して行ってくれる。
 * @author 19026194　星 幸之介　// 自分の氏名・番号を記入して下さい
 */
public class ServerThread extends Thread {
    MyServer parent;  // 親クラス
    Socket socket;    // 通信ソケット
    InputStream in;   // ソケットの入力インタフェース
    PrintWriter out;  // ソケットの出力インタフェース
    
    public BufferedReader tin = null; // スレッドの読込み元

    private boolean status = true;

    //=========================================================================
    /**
     * コンストラクタ
     * @param parent MyServerへの参照
     * @param socket 通信相手とのSocket
     * @throws Exception 
     */
    ServerThread(MyServer parent, Socket socket) throws Exception {
        this.parent = parent;
        // ソケットと出力先パイプの取得
        this.socket = socket;
        // ソケットから入力インタフェースを取得
        in = socket.getInputStream();
        // ソケットから出力インタフェースを取得
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        // 入力インタフェースをラップして行単位での読み込みを可能にする
        tin = new BufferedReader(new InputStreamReader(in));
        // 初期化終了メッセージを出力
        System.out.println("Connect to client.");
    }

    //=========================================================================
    /**
     * メイン処理部
     */
    @Override
    public void run() {
        // ソケットにクライアントからメッセージデータが届いたら、
        // 末尾に改行を付加して、そのまま配信部に流す
        while (status) {
            try {
                // メッセージを受け取るまで待機
                String message = tin.readLine(); 

                // メッセージ処理
                if(message == null){
                    // クライアントに強制切断された場合などにnullが届くので、
                    // その場合はループを終了させる
                    status = false;
                }else{
//******************************************************************
                    // 通常のメッセージならば末尾に改行を付けて配信部に流す
                    parent.sendMessage(message+"\n");
                    // 全員課題でのメッセージの転送処理ではこの処理はこのまま使う
//******************************************************************
                }
            } catch (IOException ex) {
                // その他の不具合が起こるとこの例外に入る(かも)
                status = false;
                System.err.println("通信異常が発生したため接続を切断します.");
            }
        }
        // メインループの後処理
        System.out.println("クライアントから切断されました.");
        close(); // 終了処理
    }

    //=========================================================================
    /**
     * ライターの取得
     * @return MessageSenderが送信に使用するライター
     */
    public PrintWriter getWriter() {
        return out;
    }

    //=========================================================================
    /**
     * 終了処理
     */
    public void close() {
        // 送信先リストから除外する
        parent.closeConnection(this);
        // 各種接続の停止
        try {
//******************************************************************
            // 初期化
            status = false;
            tin = null;
            // ソケットの閉鎖(tryでリソース取得していないので手動で閉じる)
            in.close();
            out.close();
            socket.close();
            // 変数の初期化
            in = null;
            out = null;
            socket = null;
            parent = null;
//******************************************************************
        } catch (IOException ex) {}
    }

}
