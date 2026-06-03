package org.marllon.caip.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Map;

@Profile("test")
@Configuration
public class TestConfig {

    @Bean
    @Primary
    public Cloudinary cloudinary() throws IOException {
        Cloudinary cloudinary = Mockito.mock(Cloudinary.class);
        Uploader uploader = Mockito.mock(Uploader.class);

        Mockito.when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, String> mockResponse = ObjectUtils.asMap(
                "secure_url", "https://res.cloudinary.com/test-cloud/image/upload/v1/caip/reports/test.jpg"
        );

        Mockito.when(uploader.upload(Mockito.any(byte[].class), Mockito.anyMap()))
               .thenReturn(mockResponse);

        return cloudinary;
    }
}