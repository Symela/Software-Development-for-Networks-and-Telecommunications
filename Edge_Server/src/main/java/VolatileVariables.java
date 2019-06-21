public class VolatileVariables {

    volatile String mac1, mac2;
    volatile int timesClosed1;
    volatile int timesClosed2;
    volatile double mobile1Lat;
    volatile double mobile1Lon;
    volatile double mobile2Lat;
    volatile double mobile2Lon;
    volatile int total_tests;
    volatile int success_counter;


    public VolatileVariables() {
        timesClosed1 = 0;
        timesClosed2 = 0;
        mobile1Lat = 0.0;
        mobile1Lon = 0.0;
        mobile2Lat = 0.0;
        mobile2Lon = 0.0;
        total_tests = 0;
        success_counter = 0;
        mac1 = "0.0.0.0";
        mac2 = "0.0.0.0";
    }

    // Setters ----------------------------------------------------------------------------

    public void setTimesClosed1(int timesClosed1) {
        this.timesClosed1 = timesClosed1;
    }

    public void setTimesClosed2(int timesClosed2) {
        this.timesClosed2 = timesClosed2;
    }

    public void setMobile1Lat(double mobile1Lat) {
        this.mobile1Lat = mobile1Lat;
    }

    public void setMobile1Lon(double mobile1Lon) {
        this.mobile1Lon = mobile1Lon;
    }

    public void setMobile2Lat(double mobile2Lat) {
        this.mobile2Lat = mobile2Lat;
    }

    public void setMobile2Lon(double mobile2Lon) {
        this.mobile2Lon = mobile2Lon;
    }

    public void setMac1(String mac1) {
        this.mac1 = mac1;
    }

    public void setMac2(String mac2) {
        this.mac2 = mac2;
    }
    // Getters ----------------------------------------------------------------------------

    public int getTimesClosed1() {
        return timesClosed1;
    }

    public int getTimesClosed2() {
        return timesClosed2;
    }

    public double getMobile1Lat() {
        return mobile1Lat;
    }

    public double getMobile1Lon() {
        return mobile1Lon;
    }

    public double getMobile2Lat() {
        return mobile2Lat;
    }

    public double getMobile2Lon() {
        return mobile2Lon;
    }

    public String getMac1() {
        return mac1;
    }

    public String getMac2() {
        return mac2;
    }

    // Other --------------------------------------------------------------------------------
    public void increaseCounter1() {
        this.timesClosed1 = this.timesClosed1 + 1;
    }

    public void increaseCounter2() {
        this.timesClosed2 = this.timesClosed2 + 1;
    }

    public void increaseTotal() {
        this.total_tests = this.total_tests + 1;
    }

    public void increaseSuccessCounter() {
        this.success_counter = this.success_counter + 1;
    }
}
