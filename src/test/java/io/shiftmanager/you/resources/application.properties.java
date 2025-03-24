spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver


spring.thymeleaf.check-template-location=false

        # セキュリティ設定
spring.security.csrf.enabled=true

        # MyBatis設定
mybatis.configuration.map-underscore-to-camel-case=true

        # ロギング設定
logging.level.org.springframework=INFO
logging.level.io.shiftmanager.you=DEBUG

# H2コンソール設定（テスト時に便利）
spring.h2.console.enabled=true