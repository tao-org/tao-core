package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.Tuple;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ProductByDateOffsetAssociation extends Association<EOProduct> {

    private int offsetDays;

    public ProductByDateOffsetAssociation() {
        super();
        this.offsetDays = 0;
    }

    public ProductByDateOffsetAssociation(int offset) {
        super();
        this.offsetDays = offset;
    }

    @Override
    public String[] description() { return new String[] { friendlyName(), Integer.class.getName() }; }

    public int getOffsetDays() { return offsetDays; }
    public void setOffsetDays(int offsetDays) { this.offsetDays = offsetDays; }

    @Override
    public List<Tuple<EOProduct, EOProduct>> associate(List<EOProduct> source) {
        if (source == null) return null;
        List<Tuple<EOProduct, EOProduct>> results = new ArrayList<>();
        final int sourceSize = source.size();
        EOProduct first, second;
        for (int i = 0; i < sourceSize - 1; i++) {
            first = source.get(i);
            for (int j = i + 1; j < sourceSize; j++) {
                second = source.get(j);
                LocalDate date1 = first.getAcquisitionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate date2 = second.getAcquisitionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int offset;
                if (date1.compareTo(date2) >= 0) {
                    offset = Period.between(date2, date1).getDays() + 1;
                    if (offsetDays == offset) {
                        results.add(new Tuple<>(first, second));
                    }
                } else {
                    offset = Period.between(date1, date2).getDays() + 1;
                    if (offsetDays == offset) {
                        results.add(new Tuple<>(second, first));
                    }
                }
            }
        }
        return results;
    }

    @Override
    protected String friendlyName() { return "Pair by Days Offset"; }
}
