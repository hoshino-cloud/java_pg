public class Item { // Itemという名前のプログラムを定義します
    public static void main(String[] args) { // プログラムがここから動き始めます
        String title = "本を読む"; // 文字を入れる変数（名前付きの入れ物）を作ります
        String html = "<li>" + title + "</li>"; // +で文字をつなぎ、HTMLの1行を作ります
        System.out.println(html); // 作った文字をターミナル（文字で操作する画面）に表示します

        boolean done = false;
        System.out.println(done);

        int count = 3;
        System.out.println("いま" + count + "件");
    } // 動き始める部分はここまでです
} // Itemプログラムの定義はここまでです
