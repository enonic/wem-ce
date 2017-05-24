
import ActionButton = api.ui.button.ActionButton;
import {ShowIssuesDialogAction} from '../../browse/action/ShowIssuesDialogAction';
import {IssueStatsJson} from '../json/IssueStatsJson';
import {IssueFetcher} from '../IssueFetcher';
import IssueServerEventsHandler = api.issue.event.IssueServerEventsHandler;

export class ShowIssuesDialogButton extends ActionButton {

    constructor() {
        super(new ShowIssuesDialogAction());

        this.addClass('show-issues-dialog-button');

        this.updateShowIssuesDialogButton();

        this.initEventsListeners();
    }

    private initEventsListeners() {
        IssueServerEventsHandler.getInstance().onIssueCreated(() => {
            this.updateShowIssuesDialogButton();
        });

        IssueServerEventsHandler.getInstance().onIssueUpdated(() => {
            this.updateShowIssuesDialogButton();
        });
    }

    private updateShowIssuesDialogButton() {
        IssueFetcher.fetchIssueStats().then((stats: IssueStatsJson) => {
            this.toggleClass('has-assigned-issues', stats.assignedToMe > 0);
            this.getEl().setTitle((stats.assignedToMe == 0) ?
                                  'Publishing Issues' :
                                  'You have unclosed Publishing Issues');
        }).catch((reason: any) => {
            api.DefaultErrorHandler.handle(reason);
        });
    }
}
