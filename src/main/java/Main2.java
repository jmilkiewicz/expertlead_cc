import java.util.Scanner;

public class Main2 {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String url = in.nextLine();
        String template = in.nextLine();
        App app = new App();
        String result = app.doApp(url, template)
                .map(App.Response::toString)
                .orElse("{}");

        System.out.println(result);
    }
}
