public class Main {

    public static void main(String[] args) {
        String url = "https://www.expertlead.com/en/products/12/x";
        String template = "[/:lang]/products[/:id]/x";

        String result = App.doApp(url, template)
                .map(App.Response::toString)
                .orElse("{}");

        System.out.println(result);
    }
}
