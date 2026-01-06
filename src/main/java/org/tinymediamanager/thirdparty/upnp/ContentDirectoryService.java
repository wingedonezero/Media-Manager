/*
 * Copyright 2012 - 2026 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.thirdparty.upnp;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.jupnp.binding.annotations.UpnpService;
import org.jupnp.binding.annotations.UpnpServiceId;
import org.jupnp.binding.annotations.UpnpServiceType;
import org.jupnp.binding.annotations.UpnpStateVariable;
import org.jupnp.binding.annotations.UpnpStateVariables;
import org.jupnp.model.types.ErrorCode;
import org.jupnp.support.contentdirectory.AbstractContentDirectoryService;
import org.jupnp.support.contentdirectory.ContentDirectoryErrorCode;
import org.jupnp.support.contentdirectory.ContentDirectoryException;
import org.jupnp.support.contentdirectory.DIDLParser;
import org.jupnp.support.model.BrowseFlag;
import org.jupnp.support.model.BrowseResult;
import org.jupnp.support.model.DIDLContent;
import org.jupnp.support.model.DIDLObject;
import org.jupnp.support.model.SortCriterion;
import org.jupnp.support.model.container.Container;
import org.jupnp.support.model.container.GenreContainer;
import org.jupnp.support.model.container.StorageFolder;
import org.jupnp.support.model.item.Item;
import org.jupnp.support.model.item.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.scraper.util.MetadataUtil;

@UpnpService(serviceId = @UpnpServiceId("ContentDirectory"), serviceType = @UpnpServiceType(value = "ContentDirectory"))

@UpnpStateVariables({ @UpnpStateVariable(name = "A_ARG_TYPE_ObjectID", sendEvents = false, datatype = "string"),
    @UpnpStateVariable(name = "A_ARG_TYPE_Result", sendEvents = false, datatype = "string"),
    @UpnpStateVariable(name = "A_ARG_TYPE_BrowseFlag", sendEvents = false, datatype = "string", allowedValuesEnum = BrowseFlag.class),
    @UpnpStateVariable(name = "A_ARG_TYPE_Filter", sendEvents = false, datatype = "string"),
    @UpnpStateVariable(name = "A_ARG_TYPE_SortCriteria", sendEvents = false, datatype = "string"),
    @UpnpStateVariable(name = "A_ARG_TYPE_Index", sendEvents = false, datatype = "ui4"),
    @UpnpStateVariable(name = "A_ARG_TYPE_Count", sendEvents = false, datatype = "ui4"),
    @UpnpStateVariable(name = "A_ARG_TYPE_UpdateID", sendEvents = false, datatype = "ui4"),
    @UpnpStateVariable(name = "A_ARG_TYPE_URI", sendEvents = false, datatype = "uri"),
    @UpnpStateVariable(name = "A_ARG_TYPE_SearchCriteria", sendEvents = false, datatype = "string") })
public class ContentDirectoryService extends AbstractContentDirectoryService {

  public ContentDirectoryService() {
    super();
  }

  public ContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities) {
    super(searchCapabilities, sortCapabilities);
  }

  private static final Logger LOGGER  = LoggerFactory.getLogger(ContentDirectoryService.class);

  private StorageFolder       cdsTree = null;

  private void createTreeIfNull() {
    if (cdsTree == null) {
      // generate the whole browsable CDS tree, with parents, counts et all - but only ONCE
      // Movie: 1/t/<uid>
      // Movie: 1/g/Action/<uid>
      // Show: 2/<uid>/s/e

      // *********************************************
      // STRUCTURE
      // *********************************************
      StorageFolder uRoot = new StorageFolder(Upnp.ID_ROOT, "-1", "All", "", 0, 0L);

      // *********************************************
      StorageFolder uMovies = new StorageFolder(Upnp.ID_MOVIES, uRoot, TmmResourceBundle.getString("tmm.movies"), "", 0, 0L);

      List<org.tinymediamanager.core.movie.entities.Movie> movies = MovieModuleManager.getInstance().getMovieList().getMovies();
      movies.sort((m1, m2) -> String.CASE_INSENSITIVE_ORDER.compare(m1.getTitleSortable(), m2.getTitleSortable()));
      StorageFolder grpTitles = new StorageFolder(uMovies.getId() + "/t", uMovies, TmmResourceBundle.getString("metatag.title"), "",
          MovieModuleManager.getInstance().getMovieList().getMovieCount(), 0L);
      // add movies to titles
      for (org.tinymediamanager.core.movie.entities.Movie movie : movies) {
        Movie um = Metadata.getUpnpMovie(movie, false);
        um.setId(grpTitles.getId() + "/" + um.getId()); // only get ID - prepend path
        um.setParentID(grpTitles.getId());
        grpTitles.addItem(um);
      }
      uMovies.addContainer(grpTitles);

      Collection<MediaGenres> mgs = MovieModuleManager.getInstance().getMovieList().getUsedGenres();
      GenreContainer grpGenres = new GenreContainer(uMovies.getId() + "/g", uMovies, TmmResourceBundle.getString("metatag.genre"), "", mgs.size());
      for (MediaGenres mg : mgs) {
        GenreContainer gc = new GenreContainer(grpGenres.getId() + "/" + mg.getLocalizedName(), grpGenres, mg.getLocalizedName(), "", 0);
        // add movies to genres
        for (org.tinymediamanager.core.movie.entities.Movie movie : movies) {
          if (movie.getGenres().contains(mg)) {
            Movie um = Metadata.getUpnpMovie(movie, false);
            um.setId(gc.getId() + "/" + um.getId()); // only get ID - prepend path
            um.setParentID(gc.getId());
            gc.addItem(um);
          }
        }
        gc.setChildCount(gc.getContainers().size() + gc.getItems().size());
        grpGenres.addContainer(gc);
      }
      uMovies.addContainer(grpGenres);

      ArrayList<Integer> years = new ArrayList<Integer>(MovieModuleManager.getInstance().getMovieList().getYearsInMovies());
      Collections.sort(years, Collections.reverseOrder());
      StorageFolder grpYear = new StorageFolder(uMovies.getId() + "/y", uMovies, TmmResourceBundle.getString("metatag.year"), "", years.size(), 0L);
      for (int year : years) {
        StorageFolder yc = new StorageFolder(grpYear.getId() + "/" + year, grpYear, "" + year, "", 0, 0L);
        // add movies to years
        for (org.tinymediamanager.core.movie.entities.Movie movie : movies) {
          if (movie.getYear() == year) {
            Movie um = Metadata.getUpnpMovie(movie, false);
            um.setId(yc.getId() + "/" + um.getId()); // only get ID - prepend path
            um.setParentID(yc.getId());
            yc.addItem(um);
          }
        }
        yc.setChildCount(yc.getContainers().size() + yc.getItems().size());
        grpYear.addContainer(yc);
      }
      uMovies.addContainer(grpYear);

      uMovies.setChildCount(uMovies.getContainers().size() + uMovies.getItems().size());
      uRoot.addContainer(uMovies);

      // *********************************************
      StorageFolder uTvShows = new StorageFolder(Upnp.ID_TVSHOWS, uRoot, TmmResourceBundle.getString("tmm.tvshows"), "",
          TvShowModuleManager.getInstance().getTvShowList().getTvShowCount(), 0L);

      List<org.tinymediamanager.core.tvshow.entities.TvShow> tmmShows = TvShowModuleManager.getInstance().getTvShowList().getTvShows();
      tmmShows.sort((t1, t2) -> String.CASE_INSENSITIVE_ORDER.compare(t1.getTitleSortable(), t2.getTitleSortable()));
      for (org.tinymediamanager.core.tvshow.entities.TvShow t : tmmShows) {
        StorageFolder uTvShow = new StorageFolder(Upnp.ID_TVSHOWS + "/" + t.getDbId(), uTvShows, t.getTitle(), "", t.getSeasonCount(), 0L);
        for (TvShowSeason s : t.getSeasons()) {
          StorageFolder uSeason = new StorageFolder(uTvShow.getId() + "/" + s.getSeason(), uTvShow, "Season " + s.getSeason(), "",
              t.getEpisodeCount(), 0L);
          for (TvShowEpisode ep : s.getEpisodes()) {
            Movie um = Metadata.getUpnpTvShowEpisode(t, ep, false);
            uSeason.addItem(um);
          }
          uTvShow.addContainer(uSeason);
        }
        uTvShows.addContainer(uTvShow);
      }
      uRoot.addContainer(uTvShows);

      // *********************************************
      uRoot.setChildCount(uRoot.getContainers().size());

      cdsTree = uRoot;
    }
  }

  @Override
  public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter, long firstResult, long maxResults, SortCriterion[] orderby)
      throws ContentDirectoryException {
    try {
      LOGGER.debug("ObjectId: {}", objectID);
      LOGGER.debug("BrowseFlag: {}", browseFlag);
      LOGGER.debug("Filter: {}", filter);
      LOGGER.debug("FirstResult: {}", firstResult);
      LOGGER.debug("MaxResults: {}", maxResults);
      LOGGER.debug("OrderBy: {}", SortCriterion.toString(orderby));
      createTreeIfNull();

      DIDLContent didl = new DIDLContent();

      String[] path = StringUtils.split(objectID, '/');
      if (path == null) {
        throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, "path was NULL");
      }
      String request = path[path.length - 1];
      String parent = "";
      if (objectID.contains("/")) {
        parent = objectID.substring(0, objectID.lastIndexOf("/")); // remove uuid
      }

      if (browseFlag.equals(BrowseFlag.METADATA)) {
        DIDLObject obj = findId(objectID, cdsTree);
        if (obj instanceof Container) {
          didl.addContainer((Container) obj);
        }
        else if (obj instanceof Item) {
          Item item = (Item) obj; // only mandatory metadata

          // get em fresh from DB, for FULL metadata
          if (path[0].equals(Upnp.ID_MOVIES) && isUUID(request)) {
            org.tinymediamanager.core.movie.entities.Movie m = MovieModuleManager.getInstance()
                .getMovieList()
                .findByDbId(UUID.fromString(request))
                .orElse(null);
            if (m != null) {
              Movie um = Metadata.getUpnpMovie(m, true);
              um.setId(parent + "/" + um.getId());
              um.setParentID(parent);
              item = um;
            }
          }
          else if (path[0].equals(Upnp.ID_TVSHOWS) && path.length == 4) {
            org.tinymediamanager.core.tvshow.entities.TvShow t = TvShowModuleManager.getInstance()
                .getTvShowList()
                .lookupTvShow(UUID.fromString(path[1]));
            if (t != null) {
              TvShowEpisode ep = t.getEpisode(getInt(path[2]), getInt(path[3])).stream().findFirst().orElse(null);
              if (ep != null) {
                item = Metadata.getUpnpTvShowEpisode(t, ep, true);
              }
            }
          }
          didl.addItem(item);
        }
        return returnResult(didl, 1); // always 1 item
      }
      else if (browseFlag.equals(BrowseFlag.DIRECT_CHILDREN)) {
        DIDLObject obj = findId(objectID, cdsTree);
        long total = 0;
        // if we browse children, this MUST be a container with children ;)
        if (obj instanceof Container) {
          Container cont = (Container) obj;
          didl = createContentDidl(cont, firstResult, maxResults);
          // total size of objects - can be different to actual didl
          total = (long) cont.getContainers().size() + (long) cont.getItems().size();
        }
        return returnResult(didl, total);
      }

      throw new ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT, "BrowseFlag wrong " + browseFlag);
    }
    catch (Exception ex) {
      LOGGER.debug("Browse failed", ex);
      throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
    }
  }

  /**
   * recursive search uRoot object containers for matching tree ID
   * 
   * @param id
   *          the objectID / path
   * @param folder
   *          mostly start with uRoot
   * @return
   */
  private DIDLObject findId(String id, StorageFolder folder) {
    DIDLObject ret = folder;

    String[] path = id.split("/");
    String parent = "";
    for (int i = 0; i < path.length; i++) {
      String s = path[i];
      ret = getTreeObject(parent + s, ret);
      parent += s + "/";
      if (ret == null) {
        break;
      }
    }
    return ret;
  }

  /**
   * gets the specified ID from its sub containers/items
   * 
   * @param id
   *          the objectID / path
   * @param obj
   * @return
   */
  private DIDLObject getTreeObject(String id, DIDLObject obj) {
    DIDLObject ret = null;
    if (id.equalsIgnoreCase(obj.getId())) {
      // root
      return obj;
    }

    if (obj instanceof Container) {
      for (Container c : ((Container) obj).getContainers()) {
        if (c.getId().equalsIgnoreCase(id)) {
          ret = c;
          break;
        }
      }
      for (Item i : ((Container) obj).getItems()) {
        if (i.getId().equalsIgnoreCase(id)) {
          ret = i;
          break;
        }
      }
    }

    return ret;
  }

  private boolean isUUID(String uuid) {
    return uuid.length() == 36;
  }

  private DIDLContent createContentDidl(Container cont, long firstResult, long maxResults) {
    DIDLContent didl = new DIDLContent();
    int cnt = 0;
    for (Container c : cont.getContainers()) {
      if (firstResult > 0) {
        firstResult--;
        continue;
      }
      if (maxResults == 0 || cnt < maxResults) {
        didl.addContainer(c);
        cnt++;
      }
    }
    for (Item i : cont.getItems()) {
      if (firstResult > 0) {
        firstResult--;
        continue;
      }
      if (maxResults == 0 || cnt < maxResults) {
        didl.addItem(i);
        cnt++;
      }
    }

    return didl;
  }

  private BrowseResult returnResult(DIDLContent didl, long total) throws Exception {
    DIDLParser dip = new DIDLParser();
    String ret = dip.generate(didl);
    LOGGER.trace(prettyFormat(ret, 2));
    return new BrowseResult(ret, didl.getCount(), total);
  }

  private int getInt(String s) {
    return MetadataUtil.parseInt(s, 0);
  }

  /**
   * Override this method to implement searching of your content.
   * <p>
   * The default implementation returns an empty result.
   * </p>
   */
  @Override
  public BrowseResult search(String containerId, String searchCriteria, String filter, long firstResult, long maxResults, SortCriterion[] orderBy)
      throws ContentDirectoryException {
    try {
      // TODO: implement search
      return new BrowseResult(new DIDLParser().generate(new DIDLContent()), 0, 0);
    }
    catch (Exception e) {
      throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, e.toString());
    }
  }

  public static String prettyFormat(String input, int indent) {
    try {
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      TransformerFactory transformerFactory = TransformerFactory.newInstance(); // NOSONAR
      transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);
      return xmlOutput.getWriter().toString();
    }
    catch (Exception e) {
      return "! error parsing xml !";
    }
  }
}
