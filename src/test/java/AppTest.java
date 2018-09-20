import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;

public class AppTest {
    private App app = new App();

    @Test
    public void forSimple(){
        String url = "https://www.expertlead.com/en/products/12/x";
        String template = "[/:lang]/products[/:id]/x";

        App.Response response = app.doApp(url, template).get();


        List<App.Tuple<String,String>> expectedTuples = Arrays.asList(new App.Tuple("lang", "en"), new App.Tuple("id", "12"));
        App.Response expected = new App.Response("https", "www.expertlead.com", "en/products/12/x", expectedTuples);

        assertThat(response, equalTo(expected));
    }

    @Test
    public void emptyPath(){
        String url = "https://www.expertlead.com/";
        String template = "/";

        App.Response response = app.doApp(url, template).get();

        assertThat(response.getParameters(), is(empty()));
        assertThat(response.getPath(), is(""));
    }

    @Test
    public void singleOptional(){
        String url = "https://www.expertlead.com/de";
        String template = "[/:lang]";

        App.Response response = app.doApp(url, template).get();

        assertThat(response.getParameters(), containsInAnyOrder(new App.Tuple("lang", "de")));
    }

    @Test
    public void optionalInMiddle(){
        String url = "https://www.expertlead.com/v1/de/products";
        String template = "/v1[/:lang]/products";

        App.Response response = app.doApp(url, template).get();

        assertThat(response.getParameters(), containsInAnyOrder(new App.Tuple("lang", "de")));
    }

    @Test
    public void noOptional(){
        String url = "https://www.expertlead.com/v1/de/products/";
        String template = "/v1/de/products";

        App.Response response = app.doApp(url, template).get();

        assertThat(response.getParameters(), is(empty()));
        assertThat(response.getPath(), is("v1/de/products/"));
    }


    @Test
    public void theirTest(){
        String url = "non-url-string";
        String template = "/";

        Optional<App.Response> response = app.doApp(url, template);
        assertThat(response, is(Optional.empty()));
    }

}
