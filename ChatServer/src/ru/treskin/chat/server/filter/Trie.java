package ru.treskin.chat.server.filter;

import java.util.HashMap;
import java.util.Map;

public class Trie {
    private final Node root;
    private int size;
    public Trie() {
        root = new Node();
        size = 0;
    }
    public int insert(String s) {
        Node cur = root;
        for (char c : s.toCharArray()) {
            if (!cur.nodes.containsKey(c)) cur.nodes.put(c, new Node());
            cur = cur.nodes.get(c);
        }
        cur.setEndPoint(true, size);
        return size++;
    }
    public int hasWord(String s) {
        Node cur = root;
        for (char c : s.toCharArray()) {
            if (!cur.nodes.containsKey(c)) return -1;
            cur = cur.nodes.get(c);
        }
        return cur.isEndOfWord() ? cur.getIndex() : -1;
    }
}
class Node {
    public Map<Character, Node> nodes;
    private boolean isEnd;
    private int index;
    protected Node() {
        nodes = new HashMap<>();
        isEnd = false;
        index = -1;
    }
    public void setEndPoint(boolean flg, int index) {
        this.isEnd = flg;
        this.index = index;
    }
    public boolean isEndOfWord() {
        return isEnd;
    }
    public int getIndex() {
        return index;
    }
}