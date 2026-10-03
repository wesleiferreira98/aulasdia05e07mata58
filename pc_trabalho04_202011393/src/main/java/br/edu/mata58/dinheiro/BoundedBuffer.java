package src.main.java.br.edu.mata58.dinheiro;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Buffer FIFO: vagas/itens reservam disponibilidade; mutex protege o estado. */
public final class BoundedBuffer {
    public record Snapshot(List<Integer> slots, int size, int nextWrite, int nextRead,
                           long produced, long consumed) {}
    private final int[] items;
    private final Semaphore empty, full = new Semaphore(0), mutex = new Semaphore(1);
    private int head, tail, size;
    private long produced, consumed;

    public BoundedBuffer(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacidade deve ser positiva");
        items = new int[capacity];
        empty = new Semaphore(capacity);
    }

    public boolean offer(int value, long timeoutMillis) throws InterruptedException {
        if (value <= 0) throw new IllegalArgumentException("Item deve ser positivo");
        if (!empty.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) return false;
        boolean locked = false, inserted = false;
        try {
            mutex.acquire();
            locked = true;
            items[tail] = value;
            tail = (tail + 1) % items.length;
            size++;
            produced++;
            inserted = true;
        } finally {
            if (locked) mutex.release();
            // Devolve a reserva se interrompido antes da insercao.
            if (inserted) full.release(); else empty.release();
        }
        return true;
    }

    public Integer poll(long timeoutMillis) throws InterruptedException {
        if (!full.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) return null;
        boolean locked = false, removed = false;
        int value;
        try {
            mutex.acquire();
            locked = true;
            value = items[head];
            items[head] = 0;
            head = (head + 1) % items.length;
            size--;
            consumed++;
            removed = true;
        } finally {
            if (locked) mutex.release();
            if (removed) empty.release(); else full.release();
        }
        return value;
    }

    public Snapshot snapshot() {
        mutex.acquireUninterruptibly();
        try {
            List<Integer> slots = new ArrayList<>();
            for (int item : items) slots.add(item);
            return new Snapshot(List.copyOf(slots), size, tail, head, produced, consumed);
        } finally { mutex.release(); }
    }
}
