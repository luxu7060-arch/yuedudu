# Keep Jsoup and OkHttp models
-keep class org.jsoup.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.ao3reader.app.data.model.** { *; }
