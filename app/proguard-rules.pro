# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-libraryjars libs/terminalSdk-2.0.3.jar
#-libraryjars libs/VisaSensoryBranding.aar
#-libraryjars libs/TTPKernelv21-prod-release-protected.aar
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,InnerClasses,Signature

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


#Specifies to repackage all packages that are renamed, by moving them into the single given parent package
#-flattenpackagehierarchy com.pa.pr

#Specifies to repackage all class files that are renamed, by moving them into the single given package. Without argument or with an empty string (''), the package is removed completely.
-repackageclasses prw
#-useuniqueclassmembernames
-keeppackagenames doNotKeepAThing

-adaptresourcefilenames **.properties,**.gif,**.jpg
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

#For example, if your code contains a large number of hard-coded strings that refer to classes, and you prefer not to keep their names, you may want to use this option
-adaptclassstrings

-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses

-allowaccessmodification

-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }

-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

-verbose

#Specifies to print any warnings about unresolved references and other important problems, but to continue processing in any case.
-ignorewarnings

#-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt

#-keepresourcefiles assets/*.*
-keepdirectories assets/**
#-keepresourcefiles res/*.*
-keepdirectories assets/**.pem
-keepdirectories assets/**.cer
-keepdirectories assets/**.json
#-keepresourcefiles drawable/*.*
-keepdirectories assets/**.der
-keepdirectories "MD5"

#-keep class com.visa.** { *; }
#-keepnames class com.visa.** { *; }

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove Android logging calls (in this case, including errors).R
-assumenosideeffects class android.util.Log {
   public static boolean isLoggable(java.lang.String, int);
   public static int v(...);
   public static int d(...);
   public static int i(...);
   public static int w(...);
   public static int e(...);
   public static int println(int, java.lang.String, java.lang.String);
   public static java.lang.String getStackTraceString(java.lang.Throwable);
   # Warning: removing terrible failure logging calls might change behavior
   #          for devices < API Level 23.
   public static int wtf(...);
 }

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  public static void checkNotNull(java.lang.Object);
  public static void checkNotNull(java.lang.Object, java.lang.String);
  public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
  public static void checkNotNullParameter(java.lang.Object, java.lang.String);
  public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  public static void throwUninitializedPropertyAccessException(java.lang.String);
}

-keep, allowobfuscation class com.payment.payrowapp.wizzit.*
-keepclassmembers, allowobfuscation class * {
    *;
}

#-keepnames class com.payment.payrowmobile.wizzit.PaymentActivity


-keep, allowobfuscation class com.payment.payrowapp.paymentlink.*

-keepnames class com.payment.payrowapp.paymentlink.PaymentLinkActivity
-keepclassmembernames class com.payment.payrowapp.paymentlink.PaymentLinkActivity {
    public <methods>;
    public <fields>;
    #!private *; also tried this but it didn't work
}

-keep, allowobfuscation class com.payment.payrowapp.utils.*

-keepnames class com.payment.payrowapp.crypto.EncryptDecrypt
-keepclassmembernames class com.payment.payrowapp.crypto.EncryptDecrypt {
    public <methods>;
    public <fields>;
    #!private *; also tried this but it didn't work
}

-keep class com.sunmi.rki.SunmiRKIKernel { *; }
-keepnames class com.sunmi.rki.SunmiRKIKernel { *; }

-keep class com.sunmi.keyinject.** { *; }
-keepnames class com.sunmi.keyinject.** { *; }

-keep class com.sunmi.paylib.** { *; }
-keepnames class com.sunmi.paylib.** { *; }

-keep class com.sunmi.pay.hardware.** { *; }
-keepnames class com.sunmi.pay.hardware.** { *; }
-obfuscationdictionary build/obfuscation-dictionary.txt
-classobfuscationdictionary build/class-dictionary.txt
-packageobfuscationdictionary build/package-dictionary.txt


# removes such information by default, so configure it to keep all of it.
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
#-keep class com.mastercard.cpos.network.model.** { <fields>; }
# Prevent proguard from stripping interface information from TypeAdapter,TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in@JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory

#MasterCard Isolated SDK
-keepclassmembers class com.google.gson.Gson {
public private protected *;
}
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
@com.google.gson.annotations.SerializedName <fields>;
}
##---------------End: proguard configuration for Gson ----------
-keepclassmembers enum * { *; }
-keep class com.mastercard.cpos.facade.** { *; }
-keep class com.mastercard.cpos.domain.** { *; }
-keep class com.mastercard.cpos.entrypoint.** { *; } -keep class o.** { *; }
#Master card Isolated sdk
-keep class com.mastercard.cpos.facade.CposApplication.** {
public *; }

-dontwarn io.reactivex.rxjava3.**
-keepclassmembers class io.reactivex.rxjava3.** { *; }
-keep class io.reactivex.rxjava3.** { *; }

-keep class com.google.android.gms.** { *; }
-keep class com.google.android.location.** { *; }

-keep class com.google.android.gms.** { *; }
-keep class com.google.android.location.** { *; }

# Keep Serializable implementation classes
-keep class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    private void readObjectNoData();
}

# Keep Parcelable implementation classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep the CREATOR field and required methods
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
    void writeToParcel(android.os.Parcel, int);
}
