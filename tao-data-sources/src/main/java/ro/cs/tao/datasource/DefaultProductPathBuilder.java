package ro.cs.tao.datasource;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.datasource.remote.ProductFormat;
import ro.cs.tao.eodata.EOProduct;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

public class DefaultProductPathBuilder implements ProductPathBuilder {
    protected String localPathFormat;
    protected final Path repositoryPath;
    protected final ProductFormat productFormat;
    protected final boolean appendSAFE;
    protected final Properties properties;
    protected final Logger logger;

    public DefaultProductPathBuilder(Path repositoryPath, String localPathFormat, Properties properties) {
        this.logger = Logger.getLogger(getClass().getName());
        this.localPathFormat = localPathFormat;
        this.repositoryPath = repositoryPath;
        this.properties = properties;
        this.productFormat = properties != null ?
                EnumUtils.getEnumConstantByName(ProductFormat.class, properties.getProperty("product.format", "folder").toUpperCase()) :
                ProductFormat.FOLDER;
        this.appendSAFE = properties != null && Boolean.parseBoolean(properties.getProperty("path.append.safe", "false"));
    }

    @Override
    public Path getProductPath(Path repositoryPath, EOProduct product) {
        // Products are assumed to be organized according to the pattern defined in tao.properties
        Date date = product.getAcquisitionDate();
        String productName = getProductName(product);
        Path productFolderPath = dateToPath(this.repositoryPath, date, this.localPathFormat);
        Path fullProductPath = productFolderPath.resolve(productName);
        logger.fine(String.format("Looking for product %s into %s", product.getName(), fullProductPath));
        if (!Files.exists(fullProductPath)) {
            // Maybe it's an archived product
            // maybe products are grouped by processing date
            date = product.getProcessingDate();
            if (date != null) {
                productFolderPath = dateToPath(this.repositoryPath, date, this.localPathFormat);
                fullProductPath = productFolderPath.resolve(productName);
                logger.fine(String.format("Alternatively looking for product %s into %s", product.getName(), fullProductPath));
                if (!Files.exists(fullProductPath)) {
                    fullProductPath = null;
                }
            } else {
                fullProductPath = null;
            }
        }
        return fullProductPath;
    }

    protected String getProductName(EOProduct product) {
        String name = (product.getAttributeValue("filename") != null ?
                            product.getAttributeValue("filename") : product.getName());
        if (name.endsWith(".SAFE") && !this.appendSAFE) {
            name = name.replace(".SAFE", "");
        } else if (this.appendSAFE && !name.endsWith(".SAFE")) {
            name += ".SAFE";
        }
        // the friendlyName of a productFormat is the extension (or '' for folders)
        name += this.productFormat.friendlyName();
        return name;
    }

    protected Path dateToPath(Path root, Date date, String formatOnDisk) {
        final DateFormatTokenizer tokenizer = new DateFormatTokenizer(formatOnDisk);
        return root.resolve(tokenizer.getYearPart(date))
                .resolve(tokenizer.getMonthPart(date))
                .resolve(tokenizer.getDayPart(date));
    }

    private class DateFormatTokenizer {
        private String yearPart;
        private String monthPart;
        private String dayPart;
        private String hourPart;
        private String minutePart;
        private String secondPart;

        DateFormatTokenizer(String format) {
            yearPart = "";
            monthPart = "";
            dayPart = "";
            hourPart = "";
            minutePart = "";
            secondPart = "";
            parse(format);
        }

        String getYearPart(Date date) {
            return new SimpleDateFormat(yearPart).format(date);
        }

        String getMonthPart(Date date) {
            return new SimpleDateFormat(monthPart).format(date);
        }

        String getDayPart(Date date) {
            return new SimpleDateFormat(dayPart).format(date);
        }

        public String getHourPart(Date date) {
            return new SimpleDateFormat(hourPart).format(date);
        }

        public String getMinutePart(Date date) {
            return new SimpleDateFormat(minutePart).format(date);
        }

        public String getSecondPart(Date date) {
            return new SimpleDateFormat(secondPart).format(date);
        }

        @SuppressWarnings("StringConcatenationInLoop")
        private void parse(String format) {
            Scanner scanner = new Scanner(format);
            scanner.useDelimiter("");
            while (scanner.hasNext()) {
                String ch = scanner.next();
                switch (ch) {
                    case "y":
                    case "Y":
                        yearPart += ch;
                        break;
                    case "M":
                        monthPart += ch;
                        break;
                    case "d":
                    case "D":
                        dayPart += ch;
                        break;
                    case "h":
                    case "H":
                        hourPart += ch;
                        break;
                    case "m":
                        minutePart += ch;
                        break;
                    case "s":
                        secondPart += ch;
                        break;
                    default:
                        break;
                }
            }
        }
    }
}