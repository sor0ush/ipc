package fornaio;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FornaioTest {


  public static final AtomicInteger criticalSectionCounter = new AtomicInteger(0);

  /**
   * Il codice che viene eseguito per la sezione critica di un thread di un certo pid
   */
  static private final IntConsumer sezioneCritica = (int pid) -> {

    int numeroThreadInSezioneCritica = criticalSectionCounter.incrementAndGet();
    System.out.printf("Numero Thread in sezione critica: %d\n", numeroThreadInSezioneCritica);

    if (numeroThreadInSezioneCritica
        != 1) {// SE SUCCEDE QUESTO VUOL DIRE CHE L'ALGORITMO NON E' CORRETTO.
      System.err.printf("ERRORE. Numero Thread in sezione critica: %d\n",
          numeroThreadInSezioneCritica);
    }

    try {
      int delay = ThreadLocalRandom.current().nextInt(300);
      System.out.printf("PID: %d. Aspettiamo %dms per creare race condition.\n", pid, delay);
      Thread.sleep(delay);
    } catch (Exception ex) {
    }

    System.out.printf("SEZIONE CRITICA per PID=%d\n", pid);
    criticalSectionCounter.decrementAndGet();
  };


  public static void main(String[] args) {
    final int N = 100;
    Fornaio fornaio = new Fornaio(N);
    List<Thread> threads = IntStream.range(0, N)
        .mapToObj((pid) -> new Thread(() -> { //Runnable del thread
              fornaio.setPid(pid);
              fornaio.eseguiSezioneCritica(() -> sezioneCritica.accept(pid));// la sezione critica
            })
        ).collect(Collectors.toList());

    System.out.printf("Abbiamo creato %d thread. Iniziamo!", N);

    threads.stream()
        .forEachOrdered(Thread::start);
  }
}
