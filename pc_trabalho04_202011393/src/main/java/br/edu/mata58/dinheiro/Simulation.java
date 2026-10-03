package src.main.java.br.edu.mata58.dinheiro;

import java.util.ArrayDeque;
import java.util.List;

/** Ciclo de vida cooperativo. Nunca suspende uma thread segurando um semaforo. */
public final class Simulation implements AutoCloseable {
    public enum State { READY, RUNNING, PAUSED, CLOSED }
    public record View(BoundedBuffer.Snapshot buffer, State state, String producerStatus,
                       String consumerStatus, List<String> events) {}
    private final Object gate = new Object();
    private BoundedBuffer buffer = new BoundedBuffer(10);
    private final ArrayDeque<String> events = new ArrayDeque<>();
    private State state = State.READY;
    private String producerStatus = "Pronto", consumerStatus = "Pronto";
    private double producerRate = 2, consumerRate = 2;
    private int nextItem = 1;
    private long generation;
    private final Thread producer = new Thread(() -> work(true), "produtor");
    private final Thread consumer = new Thread(() -> work(false), "consumidor");

    public Simulation() {
        producer.setDaemon(true);
        consumer.setDaemon(true);
        producer.start();
        consumer.start();
    }

    public void start() {
        synchronized (gate) {
            ensureOpen();
            if (state != State.RUNNING) {
                state = State.RUNNING;
                generation++;
                log("Simulacao em execucao");
                gate.notifyAll();
            }
        }
    }

    public void pause() {
        synchronized (gate) {
            ensureOpen();
            if (state == State.RUNNING) {
                state = State.PAUSED;
                generation++;
                producerStatus = consumerStatus = "Pausado";
                log("Pausa: buffer preservado");
                gate.notifyAll();
            }
        }
    }

    public void reset() {
        synchronized (gate) {
            ensureOpen();
            state = State.READY;
            generation++;
            buffer = new BoundedBuffer(10);
            nextItem = 1;
            producerStatus = consumerStatus = "Pronto";
            events.clear();
            log("Buffer reiniciado");
            gate.notifyAll();
        }
    }

    public void rates(double production, double consumption) {
        if (!Double.isFinite(production) || !Double.isFinite(consumption)
                || production < 0.2 || production > 10 || consumption < 0.2 || consumption > 10)
            throw new IllegalArgumentException("Velocidades entre 0.2 e 10 itens/s");
        synchronized (gate) {
            ensureOpen();
            producerRate = production;
            consumerRate = consumption;
            gate.notifyAll();
        }
    }

    private void work(boolean producing) {
        try {
            while (true) {
                synchronized (gate) {
                    while (state != State.RUNNING) {
                        if (state == State.CLOSED) return;
                        gate.wait();
                    }
                    boolean changed;
                    if (producing) {
                        changed = buffer.offer(nextItem, 0);
                        producerStatus = changed ? "Produzindo" : "Aguardando vaga: buffer cheio";
                        if (changed) log("Produziu nota #" + nextItem++);
                    } else {
                        Integer item = buffer.poll(0);
                        changed = item != null;
                        consumerStatus = changed ? "Consumindo" : "Aguardando item: buffer vazio";
                        if (changed) log("Consumiu nota #" + item);
                    }
                    if (!changed) {
                        gate.wait(); // Libera gate; outra thread pode alterar a disponibilidade.
                        continue;
                    }
                    gate.notifyAll();
                    // Notificacoes de disponibilidade nao encurtam o intervalo de velocidade.
                    double rate = producing ? producerRate : consumerRate;
                    long currentGeneration = generation;
                    long deadline = System.nanoTime() + (long)(1_000_000_000 / rate);
                    while (state == State.RUNNING && generation == currentGeneration) {
                        long remaining = deadline - System.nanoTime();
                        if (remaining <= 0) break;
                        gate.wait(Math.max(1, remaining / 1_000_000));
                    }
                }
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public View view() {
        synchronized (gate) {
            return new View(buffer.snapshot(), state, producerStatus, consumerStatus,
                            List.copyOf(events));
        }
    }

    private void log(String event) {
        events.addFirst(event);
        while (events.size() > 12) events.removeLast();
    }

    private void ensureOpen() {
        if (state == State.CLOSED) throw new IllegalStateException("Simulacao encerrada");
    }

    @Override public void close() {
        synchronized (gate) {
            state = State.CLOSED;
            producerStatus = consumerStatus = "Encerrado";
            gate.notifyAll();
        }
        producer.interrupt();
        consumer.interrupt();
    }

    public boolean awaitTermination(long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000;
        for (Thread thread : new Thread[]{producer, consumer}) {
            long remaining = deadline - System.nanoTime();
            if (remaining > 0) thread.join(Math.max(1, remaining / 1_000_000));
        }
        return !producer.isAlive() && !consumer.isAlive();
    }
}
