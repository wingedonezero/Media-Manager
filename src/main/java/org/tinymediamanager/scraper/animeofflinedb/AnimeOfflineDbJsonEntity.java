package org.tinymediamanager.scraper.animeofflinedb;

import java.util.List;

import org.tinymediamanager.scraper.entities.BaseJsonEntity;

public class AnimeOfflineDbJsonEntity extends BaseJsonEntity {

  public List<String>  sources;
  public String        title;
  public String        type;
  public Integer       episodes;
  public String        status;
  public AnimeSeason   animeSeason;
  public String        picture;
  public String        thumbnail;
  public AnimeDuration duration;
  public AnimeScore    score;
  public List<String>  synonyms;
  public List<String>  studios;
  public List<String>  producers;
  public List<String>  relatedAnime;
  public List<String>  tags;

  public class AnimeSeason extends BaseJsonEntity {
    public String year;
    public String season;
  }

  public class AnimeDuration extends BaseJsonEntity {
    public Integer value;
    public String  unit;
  }

  public class AnimeScore extends BaseJsonEntity {
    public Float arithmeticGeometricMean;
    public Float arithmeticMean;
    public Float median;
  }
}
