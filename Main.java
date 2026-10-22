import java.util.ArrayList; // Todoを複数入れる箱を使う準備をします。
import java.util.List; // Todoの一覧を扱う型を使う準備をします。

class Todo { // Todo（やること1件）の情報をまとめるクラスです。
    String title; // やることのタイトルを保存します。
    boolean done; // 済んでいるかどうかを保存します。

    Todo(String title, boolean done) { // Todoを作るときにタイトルと済んだかを受け取ります。
        this.title = title; // 受け取ったタイトルをこのTodoに保存します。
        this.done = done; // 受け取った済んだかどうかをこのTodoに保存します。
    } // Todoを作る処理はここまでです。

    String toItem() { // TodoをHTML風の1行に変換するメソッド（処理）です。
        if (done) { // 済んでいるかどうかを調べます。
            return "<li>[終了] " + title + "</li>"; // 済んでいれば「[済]」を付けた1行を返します。
        } // 済んでいる場合の処理はここまでです。
        return "<li>" + title + "</li>"; // 済んでいなければタイトルだけの1行を返します。
    } // 1行に変換する処理はここまでです。
} // Todoクラスはここまでです。

public class Main { // プログラムの実行を始めるクラスです。
    public static void main(String[] args) { // Javaが最初に実行する場所です。
        List<Todo> todos = new ArrayList<>(); // Todoを入れる空の一覧を作ります。
        todos.add(new Todo("牛乳を買う", false)); // 済んでいないTodoを一覧に追加します。
        todos.add(new Todo("ゴミを出す", true)); // 済んだTodoを一覧に追加します。
        todos.add(new Todo("本を読む", true));

        for (Todo todo : todos) { // 一覧からTodoを1件ずつ取り出して繰り返します。
            System.out.println(todo.toItem()); // 変換した1行をターミナルに表示します。
        } // 繰り返しはここまでです。
    } // mainメソッド（実行の入口）はここまでです。
} // Mainクラスはここまでです。
