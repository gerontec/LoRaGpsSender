# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep USB Serial library classes
-keep class com.hoho.android.usbserial.** { *; }
-keepclassmembers class com.hoho.android.usbserial.** { *; }

# Keep application classes
-keep class com.gerontec.loragpssender.** { *; }
