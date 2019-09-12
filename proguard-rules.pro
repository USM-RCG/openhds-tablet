
-dontobfuscate

# not kept apparently because of custom namespace in res xml?
-keep class android.support.v7.widget.SearchView { *; }

# androidplot accesses resources dynamically
-keep class org.cimsbioko.R$* { *; }

# app's scriptable classes
-keep class * {
    @org.cimsbioko.navconfig.UsedByJSConfig *;
}
-keep class * extends org.cimsbioko.fragment.navigate.detail.DetailFragment { *; }
-keep class org.jdom2.* { *; }

# jdom (xml)
-dontwarn org.jdom2.xpath.jaxen.**
-dontwarn org.jdom2.input.StAXEventBuilder
-dontwarn org.jdom2.input.StAXStreamBuilder
-dontwarn org.jdom2.output.StAXEventOutputter
-dontwarn org.jdom2.output.StAXStreamOutputter
-dontwarn org.jdom2.output.support.**

# mozilla rhino (scripting engine)
-dontwarn org.mozilla.javascript.tools.debugger.**
-dontwarn org.mozilla.javascript.tools.shell.**
-keep class org.mozilla.javascript.jdk15.VMBridge_jdk15 { *; }
-keep class org.mozilla.javascript.ImporterTopLevel { *; }
-keep class org.mozilla.javascript.NativeJavaTopPackage { *; }

# android plot (graphs)
-keep class com.androidplot.** { *; }

# lucene (full-text search)
-dontwarn org.apache.lucene.util.RamUsageEstimator
-dontwarn org.apache.lucene.sandbox.queries.regex.**
-keep class * extends org.apache.lucene.codecs.Codec
-keep class * extends org.apache.lucene.codecs.PostingsFormat
-keep class * extends org.apache.lucene.codecs.DocValuesFormat
-keep class * implements org.apache.lucene.util.Attribute
