package ro.cs.tao.services.interfaces;

import ro.cs.tao.workspaces.Site;

import java.util.List;

public interface SiteService extends CRUDService<Site, String> {

    List<Site> getByUser(String userName);
}
