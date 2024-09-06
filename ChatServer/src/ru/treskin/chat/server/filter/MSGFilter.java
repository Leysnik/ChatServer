package ru.treskin.chat.server.filter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MSGFilter {
    private final List<String> badWords;
    private final Trie wordTrie = new Trie();
    public MSGFilter() {
        //change it next time
        badWords = readTxt("ChatServer/blacklist.txt");
        for (String s : badWords) wordTrie.insert(s);
    }
    private List<String> readTxt(String path)  {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return (reader == null ? new ArrayList() : reader.lines().toList());
    }
    public int checkWord(String word) {
        return wordTrie.hasWord(word);
    }
    public String wordAt(int index) {
        return badWords.get(index);
    }
}
