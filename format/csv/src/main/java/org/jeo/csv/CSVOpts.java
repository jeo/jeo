package org.jeo.csv;

public class CSVOpts {

    Delimiter delim = CSV.DELIM.getDefault();
    boolean header = CSV.HEADER.getDefault();

    String xcol, ycol;
    Integer x, y;

    public Delimiter getDelimiter() {
        return delim;
    }

    public CSVOpts delimiter(Delimiter delim) {
        this.delim = delim;
        return this;
    }

    public boolean hasHeader() {
        return header;
    }

    public CSVOpts header(boolean header) {
        this.header = header;
        return this;
    }

    public CSVOpts xy(String x, String y) {
        this.xcol = x;
        this.ycol = y;
        return this;
    }

    public CSVOpts xy(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        return this;
    }

    Integer getX() {
        return x;
    }

    String getXcol() {
        return xcol;
    }

    Integer getY() {
        return y;
    }

    String getYcol() {
        return ycol;
    }

    CSVHandler handler() {
        //sanity check
        if (x == null || y == null) {
            if (xcol == null || ycol == null) {
                throw new IllegalArgumentException("Must specify x/y columns");
            }
        }

        if (xcol != null && !header) {
            throw new IllegalArgumentException("specifying column names requires a header");
        }

        return new XYHandler(this);
    }
}
