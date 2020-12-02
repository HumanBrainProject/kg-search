package eu.ebrains.kg.search.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import java.util.Arrays;

public class UrlSetTest {

    @Test
    public void serializeXml() throws JsonProcessingException {
        Sitemap set = new Sitemap();

        Sitemap.Url a = new Sitemap.Url();
        a.setLoc("a");
        Sitemap.Url b = new Sitemap.Url();
        b.setLoc("b");
        set.setUrl(Arrays.asList(a, b));

        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(set);
        System.out.println(xml);
    }

}