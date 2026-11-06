package ro.cs.tao.persistence;

import ro.cs.tao.userdb.SavedQuery;
import ro.cs.tao.userdb.SavedQueryId;

import java.util.List;

public interface SavedQueryProvider extends EntityProvider<SavedQuery, SavedQueryId> {
    List<SavedQuery> getByUserId(String userId);
}
