module app.wizard.action {

    export class ShowLiveEditAction extends api.ui.Action {

        constructor(wizard:app.wizard.ContentWizardPanel) {
            super("LIVE");

            this.setEnabled(true);
            this.addExecutionListener(() => {
                new ShowLiveEditEvent().fire();
                wizard.showLiveEdit();

            });
        }
    }

}
