package org.tinymediamanager.scraper.imdb.entities;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.scraper.entities.BaseJsonEntity;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ImdbSearchResult extends BaseJsonEntity {

  public String   index    = "";
  public ListItem listItem = null;

  public class ListItem extends BaseJsonEntity {
    @JsonAlias("titleId")
    public String          id                    = "";
    @JsonAlias("titleText")
    public String          titleNameText         = "";
    @JsonAlias("releaseYear")
    public String          titleReleaseText      = "";
    public String          titleTypeText         = "";
    @JsonAlias("primaryImage")
    public ImdbImageString titlePosterImageModel = null;
    public List<String>    topCredits            = new ArrayList<>();
    public String          imageType             = "";
    public String          seriesId              = "";
    public String          seriesNameText        = "";
    public String          seriesReleaseText     = "";
    public String          seriesTypeText        = "";
    public String          seriesSeasonText      = "";
    public String          seriesEpisodeText     = "";
  }
}