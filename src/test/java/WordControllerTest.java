import me.kesx.tool.VocabularyApp;
import me.kesx.tool.entity.WordVo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = VocabularyApp.class)
public class WordControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @Test
    public void add() throws Exception {

        final String baseUrl = "http://localhost:"+port+"/add/";
        URI uri = new URI(baseUrl);
        WordVo wordVo = new WordVo();
        wordVo.setWord("1");
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<WordVo> request = new HttpEntity<>(wordVo, headers);
        System.out.println("req=  "+request);

        ResponseEntity<String> result = this.restTemplate.postForEntity(uri, request, String.class);

        //Verify request succeed
        Assert.assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void update() throws Exception {

        final String baseUrl = "http://localhost:"+port+"/update";
        URI uri = new URI(baseUrl);
        WordVo wordVo = new WordVo();
        wordVo.setWordId(4);
        wordVo.setPos("1234");
        wordVo.setNotes("123");
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<WordVo> request = new HttpEntity<>(wordVo, headers);
        System.out.println("req=  "+request);

        ResponseEntity<String> result = this.restTemplate.postForEntity(uri, request, String.class);
        //Verify request succeed
        Assert.assertEquals(200, result.getStatusCodeValue());
    }
}
