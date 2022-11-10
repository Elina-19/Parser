package ru.itis.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ru.itis.exception.ParsingException;
import ru.itis.model.Item;
import ru.itis.service.ItemService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Component
public class Parser {

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    private final ItemService itemService;

    private String url;

    private String offset;

    private String limit;

    public void parse() {
        initialize();
        String response = makeRequest();
        List<Item> items = parseJson(response);

        // Возвращаемые значения цены указаны в копейках, перевод в рубли
        items = items.stream()
                .map(x -> {
                    x.setMinFullPrice(x.getMinFullPrice() / 100);
                    x.setMinSellPrice(x.getMinSellPrice() / 100);

                    return x;
                }).collect(Collectors.toList());

        itemService.saveAll(items);
    }

    private void initialize() {
        Properties properties = new Properties();

        try {
            File file = ResourceUtils.getFile("classpath:application.properties");
            InputStream in = new FileInputStream(file);
            properties.load(in);

            url = properties.getProperty("url");
            offset = properties.getProperty("offset");
            limit = properties.getProperty("limit");
        } catch (IOException e) {
            throw new ParsingException("Read properties error", e);
        }
    }

    private String makeRequest() {
        RequestBody formBody = RequestBody.create("{\"operationName\":\"getMakeSearch\"," +
                        "\"variables\":{\"queryInput\":{\"categoryId\":\"10044\",\"showAdultContent\":\"NONE\"," +
                        "\"filters\":[],\"sort\":\"BY_RELEVANCE_DESC\",\"pagination\":" +
                        "{\"offset\":" + offset + ",\"limit\":" + limit + "}}}," +
                        "\"query\":\"query getMakeSearch($queryInput: MakeSearchQueryInput!) " +
                        "{\\n  makeSearch(query: $queryInput) {\\n  category {\\n   ...CategoryShortFragment\\n}\\n " +
                        "categoryTree {\\n      category {\\n        ...CategoryFragment\\n      }\\n      }\\n    " +
                        "items {\\n      catalogCard {\\n        ...SkuGroupCardFragment\\n      }\\n      }\\n    " +
                        "facets {\\n      ...FacetFragment\\n      }\\n    }\\n}\\n\\nfragment FacetFragment on Facet " +
                        "{\\n  filter {\\n    id\\n    title\\n    type\\n    measurementUnit\\n    description\\n    " +
                        "__typename\\n  }\\n  buckets {\\n    filterValue {\\n      id\\n      description\\n      " +
                        "image\\n      name\\n      }\\n    total\\n    }\\n  range {\\n    min\\n    max\\n    " +
                        "__typename\\n  }\\n  __typename\\n}\\n\\nfragment CategoryFragment on Category {\\n  id\\n  " +
                        "icon\\n  parent {\\n    id\\n    __typename\\n  }\\n  seo {\\n    header\\n    metaTag\\n   " +
                        " __typename\\n  }\\n  title\\n  adult\\n  __typename\\n}\\n\\nfragment " +
                        "CategoryShortFragment on Category {\\n  id\\n    title\\n     }\\n\\nfragment " +
                        "SkuGroupCardFragment on SkuGroupCard {\\n  ...DefaultCardFragment\\n }\\n     " +
                        "\\nfragment DefaultCardFragment on CatalogCard {\\n  id\\n  minFullPrice\\n  minSellPrice\\n" +
                        "productId\\n  rating\\n  title\\n}\"}",
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:106.0) Gecko/20100101 Firefox/106.0" )
                .addHeader("Accept-Language","en-US")
                .addHeader("content-type", "application/json")
                .addHeader("Connection", "keep-alive")
                .addHeader("authorization", "Basic a2F6YW5leHByZXNzLWN1c3RvbWVyOmN1c3RvbWVyU2VjcmV0S2V5")
                .addHeader("x-iid", "96600214-1cd1-4012-a602-1ba8adc2cbe2")
                .post(formBody)
                .build();

        Call call = client.newCall(request);

        try {
            return call.execute().body().string();
        } catch (IOException e) {
            throw new ParsingException("Request error", e);
        }
    }

    private List<Item> parseJson(String json) {
        try {
            JsonNode jsonNode = mapper.readTree(json)
                    .path("data")
                    .path("makeSearch")
                    .path("items");

            List<JsonNode> list = StreamSupport.stream(jsonNode.spliterator(), true)
                    .map(x -> x.get("catalogCard"))
                    .collect(Collectors.toList());

            return mapper.readValue(list.toString(), new TypeReference<List<Item>>(){});
        } catch (IOException e) {
            throw new ParsingException("Parsing json error", e);
        }
    }
}
