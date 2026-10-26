import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための機能を読み込みます
import java.net.InetSocketAddress; // ポート番号を指定する機能を読み込みます
import java.net.URLDecoder; // URLの文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // UTF-8を指定する機能を読み込みます
import java.util.ArrayList; // Todoの一覧を入れるための機能を読み込みます
import java.util.List; // Todoの一覧を扱うための機能を読み込みます

public class App { // Appという名前のプログラムを定義します
    static List<Todo> todos = new ArrayList<>(); // ★変更
    static int nextId = 1; // ★変更

    public static void main(String[] args) throws Exception { // プログラムの開始位置です
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します
        todos.add(new Todo(nextId++, "牛乳を買う")); // ★変更
        Todo egg = new Todo(nextId++, "卵を買う"); // ★変更
        egg.setDone(true); // ★変更
        todos.add(egg); // ★変更
        server.createContext("/", exchange -> { // "/"へのアクセスに返事をする処理を登録します
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // GETかPOSTかという送信方法を取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 基本の返す形式を文字のデータにします
            if (path.equals("/add") && method.equals("POST")) { // /addへのPOSTならTodoを追加します
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られてきた中身を受け取ります
                String value = body.substring(5); // todo=の5文字を除きます
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // ★変更
                if (!title.isEmpty()) { // ★変更
                    todos.add(new Todo(nextId, title)); // ★変更
                    nextId++; // ★変更
                } // 空の入力の確認を終えます
                exchange.getResponseHeaders().set("Location", "/"); // 戻る先を/にします
                exchange.sendResponseHeaders(303, -1); // /へ戻す返事を送ります
                exchange.close(); // 通信を終えます
                return; // /addの処理をここで終えます
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        for (Todo todo : todos) { // ★追加
                            if (todo.getId() == id) { // ★追加
                                todo.setDone(true); // ★追加
                                break; // ★追加
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        todos.removeIf(todo -> todo.getId() == id); // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/")) { // /ならフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form>"; // Todoを入力して送るフォームを用意します
                html += "<ul>"; // HTMLのリストの始まりを用意します
                for (Todo todo : todos) { // ★変更
                    String title = todo.getTitle(); // ★変更
                    if (todo.isDone()) { // ★変更
                        title = "<s>" + title + "</s>"; // ★変更
                    } // ★変更
                    html += "<li>" + title + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // ★追加
                } // Todoを1件ずつ表示する処理を終えます
                html += "</ul>"; // HTMLのリストを閉じます
                message = html; // 組み立てたHTMLを返す内容にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 返す形式をHTMLにします
            } else { // ★変更
                message = "ページが見つかりません"; // ★変更
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

class Todo { // ★変更
    private final int id; // ★変更
    private final String title; // ★変更
    private boolean done; // ★変更

    Todo(int id, String title) { // ★変更
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.done = false; // ★変更
    } // ★変更

    int getId() { // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更
        return title; // ★変更
    } // ★変更

    boolean isDone() { // ★変更
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // ★変更
        this.done = done; // ★変更
    } // ★変更
} // ★変更
