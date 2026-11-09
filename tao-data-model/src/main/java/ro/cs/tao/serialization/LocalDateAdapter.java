package ro.cs.tao.serialization;

import ro.cs.tao.utils.DateUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Adapter class for converting between {@code LocalDate} and {@code String} during XML serialization and deserialization.
 * This class extends {@code XmlAdapter} to support JAXB-based conversions.
 *
 * The adapter uses a primary formatter with the pattern "yyyy-MM-dd" for marshaling {@code LocalDate}
 * and a resilient formatter derived from {@code DateUtils.getResilientFormatterAtUTC} for unmarshaling strings
 * into {@code LocalDate}. The resilient formatter allows parsing of various date formats.
 *
 * Methods:
 * - {@code unmarshal(String v)}: Converts a {@code String} to a {@code LocalDate}, leveraging the resilient formatter.
 * - {@code marshal(LocalDate v)}: Converts a {@code LocalDate} to a {@code String} in the "yyyy-MM-dd" format.
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter resilientFormatter = DateUtils.getResilientFormatterAtUTC();
    @Override
    public LocalDate unmarshal(String v) throws Exception {
        //return LocalDate.parse(v, formatter);
        return LocalDate.parse(v, resilientFormatter);
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        return v != null ? formatter.format(v) : null;
    }
}
