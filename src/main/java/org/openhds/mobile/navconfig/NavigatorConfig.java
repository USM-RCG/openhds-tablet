package org.openhds.mobile.navconfig;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.openhds.mobile.navconfig.forms.Binding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableCollection;
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

    private static NavigatorConfig instance;

    private Map<String, NavigatorModule> modules = emptyMap();
    private Map<String, Binding> bindings = emptyMap();


    protected NavigatorConfig() {
        init();
    }

    private void init() {
        initModules();
        initFormBindings();
    }

    /*
     * Define the navigation modules. They will show up in the interface in the order specified.
     */
    private void initModules() {
        modules = new LinkedHashMap<>();
        try {
            initCoreModules();
            initExtendedModules();
        } catch (IOException e) {
            Log.e(TAG, "failure initializing modules", e);
        }
    }

    private void initExtendedModules() throws IOException {
        executeConfigScript("/extensions.js");
    }

    private void initCoreModules() throws IOException {
        executeConfigScript("/core.js");
        executeConfigScript("/update.js");
    }

    private void executeConfigScript(String resourcePath) throws IOException {
        InputStream scriptStream = NavigatorConfig.class.getResourceAsStream(resourcePath);
        if (scriptStream != null) {
            Reader scriptReader = new InputStreamReader(scriptStream);
            Context ctx = Context.enter();
            ctx.setOptimizationLevel(-1);
            try {
                Scriptable scope = ctx.initStandardObjects();
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

    public void addModule(NavigatorModule module) {
        modules.put(module.getActivityTitle(), module);
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
     * Get a localized string from the modular {@link java.util.ResourceBundle}.
     *
     * @param key the resource key for a localized string
     * @return the string, localized for the current {@link java.util.Locale}
     */
    public String getString(String key) {
        return getBundle("strings").getString(key);
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