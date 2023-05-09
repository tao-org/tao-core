package ro.cs.tao.serialization;

import ro.cs.tao.utils.DateUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
