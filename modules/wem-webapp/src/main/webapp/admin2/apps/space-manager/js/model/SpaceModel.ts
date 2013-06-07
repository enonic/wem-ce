Ext.define('Admin.model.SpaceModel', {
    extend: 'Ext.data.Model',

    fields: <any[]>[
        'name', 'displayName', 'iconUrl', 'rootContentId',
        {name: 'createdTime', type: 'date', default: new Date()},
        {name: 'modifiedTime', type: 'date', default: new Date()},
        { name: 'editable', type: 'boolean' },
        { name: 'deletable', type: 'boolean' },
    ],

    idProperty: 'name'
});

module app_model {

    export interface SpaceModel extends Ext_data_Model {
        data:{
            name:string;
            displayName:string;
            iconUrl:string;
            rootContentId:number;
            createdTime:Date;
            modifiedTime:Date;
            editable:bool;
            deletable:bool;
        };
    }
}