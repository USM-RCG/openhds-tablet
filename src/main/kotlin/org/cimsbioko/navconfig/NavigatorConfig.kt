package org.cimsbioko.navconfig

import android.util.Log
import org.cimsbioko.scripting.JsConfig
import org.cimsbioko.search.SearchQueryBuilder
import org.cimsbioko.search.SearchSource
import org.cimsbioko.utilities.CampaignUtils.loadCampaign

/**
 * The configuration source for hierarchy navigation, form display and form data binding for the field worker section of
 * the application.
 *
 * @see org.cimsbioko.activity.FieldWorkerActivity
 * @see org.cimsbioko.activity.HierarchyNavigatorActivity
 */
class NavigatorConfig private constructor() {

    init {
        init()
    }

    private fun init() {
        loadConfig()
        consolidateFormBindings()
    }

    private lateinit var hierarchy: Hierarchy
    private lateinit var mods: Map<String, NavigatorModule>
    private lateinit var bindings: Map<String, Binding>
    private lateinit var config: JsConfig

    var adminSecret: String? = null
        private set

    lateinit var searchSources: Map<String, SearchSource>
        private set

    lateinit var searchQueryBuilder: SearchQueryBuilder

    /**
     * Reloads the configuration, ensuring url resource caching doesn't get in the way.
     */
    fun reload() {
        config.close()
        init()
    }

    /*
     * Define the navigation modules. They will show up in the interface in the order specified.
     */
    private fun loadConfig() {
        try {
            config = loadCampaign()
            hierarchy = config.hierarchy
            adminSecret = config.adminSecret
            mods = config.navigatorModules.associateBy { it.name }
            searchSources = config.searchSources
            searchQueryBuilder = config.searchQueryBuilder
        } catch (e: Exception) {
            Log.e(TAG, "failure initializing js config", e)
        }
    }

    /**
     * Creates an index of all module [Binding] objects by name for fast access.
     */
    private fun consolidateFormBindings() {
        bindings = HashMap<String, Binding>().apply { mods.values.forEach { putAll(it.bindings) } }
    }

    /**
     * Gets all configured navigator modules.
     *
     * @return a list of configured [NavigatorModule]s in definition order
     */
    val modules: Collection<NavigatorModule>
        get() = mods.values

    /**
     * Gets all configured navigator module names.
     *
     * @return a list of configured [NavigatorModule] names in definition order
     */
    val moduleNames: Set<String>
        get() = mods.keys

    /**
     * Get logical names for the configured hierarchy levels.
     *
     * @return a list of configured hier levels, from highest to lowest.
     */
    val levels: List<String>
        get() = hierarchy.levels

    /**
     * Get logical names for admin hierarchy levels.
     *
     * @return a list of hier levels corresponding to admin hierarchy, from highest to lowest.
     */
    val adminLevels: List<String>
        get() = hierarchy.adminLevels

    /**
     * Returns the level value corresponding to immediately more general value in the hierarchy.
     * For example, if 'subdistrict' was specified, 'district' would be the result.
     *
     * @param level the cimsbioko app level value (the child)
     * @return the level value corresponding to the parent level of the specified one
     */
    fun getParentLevel(level: String): String? = hierarchy.getParentLevel(level)

    /**
     * Convenience method for getting top-most level. Same as getLevels().get(0).
     *
     * @return the top-most level
     */
    val topLevel: String?
        get() = levels.firstOrNull()

    /**
     * Convenience method for getting bottom-most level.
     *
     * @return the bottom-most level
     */
    val bottomLevel: String?
        get() = levels.lastIndex.let { if (it in levels.indices) levels[it] else null }

    /**
     * Get a localized label for the logical hierarchy level.
     *
     * @param level the hierarchy level
     * @return the level's label for the current locale
     */
    fun getLevelLabel(level: String): String? = getString(hierarchy.levelLabels[level])

    fun getModule(name: String): NavigatorModule? = mods[name]

    /**
     * Finds a configured form binding by name.
     *
     * @param name the name of the binding to retrieve
     * @return the [Binding] object for the given name or null if none exists
     */
    fun getBinding(name: String): Binding? = bindings[name]

    /**
     * Returns the unique set of form ids bound with the loaded [Binding]s.
     *
     * @return  set of form ids from the loaded bindings
     */
    val formIds: Set<String>
        get() = HashSet<String>().apply { bindings.values.forEach { add(it.form) } }

    /**
     * Get a localized string from the [JsConfig]'s [java.util.ResourceBundle].
     *
     * @param key the resource key for a localized string
     * @return the string, localized for the current [java.util.Locale]
     */
    fun getString(key: String?): String = config.getString(key)

    companion object {
        private val TAG = NavigatorConfig::class.java.simpleName
        val instance: NavigatorConfig by lazy { NavigatorConfig() }
    }
}