const hierFormatter = new HierFormatter({
    formatItem(item) {
        const wrapped = item.wrapped;
        return new HierItemDisplay({
            getHeading() {
                return wrapped.name;
            },
            getSubheading() {
                return wrapped.extId;
            },
            getDetails() {
                return {};
            }
        });
    }
});

const navMod = new NavigatorModule({
    getName() {
        return "default";
    },
    getActivityTitle() {
        return "Default Campaign";
    },
    getLaunchLabel() {
        return "Default";
    },
    getLaunchDescription() {
        return "Fallback mode, no campaign loaded";
    },
    getBindings() {
        return {};
    },
    getLaunchers(level) {
        return [];
    },
    getItemFormatter(level) {
        return null;
    },
    getHierFormatter(level) {
        return hierFormatter;
    }
});

exports.module = navMod;