module LiveEdit.model {
    var $ = $liveedit;

    export class Paragraph extends LiveEdit.model.Base {

        private selectedParagraph:JQuery = null;
        private modes:any = {};
        private currentMode:number;

        constructor() {
            super();

            this.cssSelector = '[data-live-edit-type=paragraph]';
            this.modes = {
                UNSELECTED: 0,
                SELECTED: 1,
                EDIT: 2
            };
            this.currentMode = this.modes.UNSELECTED;
            this.attachMouseOverEvent();
            this.attachMouseOutEvent();
            this.attachClickEvent();
            this.registerGlobalListeners();

            console.log('Paragraph model instantiated. Using jQuery ' + $().jquery);
        }


        registerGlobalListeners():void {
            $(window).on('shader.onClick component.onDeselect', (event:JQueryEventObject) => {
                this.leaveEditMode();
            });
        }


        // Override base attachClickEvent
        attachClickEvent():void {
            $(document).on('click contextmenu touchstart', this.cssSelector, (event:JQueryEventObject) => {
                this.handleClick(event);
            });
        }


        handleClick(event):void {
            event.stopPropagation();
            event.preventDefault();

            // Remove the inlined css cursor when the mode is not EDIT.
            if (this.selectedParagraph && !(this.currentMode === this.modes.EDIT)) {
                this.selectedParagraph.css('cursor', '');
            }

            var $paragraph = $(event.currentTarget);

            // Reset mode in case another paragraph is selected.
            if (!$paragraph.is(this.selectedParagraph)) {
                this.currentMode = this.modes.UNSELECTED;
            }

            this.selectedParagraph = $paragraph;

            if (this.currentMode === this.modes.UNSELECTED) {
                this.setSelectMode(event);
            } else if (this.currentMode === this.modes.SELECTED) {
                this.setEditMode();
            } else {
            }

        }


        setSelectMode(event:JQueryEventObject):void {
            this.selectedParagraph.css('cursor', 'url(../../../admin2/live-edit/images/pencil.png) 0 40, text');

            this.currentMode = this.modes.SELECTED;

            // Make sure Chrome does not selects the text on context click
            if (window.getSelection) {
                window.getSelection().removeAllRanges();
            }

            var pagePosition:any = {
                x: event.pageX,
                y: event.pageY
            };

            $(window).trigger('component.onSelect', [this.selectedParagraph, pagePosition]);
            $(window).trigger('component.onParagraphSelect', [this.selectedParagraph]);
        }


        setEditMode():void {
            var $paragraph = this.selectedParagraph;

            $(window).trigger('component.onParagraphEdit', [this.selectedParagraph]);

            $paragraph.css('cursor', 'text');
            $paragraph.addClass('live-edit-edited-paragraph');

            this.currentMode = this.modes.EDIT;
        }


        leaveEditMode():void {
            var $paragraph = this.selectedParagraph;
            if ($paragraph === null) {
                return;
            }
            $(window).trigger('component.onParagraphEditLeave', [this.selectedParagraph]);

            $paragraph.css('cursor', '');
            $paragraph.removeClass('live-edit-edited-paragraph');
            this.selectedParagraph = null;

            this.currentMode = this.modes.UNSELECTED;
        }
    }
}
