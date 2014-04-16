package com.enonic.wem.api.module;

import com.enonic.wem.api.Identity;
import com.enonic.wem.api.form.Form;
import com.enonic.wem.api.schema.content.ContentTypeNames;

public interface Module
    extends Identity<ModuleKey, ModuleName>
{
    public ModuleKey getKey();

    public ModuleKey getModuleKey();

    public ModuleName getName();

    public ModuleVersion getVersion();

    public String getDisplayName();

    public String getInfo();

    public String getUrl();

    public String getVendorName();

    public String getVendorUrl();

    public Form getConfig();

    public ModuleVersion getMinSystemVersion();

    public ModuleVersion getMaxSystemVersion();

    public ModuleKeys getModuleDependencies();

    public ContentTypeNames getContentTypeDependencies();
}
