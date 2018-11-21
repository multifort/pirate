package test;/**
 * @Author: langzi
 * @Date: Created on 2017/10/27
 * @Description:
 */

import com.bocloud.paas.service.repository.util.RegistryClient;
import com.spotify.docker.client.ImageRef;

/**
 * registry client test
 *
 * @author langzi
 * @email lining@beyondcent.com
 * @time 2017-10-27 17:08
 */
public class RegistryClientTest {
    private static String url = "http://192.168.56.11:5000";

    public static void main(String[] args) {
        RegistryClient client = new RegistryClient(url);
        boolean isConnected = client.isConnected();
        System.out.println(isConnected);
//       List<String> images = client.getImageNames();
        /*if (!images.isEmpty()) {
            for (String image : images) {
                System.out.println(image);
            }
        }*/
       /* String image = "library/registry";
        String tag = client.getImageTag(image);
        JSONObject jsonObject = JSONObject.parseObject(tag);
        String[] tags = new String[10];
        jsonObject.getJSONArray("tags").toArray(tags);
        //String[] tags = (String[]) jsonObject.get("tags");
        for (int i = 0; i < tags.length; i++) {
            System.out.println(tags[i]);
        }*/
        System.out.println(client.isConnected());
        String image = "library/busybox";
        String tag = "latest";
        //String digest = "sha256:be3c11fdba7cfe299214e46edc642e09514dbb9bbefcd0d3836c05a1e0cd0642";
        String digest = client.getImageDigest(image, tag);
        System.out.println(digest);

        //test delete images
        //RegistryClient client1 = new RegistryClient(url);
        //boolean isDeleted = client1.deleteImage(image, digest);
        //System.out.println(isDeleted);
        /*String[] images = client.listImageNames();
        if (null != images) {
            for (String image : images) {
                System.out.println(image);
                RegistryClient client1 = new RegistryClient(url);
                String[] tags = client1.listImageTags(image);
                if (null != tags) {
                    System.out.println(tags[0]);
                }
            }
        }*/
    }

}
