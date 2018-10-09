package pl.mkm.webAlignSeq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.mkm.webAlignSeq.property.FileStorageProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class WebAlignSeqApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebAlignSeqApplication.class, args);
	}
}
