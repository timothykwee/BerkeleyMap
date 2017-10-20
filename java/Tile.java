/**
 * Created by parisl on 4/14/17.
 */
class Tile {
    private String name;
    private double ullon;
    private double ullat;
    private double lrlon;
    private double lrlat;

    Tile(String name, double ullon, double ullat, double lrlon, double lrlat) {
        this.name = name;
        this.ullon = ullon;
        this.ullat = ullat;
        this.lrlon = lrlon;
        this.lrlat = lrlat;
    }

    String getName() {
        return name;
    }

    double getUpperLeftLon() {
        return ullon;
    }

    double getUpperLeftLat() {
        return ullat;
    }

    double getLowerRightLon() {
        return lrlon;
    }

    double getLowerRightLat() {
        return lrlat;
    }

    double getMiddleLon() {
        return lrlon - (lrlon - ullon) / 2;
    }

    double getMiddleLat() {
        return ullat - (ullat - lrlat) / 2;
    }

}
