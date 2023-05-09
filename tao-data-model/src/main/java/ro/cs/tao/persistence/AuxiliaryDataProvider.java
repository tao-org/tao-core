package ro.cs.tao.persistence;

import ro.cs.tao.eodata.AuxiliaryData;

import java.util.List;

public interface AuxiliaryDataProvider extends EntityProvider<AuxiliaryData, String> {

    List<AuxiliaryData> list(String userName);
    List<AuxiliaryData> list(String userName, String...locations);
    AuxiliaryData getByLocation(String location);
}
