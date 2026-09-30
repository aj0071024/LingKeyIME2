package com.example.lingkeyime;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.icu.text.Transliterator;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class LingKeyInputMethodService extends InputMethodService {

    private KeyboardView keyboard;

    @Override
    public View onCreateInputView() {
        keyboard = new KeyboardView();
        return keyboard;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        if (keyboard != null) {
            keyboard.resetForNewInput();
        }
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);

        if (keyboard == null) {
            keyboard = new KeyboardView();
            setInputView(keyboard);
        }

        keyboard.resetForNewInput();
        keyboard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        if (keyboard != null) {
            keyboard.resetForNewInput();
        }

        super.onFinishInputView(finishingInput);
    }

    class KeyboardView extends LinearLayout {

        LinearLayout candidates;
        LinearLayout keys;

        StringBuilder code = new StringBuilder();

        boolean simplified = false;
        boolean english = false;

        Transliterator tradToSimp;
        Transliterator simpToTrad;

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

        final LinkedHashMap<String, String[]> dict =
                new LinkedHashMap<>();

        KeyboardView() {
            super(LingKeyInputMethodService.this);

            setOrientation(VERTICAL);
            setPadding(dp(6), dp(6), dp(6), dp(6));
            setBackgroundColor(Color.rgb(238, 238, 243));

            candidates = new LinearLayout(
                    LingKeyInputMethodService.this
            );

            candidates.setGravity(Gravity.CENTER_VERTICAL);

            addView(
                    candidates,
                    new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            dp(48)
                    )
            );

            keys = new LinearLayout(
                    LingKeyInputMethodService.this
            );

            keys.setOrientation(VERTICAL);

            addView(
                    keys,
                    new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                    )
            );

            initDict();
            initTransliteratorSafely();

            rebuild();
        }

        int dp(int value) {
            return (int) (
                    value *
                    getResources()
                            .getDisplayMetrics()
                            .density
            );
        }

        void initTransliteratorSafely() {

            try {

                tradToSimp =
                        Transliterator.getInstance(
                                "Traditional-Simplified"
                        );

                simpToTrad =
                        Transliterator.getInstance(
                                "Simplified-Traditional"
                        );

            } catch (Throwable ignored) {

                tradToSimp = null;
                simpToTrad = null;
            }
        }

        void resetForNewInput() {

            code.setLength(0);

            english = false;

            rebuild();
        }

        void initDict() {

            put("ㄋㄧ", "你", "呢", "泥");
            put("ㄏㄠ", "好", "號", "毫");

            put("ㄓㄨㄥ", "中", "終", "鐘", "忠");
            put("ㄨㄣ", "文", "聞", "蚊", "紋");

            put("ㄊㄧㄢ", "天", "田", "甜", "填");
            put("ㄨㄛ", "我", "握", "窩");

            put("ㄉㄜ", "的", "得", "德");
            put("ㄕ", "是", "時", "事", "十");

            put("ㄕㄥ", "生", "聲", "升", "省");
            put("ㄔㄢ", "產", "纏", "禪");

            put("ㄨㄌㄧㄠ", "物料", "物料表");
            put("ㄑㄧㄥㄍㄡ", "請購", "請購單");

            put("ㄑㄩㄝㄌㄧㄠ", "缺料", "缺料表");
            put("ㄅㄟㄌㄧㄠ", "備料", "備料數量");

            put("ㄍㄨㄥㄉㄢ", "工單", "工單號");
            put("ㄕㄥㄔㄢ", "生產", "生產線");

            put("ㄘㄞㄌㄧㄠ", "材料", "材料表");
            put("ㄘㄞㄍㄡ", "採購", "採購單");

            put("ㄍㄨㄢㄌㄧ", "管理", "管理表");
            put("ㄓㄨㄓㄨㄢ", "主管", "主管會議");

            put("ㄏㄨㄟㄅㄠ", "彙報", "彙報資料");
            put("ㄕㄨㄐㄩ", "數據", "數據分析");

            put("ㄊㄧㄌㄧㄢ", "提料", "提料單");
            put("ㄅㄨㄌㄧㄠ", "補料", "補料單");

            put("ㄐㄧㄢㄧㄢ", "檢驗", "檢驗報告");
            put("ㄆㄧㄣㄓㄧ", "品質", "品質管理");
        }

        void put(String key, String... values) {
            dict.put(key, values);
        }

        TextView button(String text) {

            TextView t =
                    new TextView(
                            LingKeyInputMethodService.this
                    );

            t.setText(text);
            t.setGravity(Gravity.CENTER);

            t.setTextSize(
                    text.length() > 3 ? 13 : 18
            );

            t.setTextColor(Color.DKGRAY);

            t.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
            );

            t.setMinHeight(dp(44));

            GradientDrawable g =
                    new GradientDrawable();

            g.setColor(Color.WHITE);
            g.setCornerRadius(dp(10));

            t.setBackground(g);

            return t;
        }

        String normalize(String text) {

            return text
                    .replace("ˊ", "")
                    .replace("ˇ", "")
                    .replace("ˋ", "")
                    .replace("˙", "");
        }

        String convert(String text) {

            if (text == null || text.isEmpty()) {
                return text;
            }

            try {

                if (simplified && tradToSimp != null) {
                    return tradToSimp.transliterate(text);
                }

                if (!simplified && simpToTrad != null) {
                    return simpToTrad.transliterate(text);
                }

            } catch (Throwable ignored) {
            }

            return text;
        }

        void rebuild() {

            candidates.removeAllViews();

            String[] cs =
                    suggestions(code.toString());

            for (String c : cs) {

                if (c == null || c.isEmpty()) {
                    continue;
                }

                final String out = convert(c);

                TextView t = button(out);

                t.setTextSize(18);

                t.setOnClickListener(
                        v -> commit(out)
                );

                candidates.addView(
                        t,
                        new LinearLayout.LayoutParams(
                                0,
                                LayoutParams.MATCH_PARENT,
                                1
                        )
                );
            }

            keys.removeAllViews();

            String[][] rows =
                    english ? englishRows : zhuyinRows;

            for (String[] row : rows) {

                LinearLayout r =
                        new LinearLayout(
                                LingKeyInputMethodService.this
                        );

                r.setGravity(Gravity.CENTER);

                r.setPadding(
                        dp(1),
                        dp(2),
                        dp(1),
                        dp(2)
                );

                for (String k : row) {

                    TextView t = button(k);

                    t.setOnClickListener(
                            v -> press(k)
                    );

                    LinearLayout.LayoutParams p =
                            new LinearLayout.LayoutParams(
                                    0,
                                    dp(48),
                                    1
                            );

                    p.setMargins(
                            dp(2),
                            dp(1),
                            dp(2),
                            dp(1)
                    );

                    r.addView(t, p);
                }

                keys.addView(
                        r,
                        new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                dp(50)
                        )
                );
            }

            requestLayout();
        }

        void press(String k) {

            // 中文／英文切換不需要 InputConnection
            if (k.equals("中/英")) {

                english = !english;

                code.setLength(0);

                rebuild();

                return;
            }

            // 繁體／簡體切換
            if (k.equals("繁/簡")) {

                toggleConversion();

                return;
            }

            if (k.equals("下一頁")) {
                return;
            }

            InputConnection ic =
                    getCurrentInputConnection();

            if (ic == null) {
                return;
            }

            // Backspace
            if (k.equals("⌫")) {

                if (code.length() > 0) {

                    code.deleteCharAt(
                            code.length() - 1
                    );

                    rebuild();

                } else {

                    ic.deleteSurroundingText(1, 0);
                }

                return;
            }

            // 空白
            if (k.equals("空白")) {

                if (!english &&
                        code.length() > 0) {

                    String[] s =
                            suggestions(
                                    code.toString()
                            );

                    if (s.length > 0 &&
                            s[0] != null &&
                            !s[0].isEmpty()) {

                        commit(s[0]);
                    }
                }

                ic.commitText(" ", 1);

                code.setLength(0);

                rebuild();

                return;
            }

            // Enter
            if (k.equals("↵")) {

                if (!english &&
                        code.length() > 0) {

                    String[] s =
                            suggestions(
                                    code.toString()
                            );

                    if (s.length > 0) {

                        commit(s[0]);
                    }
                }

                ic.sendKeyEvent(
                        new android.view.KeyEvent(
                                android.view.KeyEvent.ACTION_DOWN,
                                android.view.KeyEvent.KEYCODE_ENTER
                        )
                );

                ic.sendKeyEvent(
                        new android.view.KeyEvent(
                                android.view.KeyEvent.ACTION_UP,
                                android.view.KeyEvent.KEYCODE_ENTER
                        )
                );

                code.setLength(0);

                rebuild();

                return;
            }

            // 標點
            if (k.equals("，") ||
                    k.equals("。") ||
                    k.equals("？")) {

                ic.commitText(k, 1);

                return;
            }

            // 英文
            if (english) {

                ic.commitText(k, 1);

                return;
            }

            // 注音
            code.append(k);

            rebuild();
        }

        void toggleConversion() {

            simplified = !simplified;

            InputConnection ic =
                    getCurrentInputConnection();

            if (ic != null) {

                CharSequence before =
                        ic.getTextBeforeCursor(
                                1000,
                                0
                        );

                if (before != null &&
                        before.length() > 0) {

                    String old =
                            before.toString();

                    String converted =
                            convert(old);

                    if (!old.equals(converted)) {

                        ic.deleteSurroundingText(
                                old.length(),
                                0
                        );

                        ic.commitText(
                                converted,
                                1
                        );
                    }
                }
            }

            rebuild();
        }

        void commit(String text) {

            InputConnection ic =
                    getCurrentInputConnection();

            if (ic != null) {

                ic.commitText(
                        convert(text),
                        1
                );
            }

            code.setLength(0);

            rebuild();
        }

        String[] suggestions(String c) {

            if (c.isEmpty()) {

                return new String[]{
                        "你好",
                        "今天",
                        "我",
                        "的",
                        "是"
                };
            }

            String n = normalize(c);

            if (dict.containsKey(n)) {

                return dict.get(n);
            }

            if (dict.containsKey(c)) {

                return dict.get(c);
            }

            return new String[]{
                    c,
                    "？"
            };
        }
    }
}
