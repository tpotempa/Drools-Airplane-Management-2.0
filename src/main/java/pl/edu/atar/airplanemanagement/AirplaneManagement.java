package pl.edu.atar.airplanemanagement;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.time.SessionPseudoClock;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.atar.airplanemanagement.events.LocationEvent;

public class AirplaneManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirplaneManagement.class);

    public static void main(String[] args) {
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

        // Symulacja generowania ciągu zdarzeń z wykorzystaniem (psuedo)zegara
        new Thread(() -> {
            try {
                double latitude = 37.8999;
                double longitude = -108.8726;
                long interval = 5;
                while (true) {

                    // Dodanie zdarzeń, alternatywnie zdarzenia mogą dostarczane przez odpowiedni broker np. Kafkę.
                    kSession.insert(new LocationEvent("SXS48Z", latitude, longitude, pclock.getCurrentTime()));

                    // Przesunięcie (pseudo)zegara w przód aby symulować upływ czasu
                    pclock.advanceTime(interval, TimeUnit.SECONDS);

                    // Uśpienie wątku w celu obserwowania w "slow-motion" działania silnika wnioskującego
                    Thread.sleep(1000);
                    latitude -= 0.0001*interval;
                    longitude -= 0.0002*interval;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Dodanie możliwości poprawnego zamknięcia sesji
        Runtime.getRuntime().addShutdownHook(new Thread(kSession::halt));

    }
}