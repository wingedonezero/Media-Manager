package org.tinymediamanager.thirdparty;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.core.BasicTest;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.mediainfo.MediaInfoFile;
import org.tinymediamanager.library.bluray.bdjo.BDJO;
import org.tinymediamanager.library.bluray.bdjo.BDJOReader;
import org.tinymediamanager.library.bluray.clipinf.CLPIObject;
import org.tinymediamanager.library.bluray.clipinf.CLPIReader;
import org.tinymediamanager.library.bluray.playlist.MPLSObject;
import org.tinymediamanager.library.bluray.playlist.MPLSReader;

public class ITDiscFolderTests extends BasicTest {

  @Before
  public void setup() throws Exception {
    super.setup();
    setTraceLogging();
  }

  @Test
  public void playlist() {
    MediaFile mf = new MediaFile(Path.of("..\\libbluray\\src\\test\\resources\\blurays\\Tenebre"));
    List<MediaInfoFile> mifs = MediaFileHelper.detectRelevantFiles(mf);
    for (MediaInfoFile mif : mifs) {
      System.out.println("Relevant: " + mif.getFilename() + " - " + mif.getDuration());
    }
    for (MediaInfoFile mif : mifs) {
      try {
        switch (mif.getFileExtension()) {
          case "mpls": {
            MPLSObject idObject = null;
            FileInputStream fin = new FileInputStream(mif.getFileAsPath().toFile());
            DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
            idObject = new MPLSReader().readBinary(din);
            din.close();
            System.out.println(idObject.toXML());
            break;
          }

          case "clpi": {
            CLPIObject idObject = null;
            FileInputStream fin = new FileInputStream(mif.getFileAsPath().toFile());
            DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
            idObject = new CLPIReader().readBinary(din);
            din.close();
            System.out.println(idObject.toXML());
            break;
          }

          case "bdjo": {
            BDJO idObject = null;
            FileInputStream fin = new FileInputStream(mif.getFileAsPath().toFile());
            DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
            idObject = BDJOReader.readBDJO(din);
            din.close();
            System.out.println(idObject.toXML());
            break;
          }

          default:
            throw new IllegalArgumentException("Unexpected value: " + mif.getFileExtension());
          // otf - OpenType Font file
        }
      }
      catch (Exception e) {
        System.err.println(e);
      }
    }
  }
}
