# --- Rules for SLF4J (Fixes the StaticLoggerBinder error) ---
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# --- Rules for Supabase, Ktor & Serialization ---
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses