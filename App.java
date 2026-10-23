import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための機能を読み込みます
import java.net.InetSocketAddress; // ポート番号を指定する機能を読み込みます

public class App { // Appという名前のプログラムを定義します
    public static void main(String[] args) throws Exception { // プログラムの開始位置です
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します
        server.createContext("/", exchange -> { // "/"へのアクセスに返事をする処理を登録します 1
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String message;
            if (path.equals("/hello")) { // パスが /hello か比べます
                message = "こんにちは！";
            } else if (path.equals("/bye")) {
                message = "さようなら！";
            } else {
                message = "ページが見つかりません";
            }
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す文字の種類と文字コードを指定します 2
            byte[] body = message.getBytes("UTF-8"); // 返す文字をUTF-8のデータにします
            exchange.sendResponseHeaders(200, body.length); // 正常な返事を送る準備をします
            exchange.getResponseBody().write(body); // 返す文字を送ります
            exchange.getResponseBody().close(); // 返事の送信を終えます
        }); // アクセスへの処理を登録し終えます
        server.start(); // サーバーを起動します
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示します
    } // mainメソッドを終えます
} // Appクラスを終えます
