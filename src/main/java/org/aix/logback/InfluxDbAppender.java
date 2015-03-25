package org.aix.logback;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

/**
 * Created by nicolas on 12/03/15.
 */
public class InfluxDbAppender extends AppenderBase<ILoggingEvent> {

    private AppenderExecutor appenderExecutor;

    private String influxDbUrl;
    private String influxDbPort;
    private String influxDbLogin;
    private String influxDbPassword;
    private SerieConfig serieConfig;

    public void setSerieConfig(SerieConfig serieConfig) {
        this.serieConfig = serieConfig;
    }

    @Override
    public String toString() {
        return "InfluxdbAppender{" +
                "serieConfig=" + serieConfig +
                ", influxDbUrl='" + influxDbUrl + '\'' +
                ", influxDbPort='" + influxDbPort + '\'' +
                ", influxDbLogin='" + influxDbLogin + '\'' +
                ", influxDbPassword='" + influxDbPassword + '\'' +
                '}';
    }

    public void setInfluxDbUrl(String influxDbUrl) {
        this.influxDbUrl = influxDbUrl;
    }

    public void setInfluxDbPort(String influxDbPort) {
        this.influxDbPort = influxDbPort;
    }

    public void setInfluxDbLogin(String influxDbLogin) {
        this.influxDbLogin = influxDbLogin;
    }

    public void setInfluxDbPassword(String influxDbPassword) {
        this.influxDbPassword = influxDbPassword;
    }

    private InfluxDB influxDB;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        appenderExecutor.append(iLoggingEvent);
    }

    @Override
    public void start() {
        super.start();
        initExecutor();
    }

    /**
     * This is an ad-hoc dependency injection mechanism. We don't want create all these classes every time a message is
     * logged. They will hang around for the lifetime of the appender.
     */
    private void initExecutor() {
        System.out.println(":: initExecutor :: begin");
        System.out.println(toString());

        this.influxDB = InfluxDBFactory.connect(influxDbUrl + ":" + influxDbPort, influxDbLogin, influxDbPassword);
        try {
            this.influxDB.createDatabase("mydb");
        } catch(Exception e) {
            if (e.getMessage().contains("exists")==false) {
                e.printStackTrace();
            }
        }

        InfluxDbConverter converter = new InfluxDbConverter();
        appenderExecutor = new AppenderExecutor(converter, serieConfig, influxDB, getContext());
        System.out.println(":: initExecutor :: end");

    }


}