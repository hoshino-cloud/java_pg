import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための機能を読み込みます
import java.net.InetSocketAddress; // ポート番号を指定する機能を読み込みます
import java.net.URLDecoder; // URLの文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // UTF-8を指定する機能を読み込みます
import java.util.ArrayList; // Todoの一覧を入れるための機能を読み込みます
import java.util.List; // Todoの一覧を扱うための機能を読み込みます

public class App { // Appという名前のプログラムを定義します
    public static void main(String[] args) throws Exception { // プログラムの開始位置です
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します
        List<String> todos = new ArrayList<>();// Todoをためておく一覧を一度だけ作ります
        server.createContext("/", exchange -> { // "/"へのアクセスに返事をする処理を登録します
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // GETかPOSTかという送信方法を取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 基本の返す形式を文字のデータにします
            if (path.equals("/add") && method.equals("POST")) { // /addへのPOSTならTodoを追加します
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られてきた中身を受け取ります
                String value = body.substring(5); // todo=の5文字を除きます
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8); // 日本語を元に戻します
                if (!todo.isEmpty()) { // 空の入力でないかを確認します
                    todos.add(todo); // Todoを一覧に追加します
                } // 空の入力の確認を終えます
                exchange.getResponseHeaders().set("Location", "/"); // 戻る先を/にします
                exchange.sendResponseHeaders(303, -1); // /へ戻す返事を送ります
                exchange.close(); // 通信を終えます
                return; // /addの処理をここで終えます
            } else if (path.equals("/")) { // /ならフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form>"; // Todoを入力して送るフォームを用意します
                html += "<ul>"; // HTMLのリストの始まりを用意します
                for (String todo : todos) { // Todoを1件ずつ取り出します
                    html += "<li>" + todo + "</li>"; // Todoをリストの項目にします
                } // Todoを1件ずつ表示する処理を終えます
                html += "</ul>"; // HTMLのリストを閉じます
                message = html; // 組み立てたHTMLを返す内容にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 返す形式をHTMLにします
            } else if (path.equals("/hello")) { // パスが /hello か比べます
                String query = exchange.getRequestURI().getRawQuery(); // クエリ文字列を取り出す
                String name = "ゲスト";
                if (query != null) {
                    name = java.net.URLDecoder.decode(query.substring(5), java.nio.charset.StandardCharsets.UTF_8); // URLの文字を元に戻す
                } // すべてのTodoをリストの項目にします
                message = "こんにちは、" + name + "さん！"; // 名前を応答に混ぜる
                System.out.println("query = " + query);
            } else if (path.equals("/bye")) {
                message = "さようなら！";
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8"); // 返す文字をUTF-8のデータにします
            exchange.sendResponseHeaders(200, body.length); // 正常な返事を送る準備をします
            exchange.getResponseBody().write(body); // 返す文字を送ります
            exchange.getResponseBody().close(); // 返事の送信を終えます
        }); // アクセスへの処理を登録し終えます
        server.start(); // サーバーを起動します
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示します
    } // mainメソッドを終えます
} // Appクラスを終えます
