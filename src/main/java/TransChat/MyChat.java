package TransChat;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;

/**
 * オブジェクト指向応用演習　後半課題
 * Excite翻訳を利用した多言語対応チャット
 * @author 19026194　星 幸之介　// 自分の氏名・番号を記入して下さい
 */
public class MyChat extends Application {
    // アプリ固有に設定した通信ポート番号(定数)
    final int port = 13579; // チャットアプリ用のポート番号

    //=======================================================
    // メソッドをまたいで参照されるGUIコントロール
    //=======================================================
    TextArea output;            // 出力エリア
    TextField input;            // メッセージ入力欄
    Button enter;               // 発言ボタン
    ChoiceBox<String> choice;   // 言語選択欄
    
    //=======================================================
    // 他の処理で参照する変数
    //=======================================================
    static MyClient client;    // クライアントスレッド
    static PrintWriter netout; // 送信用ライタ

    // ユーザ名
    String username = "Unknown";
    
    // 状態管理用変数
    int language_type = 0;  // 使用言語
   
    // 言語リスト(選択肢に利用する)
    List<String> lang_list = Arrays.asList(
                "Japanese","English","Chinese"
            );
   
    //------------------------------------------------------------------
    /**
     * 処理部メイン
     * @param stage 
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("多言語チャット");
        stage.setWidth(800);              
        stage.setHeight(600);

        //=============================================================
        // 出力欄となるテキストエリアの設定
        output = new TextArea();
        // 出力欄は直接書き込み不可
        output.setEditable(false);
        // 領域のはじで文字列を折り返すように設定
        output.setWrapText(true);
        // フォントを見やすく変更
        output.setStyle("-fx-font:14pt Meiryo;");
        
        //=============================================================
        // メッセージ入力欄
        input = new TextField();
        // サイズの調整
        input.setMinWidth(520);
        input.setPrefWidth(520);
        input.setMaxWidth(520);
        // フォントを見やすく変更
        input.setStyle("-fx-font:14pt Meiryo;");

        //=============================================================
        // 送信ボタン
        enter = new Button(" 送信 ");
        // 押下時，メッセージ入力欄の内容を送信する
        enter.setOnAction(event->sendAction());
        // エンターキー押下時，メッセージ入力欄の内容を送信する
        input.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    sendAction();
                }
            }
        });
        //        scene.setOnKeyPressed(this::keyPressed);
        // フォントを見やすく変更
        enter.setStyle("-fx-font:14pt Meiryo;");
        
        //=============================================================
        // 言語選択欄
        choice = new ChoiceBox<>();
        choice.getItems().addAll(lang_list); // 言語リストから項目を作成する
        choice.setStyle("-fx-font:14pt Meiryo;");

        // 選択時の動作を設定
        // オブジェクト指向言語の演習で示したサンプルを参考に自分で設定する
        // 言語番号の設定はこのプログラムにあるsetLanguageTypeを使うこと
        choice.getSelectionModel().selectedItemProperty().addListener(
        new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> ov, String old_val, String new_val){
                // この部分に選択された時の処理を記述する==============================
                // label.setText("「"+cb.getValue()+"」が選択されています."); // ここ以外はいつも共通
                switch(choice.getValue()){
                    case "Japanese":
                        setLanguageType(0);
                        break;
                    case "English":
                        setLanguageType(1);
                        break;
                    case "Chinese":
                        setLanguageType(2);
                        break;                
                }
                // ここまで==========================================================
            }
        });
        // 最初の選択肢が選択された状態を初期状態にする
        choice.getSelectionModel().selectFirst();
        
        //=============================================================
        // メニューバーを使用する
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        // メニューの設定
        Menu ctrlMenu = new Menu("操作");
        ctrlMenu.setStyle("-fx-font:14pt Meiryo;");
        
        // サブメニューの設定と割り当て
        MenuItem mnuConnect = new MenuItem("通信開始");
        mnuConnect.setOnAction(event -> connectAction(stage));

        MenuItem mnuDisconnect = new MenuItem("切断");
        mnuDisconnect.setOnAction(event -> disconnectAction());

        MenuItem mnuExit = new MenuItem("終了");
        mnuExit.setOnAction(event -> exit());

        // サブメニューに個別メニューを登録
        ctrlMenu.getItems().addAll(mnuConnect,mnuDisconnect,mnuExit);
        // メインメニューにサブメニューを登録
        menuBar.getMenus().add(ctrlMenu);
        
        //=============================================================
        
        // 入力欄を横一列に配置
        HBox textPane = new HBox();
        textPane.setPadding(new Insets(10,10,10,10));
        textPane.setSpacing(10);
        textPane.getChildren().addAll(input,enter,choice);

        // メニューバーとテキストエリアを配置する
        BorderPane root = new BorderPane();
        root.setTop(menuBar);       // 上部にメニュー
        root.setCenter(output);     // 中央はメッセージ表示画面
        root.setBottom(textPane);   // 下部はメッセージ入力欄
        
        // シーンの割当てと表示
        stage.setScene(new Scene(root));
        stage.show();                     
    }

    //------------------------------------------------------------------
    /**
     * 「通信開始」メニュー選択時の処理
     */
    void connectAction(Stage stage) {
        
        // すでに接続中の場合にはエラー表示
        if (netout != null) {
            append("すでにサーバと接続中です.");
            return;
        }

//******************************************************************
        //--------------------------------------------
        // ダイアログからの接続先情報の取得
        //--------------------------------------------
        // ここにダイアログを表示して情報を取得する処理を入れる
        // (1) ダイアログのオブジェクトの生成
        LoginDialog login = new LoginDialog(stage);
        
        // (2) ダイアログの表示 ※閉じられるまで処理を止める
        login.showAndWait();
        
        // (5) キャンセルボタン押下時の処理 ※これは(5)番目にやること
        // ※「キャンセルされました」と画面表示してメソッドを終了(return)する
        // ダイアログから入力欄の内容を取得したときにnullであるならばキャンセルが
        // 押された場合である。接続ボタンが押された場合はnull以外の値を持っている。
        // ※ ある変数の値がnullだった時にメッセージを表示する処理は、
        // 150-153行目を参考にして作成してください。変数netoutをダイアログが
        // キャンセルされたときにnull値を返す変数に変更して使います。
        if(login.getUserName() == null){
           append("キャンセルされました") ;
           return;
        }

        // (3) ホスト名をダイアログから取得
        // ※必要なゲッターはダイアログ側で用意されている
        //--------------------------------------------
        // ディフォルトのホスト名を設定
        // このプログラムを実行したホスト(localhost)を指定
        String default_hostname = "localhost";
        String hostname;
        // ダイアログからの取得が空でない場合は，取得した情報をhostnameとする。
        // 空の時は場合はdefault_hostnameをhostnameとする。
        
        // hostname=default_hostname; // これに改良を加える
        
        hostname = login.getHostName();
        if (hostname.isEmpty()){
            hostname=default_hostname;
        }

        
        //--------------------------------------------

        // (4) ユーザ名をダイアログから取得
        // ※必要なゲッターはダイアログ側で用意されている
        //--------------------------------------------
        // ディフォルトのユーザ名の設定
        String default_username = "";
        try {
            // このプログラムを実行したホストの名前を取得
            default_username = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            default_username = "Unknown";
        }
        // ダイアログからの取得が空でない場合は，取得した情報をusernameとする。
        // 空の時は場合はdefault_usernameをusernameとする。※usernameは40行目で定義済み
        
        // username=default_username; // これに改良を加える
        username = login.getUserName();
        if (username.isEmpty()){
            username=default_username;
        }
        
        //--------------------------------------------
        
//******************************************************************

        //--------------------------------------------
        // サーバへの接続
        //--------------------------------------------
        try {
            // クライアントスレッドの生成と開始
            client = new MyClient(this, hostname, port);
            client.start();
            // 送信用パイプの取得
            netout = client.getWriter();
        } catch (Exception ex) {
            // 接続時に例外が発生(接続失敗)
            append("サーバとの通信が開始できませんでした.");
        }

        if (netout != null) {
            append("サーバに接続しました.");
            append("Server:" + hostname + " Port:" + port);
        } else {
            // 接続処理は終了したがインタフェースが生成されていない(接続失敗)
            append("サーバとの接続に失敗しました.");
        }
    }
    
    //------------------------------------------------------------------
    /**
     * 「切断」メニュー選択時の処理
     */
    void disconnectAction() {
        close();
    }
    
    //------------------------------------------------------------------
    /**
     * 「送信ボタン」押下時の処理
     */
    void sendAction() {
        // 未接続の場合にはエラー表示
        if (netout == null) {
            append("サーバに接続されていません.");
            return;
        }
        // メッセージ文の取得
        String str = input.getText() + "\t" + getLanguageType();
        if (str.length() <= 0) {
            return;
        }
        
        // 取得したメッセージを送信する
        sendMessage(str);
        // 入力欄を初期化する
        input.setText("");
    }
    
    
    //------------------------------------------------------------------
    /**
     * 選択中の言語種別を返す(翻訳機能と連動させる際に用いる)
     * @return 選択された言語
     */
    public int getLanguageType(){
        return language_type;
    }

    //------------------------------------------------------------------
    /**
     * 引数で指定された言語種別に設定(ChoiceBox選択時に呼ばれる)
     * @param type 
     */
    void setLanguageType(int type){
        language_type = type;
    }

    //------------------------------------------------------------------
    /**
     * メッセージ送信処理
     * @param str 送信されるメッセージ
     */
    public void sendMessage(String str) {
        // 末尾改行の追加
        if (!str.endsWith("\n")) {
            str += "\n";
        }

        // 送信メッセージの生成
        str = username + "\t" + str; // ユーザ名＋メッセージ

        // メッセージの送信
        if (netout != null) {  // サーバと接続済みかを確認
            netout.print(str); // サーバにメッセージを送信する
            netout.flush();
        }
    }

    //------------------------------------------------------------------
    /**
     * 出力欄にメッセージを追記する
     * @param str メッセージ
     */
    public void append(String str) {
        // 末尾改行の追加
        if (!str.endsWith("\n")) {
            str += "\n";
        }
        // 出力欄の末尾に文字列を追記
        output.appendText(str);
    }

    //------------------------------------------------------------------
    /**
     * 切断処理
     */
    public void close() {
        if (netout != null) {
            // 出力インタフェースを閉じる
            netout = null;
            // クライアント接続を切断
            if (client != null) {
                client.close(); // 接続停止処理
                client = null;  // クライアントオブジェクトを削除
            }
        }
    }

    //------------------------------------------------------------------
    /**
     * 終了処理
     */
    public void exit() {
        // ネットワーク接続を停止
        close();
        // プログラム終了
        System.exit(0);
    }

    //------------------------------------------------------------------
    /**
     * JavaFXアプリケーションを起動するだけのメインメソッド
     * ※基本的にはこの内容で固定と考えてよい
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);  // JavaFXアプリケーションを起動する
    }
    
}
