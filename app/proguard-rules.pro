-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontoptimize
-dontpreverify
-dontwarn org.jaxen.**
-dontwarn java.awt.**
-dontwarn javax.xml.**
-dontwarn org.codehaus.**
-dontwarn java.nio.**
-dontwarn javax.swing.**
-dontwarn junit.swingui.**
-dontwarn junit.awtui.**
-dontwarn android.test.**
-dontwarn com.squareup.**
-ignorewarnings

-keepattributes *Annotation*

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-dontwarn android.support.**
