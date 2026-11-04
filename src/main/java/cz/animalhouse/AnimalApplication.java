package cz.animalhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.net.URI;

@SpringBootApplication
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class AnimalApplication {

    public static void main(String[] args) {
        RenderCom_PostgreSQL_ConnectionString_To_Spring_JDBC_DatasourceUrl();

        SpringApplication.run(AnimalApplication.class, args);
    }

    private static void RenderCom_PostgreSQL_ConnectionString_To_Spring_JDBC_DatasourceUrl() {
        //	Render PostgreSQL — connectionString to Spring JDBC — spring.datasource.url
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            URI uri = URI.create(databaseUrl);

            String jdbcUrl = "jdbc:postgresql://"
                    + uri.getHost()
                    + ":" + (uri.getPort() == -1 ? 5432 : uri.getPort())
                    + uri.getRawPath();

            System.setProperty("spring.datasource.url", jdbcUrl);
        }
    }

}
