package ro.cs.tao.eodata;

import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.FallbackAuthorityFactory;
import org.geotools.util.factory.Hints;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import java.util.*;

public class Projection {
    private static final Projection instance;
    private final TreeBidiMap projections;

    static {
        instance = new Projection();
    }

    public static String getDescription(String code) {
        return (String) instance.projections.get(code);
    }

    public static String getCode(String description) {
        return (String) instance.projections.getKey(description);
    }

    public static Map<String, String> getSupported() {
        return (Map<String, String>) instance.projections;
    }

    private Projection() {
        this.projections = new TreeBidiMap();
        initialize();
    }

    private void initialize() {
        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true);
        Set<CRSAuthorityFactory> factories = ReferencingFactoryFinder.getCRSAuthorityFactories(hints);
        final List<CRSAuthorityFactory> filtered = new ArrayList<CRSAuthorityFactory>();
        for (final CRSAuthorityFactory factory : factories) {
            if (Citations.identifierMatches(factory.getAuthority(), "EPSG")) {
                filtered.add(factory);
            }
        }
        CRSAuthorityFactory crsAuthorityFactory = FallbackAuthorityFactory.create(CRSAuthorityFactory.class, filtered);
        Set<String> codes = new HashSet<>();
        retrieveCodes(codes, GeodeticCRS.class, crsAuthorityFactory);
        retrieveCodes(codes, ProjectedCRS.class, crsAuthorityFactory);
        codes.stream().sorted().forEachOrdered(
                code -> {
                    final String authCode = String.format("%s:%s", "EPSG", code);
                    try {
                        projections.put(authCode, crsAuthorityFactory.getDescriptionText(authCode).toString());
                    } catch (FactoryException e) {
                        e.printStackTrace();
                    }
                });
        codes.clear();
    }

    private void retrieveCodes(Set<String> codes, Class<? extends CoordinateReferenceSystem> crsType, CRSAuthorityFactory factory) {
        Set<String> localCodes;
        try {
            localCodes = factory.getAuthorityCodes(crsType);
        } catch (FactoryException ignore) {
            return;
        }
        codes.addAll(localCodes);
    }
}
