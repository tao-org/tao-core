package ro.cs.tao.datasource.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.cli.*;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.*;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.TaskProgress;
import ro.cs.tao.messaging.progress.*;
import ro.cs.tao.utils.NetUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class DownloadTool {
    private static final Options options;
    private static Notifiable progressMonitor;

    static {
        options = new Options();
        ObjectMapper objectMapper = new ObjectMapper();
        final ObjectReader jsonReader = objectMapper.reader();
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
            progressMonitor = new ProgressMonitor();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("download-tool", options);
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
            default:
                optionBuilder.hasArgs().valueSeparator(option.getValueSeparator().charAt(1));
                break;
        }
        return optionBuilder.build();
    }

    private static int execute(CommandLine commandLine) {
        int retCode = ReturnCode.OK;
        try {

            if (commandLine.hasOption(Constants.HELP)) {
                processHelp(commandLine.getOptionValues(Constants.HELP));
            } else {
                final ConfigurationManager cfgManager = ConfigurationManager.getInstance();
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
                final DataSource dataSource = dataSourceManager.get(satellite, dataSourceName);
                if (dataSource == null) {
                    throw new IllegalArgumentException(Constants.REPOSITORY);
                }
                DataSourceComponent dsComponent = new DataSourceComponent(satellite, dataSourceName);
                dsComponent.setFetchMode(getArgValue(commandLine, Constants.MODE, FetchMode.class, FetchMode.OVERWRITE));
                dsComponent.setUserCredentials(getArgValue(commandLine, Constants.USER, String.class, null),
                                               getArgValue(commandLine, Constants.PWD, String.class, null));
                final String[] dsArgs = commandLine.getArgs();
                final DataQuery query = dsComponent.createQuery();
                for (int i = 0; i < dsArgs.length; i += 2) {
                    query.addParameter(dsArgs[0], dsArgs[1]);
                }
                final List<EOProduct> results = query.execute();
                if (getArgValue(commandLine, Constants.QUERY, Boolean.class, false)) {
                    if (results == null || results.size() == 0) {
                        System.out.println("No results");
                    } else {
                        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        for (EOProduct result : results) {
                            System.out.println("name=" + result.getName() + "date=" + dateFormat.format(result.getAcquisitionDate()) + "; url=" + result.getLocation());
                        }
                    }
                } else {
                    dsComponent.doFetch(results, null, targetFolder);
                }
            }
        } catch (IllegalArgumentException | QueryException e1) {
            System.err.println(String.format("Invalid argument or value [%s]", e1.getMessage()));
            retCode = ReturnCode.INVALID_ARG;
        } catch (ClassNotFoundException e2) {
            System.err.println(String.format("No plugin found for [%s]", e2.getMessage()));
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

    private static <T> T getArgValue(CommandLine cmd, String argName, Class<T> clazz, T defaultValue) {
        if (!cmd.hasOption(argName)) {
            return defaultValue;
        } else {
            final String optionValue = cmd.getOptionValue(argName);
            if (Boolean.class.isAssignableFrom(clazz)) {
                return clazz.cast(true);
            } else if (Integer.class.isAssignableFrom(clazz)){
                return clazz.cast(Integer.parseInt(optionValue));
            } else if (Double.class.isAssignableFrom(clazz)) {
                return clazz.cast(Double.parseDouble(optionValue));
            } else {
                return clazz.cast(optionValue);
            }
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
                final SortedSet<String> supportedSensors = dataSourceManager.getSupportedSensors();
                System.out.println("Supported sensors:");
                for (String sensor : supportedSensors) {
                    System.out.println(sensor);
                }
                break;
            case 1:
                final List<String> names = dataSourceManager.getNames(values[0]);
                System.out.println("Repositories for " + values[0]);
                for (String name : names) {
                    System.out.println(name);
                }
                break;
            case 2:
                final Map<String, DataSourceParameter> parameters = dataSourceManager.getSupportedParameters(values[0], values[1]);
                if (parameters == null) {
                    throw new ClassNotFoundException(values[0] + " " + values[1]);
                }
                System.out.println("Parameters for " + values[0] + " " + values[1]);
                StringBuilder lineBuilder = new StringBuilder();
                StringBuilder descBuilder = new StringBuilder();
                for (DataSourceParameter parameter : parameters.values()) {
                    boolean required = parameter.isRequired();
                    if (!required) {
                        lineBuilder.append("[");
                    }
                    lineBuilder.append("--").append(parameter.getName());
                    if (!required) {
                        lineBuilder.append("] ");
                    }
                    descBuilder.append("--").append(parameter.getName())
                            .append(" <").append(parameter.getType().getSimpleName()).append(">\t")
                            .append(parameter.getLabel()).append(".");
                    final Object[] valueSet = parameter.getValueSet();
                    if (valueSet != null) {
                        descBuilder.append("Possible values: ");
                        for (Object value : valueSet) {
                            descBuilder.append(value).append(",");
                        }
                        descBuilder.setCharAt(descBuilder.length() - 1, '.');
                    }
                    if (parameter.getDefaultValue() != null) {
                        descBuilder.append("Default value: ").append(parameter.getDefaultValue());
                    }
                    descBuilder.append("\n");
                }
                System.out.println(lineBuilder.toString());
                System.out.println(descBuilder.toString());
                lineBuilder.setLength(0);
                descBuilder.setLength(0);
                break;
            default:
                throw new IllegalArgumentException(Constants.HELP);
        }
    }

    private static class ProgressMonitor extends Notifiable {
        private final static String SIMPLE_MESSAGE = "%s: %.2f%%\r";
        private final static String MEDIUM_MESSAGE = "%s[%s]: %.2f%%\r";
        private final static String DETAIL_MESSAGE = "%s[%s]: %.2f%%[%.2f%%]\r";
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
                System.out.print(String.format(SIMPLE_MESSAGE, taskName, current.getProgress()));
            } else if (message instanceof SubActivityStartMessage) {
                SubActivityStartMessage casted = (SubActivityStartMessage) message;
                taskName = casted.getTaskName();
                if (current == null) {
                    current = new TaskProgress(taskName, category, 0.0, casted.getSubTaskName(), 0.0);
                } else {
                    current = new TaskProgress(taskName, category, current.getProgress(), casted.getSubTaskName(), 0.0);
                }
                if (Double.compare(current.getProgress(), 0.0) != 0) {
                    System.out.print(String.format(DETAIL_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress(), 0.0));
                } else {
                    System.out.print(String.format(MEDIUM_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress()));
                }
            } else if (message instanceof SubActivityEndMessage) {
                SubActivityEndMessage casted = (SubActivityEndMessage) message;
                taskName = casted.getTaskName();
                current = new TaskProgress(taskName, category, current.getProgress(), casted.getSubTaskName(), 100.0);
                if (Double.compare(current.getProgress(), 0.0) != 0) {
                    System.out.print(String.format(DETAIL_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress(), 100.0));
                } else {
                    System.out.print(String.format(MEDIUM_MESSAGE, taskName, casted.getSubTaskName(), 100.0));
                }
            } else if (message instanceof SubActivityProgressMessage) {
                SubActivityProgressMessage casted = (SubActivityProgressMessage) message;
                taskName = casted.getTaskName();
                this.current = new TaskProgress(taskName, category, casted.getTaskProgress(), casted.getSubTaskName(), casted.getSubTaskProgress());
                if (Double.compare(current.getProgress(), 0.0) != 0) {
                    System.out.print(String.format(DETAIL_MESSAGE, taskName, casted.getSubTaskName(), current.getProgress(), current.getSubTaskProgress().getValue()));
                } else {
                    System.out.print(String.format(MEDIUM_MESSAGE, taskName, casted.getSubTaskName(), current.getSubTaskProgress().getValue()));
                }
            } else if (message instanceof ActivityProgressMessage) {
                ActivityProgressMessage casted = (ActivityProgressMessage) message;
                taskName = casted.getTaskName();
                this.current = new TaskProgress(taskName, category, casted.getProgress());
                System.out.print(String.format(SIMPLE_MESSAGE, taskName, current.getProgress()));
            }
        }
    }
}
