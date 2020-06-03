package com.example.words;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "english_word")
    private String word;
    @ColumnInfo(name = "chinese_meaning")
    private String chineseMeaning;
    @ColumnInfo(name = "chinese_invisible")
    private  Boolean chineseInvisible;

    public Boolean getChineseInvisible() {
        return chineseInvisible;
    }

    public void setChineseInvisible(Boolean chineseInvisible) {
        this.chineseInvisible = chineseInvisible;
    }

    public Word(String word, String chineseMeaning) {
        this.word = word;
        this.chineseMeaning = chineseMeaning;
        this.setChineseInvisible(false);
    }

    public int getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getChineseMeaning() {
        return chineseMeaning;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setChineseMeaning(String chineseMeaning) {
        this.chineseMeaning = chineseMeaning;
    }
}
