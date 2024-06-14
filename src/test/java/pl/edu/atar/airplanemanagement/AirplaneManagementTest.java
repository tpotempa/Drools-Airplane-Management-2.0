package pl.edu.atar.airplanemanagement;

import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.time.SessionPseudoClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AirplaneManagementTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirplaneManagementTest.class);

    @Test
    public void testWithSystemClock() throws InterruptedException {
        KieServices kService = KieServices.Factory.get();
        KieContainer kContainer = kService.getKieClasspathContainer();
        KieBase kBase = kContainer.getKieBase("airplane");

        KieSessionConfiguration configuration = kService.newKieSessionConfiguration();
        configuration.setOption(ClockTypeOption.PSEUDO);

        KieSession kSession = kBase.newKieSession(configuration, null);

        // Utworzenie (pseudo)zegara dla sesji
        SessionPseudoClock pclock = kSession.getSessionClock();
        pclock.advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        // Uruchomienie wątku, który w trybie ciągłym monitoruje zdarzenia
        // oraz w przypadku, gdy przesłanki reguły są spełnione uruchamia akcje
        new Thread(kSession::fireUntilHalt).start();

        // Logowanie zebranych informacji
        kService.getLoggers().newConsoleLogger(kSession);
        kService.getLoggers().newFileLogger(kSession, "./logs/reasoning_" + pclock.getCurrentTime());

        // Dodanie możliwości poprawnego zamknięcia sesji
        Runtime.getRuntime().addShutdownHook(new Thread(kSession::halt));

    }
}