package service.provider.api;

public class ParsedAdvertHeader {

    private long publicationTimestamp;
    private String advertUrl;

    public long getPublicationTimestamp() {
        return publicationTimestamp;
    }

    public ParsedAdvertHeader setPublicationTimestamp(long publicationTimestamp) {
        this.publicationTimestamp = publicationTimestamp;
        return this;
    }

    public String getAdvertUrl() {
        return advertUrl;
    }

    public ParsedAdvertHeader setAdvertUrl(String advertUrl) {
        this.advertUrl = advertUrl;
        return this;
    }

    @Override
    public String toString() {
        return advertUrl + "\\" + publicationTimestamp;
    }
}