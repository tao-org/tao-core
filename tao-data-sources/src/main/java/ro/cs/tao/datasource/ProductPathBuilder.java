package ro.cs.tao.datasource;

import ro.cs.tao.eodata.EOProduct;

import java.nio.file.Path;

/**
 * A product path builder is responsible with determining the path of a product
 * in a local repository, following the specific repository organisation.
 *
 * @author Cosmin Cara
 */
public interface ProductPathBuilder {
    String LOCAL_ARCHIVE_PATH_FORMAT = "local.archive.path.format";
    String PATH_SUFFIX = "path.suffix";
    String PRODUCT_FORMAT = "product.format";
    String PATH_BUILDER_CLASS = "path.builder.class";

        /**
     * Returns the product path in the repository, or <code>null</code> if not found.
     * @param repositoryPath    The root of the repository
     * @param product           The product for which to build the path
     */
    Path getProductPath(Path repositoryPath, EOProduct product);
}
