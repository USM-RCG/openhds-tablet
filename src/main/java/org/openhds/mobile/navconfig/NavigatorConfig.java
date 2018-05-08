package org.openhds.mobile.navconfig;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.LazilyLoadedCtor;
import org.mozilla.javascript.ScriptableObject;
import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import static java.util.ResourceBundle.getBundle;


/**
 * The configuration source for hierarchy navigation, form display and form data binding for the field worker section of
 * the application.
 *
 * @see org.openhds.mobile.activity.FieldWorkerActivity
 * @see org.openhds.mobile.activity.HierarchyNavigatorActivity
 */
public class NavigatorConfig {

    private static final String TAG = NavigatorConfig.class.getSimpleName();
    private static final String INIT_SCRIPT_NAME = "init.js";
    private static final String BUNDLE_NAME = "strings";

    private static NavigatorConfig instance;

    private ClassLoader moduleLoader = NavigatorConfig.class.getClassLoader();
    private Map<String, NavigatorModule> modules = emptyMap();
    private Map<String, Binding> bindings = emptyMap();


    protected NavigatorConfig() {
        init();
    }

    public void init() {
        initModules();
        initFormBindings();
    }

    public void setModules(URL modulesUrl) throws MalformedURLException {
        ClassLoader defaultLoader = NavigatorConfig.class.getClassLoader();
        moduleLoader = URLClassLoader.newInstance(new URL[]{modulesUrl}, defaultLoader);
    }

    /*
     * Define the navigation modules. They will show up in the interface in the order specified.
     */
    private void initModules() {
        modules = new LinkedHashMap<>();
        try {
            executeScript(INIT_SCRIPT_NAME);
        } catch (IOException e) {
            Log.e(TAG, "failure initializing modules", e);
        }
    }

    @UsedByJSConfig
    public void executeScript(String resourcePath) throws IOException {
        InputStream scriptStream = moduleLoader.getResourceAsStream(resourcePath);
        if (scriptStream != null) {
            Reader scriptReader = new InputStreamReader(scriptStream);
            Context ctx = Context.enter();
            ctx.setOptimizationLevel(-1);
            try {
                ScriptableObject scope = ctx.initSafeStandardObjects();
                new LazilyLoadedCtor(scope,
                        "JavaImporter", "org.mozilla.javascript.ImporterTopLevel", false);
                new LazilyLoadedCtor(scope,
                        "org", "org.mozilla.javascript.NativeJavaTopPackage", false);
                scope.put("config", scope, this);
                Log.i(TAG, "executing config script " + resourcePath);
                ctx.evaluateReader(scope, scriptReader, resourcePath, 1, null);
            } finally {
                Context.exit();
                scriptStream.close();
            }
        }
    }

    /**
     * Creates and index of all module {@link Binding} objects by name for fast access.
     */
    private void initFormBindings() {
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
     * Translates the specified level to the corresponding value used in the mobile db, since it
     * is constructed by the server (with its level values).
     *
     * TODO: Unify the level values generated by the server, so there is no need to translate
     *
     * @param level the mobile db level
     * @return the corresponding level value used on the server
     */
    public String getServerLevel(String level) {
        return BiokoHierarchy.INSTANCE.getServerLevel(level);
    }

    /**
     * Translates the specified server level value to the corresponding level value used in this
     * mobile application.
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
     * @param level the mobile app level value (the child)
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
     * @param name the name of the binding to retrieve
     * @return the {@link Binding} object for the given name or null if none exists
     */
    public Binding getBinding(String name) {
        return bindings.get(name);
    }
}