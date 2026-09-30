LingKeyIME 0.3.0 — GitHub 上傳版

請把本資料夾「裡面的所有內容」上傳到 GitHub Repository 的 main 分支，不要再多包一層 LingKeyIME-GitHub-0.3.0 資料夾。

重要：.github 是隱藏資料夾，裡面有：
.github/workflows/build-apk.yml

如果你的手機檔案管理器看不到 .github，請在 GitHub 網頁用 Add file -> Create new file，建立：
.github/workflows/build-apk.yml
再把 ZIP 裡同名檔案的內容貼進去。

正確結構：
.github/workflows/build-apk.yml
app/build.gradle
app/src/main/AndroidManifest.xml
app/src/main/java/com/example/lingkeyime/LingKeyInputMethodService.java
app/src/main/res/xml/method.xml
app/src/main/res/values/strings.xml
build.gradle
gradle.properties
settings.gradle
