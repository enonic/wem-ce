module api_content_page {

    export class PageTemplate extends Template<PageTemplateKey,PageTemplateName> {

        private canRender: api_schema_content.ContentTypeName[];

        private descriptor:PageDescriptor;

        constructor(builder: PageTemplateBuilder) {
            super(builder);
            this.canRender = builder.canRender;
            this.descriptor = builder.descriptor;
        }

        getCanRender(): api_schema_content.ContentTypeName[] {
            return this.canRender;
        }

        getDescriptor():PageDescriptor {
            return this.descriptor;
        }
    }

    export class PageTemplateBuilder extends TemplateBuilder<PageTemplateKey,PageTemplateName> {

        canRender: api_schema_content.ContentTypeName[] = [];

        descriptor:PageDescriptor;

        fromJson(json: api_content_page_json.PageTemplateJson): PageTemplateBuilder {

            this.setKey(PageTemplateKey.fromString(json.key));
            this.setName(new PageTemplateName(json.name));
            this.setDisplayName(json.displayName);
            this.setDescriptorModuleResourceKey(api_module.ModuleResourceKey.fromString(json.descriptorModuleResourceKey));
            this.descriptor = new PageDescriptorBuilder().fromJson(json.descriptor).build();
            this.setConfig(api_data.DataFactory.createRootDataSet(json.config));
            json.canRender.forEach((name: string)=> {
                this.canRender.push(new api_schema_content.ContentTypeName(name))
            });
            return this;
        }

        setCanRender(value: api_schema_content.ContentTypeName[]): PageTemplateBuilder {
            this.canRender = value;
            return this;
        }

        public build(): PageTemplate {
            return new PageTemplate(this);
        }
    }
}