package cafeLogProject.cafeLog;

import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocumentRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories(excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = NicknameDocumentRepository.class))
@SpringBootApplication
public class CafeLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeLogApplication.class, args);
	}

}
