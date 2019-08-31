const ji = JavaImporter(
    org.cimsbioko.navconfig,
    org.cimsbioko.navconfig.forms,
    org.cimsbioko.navconfig.forms.consumers,
    org.cimsbioko.fragment.navigate.detail
);

function Builder() {
    this.binds = {};
    this.launchers = {};
    this.details = null;
}

Builder.prototype.bind = function(b) {
    const bind_name = b.name || b.form;
    this.binds[bind_name] = new ji.Binding({
        getName() { return bind_name; },
        getForm() { return b.form; },
        getLabel() { return config.getString(b.label); },
        getBuilder() { return b.builder; },
        getConsumer() { return b.consumer || new ji.DefaultConsumer(); }
    });
    return this;
};

Builder.prototype.launcher = function(l) {
    const builder = this, level = l.level || 'root';
    this.launchers[level] = this.launchers[level] || [];
    this.launchers[level].push(new ji.Launcher({
        getLabel() { return l.label? config.getString(l.label) : 'Label'; },
        relevantFor(ctx) { return l.relevant? l.relevant(ctx) : true; },
        getBinding() { return builder.binds[l.bind]; }
    }));
    return builder;
};

const defaultDetails = {
    individual: new ji.IndividualDetailFragment()
};

Builder.prototype.detail = function(d) {
    this.details = this.details || d;
    return this;
};

Builder.prototype.build = function(m) {
    const builder = this;
    return new ji.NavigatorModule({
        getName() { return m.name || 'name'; },
        getActivityTitle() { return m.title? config.getString(m.title) : 'Title'; },
        getLaunchLabel() { return m.launchLabel? config.getString(m.launchLabel) : 'Launch Label'; },
        getLaunchDescription() { return m.launchDescription? config.getString(m.launchDescription) : 'Launch Description'; },
        getBindings() { return builder.binds; },
        getLaunchers(level) { return builder.launchers[level] || []; },
        getDetailFragment(level) { return (builder.details || defaultDetails)[level] || null; }
    });
};

exports.Builder = Builder;