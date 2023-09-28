package ro.cs.tao.eodata;

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
    private final Map<String, String> projectionsByCode;
    private final Map<String, String> projectionsByWKT;

    static {
        instance = new Projection();
    }

    public static String getDescription(String code) {
        return instance.projectionsByCode.get(code);
    }

    public static String getCode(String description) {
        return instance.projectionsByWKT.get(description);
    }

    public static Map<String, String> getSupported() {
        return instance.projectionsByCode;
    }

    private Projection() {
        this.projectionsByCode = new HashMap<>();
        this.projectionsByWKT = new HashMap<>();
        initialize();
    }

    private void initialize() {
        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true);
        Set<CRSAuthorityFactory> factories = ReferencingFactoryFinder.getCRSAuthorityFactories(hints);
        final List<CRSAuthorityFactory> filtered = new ArrayList<>();
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
                        final String wkt = crsAuthorityFactory.getDescriptionText(authCode).toString();
                        projectionsByCode.put(authCode, wkt);
                        projectionsByWKT.put(wkt, authCode);
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
