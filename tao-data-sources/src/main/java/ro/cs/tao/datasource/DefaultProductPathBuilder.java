package ro.cs.tao.datasource;

import ro.cs.tao.eodata.EOProduct;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class DefaultProductPathBuilder implements ProductPathBuilder {
    protected final String localPathFormat;
    protected final Path repositoryPath;

    public DefaultProductPathBuilder(Path repositoryPath, String localPathFormat) {
        this.localPathFormat = localPathFormat;
        this.repositoryPath = repositoryPath;
    }

    @Override
    public Path getProductPath(Path repositoryPath, EOProduct product) {
        // Products are assumed to be organized according to the pattern defined in tao.properties
        Date date = product.getAcquisitionDate();
        final String productName = product.getAttributeValue("filename") != null ?
                product.getAttributeValue("filename") : product.getName();
        Path productFolderPath = dateToPath(this.repositoryPath, date, this.localPathFormat);
        Path fullProductPath = productFolderPath.resolve(productName);
        if (!Files.exists(fullProductPath)) {
            // Maybe it's an archived product
            // maybe products are grouped by processing date
            date = product.getProcessingDate();
            if (date != null) {
                productFolderPath = dateToPath(this.repositoryPath, date, this.localPathFormat);
                fullProductPath = productFolderPath.resolve(productName);
                if (!Files.exists(fullProductPath)) {
                    fullProductPath = null;
                }
            } else {
                fullProductPath = null;
            }
        }
        return fullProductPath;
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
