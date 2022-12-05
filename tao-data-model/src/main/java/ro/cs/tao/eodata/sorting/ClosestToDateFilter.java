package ro.cs.tao.eodata.sorting;

import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.eodata.EOProduct;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class ClosestToDateFilter extends DataFilter<EOProduct> {

    private LocalDateTime dateTime;

    public ClosestToDateFilter() {
    }

    public ClosestToDateFilter(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String[] description() { return new String[] {friendlyName(), JavaType.DATE.friendlyName()}; }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public EOProduct filter(List<EOProduct> sourceList) {
        if (sourceList == null || sourceList.size() == 0) {
            return null;
        }
        long min = Long.MAX_VALUE;
        int idx = 0;
        final Function<EOProduct, Long> reducer = reducer();
        int size = sourceList.size();
        for (int i = 0; i < size; i++) {
            long diff = reducer.apply(sourceList.get(i));
            if (diff < min) {
                min = diff;
                idx = i;
            }
        }
        return sourceList.get(idx);
    }

    @Override
    protected Function<EOProduct, Long> reducer() {
        return eoProduct -> {
            LocalDateTime acquisitionDate = eoProduct.getAcquisitionDate();
            return acquisitionDate != null
                   ? Math.abs(Duration.between(acquisitionDate.toLocalTime(),
                                               dateTime.toLocalTime()).getSeconds())
                   : Long.MAX_VALUE;
        };
    }

    @Override
    protected String friendlyName() {
        return "Closest to date/time";
    }
}
