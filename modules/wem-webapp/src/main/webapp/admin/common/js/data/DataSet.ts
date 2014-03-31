module api.data {

    export class DataSet extends Data {

        private dataById: {[s:string] : Data;} = {};

        private dataArray: Data[] = [];

        constructor(name: string) {
            super(name);
        }

        nameCount(name: string): number {
            var count = 0;
            for (var i in this.dataById) {
                var data = this.dataById[i];
                if (data.getName() === name) {
                    count++;
                }
            }
            return count;
        }

        addData(data: Data) {
            data.setParent(this);
            var index = this.nameCount(data.getName());
            data.setArrayIndex(index);
            var dataId = new DataId(data.getName(), index);
            this.dataById[dataId.toString()] = data;
            this.dataArray.push(data);
        }

        removeData(idToRemove: DataId): Data {

            var dataToRemove = this.dataById[idToRemove.toString()];

            api.util.assertNotNull(dataToRemove, "Data to remove [" + idToRemove + "] not found in dataById");

            // Remove from map
            delete this.dataById[idToRemove.toString()];

            // Resolve index of Data to remove
            var indexToRemove = -1;
            this.dataArray.forEach((data: Data, index: number) => {
                if (data.getId().toString() == idToRemove.toString()) {
                    indexToRemove = index;
                }
            });
            api.util.assert(indexToRemove > -1, "Data to remove [" + idToRemove + "] not found in dataArray");

            // Remove Data from dataArray
            this.dataArray.splice(indexToRemove, 1);

            // Update the array index of the Data-s coming after...
            var dataArray = this.getDataByName(idToRemove.getName());

            for (var i = idToRemove.getArrayIndex(); i < dataArray.length; i++) {
                var data = dataArray[i];
                delete this.dataById[data.getId().toString()];
                data.setArrayIndex(i);
                this.dataById[data.getId().toString()] = data;
            }

            return dataToRemove;
        }

        getDataArray(): Data[] {
            var datas = [];
            this.dataArray.forEach((data: Data) => {
                datas.push(data);
            });
            return datas;
        }

        getData(dataId: string): Data {
            return this.getDataFromDataId(DataId.from(dataId));
        }

        getDataFromDataPath(path: DataPath): Data {

            if (path.elementCount() > 1) {
                return this.doForwardGetData(path);
            }
            else {
                return this.getDataFromDataId(path.getFirstElement().toDataId());
            }
        }

        private doForwardGetData(path: DataPath): Data {

            var data = this.getDataFromDataId(path.getFirstElement().toDataId());
            if (data == null) {
                return null;
            }

            return data.toDataSet().getDataFromDataPath(path.asNewWithoutFirstPathElement());
        }

        getDataFromDataId(dataId: DataId): Data {
            return this.dataById[dataId.toString()];
        }

        getDataByName(name: string): Data[] {

            var matches: Data[] = [];
            for (var i in this.dataById) {
                var data: Data = this.dataById[i];
                if (name === data.getName()) {
                    matches.push(data);
                }
            }

            return matches;
        }

        getProperty(path: string): Property {
            return this.getPropertyFromDataPath(DataPath.fromString(path));
        }

        getPropertyFromDataPath(path: DataPath): Property {
            var data = this.getDataFromDataPath(path);
            return data ? data.toProperty() : null;
        }

        getPropertyFromDataId(dataId: DataId): Property {
            var data = this.getDataFromDataId(dataId);
            return data ? data.toProperty() : null;
        }

        getPropertiesByName(name: string): Property[] {

            var matches: Property[] = [];
            this.getDataByName(name).forEach((data: Data) => {
                if (name === data.getName() && data instanceof Property) {
                    matches.push(<Property>data);
                }
                else if (name === data.getName() && !(data instanceof Property)) {
                    throw new Error("Expected data of type Property with name '" + name + "', got: " + data);
                }
            });
            return matches;
        }

        getDataSet(path: string): DataSet {
            return this.getDataSetFromDataPath(DataPath.fromString(path));
        }

        getDataSetFromDataPath(path: DataPath): DataSet {
            var data = this.getDataFromDataPath(path);
            if (data == null) {
                return null;
            }
            return data.toDataSet();
        }

        getDataSets(): DataSet[] {

            var dataSets: DataSet[] = [];
            for (var i in this.dataById) {
                var data: Data = this.dataById[i];
                if (data instanceof DataSet) {
                    dataSets.push(data.toDataSet());
                }
            }
            return dataSets;
        }

        getDataSetsByName(name: string): DataSet[] {

            var matches: DataSet[] = [];
            this.getDataByName(name).forEach((data: Data) => {
                if (name === data.getName() && data instanceof DataSet) {
                    matches.push(<DataSet>data);
                }
                else if (name === data.getName() && !(data instanceof DataSet)) {
                    throw new Error("Expected data of type DataSet with name '" + name + "', got: " + data);
                }
            });
            return matches;
        }

        toDataSetJson(): api.data.json.DataTypeWrapperJson {

            return <api.data.json.DataTypeWrapperJson>{ DataSet: <api.data.json.DataSetJson>{
                name: this.getName(),
                set: Data.datasToJson(this.getDataArray())
            }};
        }

        equals(dataSet: DataSet): boolean {
            var dataArray1 = this.dataArray;
            var dataArray2 = dataSet.dataArray;

            if (dataArray1.length != dataArray2.length) {
                return false;
            }

            for (var i = 0; i < dataArray1.length; i++) {
                var data1 = dataArray1[i];
                var data2 = dataSet.getData(data1.getId().toString());

                if (!data1.equals(data2)) {
                    return false;
                }
            }

            return true;
        }

    }
}