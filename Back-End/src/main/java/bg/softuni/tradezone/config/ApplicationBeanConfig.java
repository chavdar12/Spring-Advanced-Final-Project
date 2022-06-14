package bg.softuni.tradezone.config;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationBeanConfig {

    private static final ModelMapper mapper;

    private static final Gson gson;

    static {
        gson = new Gson();
        mapper = new ModelMapper();
        mapper.getConfiguration().setAmbiguityIgnored(true);
    }

    @Bean
    public ModelMapper modelMapper() {
        return mapper;
    }

    @Bean
    public Gson gson() {
        return gson;
    }
}