package me.cdh;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public record Loader() {
    static <T extends Enum<T> & Animate> Map<String, List<BufferedImage>> loadImg(EnumSet<T> entries) {
        var stateFramesMap = new HashMap<String, List<BufferedImage>>();
        var ran = new Random();
        var catVarious = switch (ran.nextInt(0, 4)) {
            case 0 -> "calico_cat";
            case 1 -> "grey_tabby_cat";
            case 2 -> "orange_cat";
            case 3 -> "white_cat";
            default -> throw new IllegalStateException("Unexpected value");
        };
        for (var action : entries) {
            if (action.frame() <= 0) continue;
            var list = new ArrayList<BufferedImage>();
            stateFramesMap.put(action.name(), list);
            var folderName = action.name().toLowerCase();
            for (int i = 1; i <= action.frame(); i++) {
                try (var inp = Loader.class.getClassLoader().getResourceAsStream(String.format("%s/%s/%s_%d.png", catVarious, folderName, folderName, i))) {
                    list.add(ImageIO.read(Objects.requireNonNull(inp)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return stateFramesMap;
    }
}
