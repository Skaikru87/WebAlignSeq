package pl.mkm.webAlignSeq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.mkm.webAlignSeq.property.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class WebAlignSeqApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebAlignSeqApplication.class, args);
	}
}
