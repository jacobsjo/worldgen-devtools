package eu.jacobsjo.util;

public class ColorUtil {

    public static RGB hsvToRgb(float hue, float saturation, float value) {

        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        return switch (h) {
            case 0 -> new RGB(value, t, p);
            case 1 -> new RGB(q, value, p);
            case 2 -> new RGB(p, value, t);
            case 3 -> new RGB(p, q, value);
            case 4 -> new RGB(t, p, value);
            case 5 -> new RGB(value, p, q);
            default -> throw new RuntimeException("Can't convert hsv to rgb");
        };
    }

    public static RGB randomFromString(String str){
        int hash = str.hashCode();
        float h = (float) (hash & 0xFF) / 0xFF;
        float s = (float) (hash >> 8 & 0xFF) / 0xFF / 2 + 0.5F;
        float v = (float) (hash >> 16 & 0xFF) / 0xFF / 2 + 0.5F;
        return hsvToRgb(h, s, v);
    }
    
    public record RGB(float r, float g, float b){
        public int asInt(){
            return ((int) (r*256) & 0xFF) << 16 | ((int) (g*256) & 0xFF) << 8 | ((int) (b*256) & 0xFF);
        }
    }
}
