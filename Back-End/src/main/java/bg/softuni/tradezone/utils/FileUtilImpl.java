package bg.softuni.tradezone.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Component
public class FileUtilImpl implements FileUtil {

    @Override
    public String fileContent(String classpathLocation) {
        try {
            return new ClassPathResource(classpathLocation).getContentAsString(StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + classpathLocation, e);
        }
    }
}
