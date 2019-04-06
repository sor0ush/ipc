package fornaio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Fornaio {

  final private int numeroDiProcessi;
  private volatile List<Integer> numero;
  private volatile List<Boolean> scelta;

  /**
   * Questi due non sono previsti dall'algoritmo. Gli ho interrotto a pervenire il fatto
   * che uno assegni pid per un thread piu' di una volta.
   * Utilizzare Metodi di syncronizzazione di Java per implementare l'algoritmo fornaio
   * non e' ammissibile pero' in questo caso sono usati esclusivamente per tenere traccia
   * dei thread Java e incapsulamento corretto della classe.
   */
  private ThreadLocal<Integer> pidThreadLocal = new ThreadLocal<>();
  private Map<Integer, Long> mappaPidAThreadId = new ConcurrentHashMap<>();

  public Fornaio(int threadNumbers) {
    this.numeroDiProcessi = threadNumbers;
    this.numero = new ArrayList<>(this.numeroDiProcessi);
    this.scelta = new ArrayList<>(this.numeroDiProcessi);
    for (int i = 0; i < this.numeroDiProcessi; i++) {
      numero.add(0);
      scelta.add(false);
    }
  }


  private void lock(int pid)  // thread ID
  {
    scelta.set(pid, true);

    int max = numero.stream().max(Integer::compareTo).get();

    numero.set(pid, 1 + max);
    scelta.set(pid, false);

    for (int i = 0; i < numero.size(); ++i) {
      if (i != pid) {
        while (scelta.get(i)) {
          Thread.yield();
        }
        while (numero.get(i) != 0 && (numero.get(pid) > numero.get(i) ||
            (numero.get(pid) == numero.get(i) && pid > i))) {
          Thread.yield();
        }
      }
    }
  }

  private void unlock(int pid){
    numero.set(pid, 0);
  }

  public List<Integer> getNumero(){
    return Collections.unmodifiableList(this.numero);
  }

  public List<Boolean> getScelta(){
    return Collections.unmodifiableList(this.scelta);
  }

  public void setPid(int pid){
    if(pid >= numeroDiProcessi){
      throw new IllegalArgumentException("pid deve essere fra 0 e numeroDiProcessi: "+ numeroDiProcessi);
    }
    if(pidThreadLocal.get() != null){
      throw new IllegalStateException("Non puoi assegnare pid piu' di una volta.");
    }
    mappaPidAThreadId.putIfAbsent(pid, Thread.currentThread().getId());
    var thIdperPid = mappaPidAThreadId.getOrDefault(pid, null);
    if(!Objects.equals(Thread.currentThread().getId(), Thread.currentThread().getId())){
      throw new IllegalStateException("Questo pid e' gia' assegnata ad un altro thread.");
    }
    pidThreadLocal.set(pid);
  }

  public void eseguiSezioneCritica(Runnable sezioneCritica){
    Integer pid = pidThreadLocal.get();
    if(pid == null){
      throw new IllegalStateException("Non hai valorizzato pid per thread.");
    }
    lock(pid);
    sezioneCritica.run();
    unlock(pid);
  }

}
