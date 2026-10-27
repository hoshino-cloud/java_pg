import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection; // ★
import java.sql.DriverManager; // ★
import java.sql.PreparedStatement; // ★
import java.sql.ResultSet; // ★
import java.sql.SQLException; // ★

public class App { // ★
    private static final String DB_URL = "jdbc:sqlite:todos.db"; // ★

    public static void main(String[] args) throws Exception { // ★
        Class.forName("org.sqlite.JDBC");
        createTable(); // ★
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // ★
        server.createContext("/api/todos", exchange -> { // Todo一覧APIの入口を追加します
            if (!"GET".equals(exchange.getRequestMethod())) { // GET以外のリクエストを判定します
                send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed"); // 許可されないメソッドを返します
                return; // このリクエストの処理を終了します
            } // GET以外の処理をここで終えます
            try { // データベース例外を処理するために開始します
                StringBuilder json = new StringBuilder("["); // JSON配列の組み立てを開始します
                for (Todo todo : findAll()) { // 全Todoを順番に処理します
                    if (json.length() > 1) json.append(","); // 2件目以降の前に区切り文字を付けます
                    json.append("{\"title\":\"").append(escapeJson(todo.title)); // タイトルをJSON文字列として追加します
                    json.append("\",\"done\":").append(todo.done).append("}"); // 完了状態をJSONの真偽値として追加します
                } // 全TodoのJSON化を終えます
                json.append("]"); // JSON配列を閉じます
                send(exchange, 200, "application/json", json.toString()); // charsetなしのJSONレスポンスを返します
            } catch (SQLException e) { // データベースエラーを受け取ります
                send(exchange, 500, "text/plain; charset=UTF-8", "データベースエラー: " + e.getMessage()); // サーバーエラーを返します
            } // 例外処理を終えます
        }); // Todo一覧APIの入口を登録します
        server.createContext("/", exchange -> { // ★
            String path = exchange.getRequestURI().getPath(); // ★
            String method = exchange.getRequestMethod(); // ★
            try { // ★
                if (path.equals("/add") && method.equals("POST")) { // ★
                    String title = formValue(
                            new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), "todo"); // ★
                    if (!title.isBlank())
                        addTodo(title); // ★
                    redirect(exchange); // ★
                    return; // ★
                }
                if (path.equals("/done") && method.equals("GET")) { // ★
                    Integer id = queryId(exchange.getRequestURI().getQuery()); // ★
                    if (id != null)
                        updateDone(id); // ★
                    redirect(exchange); // ★
                    return; // ★
                }
                if (path.equals("/delete") && method.equals("GET")) { // ★
                    Integer id = queryId(exchange.getRequestURI().getQuery()); // ★
                    if (id != null)
                        deleteTodo(id); // ★
                    redirect(exchange); // ★
                    return; // ★
                }
                if (path.equals("/")) { // ★
                    String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><h1>やることリスト</h1>" // ★
                            + "<form method='post' action='/add'>やること <input name='todo' required><button>追加</button></form>"; // ★
                    List<Todo> todos = findAll(); // ★
                    int remaining = 0; // ★
                    StringBuilder items = new StringBuilder("<ul>"); // ★
                    for (Todo todo : todos) { // ★
                        if (!todo.done)
                            remaining++; // ★
                        String title = todo.done ? "<s>" + escapeHtml(todo.title) + "</s>" : escapeHtml(todo.title); // ★
                        items.append("<li>").append(title).append(" <a href='/done?id=").append(todo.id) // ★
                                .append("'>完了</a> <a href='/delete?id=").append(todo.id).append("'>削除</a></li>"); // ★
                    }
                    html += "<p>あと" + remaining + "件</p>" + items.append("</ul></body></html>"); // ★
                    send(exchange, 200, "text/html; charset=UTF-8", html); // ★
                    return; // ★
                }
                send(exchange, 404, "text/plain; charset=UTF-8", "ページが見つかりません"); // ★
            } catch (SQLException e) { // ★
                send(exchange, 500, "text/plain; charset=UTF-8", "データベースエラー: " + e.getMessage()); // ★
            }
        });
        server.start(); // ★
        System.out.println("サーバー起動: http://localhost:8080 （終了するには Ctrl+C）"); // ★
    }

    static void createTable() throws SQLException { // ★
        try (Connection conn = DriverManager.getConnection(DB_URL); // ★
                PreparedStatement ps = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)")) { // ★
            ps.execute(); // ★
        }
    }

    static void addTodo(String title) throws SQLException { // ★
        try (Connection conn = DriverManager.getConnection(DB_URL); // ★
                PreparedStatement ps = conn.prepareStatement("INSERT INTO todos (title, done) VALUES (?, 0)")) { // ★
            ps.setString(1, title); // ★
            ps.executeUpdate(); // ★
        }
    }

    static void updateDone(int id) throws SQLException { // ★
        try (Connection conn = DriverManager.getConnection(DB_URL); // ★
                PreparedStatement ps = conn.prepareStatement("UPDATE todos SET done = 1 WHERE id = ?")) { // ★
            ps.setInt(1, id); // ★
            ps.executeUpdate(); // ★
        }
    }

    static void deleteTodo(int id) throws SQLException { // ★
        try (Connection conn = DriverManager.getConnection(DB_URL); // ★
                PreparedStatement ps = conn.prepareStatement("DELETE FROM todos WHERE id = ?")) { // ★
            ps.setInt(1, id); // ★
            ps.executeUpdate(); // ★
        }
    }

    static List<Todo> findAll() throws SQLException { // ★
        List<Todo> todos = new ArrayList<>(); // ★
        try (Connection conn = DriverManager.getConnection(DB_URL); // ★
                PreparedStatement ps = conn.prepareStatement("SELECT id, title, done FROM todos ORDER BY id"); // ★
                ResultSet rs = ps.executeQuery()) { // ★
            while (rs.next())
                todos.add(new Todo(rs.getInt("id"), rs.getString("title"), rs.getInt("done") != 0)); // ★
        }
        return todos; // ★
    }

    static String formValue(String text, String name) { // ★
        for (String field : text.split("&")) { // ★
            String[] pair = field.split("=", 2); // ★
            if (pair.length == 2 && pair[0].equals(name))
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8); // ★
        }
        return ""; // ★
    }

    static Integer queryId(String query) { // ★
        try {
            return query == null ? null : Integer.valueOf(formValue(query, "id"));
        } catch (NumberFormatException e) {
            return null;
        } // ★
    }

    static void redirect(com.sun.net.httpserver.HttpExchange exchange) throws java.io.IOException { // ★
        exchange.getResponseHeaders().set("Location", "/"); // ★
        exchange.sendResponseHeaders(303, -1); // ★
        exchange.close(); // ★
    }

    static void send(com.sun.net.httpserver.HttpExchange exchange, int status, String type, String message)
            throws java.io.IOException { // ★
        byte[] body = message.getBytes(StandardCharsets.UTF_8); // ★
        exchange.getResponseHeaders().set("Content-Type", type); // ★
        exchange.sendResponseHeaders(status, body.length); // ★
        exchange.getResponseBody().write(body); // ★
        exchange.close(); // ★
    }

    static String escapeHtml(String value) { // ★
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); // ★
    }

    static String escapeJson(String value) { // 文字列を安全なJSON文字列の内容へ変換します
        StringBuilder escaped = new StringBuilder(); // エスケープ後の文字列を組み立てます
        for (int i = 0; i < value.length(); i++) { // 文字列を1文字ずつ確認します
            char c = value.charAt(i); // 現在の文字を取得します
            switch (c) { // JSONで特別扱いする文字を判定します
                case '"': escaped.append("\\\""); break; // 二重引用符をエスケープします
                case '\\': escaped.append("\\\\"); break; // バックスラッシュをエスケープします
                case '\b': escaped.append("\\b"); break; // バックスペースをエスケープします
                case '\f': escaped.append("\\f"); break; // 改ページをエスケープします
                case '\n': escaped.append("\\n"); break; // 改行をエスケープします
                case '\r': escaped.append("\\r"); break; // 復帰をエスケープします
                case '\t': escaped.append("\\t"); break; // タブをエスケープします
                default: // それ以外の文字を処理します
                    if (c < 0x20 || Character.isSurrogate(c)) escaped.append(String.format("\\u%04x", (int) c)); // 制御文字とサロゲートをUnicodeエスケープします
                    else escaped.append(c); // JSONでそのまま使える文字を追加します
            } // 文字ごとの処理を終えます
        } // 全文字の処理を終えます
        return escaped.toString(); // JSON用にエスケープした文字列を返します
    } // JSON文字列エスケープ処理を終えます

    static class Todo { // ★
        final int id;
        final String title;
        final boolean done; // ★

        Todo(int id, String title, boolean done) {
            this.id = id;
            this.title = title;
            this.done = done;
        } // ★
    }
}

/*
 * 旧CSV保存版（★の付いた上記コードへ置換）
 * public class App {
 * static List<Todo> todos = new ArrayList<>();
 * static int nextId = 1;
 * 
 * public static void main(String[] args) throws Exception {
 * load();
 * HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
 * server.createContext("/", exchange -> {
 * String path = exchange.getRequestURI().getPath();
 * String method = exchange.getRequestMethod();
 * String message;
 * exchange.getResponseHeaders().set("Content-Type",
 * "text/plain; charset=UTF-8");
 * if (path.equals("/add") && method.equals("POST")) {
 * String body = new String(exchange.getRequestBody().readAllBytes(),
 * StandardCharsets.UTF_8);
 * String title = "";
 * String dueDateText = "";
 * for (String field : body.split("&")) {
 * String[] pair = field.split("=", 2);
 * if (pair.length == 2 && pair[0].equals("todo")) {
 * title = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
 * } else if (pair.length == 2 && pair[0].equals("dueDate")) {
 * dueDateText = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
 * }
 * }
 * if (!title.isEmpty() && !dueDateText.isEmpty()) {
 * try {
 * LocalDate dueDate = LocalDate.parse(dueDateText);
 * todos.add(new Todo(nextId, title, dueDate));
 * nextId++;
 * save();
 * } catch (DateTimeParseException e) {
 * }
 * }
 * exchange.getResponseHeaders().set("Location", "/");
 * exchange.sendResponseHeaders(303, -1);
 * exchange.close();
 * return;
 * } else if (path.equals("/done") && method.equals("GET")) {
 * String query = exchange.getRequestURI().getQuery();
 * if (query != null && query.startsWith("id=") && query.length() > 3) {
 * try {
 * int id = Integer.parseInt(query.substring(3));
 * for (Todo todo : todos) {
 * if (todo.getId() == id) {
 * todo.setDone(true);
 * save();
 * break;
 * }
 * }
 * } catch (NumberFormatException e) {
 * }
 * }
 * exchange.getResponseHeaders().set("Location", "/");
 * exchange.sendResponseHeaders(303, -1);
 * exchange.close();
 * return;
 * } else if (path.equals("/delete") && method.equals("GET")) {
 * String query = exchange.getRequestURI().getQuery();
 * if (query != null && query.startsWith("id=") && query.length() > 3) {
 * try {
 * int id = Integer.parseInt(query.substring(3));
 * if (todos.removeIf(todo -> todo.getId() == id)) {
 * save();
 * }
 * } catch (NumberFormatException e) {
 * }
 * }
 * exchange.getResponseHeaders().set("Location", "/");
 * exchange.sendResponseHeaders(303, -1);
 * exchange.close();
 * return;
 * } else if (path.equals("/")) {
 * String html =
 * "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>body{max-width:640px;margin:32px auto;padding:0 16px;font-size:16px}input{font-size:16px}</style></head><body>"
 * ;
 * html += "<h1>やることリスト</h1>";
 * html += "<form method='post' action='/add'>"
 * + "<label>やること <input name='todo'></label> "
 * + "<label>締め切り日 <input type='date' name='dueDate' required></label> "
 * + "<button>追加</button></form>";
 * int remainingCount = 0;
 * for (Todo todo : todos) {
 * if (!todo.isDone()) {
 * remainingCount++;
 * }
 * }
 * html += "<p>あと" + remainingCount + "件</p>";
 * if (todos.isEmpty()) {
 * html += "<p>やること0件</p>";
 * }
 * html += "<ul>";
 * for (Todo todo : todos) {
 * String title = todo.getTitle();
 * if (todo.isDone()) {
 * title = "<s>" + title + "</s>";
 * }
 * html += "<li>" + title + "（締め切り: " + todo.getDueDate() +
 * "） <a href='/done?id=" + todo.getId()
 * + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
 * }
 * html += "</ul></body></html>";
 * message = html;
 * exchange.getResponseHeaders().set("Content-Type",
 * "text/html; charset=UTF-8");
 * } else {
 * message = "ページが見つかりません";
 * }
 * 
 * byte[] body = message.getBytes("UTF-8");
 * exchange.sendResponseHeaders(200, body.length);
 * exchange.getResponseBody().write(body);
 * exchange.getResponseBody().close();
 * });
 * server.start();
 * System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
 * }
 * 
 * static void save() throws java.io.IOException {
 * StringBuilder csv = new StringBuilder();
 * for (Todo todo : todos) {
 * csv.append(todo.getId()).append(',')
 * .append(todo.isDone() ? 1 : 0).append(',')
 * .append(todo.getTitle())
 * .append(System.lineSeparator());
 * }
 * Files.writeString(Path.of("todos.csv"), csv.toString(),
 * StandardCharsets.UTF_8);
 * }
 * 
 * static void load() throws java.io.IOException {
 * Path path = Path.of("todos.csv");
 * if (!Files.exists(path)) {
 * return;
 * }
 * Integer maxId = null;
 * for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
 * String[] fields = line.split(",", 3);
 * if (fields.length != 3) {
 * continue;
 * }
 * try {
 * int id = Integer.parseInt(fields[0]);
 * Todo todo = new Todo(id, fields[2], null);
 * todo.setDone(fields[1].equals("1"));
 * todos.add(todo);
 * maxId = maxId == null ? id : Math.max(maxId, id);
 * } catch (NumberFormatException e) {
 * }
 * }
 * if (maxId != null) {
 * nextId = maxId + 1;
 * }
 * }
 * }
 * 
 */ // ★
