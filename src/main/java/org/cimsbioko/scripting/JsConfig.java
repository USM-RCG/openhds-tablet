package org.cimsbioko.scripting;

import android.util.Log;
import org.cimsbioko.fragment.navigate.detail.DetailFragment;
import org.cimsbioko.fragment.navigate.detail.IndividualDetailFragment;
import org.cimsbioko.model.core.Individual;
import org.cimsbioko.model.core.Location;
import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.navconfig.Gateways;
import org.cimsbioko.navconfig.Hierarchy;
import org.cimsbioko.navconfig.NavigatorModule;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.FormBuilder;
import org.cimsbioko.navconfig.forms.FormConsumer;
import org.cimsbioko.navconfig.forms.Launcher;
import org.cimsbioko.utilities.DateUtils;
import org.cimsbioko.utilities.FormUtils;
import org.cimsbioko.utilities.IdHelper;
import org.cimsbioko.utilities.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Collections.*;

public class JsConfig implements Closeable {

    private static final String TAG = JsConfig.class.getSimpleName();
    private static final String MOBILE_INIT_MODULE = "init", BUNDLE_NAME = "strings";

    private static final String DB_NAME = "$db";
    private static final String MSG_NAME = "$msg";

    private final ClassLoader loader;
    private Hierarchy hierarchy = new StubHierarchy();
    private NavigatorModule[] navigatorModules = {};
    private String adminSecret;

    public JsConfig(ClassLoader loader) {
        if (loader != null) {
            this.loader = loader;
        } else {
            this.loader = JsConfig.class.getClassLoader();
        }
    }

    public JsConfig load() throws URISyntaxException {
        Context ctx = Context.enter();
        try {
            ScriptableObject scope = buildScope(ctx);
            installConstants(scope);
            Require require = enableJsModules(ctx, scope);
            Log.i(TAG, "loading init module");
            Scriptable init = require.requireMain(ctx, MOBILE_INIT_MODULE);
            hierarchy = ScriptableObject.getTypedProperty(init, "hierarchy", Hierarchy.class);
            navigatorModules = ScriptableObject.getTypedProperty(init, "navmods", NavigatorModule[].class);
            adminSecret = ScriptableObject.getTypedProperty(init, "adminSecret", String.class);
            return this;
        } finally {
            Context.exit();
        }
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    public NavigatorModule[] getNavigatorModules() {
        return navigatorModules;
    }

    public String getAdminSecret() {
        return adminSecret;
    }

    private static ScriptableObject buildScope(Context ctx) {
        return ctx.initSafeStandardObjects();
    }

    private void installConstants(ScriptableObject scope) {
        installTranslationService(scope);
        installDbService(scope);
        installInterfaces(scope);
        installDomainClasses(scope);
        installDetailFragments(scope, IndividualDetailFragment.class);
        installUtilityClasses(scope);
    }

    private void installDbService(ScriptableObject scope) {
        putConst(scope, DB_NAME, Gateways.getInstance());
    }

    private void installTranslationService(ScriptableObject scope) {
        putConst(scope, MSG_NAME, new MessagesScriptable(this));
    }

    public String getString(String key) {
        ResourceBundle bundle = getBundle();
        return bundle.containsKey(key) ? bundle.getString(key) : String.format("{%s}", key);
    }

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), loader, NonCachingResourceBundleControl.INSTANCE);
    }

    private static void putConst(ScriptableObject scope, String name, Object object) {
        scope.putConst(name, scope, object);
    }

    private static void installUtilityClasses(ScriptableObject scope) {
        putClasses(scope, DateUtils.class, IdHelper.class, FormUtils.class, StringUtils.class);
    }

    @SafeVarargs
    private static void installDetailFragments(ScriptableObject scope, Class<? extends DetailFragment>... classes) {
        putClasses(scope, classes);
    }

    private static void installDomainClasses(ScriptableObject scope) {
        putClasses(scope, LocationHierarchy.class, Location.class, Individual.class);
    }

    private static void installInterfaces(ScriptableObject scope) {
        putClasses(scope, Hierarchy.class, NavigatorModule.class, FormBuilder.class, FormConsumer.class, Binding.class,
                Launcher.class);
    }

    private static void putClasses(ScriptableObject scope, Class<?>... classes) {
        for (Class<?> c : classes) {
            putClass(scope, c);
        }
    }

    private static void putClass(ScriptableObject scope, Class<?> clazz) {
        scope.putConst(clazz.getSimpleName(), scope, new NativeJavaClass(scope, clazz));
    }

    private Require enableJsModules(Context ctx, ScriptableObject scope) throws URISyntaxException {
        RequireBuilder rb = new RequireBuilder()
                .setSandboxed(true)
                .setModuleScriptProvider(
                        new SoftCachingModuleScriptProvider(
                                new NonCachingModuleSourceProvider(getJsModulePath())));
        Require require = rb.createRequire(ctx, scope);
        require.install(scope);
        return require;
    }

    private List<URI> getJsModulePath() throws URISyntaxException {
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) loader;
            List<URI> uris = new ArrayList<>();
            for (URL u : urlClassLoader.getURLs()) {
                uris.add(u.toURI());
            }
            return unmodifiableList(uris);
        } else {
            URL root = loader.getResource(MOBILE_INIT_MODULE + ".js");
            if (root != null) {
                return singletonList(root.toURI());
            }
        }
        return emptyList();
    }

    @Override
    public void close() {
        ResourceBundle.clearCache(loader);
    }

    private static class NonCachingModuleSourceProvider extends UrlModuleSourceProvider {

        NonCachingModuleSourceProvider(Iterable<URI> privilegedUris) {
            super(privilegedUris, null);
        }

        @Override
        protected URLConnection openUrlConnection(URL url) throws IOException {
            URLConnection c = super.openUrlConnection(url);
            c.setUseCaches(false);
            return c;
        }
    }

    private static class NonCachingResourceBundleControl extends ResourceBundle.Control {

        static final ResourceBundle.Control INSTANCE = new NonCachingResourceBundleControl();

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IOException, IllegalAccessException, InstantiationException {
            return super.newBundle(baseName, locale, format, loader, true);
        }
    }
}
