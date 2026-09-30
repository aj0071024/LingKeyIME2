package com.example.lingkeyime;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.icu.text.Transliterator;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.*;

/** 靈鍵中文輸入法 0.3.0：注音、候選、繁簡、中英、常用工作詞，完全離線。 */
public class LingKeyInputMethodService extends InputMethodService {
    KeyboardView keyboard;
    @Override public View onCreateInputView() { keyboard = new KeyboardView(); return keyboard; }

    class KeyboardView extends LinearLayout {
        LinearLayout candidates, keys;
        StringBuilder code = new StringBuilder();
        boolean simplified = false;
        boolean english = false;
        final Transliterator tradToSimp = Transliterator.getInstance("Traditional-Simplified");
        final Transliterator simpToTrad = Transliterator.getInstance("Simplified-Traditional");

        final String[][] zhuyinRows = {
            {"ㄅ","ㄆ","ㄇ","ㄈ","ㄉ","ㄊ","ㄋ","ㄌ","⌫"},
            {"ㄍ","ㄎ","ㄏ","ㄐ","ㄑ","ㄒ","ㄓ","ㄔ","ㄕ"},
            {"ㄖ","ㄗ","ㄘ","ㄙ","ㄧ","ㄨ","ㄩ","ˊ","ˇ"},
            {"ˋ","˙","ㄚ","ㄛ","ㄜ","ㄝ","ㄞ","ㄟ","ㄠ"},
            {"ㄡ","ㄢ","ㄣ","ㄤ","ㄥ","ㄦ","，","。","？"},
            {"中/英","空白","↵","繁/簡","下一頁"}
        };
        final String[][] englishRows = {
            {"q","w","e","r","t","y","u","i","o","p"},
            {"a","s","d","f","g","h","j","k","l","⌫"},
            {"z","x","c","v","b","n","m","，","。","？"},
            {"中/英","空白","↵","繁/簡"}
        };

        final LinkedHashMap<String,String[]> dict = new LinkedHashMap<>();
        final HashMap<String,String> zhuyin = new HashMap<>();

        KeyboardView() {
            super(LingKeyInputMethodService.this);
            setOrientation(VERTICAL); setPadding(6,6,6,6); setBackgroundColor(Color.rgb(238,238,243));
            candidates = new LinearLayout(LingKeyInputMethodService.this); candidates.setGravity(Gravity.CENTER_VERTICAL);
            addView(candidates,new LinearLayout.LayoutParams(-1,56));
            keys = new LinearLayout(LingKeyInputMethodService.this); keys.setOrientation(VERTICAL);
            addView(keys,new LinearLayout.LayoutParams(-1,0,1));
            initDict(); rebuild();
        }

        void initDict() {
            put("ㄋㄧ", "你","呢","泥"); put("ㄏㄠ", "好","號","毫");
            put("ㄓㄨㄥ", "中","終","鐘","忠"); put("ㄨㄣ", "文","聞","蚊","紋");
            put("ㄊㄧㄢ", "天","田","甜","填"); put("ㄨㄛ", "我","握","窩");
            put("ㄉㄜ", "的","得","德"); put("ㄕ", "是","時","事","十");
            put("ㄕㄥ", "生","聲","升","省"); put("ㄔㄢ", "產","纏","禪");
            put("ㄨㄌㄧㄠ", "物料","物料表"); put("ㄑㄧㄥㄍㄡ", "請購","請購單");
            put("ㄑㄩㄝㄌㄧㄠ", "缺料","缺料表"); put("ㄅㄟㄌㄧㄠ", "備料","備料數量");
            put("ㄍㄨㄥㄉㄢ", "工單","工單號"); put("ㄕㄥㄔㄢ", "生產","生產線");
            put("ㄘㄞㄌㄧㄠ", "材料","材料表"); put("ㄘㄞㄍㄡ", "採購","採購單");
            put("ㄍㄨㄢㄌㄧ", "管理","管理表"); put("ㄓㄨㄓㄨㄢ", "主管","主管會議");
            put("ㄏㄨㄟㄅㄠ", "彙報","彙報資料"); put("ㄕㄨㄐㄩ", "數據","數據分析");
            put("ㄊㄧㄌㄧㄢ", "提料","提料單"); put("ㄅㄨㄌㄧㄠ", "補料","補料單");
            put("ㄐㄧㄢㄧㄢ", "檢驗","檢驗報告"); put("ㄆㄧㄣㄓㄧ", "品質","品質管理");
        }
        void put(String k,String... v){dict.put(k,v);}

        TextView button(String s) {
            TextView t=new TextView(LingKeyInputMethodService.this); t.setText(s); t.setGravity(Gravity.CENTER);
            t.setTextSize(s.length()>3?13:18); t.setTextColor(Color.DKGRAY); t.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
            GradientDrawable g=new GradientDrawable(); g.setColor(Color.WHITE); g.setCornerRadius(10); t.setBackground(g); return t;
        }
        String normalize(String s){return s.replace("ˊ","").replace("ˇ","").replace("ˋ","").replace("˙","");}
        String convert(String s){ if(s==null||s.isEmpty())return s; return simplified?tradToSimp.transliterate(s):simpToTrad.transliterate(s); }

        void rebuild(){
            candidates.removeAllViews();
            String[] cs=suggestions(code.toString());
            for(String c:cs){ final String out=convert(c); if(out.isEmpty())continue; TextView t=button(out); t.setTextSize(18); t.setOnClickListener(v->commit(out)); candidates.addView(t,new LinearLayout.LayoutParams(0,-1,1)); }
            keys.removeAllViews(); String[][] rows=english?englishRows:zhuyinRows;
            for(String[] row:rows){ LinearLayout r=new LinearLayout(LingKeyInputMethodService.this); r.setGravity(Gravity.CENTER); for(String k:row){ TextView t=button(k); t.setOnClickListener(v->press(k)); r.addView(t,new LinearLayout.LayoutParams(0,0,1)); } keys.addView(r,new LinearLayout.LayoutParams(-1,0,1)); }
        }

        void press(String k){
            InputConnection ic=getCurrentInputConnection(); if(ic==null)return;
            if(k.equals("⌫")){ if(code.length()>0){code.deleteCharAt(code.length()-1);rebuild();} else ic.deleteSurroundingText(1,0); return; }
            if(k.equals("中/英")){ english=!english; code.setLength(0); rebuild(); return; }
            if(k.equals("繁/簡")){ toggleConversion(); return; }
            if(k.equals("空白")){ if(!english && code.length()>0) commit(suggestions(code.toString())[0]); ic.commitText(" ",1); code.setLength(0); rebuild(); return; }
            if(k.equals("↵")){ if(!english && code.length()>0) commit(suggestions(code.toString())[0]); ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_ENTER)); ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,android.view.KeyEvent.KEYCODE_ENTER)); code.setLength(0);rebuild();return; }
            if(k.equals("下一頁")){ return; }
            if(k.equals("，")||k.equals("。")||k.equals("？")){ ic.commitText(k,1); return; }
            if(english){ic.commitText(k,1);return;}
            code.append(k); rebuild();
        }

        void toggleConversion(){ simplified=!simplified; InputConnection ic=getCurrentInputConnection(); if(ic!=null){CharSequence b=ic.getTextBeforeCursor(1000,0); if(b!=null&&b.length()>0){String old=b.toString(),n=simplified?tradToSimp.transliterate(old):simpToTrad.transliterate(old); if(!old.equals(n)){ic.deleteSurroundingText(old.length(),0);ic.commitText(n,1);}}} rebuild(); }
        void commit(String s){InputConnection ic=getCurrentInputConnection(); if(ic!=null){ic.commitText(convert(s),1);code.setLength(0);rebuild();}}

        String[] suggestions(String c){
            if(c.isEmpty()) return new String[]{"你好","今天","我","的","是"};
            String n=normalize(c); if(dict.containsKey(n)) return dict.get(n);
            if(dict.containsKey(c)) return dict.get(c);
            return new String[]{c,"？",""};
        }
    }
}
