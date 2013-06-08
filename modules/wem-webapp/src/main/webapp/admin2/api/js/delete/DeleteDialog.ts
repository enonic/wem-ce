module api_delete{

    export class DeleteDialog extends api_ui_dialog.ModalDialog {

        private modelName:string;

        private deleteAction:api_action.Action;

        private cancelAction:api_action.Action = new CancelDeleteDialogAction();

        private deleteItems:DeleteItem[];

        private itemList:DeleteDialogItemList = new DeleteDialogItemList();

        constructor(modelName:string) {
            super({
                title: "Delete " + modelName,
                width: 500,
                height: 300
            });

            this.modelName = modelName;

            this.getEl().addClass("delete-dialog");
            this.appendChildToContentPanel(this.itemList);
            this.addAction(this.cancelAction);

            this.cancelAction.addExecutionListener(()=> {
                this.close();
            })
        }

        setDeleteAction(action:api_action.Action) {
            this.deleteAction = action;
            this.addAction(action);
        }

        setDeleteItems(deleteItems:DeleteItem[]) {
            this.deleteItems = deleteItems;

            this.itemList.clear();

            if( deleteItems.length > 1 ) {
                this.setTitle( "Delete " + this.modelName + "s");
            }
            else {
                this.setTitle( "Delete " + this.modelName);
            }

            for (var i in this.deleteItems) {
                var deleteItem:DeleteItem = this.deleteItems[i];
                // TODO: created and add DeleteDialogItemList
                this.itemList.appendChild(new DeleteDialogItemComponent(deleteItem));
            }
        }
    }

    export class CancelDeleteDialogAction extends api_action.Action {

        constructor() {
            super("Cancel");
        }
    }

    export class DeleteDialogItemList extends api_ui.Component {
        constructor() {
            super("DeleteDialogItemList", "div")
            this.getEl().addClass("delete-dialog-item-list");
        }

        clear() {
            this.removeChildren();
        }
    }

    class DeleteDialogItemComponent extends api_ui.Component {
        constructor(deleteItem:api_delete.DeleteItem) {
            super("DeleteDialogItem", "div");
            this.getEl().addClass("delete-dialog-item");

            var icon:api_ui.Component = new api_ui.Component("img", "img");
            icon.getImg().setSrc(deleteItem.getIconUrl());
            this.appendChild(icon);

            var displayName:api_ui.Component = new api_ui.Component("h4", "h4");
            displayName.getEl().setInnerHtml(deleteItem.getDisplayName());
            this.appendChild(displayName);
        }
    }
}
