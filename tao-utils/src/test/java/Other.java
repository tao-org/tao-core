import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Other {

    public static void main(String[] args) throws IOException {
        final Path json = Paths.get("D:\\datasets.json");
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectReader objectReader = mapper.readerForListOf(Bean.class);
        List<Bean> datasets = new ArrayList<>();
        try (InputStream stream = Files.newInputStream(json)) {
            datasets.addAll(objectReader.readValue(stream));
        }
        final Map<String, Map<String, List<Bean>>> grouped = new TreeMap<>();
        for (Bean dataSet : datasets) {
            final Map<String, List<Bean>> map = grouped.computeIfAbsent(dataSet.getCategory(), k -> new TreeMap<>());
            final String sensor = dataSet.getSensor();
            final int idx = sensor.indexOf('(');
            if (idx > 0) {
                dataSet.setSensor(sensor.substring(0, idx).trim());
                dataSet.setDataSourceName(dataSet.getDataSourceName() + " " + sensor.substring(idx));
                map.computeIfAbsent(dataSet.getSensor(), k -> new ArrayList<>()).add(dataSet);
            } else {
                map.computeIfAbsent(dataSet.getSensor(), k -> new ArrayList<>()).add(dataSet);
            }
        }
        ///Path md = Paths.get("D:\\datasets.md");
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Map<String, List<Bean>>> entry : grouped.entrySet()) {
            // per category
            final Map<String, List<Bean>> sensors = entry.getValue();
            builder.append("# ").append(entry.getKey()).append(" sensors\n***\n");
            // per sensor
            for (Map.Entry<String, List<Bean>> sensor : sensors.entrySet()) {
                builder.append("### ").append(sensor.getKey()).append("\n");
                final List<Bean> list = sensor.getValue();
                list.sort(Comparator.comparing(Bean::getDataSourceName));
                for (int i = 0; i < list.size(); i++) {
                    Bean item = list.get(i);
                    builder.append("#### Source : ").append(item.getDataSourceName()).append("\n");
                    builder.append("> **Description** : ").append(item.getDescription()).append("  \n");
                    builder.append("> **Spatial coverage** : *").append(item.getSpatialCoverage()).append("*  \n");
                    builder.append("> **Temporal coverage** : *").append(item.getTemporalCoverage()).append("*  \n");
                    builder.append("\n---\n");
                }
            }
            Files.write(Paths.get("D:\\" + entry.getKey().toLowerCase() + ".md"), builder.toString().getBytes(StandardCharsets.UTF_8));
            builder.setLength(0);
        }
    }

    private static class Bean {
        private String category;
        private String dataSourceName;
        private String description;
        private String sensor;
        private String temporalCoverage;
        private String spatialCoverage;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        public void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSensor() {
            return sensor;
        }

        public void setSensor(String sensor) {
            this.sensor = sensor;
        }

        public String getTemporalCoverage() {
            return temporalCoverage;
        }

        public void setTemporalCoverage(String temporalCoverage) {
            this.temporalCoverage = temporalCoverage;
        }

        public String getSpatialCoverage() {
            return spatialCoverage;
        }

        public void setSpatialCoverage(String spatialCoverage) {
            this.spatialCoverage = spatialCoverage;
        }
    }
}
