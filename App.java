import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class App {
    static List<Todo> todos = new ArrayList<>();
    static int nextId = 1;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        todos.add(new Todo(nextId++, "牛乳を買う", LocalDate.of(2026, 10, 30)));
        Todo egg = new Todo(nextId++, "卵を買う", LocalDate.of(2026, 10, 28));
        egg.setDone(true);
        todos.add(egg);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            if (path.equals("/add") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String title = "";
                String dueDateText = "";
                for (String field : body.split("&")) {
                    String[] pair = field.split("=", 2);
                    if (pair.length == 2 && pair[0].equals("todo")) {
                        title = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    } else if (pair.length == 2 && pair[0].equals("dueDate")) {
                        dueDateText = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
                if (!title.isEmpty() && !dueDateText.isEmpty()) {
                    try {
                        LocalDate dueDate = LocalDate.parse(dueDateText);
                        todos.add(new Todo(nextId, title, dueDate));
                        nextId++;
                    } catch (DateTimeParseException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/done") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        for (Todo todo : todos) {
                            if (todo.getId() == id) {
                                todo.setDone(true);
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/delete") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        todos.removeIf(todo -> todo.getId() == id);
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/")) {
                String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>body{max-width:640px;margin:32px auto;padding:0 16px;font-size:16px}input{font-size:16px}</style></head><body>";
                html += "<h1>やることリスト</h1>";
                html += "<form method='post' action='/add'>"
                        + "<label>やること <input name='todo'></label> "
                        + "<label>締め切り日 <input type='date' name='dueDate' required></label> "
                        + "<button>追加</button></form>";
                int remainingCount = 0;
                for (Todo todo : todos) {
                    if (!todo.isDone()) {
                        remainingCount++;
                    }
                }
                html += "<p>あと" + remainingCount + "件</p>";
                if (todos.isEmpty()) {
                    html += "<p>やること0件</p>";
                }
                html += "<ul>";
                for (Todo todo : todos) {
                    String title = todo.getTitle();
                    if (todo.isDone()) {
                        title = "<s>" + title + "</s>";
                    }
                    html += "<li>" + title + "（締め切り: " + todo.getDueDate() + "） <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                }
                html += "</ul></body></html>";
                message = html;
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                message = "ページが見つかりません";
            }

            byte[] body = message.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}

class Todo {
    private final int id;
    private final String title;
    private final LocalDate dueDate;
    private boolean done;

    Todo(int id, String title, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.done = false;
    }

    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    LocalDate getDueDate() {
        return dueDate;
    }

    boolean isDone() {
        return done;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}
