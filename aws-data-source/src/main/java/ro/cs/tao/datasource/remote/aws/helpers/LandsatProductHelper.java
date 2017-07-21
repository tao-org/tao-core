package ro.cs.tao.datasource.remote.aws.helpers;

import ro.cs.tao.datasource.remote.AbstractDownloader;
import ro.cs.tao.datasource.remote.ProductHelper;
import ro.cs.tao.datasource.remote.aws.LandsatProduct;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public class LandsatProductHelper extends ProductHelper {
    private static final Pattern preCollectionNamePattern = Pattern.compile("L\\w[1-8](\\d{3})(\\d{3})(\\d{4})(\\d{3})\\w{3}\\d{2}");
    private static final Pattern collection1NamePattern = Pattern.compile("L\\w\\d{2}_L[1-2]\\w{2}_(\\d{3})(\\d{3})_(\\d{4})(\\d{2})(\\d{2})_\\d{8}_\\d{2}_(\\w{2})");
    private static final String PRE_COLLECTION = "pre-collection";
    private static final String COLLECTION_1 = "collection-1";
    private boolean oldFormat;
    private String[] nameTokens;
    private String row;
    private String path;
    private LandsatProduct productType;

    public LandsatProductHelper(String name) {
        super(name);
        this.version = this.oldFormat ? PRE_COLLECTION : COLLECTION_1;
        this.nameTokens = this.oldFormat ? getTokens(preCollectionNamePattern, name, null)
                : getTokens(collection1NamePattern, name, null);
    }

    @Override
    public String getVersion() {
        if (this.version == null) {
            this.version = this.oldFormat ? PRE_COLLECTION : COLLECTION_1;
        }
        return this.version;
    }

    public LandsatProduct getProductType() {
        if (this.productType == null) {
            this.productType = Enum.valueOf(LandsatProduct.class, nameTokens[5]);
        }
        return this.productType;
    }

    String getRow() {
        if (this.row == null && this.nameTokens != null) {
            this.row = nameTokens[1];
        }
        return this.row;
    }

    void setRow(String row) {
        this.row = row;
    }

    public String getPath() {
        if (this.path == null && this.nameTokens != null) {
            this.path = nameTokens[0];
        }
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getProductRelativePath() {
        StringBuilder buffer = new StringBuilder();
        if (!this.oldFormat) {
            buffer.append("c1").append(AbstractDownloader.URL_SEPARATOR);
        }
        buffer.append("L8").append(AbstractDownloader.URL_SEPARATOR);
        buffer.append(getPath()).append(AbstractDownloader.URL_SEPARATOR);
        buffer.append(getRow()).append(AbstractDownloader.URL_SEPARATOR);
        buffer.append(this.name).append(AbstractDownloader.URL_SEPARATOR);
        return buffer.toString();
    }

    public Calendar getAcquisitionDate() {
        Calendar calendar = Calendar.getInstance();
        LocalDate localDate;
        Matcher matcher;
        if (this.oldFormat) {
            matcher = preCollectionNamePattern.matcher(name);
            //noinspection ResultOfMethodCallIgnored
            matcher.matches();
            localDate = Year.of(Integer.parseInt(matcher.group(3))).atDay(Integer.parseInt(matcher.group(4)));
        } else {
            matcher = collection1NamePattern.matcher(name);
            //noinspection ResultOfMethodCallIgnored
            matcher.matches();
            localDate = LocalDate.parse(matcher.group(3)
                                                + "-" + matcher.group(4)
                                                + "-" + matcher.group(5));
        }
        calendar.setTime(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        return calendar;
    }

    @Override
    protected boolean verifyProductName(String name) {
        this.oldFormat = preCollectionNamePattern.matcher(name).matches();
        return this.oldFormat || collection1NamePattern.matcher(name).matches();
    }
}
