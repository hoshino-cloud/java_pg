// Itemsクラス（Javaのコードをまとめる入れ物）を定義します。
class Items {
    // main（プログラムを実行したとき最初に動く場所）を定義します。
    public static void main(String[] args) {
        // todos配列（複数の値をまとめて入れるもの）にTodoを3件入れます。
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする", "本を読む" };

        // for文（同じ処理を繰り返す命令）で、配列の先頭から順に取り出します。
        // done配列（対応するTodoが済んだかを表す真偽値の一覧）を用意します。
        boolean[] done = { true, false, false, false, true };
        for (int i = 0; i < todos.length; i++) {
            // 現在のTodoが空文字列（文字が何も入っていない値）でないか確認します。
            if (!todos[i].isEmpty()) {
                // 空でないTodoを<li>と</li>で囲み、ターミナルに1行で出力します。
                // 済んでいるTodoには[済]の印を付けます。
                // 現在のTodoが済んでいるかをif / else（条件による分岐）で判定します。
                if (done[i]) {
                    // 済んでいるTodoに[済]の印を付けて出力します。
                    System.out.println("<li>[済] " + todos[i] + "</li>");
                } else {
                    // 済んでいないTodoを印なしで出力します。
                    System.out.println("<li>" + todos[i] + "</li>");
                }
            }
        }
    }
}
