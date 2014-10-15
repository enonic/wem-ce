module app.view {

    import ModuleBrowseActions = app.browse.ModuleBrowseActions;
    import ContentTypeSummary = api.schema.content.ContentTypeSummary;

    export class ModuleItemStatisticsPanel extends api.app.view.ItemStatisticsPanel<api.module.Module> {

        private upgradeNeeded: boolean = true;
        private upgradeMessageContainer: api.dom.DivEl;
        private moduleDataContainer: api.dom.DivEl;
        private moduleActions: api.ui.Action[] = [];
        private actionMenu: api.ui.menu.ActionMenu;
        private currentItem: api.app.view.ViewItem<api.module.Module>;

        constructor() {
            super("module-item-statistics-panel");

            this.upgradeMessageContainer = new api.dom.DivEl("upgrade-message-container");
            if (this.upgradeNeeded) {
                this.upgradeMessageContainer.getEl().setInnerHtml("Upgrade Available - 1.0.3");
                this.appendChild(this.upgradeMessageContainer)
            }

            this.actionMenu =
            new api.ui.menu.ActionMenu("Module actions", ModuleBrowseActions.get().START_MODULE, ModuleBrowseActions.get().STOP_MODULE,
                ModuleBrowseActions.get().UNINSTALL_MODULE);

            this.appendChild(this.actionMenu);

            this.moduleDataContainer = new api.dom.DivEl("module-data-container");
            this.appendChild(this.moduleDataContainer);
        }

        setItem(item: api.app.view.ViewItem<api.module.Module>) {
            if (this.currentItem && this.currentItem.equals(item)) {
                // do nothing in case item has not changed
                return;
            }
            this.currentItem = item;

            super.setItem(item);
            this.actionMenu.setLabel(api.util.StringHelper.capitalize(item.getModel().getState()));

            if (item.getModel().getState() == "started") {
                ModuleBrowseActions.get().START_MODULE.setEnabled(false);
                ModuleBrowseActions.get().STOP_MODULE.setEnabled(true);
            } else {
                ModuleBrowseActions.get().START_MODULE.setEnabled(true);
                ModuleBrowseActions.get().STOP_MODULE.setEnabled(false);
            }

            this.moduleDataContainer.removeChildren();

            var infoGroup = new ModuleItemDataGroup("Info");
            infoGroup.addDataList("Build date", "TBA");
            infoGroup.addDataList("Version", item.getModel().getVersion());
            infoGroup.addDataList("ModuleID", item.getModel().getModuleKey().toString());
            infoGroup.addDataList("Requirements", "Enonic 5.0.0");

            var schemasGroup = new ModuleItemDataGroup("Schemas");

            new api.schema.content.GetContentTypesByModuleRequest(item.getModel().getModuleKey()).
                sendAndParse().
                then((contentTypes: ContentTypeSummary[]) => {
                    var contentTypeNames = contentTypes.map((contentType: ContentTypeSummary) => contentType.getContentTypeName().toString());
                    schemasGroup.addDataArray("Content Types", contentTypeNames);
                }).
                catch((reason: any) => api.DefaultErrorHandler.handle(reason)).
                done();

            schemasGroup.addDataList("Mixins", "TBA");
            var metadataNames = item.getModel().getMetadataSchemaDependencies();
            var strings: string[] = [];
            metadataNames.forEach((data: api.schema.metadata.MetadataSchemaName) => {
                strings.push(data.toString());
            });

            schemasGroup.addDataArray("MetadataSchemas", strings);

            this.moduleDataContainer.appendChild(infoGroup);
            this.moduleDataContainer.appendChild(schemasGroup);
        }

    }

    export class ModuleItemDataGroup extends api.dom.DivEl {

        private header: api.dom.H2El;

        constructor(title: string) {
            super("module-item-data-group");
            this.header = new api.dom.H2El();
            this.header.getEl().setInnerHtml(title);
            this.appendChild(this.header);
        }

        addDataList(header: string, ...datas: string[]) {
            this.addDataArray(header, datas);
        }

        addDataArray(header: string, datas: string[]) {
            var dataList = new api.dom.UlEl("data-list");

            if (header) {
                var headerElement = new api.dom.LiEl();
                headerElement.addClass("list-header");

                headerElement.getEl().setInnerHtml(header);
                dataList.appendChild(headerElement);
            }

            datas.forEach((data) => {
                var dataElement = new api.dom.LiEl();
                dataElement.getEl().setInnerHtml(data);
                dataList.appendChild(dataElement);
            });

            this.appendChild(dataList);
        }
    }

}
