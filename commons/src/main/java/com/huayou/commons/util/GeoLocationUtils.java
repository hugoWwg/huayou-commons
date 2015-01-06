package com.huayou.commons.util;

/**
 * Created by wuqiang on 15-1-6. 地理位置相关Util
 */
public class GeoLocationUtils {

    /**
     * 地球平均半径（单位米）
     */
    public static final double EARTH_RADIUS_IN_METER = 6378140.0;
    /**
     * 地球平均半径（单位千米）
     */
    public static final double EARTH_RADIUS_IN_KILOMETER = 6378.0;
    //北纬90度：北极
    //南纬90度：南极

    /**
     * 坐标范围
     */
    public static class CoordinateRange {

        /**
         * 最小（最西边）经度
         */
        private double minLongitude;
        /**
         * 最打（最东边）经度
         */
        private double maxLongitude;
        /**
         * 最小（靠近赤道）纬度
         */
        private double minLatitude;
        /**
         * 最大（靠近两级）纬度
         */
        private double maxLatitude;

        /**
         * 最小（最西边）经度
         */
        public double getMinLongitude() {
            return minLongitude;
        }

        /**
         * 最小（最西边）经度
         */
        public void setMinLongitude(double minLongitude) {
            this.minLongitude = minLongitude;
        }

        /**
         * 最打（最东边）经度
         */
        public double getMaxLongitude() {
            return maxLongitude;
        }

        /**
         * 最打（最东边）经度
         */
        public void setMaxLongitude(double maxLongitude) {
            this.maxLongitude = maxLongitude;
        }

        /**
         * 最小（靠近赤道）纬度
         */
        public double getMinLatitude() {
            return minLatitude;
        }

        /**
         * 最小（靠近赤道）纬度
         */
        public void setMinLatitude(double minLatitude) {
            this.minLatitude = minLatitude;
        }

        /**
         * 最大（靠近两级）纬度
         */
        public double getMaxLatitude() {
            return maxLatitude;
        }

        /**
         * 最大（靠近两级）纬度
         */
        public void setMaxLatitude(double maxLatitude) {
            this.maxLatitude = maxLatitude;
        }
    }

    /**
     * 注意：distance的单位是“米”； 获取已知点(latitude,longitude) distance米为半径的范围（得出结果是一个矩形）
     *
     * @param latitude  是已知点的纬度
     * @param longitude 是已知点的经度
     * @param distance  以这个已知点多少米为半径的范围
     */
    public static CoordinateRange getBoundingCoordinateRangeInMeter(double latitude,
                                                                    double longitude,
                                                                    double distance) {
        return getBoundingCoordinateRange(latitude, longitude, distance, EARTH_RADIUS_IN_METER);
    }

    /**
     * 注意：distance的单位是“千米”； 获取已知点(latitude,longitude) distance千米为半径的范围（得出结果是一个矩形）
     *
     * @param latitude  是已知点的纬度
     * @param longitude 是已知点的经度
     * @param distance  以这个已知点多少千米为半径的范围
     */
    public static CoordinateRange getBoundingCoordinateRangeInKilometer(double latitude,
                                                                        double longitude,
                                                                        double distance) {
        return getBoundingCoordinateRange(latitude, longitude, distance, EARTH_RADIUS_IN_KILOMETER);
    }

    /**
     * distance和radius单位要一致
     *
     * @param latitude  latitude是已知点的纬度
     * @param longitude longitude是已知点的经度
     * @param distance  distance就是那个n米/千米范围
     * @param radius    radius是地球半径，一般取平均半径6378140米/6378千米
     */
    public static CoordinateRange getBoundingCoordinateRange(double latitude,
                                                             double longitude, double distance,
                                                             double radius) {
        double pi = Math.PI;
        latitude = latitude * pi / 180;
        longitude = longitude * pi / 180; // 先换算成弧度
        double result[];
        double rad_dist = distance / radius; // 计算X公里在地球圆周上的弧度
        double lat_min = latitude - rad_dist;
        double lat_max = latitude + rad_dist; // 计算纬度范围

        double lon_min, lon_max;
        // 因为纬度在-90度到90度之间，如果超过这个范围，按情况进行赋值
        if (lat_min > -pi / 2 && lat_max < pi / 2) {
            // 开始计算经度范围
            double lon_t = Math.asin(Math.sin(rad_dist) / Math.cos(latitude));
            lon_min = longitude - lon_t;
            // 同理，经度的范围在-180度到180度之间
            if (lon_min < -pi) {
                lon_min += 2 * pi;
            }
            lon_max = longitude + lon_t;
            if (lon_max > pi) {
                lon_max -= 2 * pi;
            }
        } else {
            lat_min = Math.max(lat_min, -pi / 2);
            lat_max = Math.min(lat_max, pi / 2);
            lon_min = -pi;
            lon_max = pi;
        }
        // 最后置换成角度进行输出
        lat_min = lat_min * 180 / pi;
        lat_max = lat_max * 180 / pi;
        lon_min = lon_min * 180 / pi;
        lon_max = lon_max * 180 / pi;
        CoordinateRange coordinateRange = new CoordinateRange();
        coordinateRange.setMinLatitude(lat_min);
        coordinateRange.setMinLongitude(lon_min);
        coordinateRange.setMaxLatitude(lat_max);
        coordinateRange.setMaxLongitude(lon_max);
        return coordinateRange;
    }

    /**
     * 获取两个经纬度坐标点的距离，返回值的单位：米
     *
     * @param latitude1  第一个点的纬度
     * @param longitude1 第一个点的经度
     * @param latitude2  第二个点的纬度
     * @param longitude2 第二个点的经度
     */
    public static double getCoordinatesDistanceInMeter(double latitude1,
                                                       double longitude1, double latitude2,
                                                       double longitude2) {
        return getCoordinatesDistance(latitude1,
                                      longitude1, latitude2,
                                      longitude2, EARTH_RADIUS_IN_METER);
    }

    /**
     * 获取两个经纬度坐标点的距离，返回值的单位：千米
     *
     * @param latitude1  第一个点的纬度
     * @param longitude1 第一个点的经度
     * @param latitude2  第二个点的纬度
     * @param longitude2 第二个点的经度
     */
    public static double getCoordinatesDistanceInKilometer(double latitude1,
                                                           double longitude1, double latitude2,
                                                           double longitude2) {
        return getCoordinatesDistance(latitude1,
                                      longitude1, latitude2,
                                      longitude2, EARTH_RADIUS_IN_KILOMETER);
    }

    /**
     * 获取两个经纬度坐标点的距离，返回值的单位取决于radius的单位
     *
     * @param latitude1  第一个点的纬度
     * @param longitude1 第一个点的经度
     * @param latitude2  第二个点的纬度
     * @param longitude2 第二个点的经度
     * @param radius     地球半径
     */
    public static double getCoordinatesDistance(double latitude1,
                                                double longitude1, double latitude2,
                                                double longitude2,
                                                double radius) {
        return Math.acos(
            Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2)
                                                        * Math.cos(longitude1 - longitude2))
               * radius;
    }
}
