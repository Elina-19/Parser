package ru.itis;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.itis.config.AppConfig;
import ru.itis.config.DataBaseConfig;
import ru.itis.service.ItemService;
import ru.itis.utils.Parser;

public class Main {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        context.register(DataBaseConfig.class);

        Parser parser = new Parser(context.getBean(OkHttpClient.class),
                context.getBean(ObjectMapper.class),
                context.getBean(ItemService.class));

        Long before = System.currentTimeMillis();

        parser.parse();

        Long after = System.currentTimeMillis();
        System.out.println(5 * (after - before) / 1000);
    }
}
