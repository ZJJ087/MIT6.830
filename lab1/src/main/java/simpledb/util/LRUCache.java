package simpledb.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache<K,V> {
    public class DLinkedNode {
        public K key;
        public V value;
        public DLinkedNode prev;
        public DLinkedNode next;
        public DLinkedNode() {}
        public DLinkedNode(K _key, V _value) {key = _key; value = _value;}

    }

    private Map<K, DLinkedNode> cache = new ConcurrentHashMap<K, DLinkedNode>();
    private int size;
    private int capacity;
    private DLinkedNode head, tail;

    public LRUCache(int capacity) {
        this.size = 0;
        this.capacity = capacity;
        head = new DLinkedNode();
        tail = new DLinkedNode();
        head.next = tail;
        head.prev = tail;
        tail.prev = head;
        tail.next = head;
    }


    public int getSize() {
        return size;
    }

    public DLinkedNode getHead() {
        return head;
    }

    public DLinkedNode getTail() {
        return tail;
    }

    public Map<K, DLinkedNode> getCache() {
        return cache;
    }
    public synchronized V get(K key) {
        DLinkedNode node = cache.get(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);
        return node.value;
    }
    public synchronized void remove(DLinkedNode node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
        cache.remove(node.key);
        size--;
    }

    public synchronized void discard(){
        DLinkedNode tail = removeTail();
        cache.remove(tail.key);
        size--;
    }
    public synchronized void put(K key, V value) {
        DLinkedNode node = cache.get(key);
        if (node == null) {
            DLinkedNode newNode = new DLinkedNode(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
            ++size;
        } else {
            node.value = value;
            moveToHead(node);
        }
    }

    private void addToHead(DLinkedNode node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(DLinkedNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }


    private void moveToHead(DLinkedNode node) {
        removeNode(node);
        addToHead(node);
    }

    private DLinkedNode removeTail() {
        DLinkedNode res = tail.prev;
        removeNode(res);
        return res;
    }

}