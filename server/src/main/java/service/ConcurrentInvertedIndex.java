package service;

import domain.Posting;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiConsumer;

public class ConcurrentInvertedIndex implements InvertedIndex {
    private final PostingUpdater updater = new PostingUpdater();
    private final AtomicInteger size = new AtomicInteger(0);
    private volatile int capacity;
    private final float loadFactor;
    private volatile AtomicReferenceArray<Node> bucket;
    private static final int MINIMUM_CAPACITY = 16;
    private static final float MINIMUM_LOAD_FACTOR = 0.3F;
    private static final float DEFAULT_LOAD_FACTOR = 0.75F;
    private static final int HASH_BITS = 0x7fffffff;//to make hashcode positive as proton!


    public ConcurrentInvertedIndex(int capacity, float loadFactor) {
        if (capacity < MINIMUM_CAPACITY || loadFactor < MINIMUM_LOAD_FACTOR) {
            String msg = STR. "MINIMUM_CAPACITY: \{ MINIMUM_CAPACITY }. MINIMUM_LOAD_FACTOR: \{ MINIMUM_LOAD_FACTOR }" ;
            throw new IllegalArgumentException(msg);
        }
        this.loadFactor = loadFactor;
        this.capacity = tableSizeFor(capacity);
        bucket = new AtomicReferenceArray<>(this.capacity);
    }

    public ConcurrentInvertedIndex(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * @link https://stackoverflow.com/questions/9249983/hashcode-giving-negative-values#:~:text=It%20is%20perfectly%20legal%20to,use%20a%20shift%20mask%20(key.
     * @see java.util.concurrent.ConcurrentHashMap#spread(int) ConcurrentHashMap.spread()
     */
    private static int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    @Override
    public List<Posting> get(String key) {
        return List.of();
    }

    @Override
    public int size() {
        return size.get();
    }

    private final Object resizeMonitor = new Object();

    @Override
    public void put(String key, List<Posting> value) {
        int hash = spread(key.hashCode());
        boolean stop = false;
        int futureSize = size.get() + 1;
        if (futureSize > capacity * loadFactor) {
            tryResize();
        }
        for (var table = bucket; !stop; table = bucket) {
            int index = hash % table.length();
            Node existingNode;
            if ((existingNode = table.get(index)) == null) {//if bucket is empty
                if (table.compareAndSet(index, null, new Node(hash, key, value))) {//try to set a head
                    size.incrementAndGet();
                    break;//success
                }
                continue;
            }
            if (existingNode == emptyNode) {
                synchronized (resizeMonitor) {
                    while (table == bucket) {
                        System.out.println(STR. "Wait: \{ Thread.currentThread().getName() }" );
                        waitResize();
                    }
                    System.out.println(STR. "Wait: \{ Thread.currentThread().getName() } released" );
                    continue;
                }
            }
            synchronized (existingNode) {
                if (existingNode == table.get(index) && table == bucket) {
                    for (var node = existingNode; ; ) {
                        if (node.key.equals(key)) {
                            updater.mergeSortedLists(node.value, value);
                            stop = true;
                            break;
                        }
                        var prev = node;
                        if ((node = node.next) == null) {
                            prev.next = new Node(hash, key, value); //create a new node in the list
                            size.incrementAndGet();
                            stop = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void waitResize() {
        try {
            resizeMonitor.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final Node emptyNode = new Node(-1, null, null);

    private synchronized void tryResize() {
        int futureSize = this.size.get() + 1;
        if (futureSize < capacity * loadFactor) {
            return;
        }
        int newCapacity = tableSizeFor((int) (1.0 + futureSize / loadFactor));
        AtomicReferenceArray<Node> newBucket = new AtomicReferenceArray<>(newCapacity);
        for (int i = 0; i < bucket.length(); i++) {
            if (bucket.compareAndSet(i, null, emptyNode)) {
                continue;
            }
            Node node = bucket.get(i);
            if (node == emptyNode) {
                continue;
            }
            synchronized (node) {
                for (; node != null; node = node.next) {
                    int newIndex = node.hash % newCapacity;
                    var newNode = new Node(node);
                    if (!newBucket.compareAndSet(newIndex, null, newNode)) {
                        Node next = newBucket.get(newIndex);
                        Node prev;
                        do {
                            prev = next;
                            next = next.next;
                        } while (next != null);
                        prev.next = newNode;
                    }
                }
            }
        }
        bucket = newBucket;
        capacity = newCapacity;
        notifyResized();
    }

    private void notifyResized() {
        synchronized (resizeMonitor) {
            resizeMonitor.notifyAll();
        }
    }

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * @see java.util.concurrent.ConcurrentHashMap#tableSizeFor(int) ConcurrentHashMap.tableSizeFor()
     */
    private static int tableSizeFor(int c) {
        int n = -1 >>> Integer.numberOfLeadingZeros(c - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    class Node {
        final int hash;
        final String key;
        volatile List<Posting> value;
        volatile Node next;

        public Node(int hash, String key, List<Posting> value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        public Node(Node src) {
            this(src.hash, src.key, src.value);
        }
    }

    @Override
    public void forEach(BiConsumer<String, List<Posting>> action) {
        var bucket = this.bucket;
        for (int i = 0; i < bucket.length(); i++) {
            Node node = bucket.get(i);
            while (node != null) {
                action.accept(node.key, node.value);
                node = node.next;
            }
        }
    }

    @Override
    public String toString() {
        return "ConcurrentInvertedIndex{" +
                "size=" + size +
                ", bucket=" + toString(bucket) +
                '}';
    }

    private String toString(AtomicReferenceArray<Node> bucket) {
        var builder = new StringBuilder();
        for (int i = 0; i < bucket.length(); i++) {
            Node node = bucket.get(i);
            builder.append(STR. "\{ i }-> [\{ toString(node) }]\n" );
        }
        return builder.toString();
    }

    private String toString(Node node) {
        var builder = new StringBuilder();
        for (; node != null; node = node.next) {
            builder.append(node.key);
            if (node.next != null) {
                builder.append(',');
            }
        }
        return builder.toString();
    }
}