package git;

import org.apache.commons.codec.digest.DigestUtils;

public class Hasher {
  public static String hashHex(String data) {
    return DigestUtils.sha1Hex(data);
  }
}
