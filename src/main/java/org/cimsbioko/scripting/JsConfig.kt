package org.cimsbioko.scripting

import android.util.Log
import org.cimsbioko.fragment.navigate.detail.DetailFragment
import org.cimsbioko.fragment.navigate.detail.IndividualDetailFragment
import org.cimsbioko.model.core.Individual
import org.cimsbioko.model.core.Location
import org.cimsbioko.model.core.LocationHierarchy
import org.cimsbioko.navconfig.Gateways
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.navconfig.NavigatorModule
import org.cimsbioko.navconfig.forms.Binding
import org.cimsbioko.navconfig.forms.FormBuilder
import org.cimsbioko.navconfig.forms.FormConsumer
import org.cimsbioko.navconfig.forms.Launcher
import org.cimsbioko.utilities.DateUtils
import org.cimsbioko.utilities.FormUtils
import org.cimsbioko.utilities.IdHelper
import org.cimsbioko.utilities.StringUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJavaClass
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.commonjs.module.Require
import org.mozilla.javascript.commonjs.module.RequireBuilder
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider
import java.io.Closeable
import java.io.IOException
import java.net.*
import java.util.*
import java.util.ResourceBundle.Control

class JsConfig(private val loader: ClassLoader = JsConfig::class.java.classLoader) : Closeable {

    var hierarchy: Hierarchy = StubHierarchy()
        private set
    var navigatorModules = arrayOf<NavigatorModule>()
        private set
    var adminSecret: String? = null
        private set

    @Throws(URISyntaxException::class)
    fun load(): JsConfig {
        val ctx = Context.enter()
        return try {
            val scope = buildScope(ctx)
            installConstants(scope)
            val require = enableJsModules(ctx, scope)
            Log.i(TAG, "loading init module")
            val init = require.requireMain(ctx, MOBILE_INIT_MODULE)
            hierarchy = ScriptableObject.getTypedProperty(init, "hierarchy", Hierarchy::class.java)
            navigatorModules = ScriptableObject.getTypedProperty(init, "navmods", Array<NavigatorModule>::class.java)
            adminSecret = ScriptableObject.getTypedProperty(init, "adminSecret", String::class.java)
            this
        } finally {
            Context.exit()
        }
    }

    private fun installConstants(scope: ScriptableObject) {
        installTranslationService(scope)
        installDbService(scope)
        installInterfaces(scope)
        installDomainClasses(scope)
        installDetailFragments(scope, IndividualDetailFragment::class.java)
        installUtilityClasses(scope)
    }

    private fun installDbService(scope: ScriptableObject) {
        putConst(scope, DB_NAME, Gateways.getInstance())
    }

    private fun installTranslationService(scope: ScriptableObject) {
        putConst(scope, MSG_NAME, MessagesScriptable(this))
    }

    fun getString(key: String?): String {
        val bundle = bundle
        return if (bundle.containsKey(key)) bundle.getString(key) else String.format("{%s}", key)
    }

    private val bundle: ResourceBundle
        get() = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), loader, NonCachingResourceBundleControl.INSTANCE)

    @Throws(URISyntaxException::class)
    private fun enableJsModules(ctx: Context, scope: ScriptableObject): Require = RequireBuilder()
            .setSandboxed(true)
            .setModuleScriptProvider(
                    SoftCachingModuleScriptProvider(
                            NonCachingModuleSourceProvider(jsModulePath)))
            .createRequire(ctx, scope)
            .apply { install(scope) }

    @get:Throws(URISyntaxException::class)
    private val jsModulePath: List<URI?>
        get() {
            if (loader is URLClassLoader) {
                val uris: MutableList<URI?> = ArrayList()
                for (u in loader.urLs) {
                    uris.add(u.toURI())
                }
                return Collections.unmodifiableList(uris)
            } else {
                val root = loader.getResource("$MOBILE_INIT_MODULE.js")
                if (root != null) {
                    return listOf(root.toURI())
                }
            }
            return emptyList<URI>()
        }

    override fun close() {
        ResourceBundle.clearCache(loader)
    }

    private class NonCachingModuleSourceProvider internal constructor(privilegedUris: Iterable<URI?>?) : UrlModuleSourceProvider(privilegedUris, null) {
        @Throws(IOException::class)
        override fun openUrlConnection(url: URL): URLConnection {
            val c = super.openUrlConnection(url)
            c.useCaches = false
            return c
        }
    }

    private class NonCachingResourceBundleControl : Control() {
        @Throws(IOException::class, IllegalAccessException::class, InstantiationException::class)
        override fun newBundle(baseName: String, locale: Locale, format: String, loader: ClassLoader, reload: Boolean): ResourceBundle? {
            return super.newBundle(baseName, locale, format, loader, true)
        }

        companion object {
            val INSTANCE: Control = NonCachingResourceBundleControl()
        }
    }

    companion object {
        private val TAG = JsConfig::class.java.simpleName
        private const val MOBILE_INIT_MODULE = "init"
        private const val BUNDLE_NAME = "strings"
        private const val DB_NAME = "\$db"
        private const val MSG_NAME = "\$msg"
        private fun buildScope(ctx: Context): ScriptableObject {
            return ctx.initSafeStandardObjects()
        }

        private fun putConst(scope: ScriptableObject, name: String, `object`: Any) {
            scope.putConst(name, scope, `object`)
        }

        private fun installUtilityClasses(scope: ScriptableObject) {
            putClasses(scope, DateUtils::class.java, IdHelper::class.java, FormUtils::class.java, StringUtils::class.java)
        }

        @SafeVarargs
        private fun installDetailFragments(scope: ScriptableObject, vararg classes: Class<out DetailFragment?>) {
            putClasses(scope, *classes)
        }

        private fun installDomainClasses(scope: ScriptableObject) {
            putClasses(scope, LocationHierarchy::class.java, Location::class.java, Individual::class.java)
        }

        private fun installInterfaces(scope: ScriptableObject) {
            putClasses(scope, Hierarchy::class.java, NavigatorModule::class.java, FormBuilder::class.java,
                    FormConsumer::class.java, Binding::class.java, Launcher::class.java)
        }

        private fun putClasses(scope: ScriptableObject, vararg classes: Class<*>) {
            for (c in classes) {
                putClass(scope, c)
            }
        }

        private fun putClass(scope: ScriptableObject, clazz: Class<*>) {
            scope.putConst(clazz.simpleName, scope, NativeJavaClass(scope, clazz))
        }
    }
}