package org.cimsbioko.navconfig;

import android.util.Log;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.scripting.JsConfig;
import java.util.*;

import static java.util.Collections.*;
import static org.cimsbioko.utilities.CampaignUtils.loadCampaign;


/**
 * The configuration source for hierarchy navigation, form display and form data binding for the field worker section of
 * the application.
 *
 * @see org.cimsbioko.activity.FieldWorkerActivity
 * @see org.cimsbioko.activity.HierarchyNavigatorActivity
 */
public class NavigatorConfig {

    private static final String TAG = NavigatorConfig.class.getSimpleName();

    private static NavigatorConfig instance;

    private Hierarchy hierarchy;
    private Map<String, NavigatorModule> modules = emptyMap();
    private Map<String, Binding> bindings = emptyMap();
    private JsConfig config;


    protected NavigatorConfig() {
        init();
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    private void init() {
        loadConfig();
        consolidateFormBindings();
    }

    /**
     * Reloads the configuration, ensuring url resource caching doesn't get in the way.
     */
    public void reload() {
        config.close();
        init();
    }

    /*
     * Define the navigation modules. They will show up in the interface in the order specified.
     */
    private void loadConfig() {
        modules = new LinkedHashMap<>();
        try {
            config = loadCampaign();
            hierarchy = config.getHierarchy();
            for (NavigatorModule module : config.getNavigatorModules()) {
                modules.put(module.getName(), module);
            }
        } catch (Exception e) {
            Log.e(TAG, "failure initializing js config", e);
        }
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
     * Get logical names for the configured hierarchy levels.
     *
     * @return a list of configured hier levels, from highest to lowest.
     */
    public List<String> getLevels() {
        return hierarchy.getLevels();
    }

    /**
     * Get logical names for admin hierarchy levels.
     *
     * @return a list of hier levels corresponding to admin hierarchy, from highest to lowest.
     */
    public List<String> getAdminLevels() {
        return hierarchy.getAdminLevels();
    }

    /**
     * Returns the level value corresponding to immediately more general value in the hierarchy.
     * For example, if 'subdistrict' was specified, 'district' would be the result.
     *
     * @param level the cimsbioko app level value (the child)
     * @return the level value corresponding to the parent level of the specified one
     */
    public String getParentLevel(String level) {
        return hierarchy.getParentLevel(level);
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
    public String getLevelLabel(String level) {
        return getString(hierarchy.getLevelLabels().get(level));
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

    /**
     * Get a localized string from the {@link JsConfig}'s {@link java.util.ResourceBundle}.
     *
     * @param key the resource key for a localized string
     * @return the string, localized for the current {@link java.util.Locale}
     */
    public String getString(String key) {
        return config.getString(key);
    }
}

