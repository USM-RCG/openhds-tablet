package org.openhds.mobile.model.form;

import org.openhds.mobile.projectdata.FormFilters.FormFilter;
import org.openhds.mobile.projectdata.FormPayloadBuilders.FormPayloadBuilder;
import org.openhds.mobile.projectdata.FormPayloadConsumers.FormPayloadConsumer;
import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.util.ArrayList;

public class FormBehaviour {

    private String formName;
    private int formLabelId;
    private FormFilter formFilter;
    private FormPayloadBuilder formPayloadBuilder;
    private FormPayloadConsumer formPayloadConsumer;

    // ArrayList, not just List, because of user with Android Parcelable interface.
    private ArrayList<FormSearchPluginModule> formSearchPluginModules;

    public FormBehaviour(String formName,
                         int formLabelId,
                         FormFilter formFilter,
                         FormPayloadBuilder formMapper,
                         FormPayloadConsumer formPayloadConsumer) {
        this(formName, formLabelId, formFilter, formMapper, formPayloadConsumer, null);
    }

    public FormBehaviour(String formName,
                         int formLabelId,
                         FormFilter formFilter,
                         FormPayloadBuilder formMapper,
                         FormPayloadConsumer formPayloadConsumer,
                         ArrayList<FormSearchPluginModule> formSearchPluginModules) {

        this.formName = formName;
        this.formLabelId = formLabelId;
        this.formFilter = formFilter;
        this.formPayloadBuilder = formMapper;
        this.formPayloadConsumer = formPayloadConsumer;
        this.formSearchPluginModules = formSearchPluginModules;
    }

    public String getFormName() {
        return formName;
    }

    public int getFormLabelId() {
        return formLabelId;
    }

    public FormFilter getFormFilter() {
        return formFilter;
    }

    public FormPayloadBuilder getFormPayloadBuilder() {
        return formPayloadBuilder;
    }

    public FormPayloadConsumer getFormPayloadConsumer() {
        return formPayloadConsumer;
    }

    public ArrayList<FormSearchPluginModule> getFormSearchPluginModules() {
        return formSearchPluginModules;
    }

    public boolean getNeedsFormFieldSearch() {
        return null != formSearchPluginModules && formSearchPluginModules.size() > 0;
    }
}