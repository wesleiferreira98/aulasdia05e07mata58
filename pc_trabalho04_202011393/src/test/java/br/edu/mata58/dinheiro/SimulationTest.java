package br.edu.mata58.dinheiro;

import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Testes sem janela nem dependencias externas. */
public final class SimulationTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static void until(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) throw new AssertionError("Tempo limite");
            Thread.sleep(5);
        }
    }
    public static void main(String[] args) throws Exception {
        BoundedBuffer buffer = new BoundedBuffer(10);
        check(buffer.poll(0) == null, "Buffer inicialmente vazio");
        for (int i = 1; i <= 10; i++) check(buffer.offer(i, 0), "Dez vagas reais");
        check(!buffer.offer(11, 0), "Nao ultrapassar capacidade");
        for (int i = 1; i <= 10; i++) check(buffer.poll(0) == i, "FIFO");
        check(buffer.snapshot().size() == 0, "Buffer vazio apos consumo");
        for (int i = 1; i <= 25; i++) {
            check(buffer.offer(i, 0), "Reutiliza indice circular");
            check(buffer.poll(0) == i, "FIFO apos dar volta");
        }
        // Interromper quem espera disponibilidade nao deve inventar permissoes.
        Thread blocked = new Thread(() -> {
            try { buffer.poll(10000); throw new AssertionError("Deveria interromper"); }
            catch (InterruptedException expected) { Thread.currentThread().interrupt(); }
        });
        blocked.start(); blocked.interrupt(); blocked.join(1000);
        check(!blocked.isAlive(), "Interrupcao encerra espera");
        check(buffer.offer(100, 0) && buffer.poll(0) == 100, "Buffer segue utilizavel");

        BoundedBuffer concurrent = new BoundedBuffer(10);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<?> producer = workers.submit(() -> {
                try {
                    for (int i = 1; i <= 3000; i++)
                        check(concurrent.offer(i, 2000), "Produtor nao progride");
                } catch (InterruptedException e) { throw new RuntimeException(e); }
            });
            Future<?> consumer = workers.submit(() -> {
                try {
                    for (int i = 1; i <= 3000; i++) {
                        Integer value = concurrent.poll(2000);
                        check(value != null && value == i, "FIFO concorrente");
                    }
                } catch (InterruptedException e) { throw new RuntimeException(e); }
            });
            producer.get(10, TimeUnit.SECONDS); consumer.get(10, TimeUnit.SECONDS);
            check(concurrent.snapshot().size() == 0, "Sem itens perdidos");
        } finally { workers.shutdownNow(); }

        try (Simulation simulation = new Simulation()) {
            simulation.rates(10, 0.2); simulation.start();
            until(() -> simulation.view().buffer().size() == 10);
            until(() -> simulation.view().producerStatus().contains("Aguardando vaga"));
            simulation.pause();
            var paused = simulation.view().buffer();
            Thread.sleep(200);
            check(paused.equals(simulation.view().buffer()), "Pausa deve congelar estado");
            simulation.reset();
            check(simulation.view().buffer().produced() == 0, "Reset zera contadores");
            simulation.rates(0.2, 10); simulation.start();
            until(() -> simulation.view().consumerStatus().contains("Aguardando item"));
            simulation.pause(); simulation.start(); simulation.close();
            check(simulation.awaitTermination(1000), "Threads devem encerrar");
        }
        System.out.println("OK: capacidade, FIFO, concorrencia, interrupcao, cheio/vazio, pausa, reset e encerramento");
    }
}
