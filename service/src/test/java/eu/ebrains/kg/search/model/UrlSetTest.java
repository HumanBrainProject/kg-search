package eu.ebrains.kg.search.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import java.util.Arrays;

public class UrlSetTest {

    @Test
    public void serializeXml() throws JsonProcessingException {
        SitemapXML set = new SitemapXML();

        SitemapXML.Url a = new SitemapXML.Url();
        a.setLoc("a");
        SitemapXML.Url b = new SitemapXML.Url();
        b.setLoc("b");
        set.setUrl(Arrays.asList(a, b));

        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(set);
        System.out.println(xml);
    }

}