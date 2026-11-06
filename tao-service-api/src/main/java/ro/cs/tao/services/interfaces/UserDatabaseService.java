package ro.cs.tao.services.interfaces;

import ro.cs.tao.userdb.QueryTemplate;
import ro.cs.tao.userdb.SavedQuery;

import java.util.List;
import java.util.Map;

public interface UserDatabaseService extends TAOService {

    String startDatabase(String userId);

    void stopDatabase(String userId);

    List<Map<String, Object>> executeQuery(String connString, String sql);

    QueryStatus startQuery(String userId, String connString, String sql);

    QueryStatus getQueryStatus(String userId, String queryId);

    QueryStatus cancelQuery(String userId, String queryId);

    List<QueryTemplate> listQueryTemplates();

    QueryTemplate getQueryTemplate(String name);

    List<SavedQuery> listSavedQueries(String userId);

    SavedQuery getSavedQuery(String userId, String name);

    SavedQuery saveQuery(String userId, String name, String description, String sql);

    final class QueryStatus {
        public enum State {
            RUNNING,
            SUCCEEDED,
            FAILED,
            CANCELLED,
            NOT_FOUND
        }

        private String id;
        private State state;
        private List<Map<String, Object>> rows;
        private String message;
        private long startedAt;
        private long completedAt;

        public QueryStatus() {
        }

        public QueryStatus(String id, State state, List<Map<String, Object>> rows, String message,
                           long startedAt, long completedAt) {
            this.id = id;
            this.state = state;
            this.rows = rows;
            this.message = message;
            this.startedAt = startedAt;
            this.completedAt = completedAt;
        }

        public String getId() { return id; }
        public State getState() { return state; }
        public List<Map<String, Object>> getRows() { return rows; }
        public String getMessage() { return message; }
        public long getStartedAt() { return startedAt; }
        public long getCompletedAt() { return completedAt; }

        public boolean isFinished() {
            return state == State.SUCCEEDED || state == State.FAILED || state == State.CANCELLED || state == State.NOT_FOUND;
        }
    }
}
