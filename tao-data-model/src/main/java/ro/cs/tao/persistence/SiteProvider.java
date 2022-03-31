package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Site;

import java.util.List;

public interface SiteProvider extends EntityProvider<Site, Integer> {

    List<Site> getByUser(String userName);
}