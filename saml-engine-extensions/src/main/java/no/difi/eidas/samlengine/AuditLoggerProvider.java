package no.difi.eidas.samlengine;

import no.idporten.log.audit.AuditLoggerELFImpl;
import no.idporten.log.elf.ELFWriter;
import no.idporten.log.elf.FileRollerDailyImpl;
import no.idporten.log.elf.WriterCreator;

import java.util.Objects;
import java.util.Properties;

public class AuditLoggerProvider {

    private AuditLoggerELFImpl logger;

    private static AuditLoggerProvider ourInstance = new AuditLoggerProvider();

    public static AuditLoggerProvider getInstance() {
        return ourInstance;
    }

    private AuditLoggerProvider() {
        Properties properties = loadProperties();

        String logDirectory = properties.getProperty("log.directory");
        Objects.requireNonNull(logDirectory, "log.directory is not defined in saml-engine audit config");

        String logFile = properties.getProperty("log.file");
        Objects.requireNonNull(logFile, "log.file is not defined in saml-engine audit config");

        logger = new AuditLoggerELFImpl();
        logger.setELFWriter(new ELFWriter(
            new FileRollerDailyImpl(logDirectory, logFile),
            new WriterCreator()
        ));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("audit.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Could not load config file for saml logging", e);
        }
        return properties;
    }

    public AuditLoggerELFImpl getLogger() {
        return logger;
    }

}