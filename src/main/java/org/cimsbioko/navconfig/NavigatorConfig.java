package org.cimsbioko.navconfig;

import android.util.Log;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.LazilyLoadedCtor;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static java.util.Collections.*;
import static java.util.ResourceBundle.getBundle;
import static org.mozilla.javascript.Context.VERSION_ES6;


/**
 * The configuration source for hierarchy navigation, form display and form data binding for the field worker section of
 * the application.
 *
 * @see org.cimsbioko.activity.FieldWorkerActivity
 * @see org.cimsbioko.activity.HierarchyNavigatorActivity
 */
public class NavigatorConfig {

    private static final String TAG = NavigatorConfig.class.getSimpleName();
    private static final String INIT_MODULE = "init";
    private static final String BUNDLE_NAME = "strings";

    private static NavigatorConfig instance;

    private ClassLoader moduleLoader = NavigatorConfig.class.getClassLoader();
    private Map<String, NavigatorModule> modules = emptyMap();
    private Map<String, Binding> bindings = emptyMap();


    protected NavigatorConfig() {
        init();
    }

    private void init() {
        loadModules();
        consolidateFormBindings();
    }

    /*
     * Define the navigation modules. They will show up in the interface in the order specified.
     */
    private void loadModules() {
        modules = new LinkedHashMap<>();
        try {
            Context ctx = Context.enter();
            ctx.setOptimizationLevel(-1);
            ctx.setLanguageVersion(VERSION_ES6);
            try {
                ScriptableObject scope = ctx.initSafeStandardObjects();
                new LazilyLoadedCtor(scope, "JavaImporter", "org.mozilla.javascript.ImporterTopLevel", false);
                new LazilyLoadedCtor(scope, "org", "org.mozilla.javascript.NativeJavaTopPackage", false);
                RequireBuilder rb = new RequireBuilder()
                        .setSandboxed(true)
                        .setModuleScriptProvider(
                                new SoftCachingModuleScriptProvider(
                                        new UrlModuleSourceProvider(getJsModulePath(), null)));
                Require require = rb.createRequire(ctx, scope);
                require.install(scope);
                scope.put("config", scope, this);
                Log.i(TAG, "loading init module");
                require.requireMain(ctx, INIT_MODULE);
            } finally {
                Context.exit();
            }
        } catch (Exception e) {
            Log.e(TAG, "failure initializing modules", e);
        }
    }

    private List<URI> getJsModulePath() throws URISyntaxException {
        List<URI> uris = new ArrayList<>();
        URL root = moduleLoader.getResource(INIT_MODULE + ".js");
        if (root != null) {
            uris.add(root.toURI());
        }
        return uris;
    }

    /**
     * Creates an index of all module {@link Binding} objects by name for fast access.
     */
    private void consolidateFormBindings() {
        bindings = new HashMap<>();
        for (NavigatorModule module : modules.values()) {
            bindings.putAll(module.getBindings());
        }
    }

    @UsedByJSConfig
    public void addModule(NavigatorModule module) {
        modules.put(module.getName(), module);
    }

    public static synchronized NavigatorConfig getInstance() {
        if (instance == null) {
            instance = new NavigatorConfig();
        }
        return instance;
    }

    /**
     * Gets all configured navigator modules.
     *
     * @return a list of configured {@link NavigatorModule}s in definition order
     */
    public Collection<NavigatorModule> getModules() {
        return unmodifiableCollection(modules.values());
    }

    /**
     * Gets all configured navigator module names.
     *
     * @return a list of configured {@link NavigatorModule} names in definition order
     */
    public Set<String> getModuleNames() {
        return unmodifiableSet(modules.keySet());
    }

    /**
     * Get a localized string from the modular {@link java.util.ResourceBundle}.
     *
     * @param key the resource key for a localized string
     * @return the string, localized for the current {@link java.util.Locale}
     */
    public String getString(String key) {
        return getBundle(BUNDLE_NAME, Locale.getDefault(), moduleLoader).getString(key);
    }

    /**
     * Get logical names for the configured hierarchy levels.
     *
     * @return a list of configured hier levels, from highest to lowest.
     */
    public List<String> getLevels() {
        return BiokoHierarchy.INSTANCE.getLevels();
    }

    /**
     * Get logical names for admin hierarchy levels.
     *
     * @return a list of hier levels corresponding to admin hierarchy, from highest to lowest.
     */
    public List<String> getAdminLevels() {
        return BiokoHierarchy.INSTANCE.getAdminLevels();
    }

    /**
     * Translates the specified level to the corresponding value used in the cimsbioko db, since it
     * is constructed by the server (with its level values).
     * <p>
     * TODO: Unify the level values generated by the server, so there is no need to translate
     *
     * @param level the cimsbioko db level
     * @return the corresponding level value used on the server
     */
    public String getServerLevel(String level) {
        return BiokoHierarchy.INSTANCE.getServerLevel(level);
    }

    /**
     * Translates the specified server level value to the corresponding level value used in this
     * cimsbioko application.
     *
     * @param level the server level value
     * @return the corresponding level value used in this application
     */
    public String getLevelForServerLevel(String level) {
        return BiokoHierarchy.INSTANCE.getLevelForServerLevel(level);
    }

    /**
     * Returns the level value corresponding to immediately more general value in the hierarchy.
     * For example, if 'subdistrict' was specified, 'district' would be the result.
     *
     * @param level the cimsbioko app level value (the child)
     * @return the level value corresponding to the parent level of the specified one
     */
    public String getParentLevel(String level) {
        return BiokoHierarchy.INSTANCE.getParentLevel(level);
    }

    /**
     * Convenience method for getting top-most level. Same as getLevels().get(0).
     *
     * @return the top-most level
     */
    public String getTopLevel() {
        return getLevels().get(0);
    }

    /**
     * Convenience method for getting bottom-most level.
     *
     * @return the bottom-most level
     */
    public String getBottomLevel() {
        return getLevels().get(getLevels().size() - 1);
    }

    /**
     * Get a localized label for the logical hierarchy level.
     *
     * @param level the hierarchy level
     * @return the level's label for the current locale
     */
    public int getLevelLabel(String level) {
        return BiokoHierarchy.INSTANCE.getLevelLabels().get(level);
    }

    public NavigatorModule getModule(String name) {
        return modules.get(name);
    }

    /**
     * Finds a configured form binding by name.
     *
     * @param name the name of the binding to retrieve
     * @return the {@link Binding} object for the given name or null if none exists
     */
    public Binding getBinding(String name) {
        return bindings.get(name);
    }
}