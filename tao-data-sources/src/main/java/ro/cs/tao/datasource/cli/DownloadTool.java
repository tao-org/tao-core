package ro.cs.tao.datasource.cli;

import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.cli.*;
import org.locationtech.jts.geom.Geometry;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.datasource.*;
import ro.cs.tao.datasource.param.CommonParameterNames;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.GeometryAdapter;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.DateUtils;
import ro.cs.tao.utils.NetUtils;
import ro.cs.tao.utils.executors.monitoring.DownloadProgressListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DownloadTool {
    private static final Options options;
    private static final Set<String> specificArgs;
    //private static Notifiable progressMonitor;

    static {
        options = new Options();
        final ObjectReader jsonReader = JsonMapper.instance().readerFor(CliDescriptor.class);
        try (InputStream inputStream = DownloadTool.class.getResourceAsStream("arguments.json")) {
            CliDescriptor descriptor = jsonReader.readValue(inputStream);
            final List<CliOptionGroup> groups = descriptor.getGroups();
            if (groups != null) {
                for (CliOptionGroup group : groups) {
                    final List<CliOption> groupOptions = descriptor.findGroupOptions(group.getName());
                    OptionGroup optionGroup = new OptionGroup();
                    for (CliOption option : groupOptions) {
                        optionGroup.addOption(buildOption(option));
                    }
                    options.addOptionGroup(optionGroup);
                }
            }
            final List<CliOption> optionList = descriptor.findNonGroupedOptions();
            for (CliOption option : optionList) {
                options.addOption(buildOption(option));
            }
            //progressMonitor = new ProgressMonitor();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        specificArgs = new HashSet<>();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(180);
            formatter.printHelp("eo-search", options);
            System.exit(0);
        }
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(ReturnCode.INVALID_ARG);
        }
        System.exit(execute(commandLine));
    }

    private static Option buildOption(CliOption option) {
        Option.Builder optionBuilder = Option.builder(option.getOpt())
                .longOpt(option.getLongOpt())
                .argName(option.getName())
                .desc(option.getDescription())
                .required(option.isRequired());
        switch (option.getCardinality()) {
            case 0:
                optionBuilder.hasArg(false);
                break;
            case 1:
                optionBuilder.hasArg(true);
                break;
            case 2:
                //optionBuilder.hasArgs().numberOfArgs(option.getCardinality()).valueSeparator(option.getValueSeparator().charAt(0));
                break;
            default:
                optionBuilder.hasArgs().numberOfArgs(Option.UNLIMITED_VALUES).valueSeparator(option.getValueSeparator().charAt(0));
                break;
        }
        return optionBuilder.build();
    }

    private static int execute(CommandLine commandLine) {
        int retCode = ReturnCode.OK;
        try {
            if (commandLine.hasOption(Constants.HELP)) {
                processHelp(commandLine.getArgs());
            } else {
                specificArgs.addAll(Arrays.stream(commandLine.getOptions()).map(Option::getOpt).collect(Collectors.toSet()));
                final ConfigurationProvider cfgManager = ConfigurationManager.getInstance();
                final String targetFolder = getArgValue(commandLine, Constants.FOLDER, String.class,
                                                        cfgManager.getValue("products.location", null));
                if (targetFolder == null) {
                    throw new IllegalArgumentException(Constants.FOLDER);
                }
                String proxyType = getArgValue(commandLine, Constants.PROXY_TYPE, String.class,
                                               nullIfEmpty(cfgManager.getValue("proxy.type", null)));
                String proxyHost = getArgValue(commandLine, Constants.PROXY_HOST, String.class,
                                               nullIfEmpty(cfgManager.getValue("proxy.host", null)));
                String proxyPort = getArgValue(commandLine, Constants.PROXY_PORT, String.class,
                                               nullIfEmpty(cfgManager.getValue("proxy.port", null)));
                String proxyUser = getArgValue(commandLine, Constants.PROXY_USER, String.class,
                                               nullIfEmpty(cfgManager.getValue("proxy.user", null)));
                String proxyPwd = getArgValue(commandLine, Constants.PROXY_PASSWORD, String.class,
                                              nullIfEmpty(cfgManager.getValue("proxy.pwd", null)));
                NetUtils.setProxy(proxyType, proxyHost, proxyPort == null ? 0 : Integer.parseInt(proxyPort), proxyUser, proxyPwd);
                final DataSourceManager dataSourceManager = DataSourceManager.getInstance();
                final String satellite = commandLine.getOptionValue(Constants.SATELLITE);
                if (dataSourceManager.getNames(satellite) == null) {
                    throw new IllegalArgumentException(Constants.SATELLITE);
                }
                final String dataSourceName = commandLine.getOptionValue(Constants.REPOSITORY);
                final DataSource<?, ?> dataSource = dataSourceManager.get(satellite, dataSourceName);
                if (dataSource == null) {
                    throw new IllegalArgumentException(Constants.REPOSITORY);
                }
                DataSourceComponent dsComponent = new DataSourceComponent(satellite, dataSourceName);
                dsComponent.setFetchMode(getArgValue(commandLine, Constants.MODE, FetchMode.class, FetchMode.OVERWRITE));
                dsComponent.setUserCredentials(getArgValue(commandLine, Constants.USER, String.class, null),
                                               getArgValue(commandLine, Constants.PWD, String.class, null));
                final DataQuery query = dsComponent.createQuery();
                Integer value = getArgValue(commandLine, Constants.PAGE_SIZE, Integer.class, null);
                if (value != null) {
                    query.setPageSize(value);
                } else {
                    query.setPageSize(10);
                }
                value = getArgValue(commandLine, Constants.PAGE, Integer.class, null);
                if (value != null) {
                    query.setPageNumber(value);
                } else {
                    query.setPageNumber(1);
                }
                value = getArgValue(commandLine, Constants.LIMIT, Integer.class, null);
                if (value != null) {
                    query.setMaxResults(value);
                } else {
                    query.setMaxResults(20);
                }
                final Boolean queryOnly = getArgValue(commandLine, Constants.QUERY, Boolean.class, false);
                final List<String> parameters = getArgValues(commandLine, Constants.PARAMETERS, String.class);
                if (parameters == null) {
                    throw new IllegalArgumentException("Argument --parameters has no value");
                }
                NetUtils.setTimeout(getArgValue(commandLine, Constants.TIMEOUT, Integer.class, 30) * 1000);
                final Map<String, DataSourceParameter> supportedParameters = dataSource.getSupportedParameters().get(satellite);
                DataSourceParameter currentParameter;
                String[] tokens;
                for (String parameter : parameters) {
                    tokens = parameter.split("=");
                    currentParameter = supportedParameters.get(tokens[0]);
                    if (currentParameter == null) {
                        System.err.printf("Parameter [%s] is not supported by the data source [%s - %s] and was ignored.%n",
                                                         tokens[0], dataSourceName, satellite);
                        continue;
                    }
                    final Class<?> type = currentParameter.getType();
                    if (LocalDate.class.isAssignableFrom(type)) {
                        query.addParameter(tokens[0], LocalDate.parse(tokens[1], DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    } else if (LocalDateTime.class.isAssignableFrom(type)) {
                        query.addParameter(tokens[0], LocalDateTime.parse(tokens[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else if (Polygon2D.class.isAssignableFrom(type)) {
                        query.addParameter(tokens[0], Polygon2D.fromWKT(tokens[1]));
                    } else if (Integer.class.isAssignableFrom(type)) {
                        query.addParameter(tokens[0], Integer.parseInt(tokens[1]));
                    } else if (Double.class.isAssignableFrom(type)) {
                        query.addParameter(tokens[0], Double.parseDouble(tokens[1]));
                    } else {
                        query.addParameter(tokens[0], tokens[1]);
                    }
                }
                final String format = getArgValue(commandLine, Constants.FORMAT, String.class, "TEXT");
                if (!("json".equalsIgnoreCase(format) || "text".equalsIgnoreCase(format))) {
                    System.err.println("Invalid value for parameter [format]");
                    System.exit(1);
                }
                final List<EOProduct> results = query.execute();
                // If flagged, keep only products that cover the most of the AOI
                if (getArgValue(commandLine, Constants.FULL_COVERAGE, Boolean.class, false)) {
                    final QueryParameter<Polygon2D> footprintParam = (QueryParameter<Polygon2D>) query.getParameters().get(CommonParameterNames.FOOTPRINT);
                    if (footprintParam != null) {
                        final GeometryAdapter geometryAdapter = new GeometryAdapter();
                        final Geometry footprint = geometryAdapter.marshal(footprintParam.getValue().toWKT());
                        if (results != null && !results.isEmpty()) {
                            int maxIntersection = results.stream().mapToInt(p -> {
                                try {
                                    final String geometry = p.getGeometry();
                                    if (geometry != null) {
                                        Geometry productGeometry = geometryAdapter.marshal(geometry);
                                        return (int) (productGeometry.intersection(footprint).getArea() / productGeometry.getArea() * 100);
                                    }
                                    return 0;
                                } catch (Exception e) {
                                    System.err.printf("Geometry error for product %s: %s%n", p.getName(), e.getMessage());
                                    return 0;
                                }
                            }).max().getAsInt();
                            results.removeIf(p -> {
                                try {
                                    final String geometry = p.getGeometry();
                                    if (geometry != null) {
                                        Geometry productGeometry = geometryAdapter.marshal(geometry);
                                        return (int) (productGeometry.intersection(footprint).getArea() / productGeometry.getArea() * 100) < maxIntersection;
                                    }
                                    return true;
                                } catch (Exception e) {
                                    System.err.printf("Geometry error for product %s: %s%n", p.getName(), e.getMessage());
                                    return true;
                                }
                            });
                        }
                    }
                }
                // If flagged, keep only the latest product from the results
                if (getArgValue(commandLine, Constants.LATEST, Boolean.class, false)) {
                    if (results != null && !results.isEmpty()) {
                        EOProduct last = results.get(results.size() - 1);
                        results.clear();
                        results.add(last);
                    }
                }
                if (queryOnly) {
                    if (results == null || results.isEmpty()) {
                        System.out.println("No results");
                    } else {
                        final DateTimeFormatter dateFormat = DateUtils.getFormatterAtUTC("yyyy-MM-dd");
                        if ("text".equalsIgnoreCase(format)) {
                            for (EOProduct result : results) {
                                System.out.println("name=" + result.getName() + "; date=" + dateFormat.format(result.getAcquisitionDate()) + "; url=" + result.getLocation());
                            }
                        } else if ("json".equalsIgnoreCase(format)) {
                            System.out.println(JsonMapper.instance().writer().writeValueAsString(results));
                        }
                    }
                } else {
                    dsComponent.setProgressListener(new DownloadProgressListener() {
                        private String current;
                        @Override
                        public void started(String taskName) {
                            current = taskName;
                            System.out.print("\r" + current + ": 0.00%");
                        }

                        @Override
                        public void subActivityStarted(String subTaskName) {
                        }

                        @Override
                        public void subActivityEnded(String subTaskName) {
                        }

                        @Override
                        public void ended() {
                            System.out.print("\rCompleted " + targetFolder + File.separator +  current + "\r\n");
                        }

                        @Override
                        public void notifyProgress(double progressValue) {
                            System.out.print("\r" + current + ": " + String.format("%.2f%% ", progressValue * 100));
                        }

                        @Override
                        public void notifyProgress(double progressValue, double transferSpeed) {
                            System.out.print("\r" + current + ": " + String.format("%.2f%%, %.2fMB/s",
                                                                                   progressValue * 100,
                                                                                   transferSpeed));
                        }

                        @Override
                        public void notifyProgress(String subTaskName, double subTaskProgress) {
                        }

                        @Override
                        public void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress) {
                        }
                    });
                    System.out.printf("Retained %d products%n", results != null ? results.size() : 0);
                    final Properties properties = new Properties();
                    properties.put("auto.uncompress", "false");
                    properties.put("progress.interval", "3000");
                    dataSource.getProductFetchStrategy(satellite).addProperties(properties);
                    dsComponent.doFetch(results, null, targetFolder, null, properties);
                }
            }
        } catch (IllegalArgumentException | QueryException e1) {
            System.err.printf("Invalid argument or value [%s]%n", e1.getMessage());
            retCode = ReturnCode.INVALID_ARG;
        } catch (ClassNotFoundException e2) {
            System.err.printf("No plugin found for [%s]%n", e2.getMessage());
            retCode = ReturnCode.NO_PLUGIN;
        } catch (Exception e3) {
            System.err.println(e3.getMessage());
            retCode = ReturnCode.FATAL;
        }
        return retCode;
    }

    private static String nullIfEmpty(String string) {
        return string != null ? (string.isEmpty() ? null : string) : null;
    }

    private static <T> List<T> getArgValues(CommandLine cmd, String argName, Class<T> elementClass) {
        try {
            if (!cmd.hasOption(argName)) {
                return null;
            } else {
                final String value = String.join(" ", cmd.getOptionValues(argName)).trim();
                final List<String> optionValues = new ArrayList<>();
                final List<Integer> indices = new ArrayList<>();
                /*int currentIdx = value.indexOf('=');
                while (currentIdx > 0) {
                    indices.add(Math.max(value.lastIndexOf(' ', currentIdx), 0));
                    currentIdx = value.indexOf('=', currentIdx + 1);
                }*/
                int currentIdx = value.lastIndexOf('=');
                indices.add(value.length());
                while (currentIdx > 0) {
                    if (value.lastIndexOf(' ', currentIdx) > 0) {
                        indices.add(value.lastIndexOf(' ', currentIdx));
                    }
                    currentIdx = value.lastIndexOf('=', currentIdx - 1);
                }
                indices.add(0);
                for (int i = 1; i < indices.size(); i++) {
                    optionValues.add(value.substring(indices.get(i), indices.get(i - 1)).trim());
                }
                /*for (int i = 0; i < tokens.length; i++) {
                    optionValues
                }*/
                //optionValues.add(value.substring(indices.size() - 1).trim());
                if (Boolean.class.isAssignableFrom(elementClass)) {
                    return optionValues.stream().map(v -> elementClass.cast(Boolean.parseBoolean(v))).collect(Collectors.toList());
                } else if (Integer.class.isAssignableFrom(elementClass)) {
                    return optionValues.stream().map(v -> elementClass.cast(Integer.parseInt(v))).collect(Collectors.toList());
                } else if (Double.class.isAssignableFrom(elementClass)) {
                    return optionValues.stream().map(v -> elementClass.cast(Double.parseDouble(v))).collect(Collectors.toList());
                } else {
                    return optionValues.stream().map(elementClass::cast).collect(Collectors.toList());
                }
            }
        } finally {
            specificArgs.remove(argName);
        }
    }

    private static <T> T getArgValue(CommandLine cmd, String argName, Class<T> clazz, T defaultValue) {
        try {
            if (!cmd.hasOption(argName)) {
                return defaultValue;
            } else {
                final String optionValue = cmd.getOptionValue(argName);
                if (Boolean.class.isAssignableFrom(clazz)) {
                    return clazz.cast(true);
                } else if (Integer.class.isAssignableFrom(clazz)) {
                    return clazz.cast(Integer.parseInt(optionValue));
                } else if (Double.class.isAssignableFrom(clazz)) {
                    return clazz.cast(Double.parseDouble(optionValue));
                } else {
                    return clazz.cast(optionValue);
                }
            }
        } finally {
            specificArgs.remove(argName);
        }
    }

    private static <T extends Enum<T>> T getArgValue(CommandLine cmd, String argName, Class<T> clazz, T defaultValue) {
        if (!cmd.hasOption(argName)) {
            return defaultValue;
        } else {
            return Enum.valueOf(clazz, cmd.getOptionValue(argName));
        }
    }

    private static void processHelp(String[] values) throws ClassNotFoundException {
        final DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        switch (values.length) {
            case 0:
                throw new IllegalArgumentException(Constants.HELP);
            case 1:
                if (Constants.SATELLITE.equals(values[0])) {
                    final SortedSet<String> supportedSensors = dataSourceManager.getSupportedSensors();
                    System.out.println("Supported sensors:");
                    for (String sensor : supportedSensors) {
                        System.out.println(sensor);
                    }
                } else if (Constants.REPOSITORY.equals(values[0])) {
                    final Set<DataSource<?, ?>> dataSources = dataSourceManager.getRegisteredDataSources();
                    System.out.println("Supported repositories:");
                    for (DataSource<?, ?> ds : dataSources) {
                        System.out.println(ds.getId() + " @ " + ds.getConnectionString());
                    }
                }
                break;
            case 2:
            default:
                String combinedValues = String.join(" ", Arrays.copyOfRange(values, 1, values.length));
                final Map<String, DataSourceParameter> parameters = dataSourceManager.getSupportedParameters(values[0], combinedValues);
                if (parameters == null) {
                    throw new ClassNotFoundException(values[0] + " - " + combinedValues);
                }
                System.out.println("Parameters for " + values[0] + " - " + combinedValues);
                StringBuilder lineBuilder = new StringBuilder();
                StringBuilder descBuilder = new StringBuilder();
                String line;
                for (DataSourceParameter parameter : parameters.values()) {
                    boolean required = parameter.isRequired();
                    if (!required) {
                        lineBuilder.append("[");
                    }
                    lineBuilder.append("--").append(parameter.getName());
                    if (!required) {
                        lineBuilder.append("]");
                    }
                    lineBuilder.append(" ");
                    line = "--" + parameter.getName() + " <" + parameter.getType().getSimpleName().toLowerCase() + ">";
                    line = String.format("%1$-60s", line);
                    descBuilder.append(line).append(parameter.getLabel()).append(".");
                    final Object[] valueSet = parameter.getValueSet();
                    if (valueSet != null) {
                        descBuilder.append(" Possible values: ");
                        for (Object value : valueSet) {
                            descBuilder.append(value).append(",");
                        }
                        descBuilder.setCharAt(descBuilder.length() - 1, '.');
                    }
                    if (parameter.getDefaultValue() != null) {
                        descBuilder.append(" Default value: ").append(parameter.getDefaultValue());
                    }
                    descBuilder.append("\n");
                }
                System.out.println(lineBuilder);
                System.out.println(descBuilder);
                lineBuilder.setLength(0);
                descBuilder.setLength(0);
        }
    }

    /*private static class ProgressMonitor extends Notifiable {
        private final static String SIMPLE_MESSAGE = "%s: %.2f%%\r";
        private final static String MEDIUM_MESSAGE = "%s[%s]: %.2f%%\r";
        private final static String DETAIL_MESSAGE = "%s[%s]: %.2f%%[%.2f%%]\r";
        private final static String END_MESSAGE = "%s: %.2f%%\r\n";
        private TaskProgress current;

        ProgressMonitor() {
            subscribe(DataSourceTopic.PRODUCT_PROGRESS.value());
        }

        @Override
        protected void onMessageReceived(Message message) {
            String taskName;
            final String category = message.getTopic();
            boolean hasMainProgress;
            if (message instanceof ActivityStartMessage) {
                ActivityStartMessage casted = (ActivityStartMessage) message;
                taskName = ((ActivityStartMessage) message).getTaskName();
                current = new TaskProgress(taskName, category, 0.0);
                System.out.printf(SIMPLE_MESSAGE, taskName, current.getProgress());
            } else if (message instanceof SubActivityStartMessage) {
                SubActivityStartMessage casted = (SubActivityStartMessage) message;
                taskName = casted.getTaskName();
                if (current == null) {
                    current = new TaskProgress(taskName, category, 0.0, casted.getSubTaskName(), 0.0);
                } else {
                    current = new TaskProgress(taskName, category, current.getProgress(), casted.getSubTaskName(), 0.0);
                }
                if (Double.compare(current.getProgress(), 0.0) != 0) {
                    System.out.printf(DETAIL_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress() * 100, 0.0);
                } else {
                    System.out.printf(MEDIUM_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress() * 100);
                }
            } else if (message instanceof SubActivityEndMessage) {
                SubActivityEndMessage casted = (SubActivityEndMessage) message;
                taskName = casted.getTaskName();
                current = new TaskProgress(taskName, category, current.getProgress(), casted.getSubTaskName(), 100.0);
                if (Double.compare(current.getProgress(), 0.0) != 0) {
                    System.out.printf(DETAIL_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress() * 100, 100.0);
                } else {
                    System.out.printf(MEDIUM_MESSAGE, taskName, casted.getSubTaskName(), 100.0);
                }
            } else if (message instanceof SubActivityProgressMessage) {
                SubActivityProgressMessage casted = (SubActivityProgressMessage) message;
                taskName = casted.getTaskName();
                this.current = new TaskProgress(taskName, category, casted.getTaskProgress(), casted.getSubTaskName(), casted.getSubTaskProgress());
                if (Double.compare(current.getProgress(), 0.0) != 0) {
                    System.out.printf(DETAIL_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress() * 100, current.getSubTaskProgress().getValue() * 100);
                } else {
                    System.out.printf(MEDIUM_MESSAGE, taskName, casted.getSubTaskName(), current.getSubTaskProgress().getValue() * 100);
                }
            } else if (message instanceof ActivityProgressMessage) {
                ActivityProgressMessage casted = (ActivityProgressMessage) message;
                taskName = casted.getTaskName();
                this.current = new TaskProgress(taskName, category, casted.getProgress());
                System.out.printf(SIMPLE_MESSAGE, taskName, current.getProgress() * 100);
            } else if (message instanceof ActivityEndMessage) {
                ActivityEndMessage casted = (ActivityEndMessage) message;
                taskName = casted.getTaskName();
                System.out.printf((END_MESSAGE) + "%n", taskName, 100.0);
            }
        }
    }*/
}
