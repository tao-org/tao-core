package ro.cs.tao.services.model.component;

import java.util.Map;

public class NamingRuleTokens {
    private String sensor;
    private int minIndex;
    private int maxIndex;
    private Map<String, String> tokens;

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public int getMinIndex() {
        return minIndex;
    }

    public void setMinIndex(int minIndex) {
        this.minIndex = minIndex;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
    }

    public Map<String, String> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, String> tokens) {
        this.tokens = tokens;
    }
}
