package no.difi.eidas.cproxy.domain.audit;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.idporten.log.audit.AuditLogger;
import no.idporten.log.audit.AuditLoggerELFImpl;
import no.idporten.log.elf.ELFWriter;
import no.idporten.log.elf.FileRollerDailyImpl;
import no.idporten.log.elf.WriterCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditLoggerProvider {
    @Bean
    public AuditLogger auditLogger(ConfigProvider config) {
        ELFWriter elfWriter = new ELFWriter(
                new FileRollerDailyImpl(config.auditLogDir(), config.auditLogFile()),
                new WriterCreator()
        );
        AuditLoggerELFImpl logger = new AuditLoggerELFImpl();
        logger.setELFWriter(elfWriter);
        logger.setDataSeparator(config.auditLogDataSeparator());
        return logger;
    }
}
