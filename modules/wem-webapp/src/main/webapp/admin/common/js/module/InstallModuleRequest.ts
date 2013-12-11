module api_module {

    export class InstallModuleRequest extends ModuleResourceRequest<api_module_json.ModuleSummaryJson> {

        private uploader:any;
        private triggerElement:api_dom.Element;
        private isExternalTriggerElement:boolean = false;

        private deferred:JQueryDeferred<api_rest.Response>;

        constructor(triggerEl?:api_dom.Element) {
            super();
            if (triggerEl) {
                this.triggerElement = triggerEl;
                this.isExternalTriggerElement = true;
            } else {
                this.triggerElement = new api_dom.ButtonEl("trigger-el");
                this.triggerElement.hide();
                api_dom.Body.get().appendChild(this.triggerElement);
            }
            this.deferred = jQuery.Deferred<api_rest.Response>();

            this.uploader = this.createUploader(this.triggerElement);
        }

        getRequestPath():api_rest.Path {
            return api_rest.Path.fromParent(super.getResourcePath(), "install");
        }

        send():JQueryPromise<api_rest.Response> {
            if (!this.isExternalTriggerElement) {
                this.triggerElement.getHTMLElement().click();
                this.triggerElement.remove();
            }
            return this.deferred;
        }

        done(fn:(resp:api_rest.JsonResponse<api_module_json.ModuleSummaryJson>[])=>void) {
            this.deferred.done(fn);
        }

        fail(fn:(resp:api_rest.Response)=>void) {
            this.deferred.fail(fn);
        }

        stop() {
            this.uploader.stop();
        }

        private createUploader(triggerElement:api_dom.Element):any {
            if (!plupload) {
                throw new Error("ImageUploader: plupload not found, check if it is included in page.");
            }
            this.uploader = new plupload.Uploader({
                runtimes: 'gears,html5,flash,silverlight,browserplus',
                multi_selection: true,
                browse_button: triggerElement.getId(),
                url: this.getRequestPath(),
                multipart: true,
                drop_element: triggerElement.getId(),
                flash_swf_url: api_util.getUri('common/js/fileupload/plupload/js/plupload.flash.swf'),
                silverlight_xap_url: api_util.getUri('common/js/fileupload/plupload/js/plupload.silverlight.xap'),
                filters: [
                    {title: 'Zip Archive', extensions: 'zip'}
                ]
            });

            this.uploader.bind('QueueChanged', (up) => {
                up.start();
            });

            var results:api_rest.JsonResponse<api_module_json.ModuleSummaryJson>[] = [];
            this.uploader.bind('FileUploaded', (up, file, response) => {
                if (response && response.status === 200) {
                    results.push(new api_rest.JsonResponse<api_module_json.ModuleSummaryJson>(response.response));
                } else {
                    this.deferred.reject(new api_rest.RequestError(response.status, response.statusText, response.responseText, null));
                }

            });

            this.uploader.bind('UploadComplete', (up, files) => {
                this.deferred.resolve(new InstallModuleResponse(results));
            });

            this.uploader.bind('Error', (up, files) => {
                this.deferred.reject(new api_rest.RequestError(null, files.code, files.message, null));
            });

            this.uploader.init();

            return this.uploader;
        }

    }

    export class InstallModuleResponse extends api_rest.Response {

        private modules:ModuleSummary[] = [];
        private errors:string[] = [];

        constructor (moduleResponses:api_rest.JsonResponse<api_module_json.ModuleSummaryJson>[]) {
            super();
            moduleResponses.forEach((response:api_rest.JsonResponse<api_module_json.ModuleSummaryJson>) => {
                var responseJson = response.getJson();
                if (responseJson.result) {
                    this.modules.push(new ModuleSummary(responseJson.result));
                } else {
                    this.errors.push(responseJson.error.message);
                }
            });
        }

        getModules():ModuleSummary[] {
            return this.modules;
        }

        getErrors():string[] {
            return this.errors;
        }
    }
}