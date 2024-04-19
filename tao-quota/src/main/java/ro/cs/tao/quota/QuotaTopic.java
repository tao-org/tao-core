package ro.cs.tao.quota;

import ro.cs.tao.messaging.Topic;

public class QuotaTopic extends Topic {

    public static final Topic USER_STORAGE_USAGE = Topic.create(Topic.RESOURCES.value(), "user.storage");

    public static final Topic USER_CPU_USAGE = Topic.create(Topic.RESOURCES.value(), "user.cpu");

    public QuotaTopic(String category, String tag) {
        super(category, tag);
    }
}
