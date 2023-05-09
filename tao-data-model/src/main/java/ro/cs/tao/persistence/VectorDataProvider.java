package ro.cs.tao.persistence;

import ro.cs.tao.eodata.VectorData;

import java.util.List;

public interface VectorDataProvider extends EntityProvider<VectorData, String> {

    List<VectorData> getByLocation(String...locations);

}
