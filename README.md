# 靈鍵中文輸入法 0.3.0

不用 Android Studio 也能建 APK。

## 0.3 功能
- 台灣注音鍵盤
- 注音候選字／常用詞
- 中英文切換
- 繁體／簡體切換
- 離線繁簡轉換（Android ICU）
- 工作常用詞：物料、請購、缺料、備料、工單、生產、材料、採購、品質等
- Android 8.0+（API 26）

## 最簡單 APK 建置法：GitHub Actions
1. 到 github.com 登入或註冊帳號。
2. 建立一個新的空白 Repository，例如 `LingKeyIME`。
3. 把這個資料夾內的所有檔案上傳到 Repository 的 `main` 分支。
4. 點上方 `Actions` → `Build APK` → `Run workflow`。
5. 等待工作完成後，進入該次 workflow，往下找到 `Artifacts`。
6. 下載 `LingKeyIME-0.3.0-debug`，解壓縮即可得到 `app-debug.apk`。
7. 把 APK 傳到 POCO，點擊安裝；若手機提示禁止安裝，允許目前使用的瀏覽器／檔案管理員「安裝未知應用程式」。
8. 到「設定 → 其他設定／更多設定 → 語言與輸入法 → 螢幕鍵盤／目前鍵盤 → 管理鍵盤」，開啟「靈鍵中文輸入法」，再切換到它。

## 注意
這是可實際安裝的 debug 測試版。首次使用請在手機上確認輸入法權限與系統相容性。
